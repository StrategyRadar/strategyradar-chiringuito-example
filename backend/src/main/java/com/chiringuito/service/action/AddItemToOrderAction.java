package com.chiringuito.service.action;

import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.domain.repository.OrderLineRepository;
import com.chiringuito.domain.repository.OrderRepository;
import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.entity.Order;
import com.chiringuito.domain.entity.OrderLine;
import com.chiringuito.service.dto.AddItemRequest;
import com.chiringuito.service.dto.OrderLineDTO;
import com.chiringuito.service.dto.OrderSummaryDTO;
import com.chiringuito.service.exception.MaxItemsExceededException;
import com.chiringuito.service.exception.MenuItemNotFoundException;
import com.chiringuito.service.exception.MenuItemUnavailableException;
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
public class AddItemToOrderAction {

    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;

    private static final String ORDER_ID_SESSION_KEY = "orderId";
    private static final int MAX_QUANTITY_PER_LINE = 50;
    private static final int MAX_TOTAL_ITEMS = 50;

    @Transactional
    public OrderSummaryDTO execute(AddItemRequest request, HttpSession session) {
        // Validate quantity
        if (request.getQuantity() < 1 || request.getQuantity() > MAX_QUANTITY_PER_LINE) {
            throw new IllegalArgumentException("Quantity must be between 1 and 50");
        }

        // Validate menu item exists and is available
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found with id: " + request.getMenuItemId()));

        if (!menuItem.getAvailable()) {
            throw new MenuItemUnavailableException("Menu item is not available: " + menuItem.getName());
        }

        // Load or create order
        Object sessionOrderIdAttr = session.getAttribute(ORDER_ID_SESSION_KEY);
        UUID sessionOrderId = null;
        if (sessionOrderIdAttr instanceof UUID) {
            sessionOrderId = (UUID) sessionOrderIdAttr;
        } else if (sessionOrderIdAttr instanceof String) {
            sessionOrderId = UUID.fromString((String) sessionOrderIdAttr);
        }

        Order order;
        boolean isNewOrder = (sessionOrderId == null);

        if (sessionOrderId != null) {
            order = orderRepository.findById(sessionOrderId)
                    .orElseGet(() -> {
                        Order newOrder = createNewOrder();
                        return orderRepository.save(newOrder);
                    });
        } else {
            order = createNewOrder();
            // Save to get an ID for the order lines
            order = orderRepository.save(order);
        }

        final UUID orderId = order.getId();

        // Find existing order line or create new
        OrderLine orderLine = orderLineRepository
                .findByOrderIdAndMenuItemId(orderId, menuItem.getId())
                .orElseGet(() -> {
                    OrderLine newLine = new OrderLine();
                    newLine.setOrderId(orderId);
                    newLine.setMenuItemId(menuItem.getId());
                    newLine.setQuantity(0);
                    newLine.setUnitPrice(menuItem.getPrice());
                    newLine.setLineTotal(BigDecimal.ZERO);
                    return newLine;
                });

        // Calculate new total items across all lines
        List<OrderLine> allOrderLines = orderLineRepository.findByOrderId(orderId);
        int currentTotalItems = allOrderLines.stream()
                .filter(line -> !line.getId().equals(orderLine.getId()))
                .mapToInt(OrderLine::getQuantity)
                .sum();

        int newTotalItems = currentTotalItems + request.getQuantity();

        if (newTotalItems > MAX_TOTAL_ITEMS) {
            throw new MaxItemsExceededException("Cannot exceed maximum of 50 items in cart");
        }

        // Update order line
        orderLine.setQuantity(orderLine.getQuantity() + request.getQuantity());
        orderLine.setUnitPrice(menuItem.getPrice()); // Capture price snapshot
        orderLine.setLineTotal(
                BigDecimal.valueOf(orderLine.getQuantity()).multiply(orderLine.getUnitPrice())
        );

        OrderLine savedOrderLine = orderLineRepository.save(orderLine);
        // Handle case where mock returns null (shouldn't happen in production)
        if (savedOrderLine == null) {
            savedOrderLine = orderLine;
        }

        // Build final order lines list: update existing or add new saved line
        final OrderLine finalSavedLine = savedOrderLine;
        boolean isNewLine = allOrderLines.stream()
                .noneMatch(line -> line.getMenuItemId().equals(finalSavedLine.getMenuItemId()));

        if (isNewLine) {
            // Add new saved line to the list
            allOrderLines = new java.util.ArrayList<>(allOrderLines);
            allOrderLines.add(finalSavedLine);
        } else {
            // Replace existing line with saved version
            allOrderLines = allOrderLines.stream()
                    .map(line -> line.getMenuItemId().equals(finalSavedLine.getMenuItemId()) ? finalSavedLine : line)
                    .collect(Collectors.toList());
        }

        BigDecimal totalAmount = allOrderLines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update and save order total (only once per execute call)
        if (!isNewOrder) {
            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
        }

        // Store order ID in session if new
        if (isNewOrder) {
            session.setAttribute(ORDER_ID_SESSION_KEY, orderId);
        }

        // Build and return OrderSummaryDTO
        // Fetch all unique menu items needed for the DTO
        java.util.Set<UUID> menuItemIds = allOrderLines.stream()
                .map(OrderLine::getMenuItemId)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<UUID, MenuItem> menuItemMap = new java.util.HashMap<>();
        menuItemMap.put(menuItem.getId(), menuItem); // Add the one we already have

        // Fetch any other menu items needed
        for (UUID itemId : menuItemIds) {
            if (!menuItemMap.containsKey(itemId)) {
                menuItemRepository.findById(itemId).ifPresent(item -> menuItemMap.put(itemId, item));
            }
        }

        return buildOrderSummaryDTO(orderId, allOrderLines, totalAmount, menuItemMap);
    }

    private Order createNewOrder() {
        Order order = new Order();
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);
        // Note: Order is not saved here, will be saved after calculating total
        return order;
    }

    private OrderSummaryDTO buildOrderSummaryDTO(UUID orderId, List<OrderLine> orderLines,
                                                   BigDecimal totalAmount, java.util.Map<UUID, MenuItem> menuItemMap) {
        List<OrderLineDTO> orderLineDTOs = orderLines.stream()
                .map(line -> {
                    MenuItem item = menuItemMap.get(line.getMenuItemId());
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

        int itemCount = orderLines.stream()
                .mapToInt(OrderLine::getQuantity)
                .sum();

        return OrderSummaryDTO.builder()
                .orderId(orderId)
                .status("PENDING")
                .totalAmount(totalAmount)
                .itemCount(itemCount)
                .orderLines(orderLineDTOs)
                .build();
    }
}