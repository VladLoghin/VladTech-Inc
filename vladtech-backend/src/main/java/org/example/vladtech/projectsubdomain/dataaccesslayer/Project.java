package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String projectIdentifier;
    private String name;
    private String clientId;
    private String clientName;
    private String clientEmail;
    private Address address;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private ProjectType projectType;
    private List<String> assignedEmployeeIds = new ArrayList<>();
    private List<String> assignedEmployeeEmails = new ArrayList<>();
    private List<ProjectPhoto> photos = new ArrayList<>();
    private ProjectStatus status;
    private ProjectState state = ProjectState.ACTIVE;
    private LocalDateTime archivedAt;
    private BigDecimal estimatedCost;
    private String estimatedCostCurrency;
    private ProjectPriority priority = ProjectPriority.MEDIUM;
}