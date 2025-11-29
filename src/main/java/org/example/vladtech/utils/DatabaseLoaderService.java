package org.example.vladtech.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

//    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading sample data into MongoDB...");

        // Check if data already exists
//        if (projectRepository.count() > 0) {
//            log.info("Database already contains project data. Skipping initialization.");
//            return;
//        }

        /// ///////////////////////////////////////////////////////// WE DELETE THE DATA EVERY TIME WE RUN IN DEVELOPMENT ENRVORNMENT. WITH DEPLOYED, IT SHOULD BE DIFFERENT
        log.info("Clearing existing data...");
//        projectRepository.deleteAll();

        /// ///////////////////////////////////////////////////////////////// use this comment block below as an example for creating new thing in data
        // Sample projects
//        createProject(
//                "PROJ-001",
//                "Kitchen Renovation",
//                "123 Main St", "Montreal", "Quebec", "Canada", "H1A 1A1",
//                "Complete kitchen remodel including cabinets and countertops",
//                LocalDate.of(2025, 1, 15),
//                LocalDate.of(2025, 3, 30),
//                ProjectType.ProjectTypeEnum.SCHEDULED
//        );
//
//        createProject(
//                "PROJ-002",
//                "Bathroom Repair",
//                "456 Oak Ave", "Montreal", "Quebec", "Canada", "H2B 2B2",
//                "Emergency plumbing repair and tile replacement",
//                LocalDate.of(2025, 2, 1),
//                LocalDate.of(2025, 2, 15),
//                ProjectType.ProjectTypeEnum.APPOINTMENT
//        );
//
//        log.info("Sample project data loaded successfully. Total projects: {}", projectRepository.count());
//    }

    /// ///////////////////////////////////////////////////////////////// use this comment block below as an example for the function to create a new thing

//    private void createProject(
//            String projectIdentifier,
//            String name,
//            String streetAddress,
//            String city,
//            String province,
//            String country,
//            String postalCode,
//            String description,
//            LocalDate startDate,
//            LocalDate dueDate,
//            ProjectType.ProjectTypeEnum projectTypeEnum) {
//
//        try {
//            Address address = new Address(streetAddress, city, province, country, postalCode);
//
//            ProjectType projectType = new ProjectType();
//            projectType.setType(projectTypeEnum);
//
//            Project project = new Project();
//            project.setProjectIdentifier(projectIdentifier);
//            project.setName(name);
//            project.setAddress(address);
//            project.setDescription(description);
//            project.setStartDate(startDate);
//            project.setDueDate(dueDate);
//            project.setProjectType(projectType);
//
//            projectRepository.save(project);
//            log.debug("Created project with identifier: {}", project.getProjectIdentifier());
//        } catch (Exception e) {
//            log.error("Error creating project record: {}", e.getMessage(), e);
//        }
//    }

    /// /////////////////////////////////////////////////////////////////////// IMPORTANT
        ////INSERT REAL DATABASE INSERTION CODE BELOW HERE PLEASE!!!!!
    /// /////////////////////////////////////////////////////////////////////// IMPORTANT
}