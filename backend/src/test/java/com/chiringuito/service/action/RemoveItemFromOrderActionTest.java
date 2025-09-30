package com.chiringuito.service.action;

import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveItemFromOrderActionTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private RemoveItemFromOrderAction removeItemFromOrderAction;

    private UUID orderId;
    private UUID menuItemId;
    private Order order;
    private OrderLine orderLine;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();

        // Create order
        order = Order.builder()
                .id(orderId)
                .status("PENDING")
                .totalAmount(new BigDecimal("10.00"))
                .build();

        // Create order line
        orderLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .menuItemId(menuItemId)
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .lineTotal(new BigDecimal("10.00"))
                .build();
    }

    @Test
    void shouldRemoveItemFromOrderWithMultipleItems() {
        // Given
        UUID secondMenuItemId = UUID.randomUUID();
        OrderLine secondOrderLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .menuItemId(secondMenuItemId)
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .lineTotal(new BigDecimal("30.00"))
                .build();

        when(session.getAttribute("orderId")).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderLineRepository.findByOrderIdAndMenuItemId(orderId, menuItemId)).thenReturn(Optional.of(orderLine));
        when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of(secondOrderLine));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        removeItemFromOrderAction.execute(menuItemId, session);

        // Then
        verify(orderLineRepository).delete(orderLine);
        verify(orderRepository).save(order);
        verify(orderRepository, never()).delete(any(Order.class));
        verify(session, never()).removeAttribute("orderId");

        // Verify order total was recalculated
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void shouldDeleteOrderAndClearSessionWhenRemovingLastItem() {
        // Given
        when(session.getAttribute("orderId")).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderLineRepository.findByOrderIdAndMenuItemId(orderId, menuItemId)).thenReturn(Optional.of(orderLine));
        when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of());

        // When
        removeItemFromOrderAction.execute(menuItemId, session);

        // Then
        verify(orderLineRepository).delete(orderLine);
        verify(orderRepository).delete(order);
        verify(session).removeAttribute("orderId");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // Given
        when(session.getAttribute("orderId")).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> removeItemFromOrderAction.execute(menuItemId, session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order not found");

        verify(orderLineRepository, never()).delete(any(OrderLine.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenItemNotInOrder() {
        // Given
        UUID differentMenuItemId = UUID.randomUUID();

        when(session.getAttribute("orderId")).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderLineRepository.findByOrderIdAndMenuItemId(orderId, differentMenuItemId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> removeItemFromOrderAction.execute(differentMenuItemId, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item not found in cart");

        verify(orderLineRepository, never()).delete(any(OrderLine.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenNoActiveOrder() {
        // Given
        when(session.getAttribute("orderId")).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> removeItemFromOrderAction.execute(menuItemId, session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No active order in session");

        verify(orderRepository, never()).findById(any());
        verify(orderLineRepository, never()).delete(any(OrderLine.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void shouldRecalculateTotalCorrectlyAfterRemoval() {
        // Given
        UUID secondMenuItemId = UUID.randomUUID();
        UUID thirdMenuItemId = UUID.randomUUID();

        OrderLine secondOrderLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .menuItemId(secondMenuItemId)
                .quantity(2)
                .unitPrice(new BigDecimal("15.50"))
                .lineTotal(new BigDecimal("31.00"))
                .build();

        OrderLine thirdOrderLine = OrderLine.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .menuItemId(thirdMenuItemId)
                .quantity(3)
                .unitPrice(new BigDecimal("5.00"))
                .lineTotal(new BigDecimal("15.00"))
                .build();

        when(session.getAttribute("orderId")).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderLineRepository.findByOrderIdAndMenuItemId(orderId, menuItemId)).thenReturn(Optional.of(orderLine));
        when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of(secondOrderLine, thirdOrderLine));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        removeItemFromOrderAction.execute(menuItemId, session);

        // Then
        verify(orderLineRepository).delete(orderLine);
        verify(orderRepository).save(order);

        // Verify order total was recalculated correctly (31.00 + 15.00)
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("46.00"));
    }

    @Test
    void shouldHandleStringOrderIdInSession() {
        // Given
        String orderIdString = orderId.toString();
        when(session.getAttribute("orderId")).thenReturn(orderIdString);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderLineRepository.findByOrderIdAndMenuItemId(orderId, menuItemId)).thenReturn(Optional.of(orderLine));
        when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of());

        // When
        removeItemFromOrderAction.execute(menuItemId, session);

        // Then
        verify(orderLineRepository).delete(orderLine);
        verify(orderRepository).delete(order);
        verify(session).removeAttribute("orderId");
    }
}