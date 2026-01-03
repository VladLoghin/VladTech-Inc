package org.example.vladtech.projectsubdomain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidEmployeeIdExceptionTest {

    @Test
    void constructor_ShouldCreateExceptionWithMessage() {
        String message = "employeeId cannot be null or blank";

        InvalidEmployeeIdException exception = new InvalidEmployeeIdException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleNullMessage() {
        InvalidEmployeeIdException exception = new InvalidEmployeeIdException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void exception_ShouldExtendRuntimeException() {
        InvalidEmployeeIdException exception = new InvalidEmployeeIdException("test");

        assertTrue(exception instanceof RuntimeException);
    }
}
