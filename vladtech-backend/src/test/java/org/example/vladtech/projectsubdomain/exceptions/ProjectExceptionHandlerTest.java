package org.example.vladtech.projectsubdomain.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ProjectExceptionHandlerTest {

    private final ProjectExceptionHandler handler = new ProjectExceptionHandler();

    @Test
    void handleProjectNotFound_ShouldReturnNotFoundStatus() {
        ProjectNotFoundException exception = new ProjectNotFoundException("PROJ-123");

        ResponseEntity<ProjectErrorResponse> response = handler.handleProjectNotFound(exception);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("PROJECT_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Project not found: PROJ-123", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleInvalidEmployeeId_ShouldReturnBadRequestStatus() {
        InvalidEmployeeIdException exception = new InvalidEmployeeIdException("employeeId cannot be null or blank");

        ResponseEntity<ProjectErrorResponse> response = handler.handleInvalidEmployeeId(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("INVALID_EMPLOYEE_ID", response.getBody().getErrorCode());
        assertEquals("employeeId cannot be null or blank", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ProjectErrorResponse> response = handler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleProjectArchived_ShouldReturnConflictStatus() {
        ProjectArchivedException exception = new ProjectArchivedException("PROJ-123");

        ResponseEntity<ProjectErrorResponse> response = handler.handleProjectArchived(exception);

        assertNotNull(response);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("PROJECT_ARCHIVED", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("PROJ-123"));
        assertNotNull(response.getBody().getTimestamp());
    }
    @Test
    void handleInvalidProjectData_ShouldReturnUnprocessableEntityStatus() {
        InvalidProjectDataException exception = new InvalidProjectDataException("Invalid data");

        ResponseEntity<ProjectErrorResponse> response = handler.handleInvalidProjectData(exception);

        assertNotNull(response);
        assertEquals(422, response.getStatusCode().value());
        assertEquals("INVALID_PROJECT_DATA", response.getBody().getErrorCode());
        assertEquals("Invalid data", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
