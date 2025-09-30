package com.chiringuito.service.action;

import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveItemFromOrderAction {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;

    private static final String ORDER_ID_SESSION_KEY = "orderId";

    @Transactional
    public void execute(UUID menuItemId, HttpSession session) {
        // Get orderId from session
        Object sessionOrderIdAttr = session.getAttribute(ORDER_ID_SESSION_KEY);
        UUID sessionOrderId = null;

        if (sessionOrderIdAttr instanceof UUID) {
            sessionOrderId = (UUID) sessionOrderIdAttr;
        } else if (sessionOrderIdAttr instanceof String) {
            sessionOrderId = UUID.fromString((String) sessionOrderIdAttr);
        }

        if (sessionOrderId == null) {
            throw new IllegalStateException("No active order in session");
        }

        // Load order
        Order order = orderRepository.findById(sessionOrderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        // Find and remove the order line
        OrderLine orderLine = orderLineRepository
                .findByOrderIdAndMenuItemId(order.getId(), menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        orderLineRepository.delete(orderLine);

        // Recalculate order total
        List<OrderLine> remainingLines = orderLineRepository.findByOrderId(order.getId());

        if (remainingLines.isEmpty()) {
            // If no items left, delete the order and clear session
            orderRepository.delete(order);
            session.removeAttribute(ORDER_ID_SESSION_KEY);
        } else {
            // Update order total
            BigDecimal totalAmount = remainingLines.stream()
                    .map(OrderLine::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
        }
    }
}