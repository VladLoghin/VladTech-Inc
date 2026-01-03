package org.example.vladtech.projectsubdomain.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String projectIdentifier) {
        super("Project not found: " + projectIdentifier);
    }
}
