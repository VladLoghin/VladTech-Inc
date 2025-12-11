package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProjectEmailMapper {

    public ProjectNotificationEmail toProjectNotificationEmail(Project project, String operation) {
        if (project == null || project.getClientEmail() == null || project.getClientEmail().isBlank()) {
            return null;
        }

        String subject = String.format("Project %s: %s", operation, project.getName());
        String addressString = formatAddress(project.getAddress());
        String projectType = project.getProjectType() != null ? project.getProjectType().getType().name() : null;

        return new ProjectNotificationEmail(
                project.getClientEmail(),
                subject,
                project.getProjectIdentifier(),
                project.getName(),
                project.getClientName(),
                project.getDescription(),
                project.getStartDate(),
                project.getDueDate(),
                addressString,
                projectType,
                operation,
                LocalDateTime.now()
        );
    }

    private String formatAddress(Address address) {
        if (address == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (address.getStreetAddress() != null && !address.getStreetAddress().isBlank()) {
            sb.append(address.getStreetAddress());
        }

        if (address.getCity() != null && !address.getCity().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCity());
        }

        if (address.getProvince() != null && !address.getProvince().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getProvince());
        }

        if (address.getPostalCode() != null && !address.getPostalCode().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(address.getPostalCode());
        }

        if (address.getCountry() != null && !address.getCountry().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCountry());
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}