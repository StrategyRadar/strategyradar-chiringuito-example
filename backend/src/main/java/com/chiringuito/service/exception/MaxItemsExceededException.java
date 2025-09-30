package com.chiringuito.service.exception;

public class MaxItemsExceededException extends RuntimeException {

    public MaxItemsExceededException(String message) {
        super(message);
    }
}