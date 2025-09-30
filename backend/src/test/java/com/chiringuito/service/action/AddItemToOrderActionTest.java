package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.service.dto.AddItemRequest;
import com.chiringuito.service.dto.OrderSummaryDTO;
import com.chiringuito.service.exception.MaxItemsExceededException;
import com.chiringuito.service.exception.MenuItemNotFoundException;
import com.chiringuito.service.exception.MenuItemUnavailableException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddItemToOrderActionTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AddItemToOrderAction addItemToOrderAction;

    private MenuItem testMenuItem;
    private UUID testMenuItemId;
    private AddItemRequest testRequest;

    @BeforeEach
    void setUp() {
        testMenuItemId = UUID.randomUUID();
        testMenuItem = MenuItem.builder()
                .id(testMenuItemId)
                .name("Paella Valenciana")
                .description("Traditional Spanish rice dish")
                .price(new BigDecimal("12.50"))
                .available(true)
                .build();

        testRequest = AddItemRequest.builder()
                .menuItemId(testMenuItemId)
                .quantity(3)
                .build();
    }

    @Test
    void shouldCreateNewOrderWhenNoExistingOrder() {
        // Given: No order exists in session
        when(session.getAttribute("orderId")).thenReturn(null);
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));
        when(orderLineRepository.findByOrderId(any(UUID.class))).thenReturn(List.of());

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .status("PENDING")
                .totalAmount(new BigDecimal("37.50"))
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderLine savedOrderLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(savedOrder.getId())
                .menuItemId(testMenuItemId)
                .quantity(3)
                .unitPrice(new BigDecimal("12.50"))
                .lineTotal(new BigDecimal("37.50"))
                .build();
        when(orderLineRepository.save(any(OrderLine.class))).thenReturn(savedOrderLine);
        when(orderLineRepository.findByOrderId(savedOrder.getId())).thenReturn(List.of(savedOrderLine));

        // When
        OrderSummaryDTO result = addItemToOrderAction.execute(testRequest, session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("37.50"));
        assertThat(result.getItemCount()).isEqualTo(3);
        assertThat(result.getOrderLines()).hasSize(1);

        verify(session).setAttribute(eq("orderId"), any(UUID.class));
        verify(orderRepository).save(any(Order.class));
        verify(orderLineRepository).save(any(OrderLine.class));
    }

    @Test
    void shouldAddItemToExistingOrder() {
        // Given: Order already exists in session
        UUID existingOrderId = UUID.randomUUID();
        when(session.getAttribute("orderId")).thenReturn(existingOrderId.toString());

        Order existingOrder = Order.builder()
                .id(existingOrderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("20.00"))
                .build();
        when(orderRepository.findById(existingOrderId)).thenReturn(Optional.of(existingOrder));
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));
        when(orderLineRepository.findByOrderIdAndMenuItemId(existingOrderId, testMenuItemId))
                .thenReturn(Optional.empty());

        // Existing order lines
        OrderLine existingLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .lineTotal(new BigDecimal("20.00"))
                .build();
        when(orderLineRepository.findByOrderId(existingOrderId)).thenReturn(List.of(existingLine));

        OrderLine newLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(testMenuItemId)
                .quantity(3)
                .unitPrice(new BigDecimal("12.50"))
                .lineTotal(new BigDecimal("37.50"))
                .build();
        when(orderLineRepository.save(any(OrderLine.class))).thenReturn(newLine);

        // When
        OrderSummaryDTO result = addItemToOrderAction.execute(testRequest, session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(existingOrderId);
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("57.50")); // 20.00 + 37.50
        assertThat(result.getItemCount()).isEqualTo(5); // 2 + 3

        verify(orderLineRepository).save(any(OrderLine.class));
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void shouldUpdateQuantityWhenSameItemAddedAgain() {
        // Given: Order exists with same menu item already added
        UUID existingOrderId = UUID.randomUUID();
        when(session.getAttribute("orderId")).thenReturn(existingOrderId.toString());

        Order existingOrder = Order.builder()
                .id(existingOrderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("25.00"))
                .build();
        when(orderRepository.findById(existingOrderId)).thenReturn(Optional.of(existingOrder));
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));

        // Existing order line with same menu item
        OrderLine existingLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(testMenuItemId)
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .lineTotal(new BigDecimal("25.00"))
                .build();
        when(orderLineRepository.findByOrderIdAndMenuItemId(existingOrderId, testMenuItemId))
                .thenReturn(Optional.of(existingLine));
        when(orderLineRepository.findByOrderId(existingOrderId)).thenReturn(List.of(existingLine));

        // When: Adding 3 more (should become 5 total)
        OrderSummaryDTO result = addItemToOrderAction.execute(testRequest, session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("62.50")); // 5 * 12.50
        assertThat(result.getItemCount()).isEqualTo(5);

        verify(orderLineRepository).save(argThat(line ->
            line.getQuantity() == 5 &&
            line.getLineTotal().equals(new BigDecimal("62.50"))
        ));
    }

    @Test
    void shouldThrowMenuItemNotFoundExceptionWhenItemDoesNotExist() {
        // Given
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addItemToOrderAction.execute(testRequest, session))
                .isInstanceOf(MenuItemNotFoundException.class)
                .hasMessageContaining("Menu item not found");
    }

    @Test
    void shouldThrowMenuItemUnavailableExceptionWhenItemNotAvailable() {
        // Given
        MenuItem unavailableItem = MenuItem.builder()
                .id(testMenuItemId)
                .name("Unavailable Item")
                .price(new BigDecimal("10.00"))
                .available(false)
                .build();
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(unavailableItem));

        // When & Then
        assertThatThrownBy(() -> addItemToOrderAction.execute(testRequest, session))
                .isInstanceOf(MenuItemUnavailableException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldThrowMaxItemsExceededExceptionWhenTotalItemsExceed50() {
        // Given: Order already has 48 items
        UUID existingOrderId = UUID.randomUUID();
        when(session.getAttribute("orderId")).thenReturn(existingOrderId.toString());

        Order existingOrder = Order.builder()
                .id(existingOrderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("480.00"))
                .build();
        when(orderRepository.findById(existingOrderId)).thenReturn(Optional.of(existingOrder));
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));

        // Existing order line with 48 items
        OrderLine existingLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(UUID.randomUUID())
                .quantity(48)
                .unitPrice(new BigDecimal("10.00"))
                .lineTotal(new BigDecimal("480.00"))
                .build();
        when(orderLineRepository.findByOrderId(existingOrderId)).thenReturn(List.of(existingLine));
        when(orderLineRepository.findByOrderIdAndMenuItemId(existingOrderId, testMenuItemId))
                .thenReturn(Optional.empty());

        // When & Then: Trying to add 3 more (would be 51 total)
        assertThatThrownBy(() -> addItemToOrderAction.execute(testRequest, session))
                .isInstanceOf(MaxItemsExceededException.class)
                .hasMessageContaining("50 items");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenQuantityLessThan1() {
        // Given
        AddItemRequest invalidRequest = AddItemRequest.builder()
                .menuItemId(testMenuItemId)
                .quantity(0)
                .build();

        // When & Then
        assertThatThrownBy(() -> addItemToOrderAction.execute(invalidRequest, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be between 1 and 50");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenQuantityGreaterThan50() {
        // Given
        AddItemRequest invalidRequest = AddItemRequest.builder()
                .menuItemId(testMenuItemId)
                .quantity(51)
                .build();

        // When & Then
        assertThatThrownBy(() -> addItemToOrderAction.execute(invalidRequest, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be between 1 and 50");
    }

    @Test
    void shouldCapturePriceSnapshotFromMenuItem() {
        // Given
        when(session.getAttribute("orderId")).thenReturn(null);
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));
        when(orderLineRepository.findByOrderId(any(UUID.class))).thenReturn(List.of());

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .status("PENDING")
                .totalAmount(new BigDecimal("37.50"))
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        addItemToOrderAction.execute(testRequest, session);

        // Then: Verify unitPrice matches MenuItem.price
        verify(orderLineRepository).save(argThat(line ->
                line.getUnitPrice().equals(testMenuItem.getPrice())
        ));
    }

    @Test
    void shouldCalculateLineTotalCorrectly() {
        // Given
        when(session.getAttribute("orderId")).thenReturn(null);
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));
        when(orderLineRepository.findByOrderId(any(UUID.class))).thenReturn(List.of());

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .status("PENDING")
                .totalAmount(new BigDecimal("37.50"))
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        addItemToOrderAction.execute(testRequest, session);

        // Then: lineTotal = quantity * unitPrice = 3 * 12.50 = 37.50
        verify(orderLineRepository).save(argThat(line ->
                line.getLineTotal().equals(new BigDecimal("37.50"))
        ));
    }

    @Test
    void shouldCalculateOrderTotalAmountCorrectly() {
        // Given: Order has multiple lines
        UUID existingOrderId = UUID.randomUUID();
        when(session.getAttribute("orderId")).thenReturn(existingOrderId.toString());

        Order existingOrder = Order.builder()
                .id(existingOrderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .build();
        when(orderRepository.findById(existingOrderId)).thenReturn(Optional.of(existingOrder));
        when(menuItemRepository.findById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));
        when(orderLineRepository.findByOrderIdAndMenuItemId(existingOrderId, testMenuItemId))
                .thenReturn(Optional.empty());

        // Existing order lines totaling 50.00
        OrderLine line1 = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .lineTotal(new BigDecimal("30.00"))
                .build();
        OrderLine line2 = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(existingOrderId)
                .menuItemId(UUID.randomUUID())
                .quantity(1)
                .unitPrice(new BigDecimal("20.00"))
                .lineTotal(new BigDecimal("20.00"))
                .build();
        when(orderLineRepository.findByOrderId(existingOrderId)).thenReturn(List.of(line1, line2));

        // When: Adding new item with lineTotal 37.50
        addItemToOrderAction.execute(testRequest, session);

        // Then: totalAmount should be 30.00 + 20.00 + 37.50 = 87.50
        verify(orderRepository).save(argThat(order ->
                order.getTotalAmount().equals(new BigDecimal("87.50"))
        ));
    }
}