package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;

public interface ProjectEmailSender {
    void send(ProjectNotificationEmail email);
}