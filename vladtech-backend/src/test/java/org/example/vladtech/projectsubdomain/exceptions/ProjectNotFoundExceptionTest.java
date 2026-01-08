package org.example.vladtech.projectsubdomain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectNotFoundExceptionTest {

    @Test
    void constructor_ShouldCreateExceptionWithFormattedMessage() {
        String projectIdentifier = "PROJ-123";

        ProjectNotFoundException exception = new ProjectNotFoundException(projectIdentifier);

        assertNotNull(exception);
        assertEquals("Project not found: PROJ-123", exception.getMessage());
    }

    @Test
    void constructor_ShouldHandleNullIdentifier() {
        ProjectNotFoundException exception = new ProjectNotFoundException(null);

        assertNotNull(exception);
        assertEquals("Project not found: null", exception.getMessage());
    }

    @Test
    void exception_ShouldExtendRuntimeException() {
        ProjectNotFoundException exception = new ProjectNotFoundException("PROJ-1");

        assertTrue(exception instanceof RuntimeException);
    }
}
