package com.library.jwtautostarter.exception;

/**
 * Thrown when a request carries invalid or malformed input.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
