package com.chiringuito.web.controller;

import com.chiringuito.service.action.AddItemToOrderAction;
import com.chiringuito.service.action.GetCartAction;
import com.chiringuito.service.action.RemoveItemFromOrderAction;
import com.chiringuito.service.action.UpdateItemQuantityAction;
import com.chiringuito.service.dto.AddItemRequest;
import com.chiringuito.service.dto.UpdateQuantityRequest;
import com.chiringuito.service.dto.OrderSummaryDTO;
import com.chiringuito.service.exception.MaxItemsExceededException;
import com.chiringuito.service.exception.MenuItemNotFoundException;
import com.chiringuito.service.exception.MenuItemUnavailableException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final AddItemToOrderAction addItemToOrderAction;
    private final GetCartAction getCartAction;
    private final RemoveItemFromOrderAction removeItemFromOrderAction;
    private final UpdateItemQuantityAction updateItemQuantityAction;

    @PostMapping("/add-item")
    public ResponseEntity<OrderSummaryDTO> addItem(
            @RequestBody @jakarta.validation.Valid AddItemRequest request,
            HttpSession session) {
        OrderSummaryDTO summary = addItemToOrderAction.execute(request, session);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/cart")
    public ResponseEntity<OrderSummaryDTO> getCart(HttpSession session) {
        OrderSummaryDTO summary = getCartAction.execute(session);
        if (summary == null) {
            return ResponseEntity.ok().build(); // Empty cart - return 200 with no body
        }
        return ResponseEntity.ok(summary);
    }

    @DeleteMapping("/remove-item/{menuItemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID menuItemId,
            HttpSession session) {
        removeItemFromOrderAction.execute(menuItemId, session);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-quantity")
    public ResponseEntity<OrderSummaryDTO> updateQuantity(
            @RequestBody @jakarta.validation.Valid UpdateQuantityRequest request,
            HttpSession session) {
        OrderSummaryDTO summary = updateItemQuantityAction.execute(request, session);
        return ResponseEntity.ok(summary);
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMenuItemNotFound(MenuItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MenuItemUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleMenuItemUnavailable(MenuItemUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MaxItemsExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxItemsExceeded(MaxItemsExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    private record ErrorResponse(String message) {}
}