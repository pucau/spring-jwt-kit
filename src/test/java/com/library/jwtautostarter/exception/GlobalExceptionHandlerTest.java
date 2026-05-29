package com.library.jwtautostarter.exception;

import com.library.jwtautostarter.exception.GlobalExceptionHandler.FieldError;
import com.library.jwtautostarter.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleAccessDeniedReturns403() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("forbidden"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    void handleBadCredentialsReturns401() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBadCredentialsException(new BadCredentialsException("bad creds"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleNotFoundReturns404() throws Exception {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNoResourceFoundException(
                        new NoResourceFoundException(null, "/some/path"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("not found");
    }

    @Test
    void handleIllegalArgumentReturns400() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("bad input");
    }

    @Test
    void handleGenericExceptionReturns500() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleException(new RuntimeException("unexpected error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
    }

    @Test
    void handleBadRequestExceptionReturns400() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBadRequestException(new BadRequestException("validation failed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("validation failed");
    }

    @Test
    void handleMethodArgumentNotValidReturns400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        org.springframework.validation.FieldError springFieldError =
                new org.springframework.validation.FieldError("request", "email", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(springFieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<List<FieldError>>> response =
                handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().getData().get(0).message()).isEqualTo("must not be blank");
    }

    @Test
    void handleHttpMessageNotReadableReturns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleHttpMessageNotReadableException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed request body");
    }

    @Test
    void handleHttpRequestMethodNotSupportedReturns405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleHttpRequestMethodNotSupportedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Method not allowed: DELETE");
    }
}
