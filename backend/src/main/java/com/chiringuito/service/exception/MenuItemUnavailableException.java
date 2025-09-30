package com.chiringuito.service.exception;

public class MenuItemUnavailableException extends RuntimeException {

    public MenuItemUnavailableException(String message) {
        super(message);
    }
}