package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.service.dto.OrderSummaryDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCartActionTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private GetCartAction getCartAction;

    private UUID testOrderId;
    private Order testOrder;
    private MenuItem testMenuItem1;
    private MenuItem testMenuItem2;
    private OrderLine testOrderLine1;
    private OrderLine testOrderLine2;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();

        testOrder = Order.builder()
                .id(testOrderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .build();

        testMenuItem1 = MenuItem.builder()
                .id(UUID.randomUUID())
                .name("Paella Valenciana")
                .description("Traditional Spanish rice dish")
                .price(new BigDecimal("12.50"))
                .available(true)
                .build();

        testMenuItem2 = MenuItem.builder()
                .id(UUID.randomUUID())
                .name("Gazpacho")
                .description("Cold tomato soup")
                .price(new BigDecimal("6.00"))
                .available(true)
                .build();

        testOrderLine1 = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(testOrderId)
                .menuItemId(testMenuItem1.getId())
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .lineTotal(new BigDecimal("25.00"))
                .build();

        testOrderLine2 = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(testOrderId)
                .menuItemId(testMenuItem2.getId())
                .quantity(4)
                .unitPrice(new BigDecimal("6.00"))
                .lineTotal(new BigDecimal("24.00"))
                .build();
    }

    @Test
    void shouldReturnNullWhenNoOrderInSession() {
        // Given: No order ID in session
        when(session.getAttribute("orderId")).thenReturn(null);

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNull();
        verify(orderRepository, never()).findById(any());
        verify(orderLineRepository, never()).findByOrderId(any());
    }

    @Test
    void shouldReturnNullWhenOrderNotFoundInDatabase() {
        // Given: Order ID exists in session but not in database
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNull();
        verify(orderLineRepository, never()).findByOrderId(any());
    }

    @Test
    void shouldReturnEmptyCartWhenOrderExistsButNoOrderLines() {
        // Given: Order exists but has no order lines
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of());

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrderId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getItemCount()).isEqualTo(0);
        assertThat(result.getOrderLines()).isEmpty();
    }

    @Test
    void shouldReturnCartWithSingleItem() {
        // Given: Order with single order line
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderLine1));
        when(menuItemRepository.findById(testMenuItem1.getId())).thenReturn(Optional.of(testMenuItem1));

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrderId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("25.00"));
        assertThat(result.getItemCount()).isEqualTo(2);
        assertThat(result.getOrderLines()).hasSize(1);

        assertThat(result.getOrderLines().get(0).getMenuItemName()).isEqualTo("Paella Valenciana");
        assertThat(result.getOrderLines().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getOrderLines().get(0).getUnitPrice()).isEqualTo(new BigDecimal("12.50"));
        assertThat(result.getOrderLines().get(0).getLineTotal()).isEqualTo(new BigDecimal("25.00"));
    }

    @Test
    void shouldReturnCartWithMultipleItems() {
        // Given: Order with multiple order lines
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderLine1, testOrderLine2));
        when(menuItemRepository.findById(testMenuItem1.getId())).thenReturn(Optional.of(testMenuItem1));
        when(menuItemRepository.findById(testMenuItem2.getId())).thenReturn(Optional.of(testMenuItem2));

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrderId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("49.00")); // 25.00 + 24.00
        assertThat(result.getItemCount()).isEqualTo(6); // 2 + 4
        assertThat(result.getOrderLines()).hasSize(2);
    }

    @Test
    void shouldHandleOrderIdAsString() {
        // Given: Order ID stored as String in session (common scenario)
        when(session.getAttribute("orderId")).thenReturn(testOrderId.toString());
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of());

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrderId);
    }

    @Test
    void shouldHandleOrderIdAsUUID() {
        // Given: Order ID stored as UUID in session
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of());

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrderId);
    }

    @Test
    void shouldHandleUnknownMenuItemGracefully() {
        // Given: Order line references a menu item that doesn't exist
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderLine1));
        when(menuItemRepository.findById(testMenuItem1.getId())).thenReturn(Optional.empty());

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderLines()).hasSize(1);
        assertThat(result.getOrderLines().get(0).getMenuItemName()).isEqualTo("Unknown Item");
    }

    @Test
    void shouldCalculateTotalAmountFromOrderLines() {
        // Given: Multiple order lines with different totals
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderLine1, testOrderLine2));
        when(menuItemRepository.findById(testMenuItem1.getId())).thenReturn(Optional.of(testMenuItem1));
        when(menuItemRepository.findById(testMenuItem2.getId())).thenReturn(Optional.of(testMenuItem2));

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then: Total should be sum of line totals
        BigDecimal expectedTotal = testOrderLine1.getLineTotal().add(testOrderLine2.getLineTotal());
        assertThat(result.getTotalAmount()).isEqualTo(expectedTotal);
    }

    @Test
    void shouldCalculateItemCountFromOrderLines() {
        // Given: Multiple order lines with different quantities
        when(session.getAttribute("orderId")).thenReturn(testOrderId);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderLineRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderLine1, testOrderLine2));
        when(menuItemRepository.findById(testMenuItem1.getId())).thenReturn(Optional.of(testMenuItem1));
        when(menuItemRepository.findById(testMenuItem2.getId())).thenReturn(Optional.of(testMenuItem2));

        // When
        OrderSummaryDTO result = getCartAction.execute(session);

        // Then: Item count should be sum of quantities
        int expectedItemCount = testOrderLine1.getQuantity() + testOrderLine2.getQuantity();
        assertThat(result.getItemCount()).isEqualTo(expectedItemCount);
    }
}