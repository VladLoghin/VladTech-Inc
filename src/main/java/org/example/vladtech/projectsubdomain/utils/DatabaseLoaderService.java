package org.example.vladtech.projectsubdomain.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading sample project data into MongoDB...");

        // Check if data already exists
//        if (projectRepository.count() > 0) {
//            log.info("Database already contains project data. Skipping initialization.");
//            return;
//        }

        log.info("Clearing existing project data...");
        projectRepository.deleteAll();

        // Sample projects
        createProject(
                "PROJ-001",
                "Kitchen Renovation",
                "123 Main St", "Montreal", "Quebec", "Canada", "H1A 1A1",
                "Complete kitchen remodel including cabinets and countertops",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        createProject(
                "PROJ-002",
                "Bathroom Repair",
                "456 Oak Ave", "Montreal", "Quebec", "Canada", "H2B 2B2",
                "Emergency plumbing repair and tile replacement",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 15),
                ProjectType.ProjectTypeEnum.APPOINTMENT
        );

        createProject(
                "PROJ-003",
                "Roof Replacement",
                "789 Pine Rd", "Laval", "Quebec", "Canada", "H3C 3C3",
                "Full roof replacement with new shingles",
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 4, 15),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        createProject(
                "PROJ-004",
                "Office Painting",
                "321 Business Blvd", "Montreal", "Quebec", "Canada", "H4D 4D4",
                "Interior painting for commercial office space",
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 20),
                ProjectType.ProjectTypeEnum.APPOINTMENT
        );

        createProject(
                "PROJ-005",
                "Deck Construction",
                "654 Lake View Dr", "Gatineau", "Quebec", "Canada", "J8T 5E5",
                "Build new wooden deck with railing",
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 5, 15),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        createProject(
                "PROJ-006",
                "Window Installation",
                "987 Park Ave", "Quebec City", "Quebec", "Canada", "G1R 6F6",
                "Replace all windows on second floor",
                LocalDate.of(2025, 3, 15),
                LocalDate.of(2025, 4, 1),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        log.info("Sample project data loaded successfully. Total projects: {}", projectRepository.count());
    }

    private void createProject(
            String projectIdentifier,
            String name,
            String streetAddress,
            String city,
            String province,
            String country,
            String postalCode,
            String description,
            LocalDate startDate,
            LocalDate dueDate,
            ProjectType.ProjectTypeEnum projectTypeEnum) {

        try {
            Address address = new Address(streetAddress, city, province, country, postalCode);

            ProjectType projectType = new ProjectType();
            projectType.setType(projectTypeEnum);

            Project project = new Project();
            project.setProjectIdentifier(projectIdentifier);
            project.setName(name);
            project.setAddress(address);
            project.setDescription(description);
            project.setStartDate(startDate);
            project.setDueDate(dueDate);
            project.setProjectType(projectType);

            projectRepository.save(project);
            log.debug("Created project with identifier: {}", project.getProjectIdentifier());
        } catch (Exception e) {
            log.error("Error creating project record: {}", e.getMessage(), e);
        }
    }
}