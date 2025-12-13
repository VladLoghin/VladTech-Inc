package org.example.vladtech.estimates.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstimatesExceptionHandlerTest {

    private final EstimatesExceptionHandler handler = new EstimatesExceptionHandler();

    @Test
    void handleIllegalArgument_ShouldReturnBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("INVALID_INPUT", response.getBody().getErrorCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleValidation_ShouldReturnBadRequest() {
        // Mock the BindingResult and FieldError
        FieldError fieldError = new FieldError("objectName", "field", "must not be null");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Mock the MethodArgumentNotValidException
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Call the handler
        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        // Assertions
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("must not be null"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }
}