package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.service.dto.OrderLineDTO;
import com.chiringuito.service.dto.OrderSummaryDTO;
import com.chiringuito.service.dto.UpdateQuantityRequest;
import com.chiringuito.service.exception.MenuItemNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateItemQuantityAction {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final MenuItemRepository menuItemRepository;

    private static final String ORDER_ID_SESSION_KEY = "orderId";

    @Transactional
    public OrderSummaryDTO execute(UpdateQuantityRequest request, HttpSession session) {
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

        // Find the order line
        OrderLine orderLine = orderLineRepository
                .findByOrderIdAndMenuItemId(order.getId(), request.getMenuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        // Verify menu item still exists
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        // Update quantity and line total
        orderLine.setQuantity(request.getQuantity());
        orderLine.setLineTotal(
                BigDecimal.valueOf(request.getQuantity()).multiply(orderLine.getUnitPrice())
        );

        orderLineRepository.save(orderLine);

        // Recalculate order total
        List<OrderLine> allOrderLines = orderLineRepository.findByOrderId(order.getId());
        BigDecimal totalAmount = allOrderLines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        // Build and return OrderSummaryDTO
        List<OrderLineDTO> orderLineDTOs = allOrderLines.stream()
                .map(line -> {
                    MenuItem item = menuItemRepository.findById(line.getMenuItemId()).orElse(null);
                    String itemName = (item != null) ? item.getName() : "Unknown Item";
                    return new OrderLineDTO(
                            line.getId(),
                            line.getMenuItemId(),
                            itemName,
                            line.getQuantity(),
                            line.getUnitPrice(),
                            line.getLineTotal()
                    );
                })
                .collect(Collectors.toList());

        int itemCount = allOrderLines.stream()
                .mapToInt(OrderLine::getQuantity)
                .sum();

        return OrderSummaryDTO.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(totalAmount)
                .itemCount(itemCount)
                .orderLines(orderLineDTOs)
                .build();
    }
}