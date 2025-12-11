package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestModel {

    private String name;
    private String clientId;
    private String clientName;
    private String clientEmail;
    private AddressRequestModel address;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String projectType;
    private List<String> assignedEmployeeIds;
}