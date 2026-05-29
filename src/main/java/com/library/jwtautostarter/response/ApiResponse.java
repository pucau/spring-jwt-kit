package com.library.jwtautostarter.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Generic wrapper for all API responses, providing a consistent envelope with
 * success flag, message, payload, and timestamp.
 *
 * @param <T> the type of the response payload
 */
public class ApiResponse<T> {

    private static final String DEFAULT_SUCCESS_MESSAGE = "OK";

    private final boolean success;
    private final String message;
    private final T data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Creates a successful response with data and the default message "OK".
     *
     * @param data    the payload
     * @param <T>     payload type
     * @return successful {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, data);
    }

    /**
     * Creates a successful response with data and a custom message.
     *
     * @param data    the payload
     * @param message custom success message
     * @param <T>     payload type
     * @return successful {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with no data.
     *
     * @param message the error message
     * @param <T>     payload type
     * @return error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Creates an error response with supplemental data (e.g. field-level validation errors).
     *
     * @param message the error message
     * @param data    supplemental error payload
     * @param <T>     payload type
     * @return error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
