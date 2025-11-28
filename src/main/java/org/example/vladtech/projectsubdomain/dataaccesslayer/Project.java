package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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
    private Address address;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private ProjectType projectType;
    private List<String> assignedEmployeeIds = new ArrayList<>();
    private List<ProjectPhoto> photos = new ArrayList<>();
}