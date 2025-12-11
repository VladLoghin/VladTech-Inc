package org.example.vladtech.projectsubdomain.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectNotificationEmail {

    private String recipientEmail;
    private String subject;
    private String projectIdentifier;
    private String projectName;
    private String clientName;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String address;
    private String projectType;
    private String operation;
    private LocalDateTime sentDate;

    public ProjectNotificationEmail(String recipientEmail, String subject, String projectIdentifier,
                                    String projectName, String clientName, String description,
                                    LocalDate startDate, LocalDate dueDate, String address,
                                    String projectType, String operation, LocalDateTime sentDate) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.projectIdentifier = projectIdentifier;
        this.projectName = projectName;
        this.clientName = clientName;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.address = address;
        this.projectType = projectType;
        this.operation = operation;
        this.sentDate = sentDate;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getAddress() {
        return address;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getOperation() {
        return operation;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }
}