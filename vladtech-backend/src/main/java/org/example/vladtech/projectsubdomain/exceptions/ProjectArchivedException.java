package org.example.vladtech.projectsubdomain.exceptions;

/**
 * Exception thrown when attempting to modify an archived (completed) project.
 */
public class ProjectArchivedException extends RuntimeException {
    public ProjectArchivedException(String projectIdentifier) {
        super("Cannot modify archived project: " + projectIdentifier);
    }
}
