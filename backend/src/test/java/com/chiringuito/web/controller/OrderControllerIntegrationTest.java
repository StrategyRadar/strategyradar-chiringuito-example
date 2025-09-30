package com.chiringuito.web.controller;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.service.dto.AddItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    private MenuItem testMenuItem;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Clean up database
        orderLineRepository.deleteAll();
        orderRepository.deleteAll();
        menuItemRepository.deleteAll();

        // Create test menu item
        testMenuItem = MenuItem.builder()
                .name("Test Paella")
                .description("Delicious test paella")
                .price(new BigDecimal("15.99"))
                .imageUrl("http://example.com/paella.jpg")
                .available(true)
                .build();
        testMenuItem = menuItemRepository.save(testMenuItem);

        // Create fresh session for each test
        session = new MockHttpSession();
    }

    @Test
    void shouldAddItemToCartAndReturn200WithOrderSummary() throws Exception {
        // Given
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(2)
                .build();

        // When & Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(31.98)) // 2 * 15.99
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.orderLines").isArray())
                .andExpect(jsonPath("$.orderLines", hasSize(1)))
                .andExpect(jsonPath("$.orderLines[0].menuItemName").value("Test Paella"))
                .andExpect(jsonPath("$.orderLines[0].quantity").value(2))
                .andExpect(jsonPath("$.orderLines[0].unitPrice").value(15.99))
                .andExpect(jsonPath("$.orderLines[0].lineTotal").value(31.98));

        // Verify order was persisted
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderLineRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturn404WhenMenuItemNotFound() throws Exception {
        // Given: Non-existent menu item ID
        UUID nonExistentId = UUID.randomUUID();
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(nonExistentId)
                .quantity(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldReturn409WhenMenuItemNotAvailable() throws Exception {
        // Given: Unavailable menu item
        testMenuItem.setAvailable(false);
        menuItemRepository.save(testMenuItem);

        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("not available")));
    }

    @Test
    void shouldReturn400WhenQuantityInvalid() throws Exception {
        // Given: Invalid quantity (0)
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Quantity must be at least 1")));
    }

    @Test
    void shouldReturn400WhenQuantityExceeds50() throws Exception {
        // Given: Quantity > 50
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(51)
                .build();

        // When & Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("must not exceed 50")));
    }

    @Test
    void shouldReturn400WhenTotalItemsExceed50() throws Exception {
        // Given: Create order with 48 items already
        Order existingOrder = Order.builder()
                .status("PENDING")
                .totalAmount(new BigDecimal("480.00"))
                .build();
        existingOrder = orderRepository.save(existingOrder);

        MenuItem anotherItem = MenuItem.builder()
                .name("Another Item")
                .price(new BigDecimal("10.00"))
                .available(true)
                .build();
        anotherItem = menuItemRepository.save(anotherItem);

        OrderLine existingLine = OrderLine.builder()
                .orderId(existingOrder.getId())
                .menuItemId(anotherItem.getId())
                .quantity(48)
                .unitPrice(new BigDecimal("10.00"))
                .lineTotal(new BigDecimal("480.00"))
                .build();
        orderLineRepository.save(existingLine);

        // Set order ID in session
        session.setAttribute("orderId", existingOrder.getId().toString());

        // When: Try to add 3 more items (total would be 51)
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(3)
                .build();

        // Then
        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("50 items")));
    }

    @Test
    void shouldPersistOrderIdInSessionAcrossMultipleAdds() throws Exception {
        // First add
        AddItemRequest request1 = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(2)
                .build();

        String response1 = mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response1).get("orderId").asText();

        // Second add (same session, different quantity)
        AddItemRequest request2 = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.itemCount").value(5)) // 2 + 3 (updated quantity)
                .andExpect(jsonPath("$.totalAmount").value(79.95)); // 5 * 15.99

        // Verify only one order was created
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderLineRepository.count()).isEqualTo(1); // Same item, so quantity updated
    }

    @Test
    void shouldReturnEmptyCartWhenNoOrderInSession() throws Exception {
        // Given: Fresh session with no order
        MockHttpSession freshSession = new MockHttpSession();

        // When & Then
        mockMvc.perform(get("/api/order/cart")
                        .session(freshSession))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnCartWithItemsWhenOrderExists() throws Exception {
        // Given: Add items to cart first
        AddItemRequest request = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(2)
                .build();

        String addResponse = mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(addResponse).get("orderId").asText();

        // When: Get cart
        mockMvc.perform(get("/api/order/cart")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(31.98)) // 2 * 15.99
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.orderLines").isArray())
                .andExpect(jsonPath("$.orderLines", hasSize(1)))
                .andExpect(jsonPath("$.orderLines[0].menuItemName").value("Test Paella"))
                .andExpect(jsonPath("$.orderLines[0].quantity").value(2));
    }

    @Test
    void shouldReturnEmptyCartWhenOrderNotFoundInDatabase() throws Exception {
        // Given: Session has order ID but order doesn't exist in DB
        session.setAttribute("orderId", UUID.randomUUID().toString());

        // When & Then
        mockMvc.perform(get("/api/order/cart")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnCartWithMultipleItemsAfterMultipleAdds() throws Exception {
        // Given: Add first item
        AddItemRequest request1 = AddItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Add second item (different menu item)
        MenuItem anotherItem = MenuItem.builder()
                .name("Test Gazpacho")
                .description("Cold soup")
                .price(new BigDecimal("8.50"))
                .available(true)
                .build();
        anotherItem = menuItemRepository.save(anotherItem);

        AddItemRequest request2 = AddItemRequest.builder()
                .menuItemId(anotherItem.getId())
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/order/add-item")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // When: Get cart
        mockMvc.perform(get("/api/order/cart")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(57.48)) // (2 * 15.99) + (3 * 8.50)
                .andExpect(jsonPath("$.itemCount").value(5)) // 2 + 3
                .andExpect(jsonPath("$.orderLines", hasSize(2)));
    }
}