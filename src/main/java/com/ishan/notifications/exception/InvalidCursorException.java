package com.ishan.notifications.exception;

public class InvalidCursorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCursorException(String message) {
        super(message);
    }
}
