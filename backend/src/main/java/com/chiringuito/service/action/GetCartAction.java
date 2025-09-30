package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.service.dto.OrderLineDTO;
import com.chiringuito.service.dto.OrderSummaryDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCartAction {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final MenuItemRepository menuItemRepository;

    private static final String ORDER_ID_SESSION_KEY = "orderId";

    @Transactional(readOnly = true)
    public OrderSummaryDTO execute(HttpSession session) {
        // Get orderId from session (handle both String and UUID types)
        Object sessionOrderIdAttr = session.getAttribute(ORDER_ID_SESSION_KEY);

        UUID sessionOrderId = null;

        if (sessionOrderIdAttr instanceof UUID) {
            sessionOrderId = (UUID) sessionOrderIdAttr;
        } else if (sessionOrderIdAttr instanceof String) {
            sessionOrderId = UUID.fromString((String) sessionOrderIdAttr);
        }

        // If no orderId, return null
        if (sessionOrderId == null) {
            return null;
        }

        // Load Order from OrderRepository
        Order order = orderRepository.findById(sessionOrderId).orElse(null);

        // If order not found, return null
        if (order == null) {
            return null;
        }

        // Load all OrderLines for the order
        List<OrderLine> orderLines = orderLineRepository.findByOrderId(order.getId());

        // Calculate totalAmount and itemCount
        BigDecimal totalAmount = orderLines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = orderLines.stream()
                .mapToInt(OrderLine::getQuantity)
                .sum();

        // Build OrderLineDTOs with menu item names
        List<OrderLineDTO> orderLineDTOs = orderLines.stream()
                .map(line -> {
                    MenuItem menuItem = menuItemRepository.findById(line.getMenuItemId()).orElse(null);
                    String itemName = (menuItem != null) ? menuItem.getName() : "Unknown Item";
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

        // Build and return OrderSummaryDTO
        return OrderSummaryDTO.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(totalAmount)
                .itemCount(itemCount)
                .orderLines(orderLineDTOs)
                .build();
    }
}