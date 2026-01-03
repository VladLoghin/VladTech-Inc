package org.example.vladtech.projectsubdomain.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice(basePackages = "org.example.vladtech.projectsubdomain")
public class ProjectExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ProjectErrorResponse> handleProjectNotFound(ProjectNotFoundException ex) {
        log.error("Project not found: {}", ex.getMessage());
        ProjectErrorResponse error = new ProjectErrorResponse(
                "PROJECT_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidEmployeeIdException.class)
    public ResponseEntity<ProjectErrorResponse> handleInvalidEmployeeId(InvalidEmployeeIdException ex) {
        log.error("Invalid employee ID: {}", ex.getMessage());
        ProjectErrorResponse error = new ProjectErrorResponse(
                "INVALID_EMPLOYEE_ID",
                ex.getMessage(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProjectErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred in project subdomain", ex);
        ProjectErrorResponse error = new ProjectErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
