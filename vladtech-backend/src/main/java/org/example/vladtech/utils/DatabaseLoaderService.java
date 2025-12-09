package org.example.vladtech.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;

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
        projectRepository.deleteAll();
        reviewRepository.deleteAll();

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

        ////////////////////////////////////////////////// add data functions below, also make sure to mark them with huge comment lines so we can easily identify what its for
        createProject(
                "PROJ-1",
                "Kitchen Renovation",
                "123 Main St", "Montreal", "Quebec", "Canada", "H1A 1A1",
                "Complete kitchen remodel including cabinets and countertops",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        createProject(
                "PROJ-2",
                "Bathroom Repair",
                "456 Oak Ave", "Montreal", "Quebec", "Canada", "H2B 2B2",
                "Emergency plumbing repair and tile replacement",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 15),
                ProjectType.ProjectTypeEnum.APPOINTMENT
        );

        ////////////////////////////////////////////////// add Reviews subdomain sample data
        log.info("Appending sample review data to MongoDB...");

        createReview("client-001", "appointment-001", "Amazing service! Highly recommend.", true, Rating.FIVE,
                List.of(new Photo("client-001", "Reno1.jpg", "image/jpeg", "/uploads/reviews/Reno1.jpg")));

        createReview("client-002", "appointment-002", "Good, but could be faster.", true, Rating.FOUR,
                List.of(new Photo("client-002", "Reno2.jpg", "image/jpeg", "/uploads/reviews/Reno2.jpg")));

        createReview("client-003", "appointment-003", "Not satisfied with the quality.", false, Rating.TWO,
                List.of(new Photo("client-003", "Reno3.jpg", "image/jpeg", "/uploads/reviews/Reno3.jpg")));

        createReview("client-004", "appointment-004", "Fantastic experience, will definitely come back!", true, Rating.FIVE,
                List.of(new Photo("client-004", "Reno4.jpg", "image/jpeg", "/uploads/reviews/Reno4.jpg")));

        createReview("client-005", "appointment-005", "Pretty good, but room for improvement.", true, Rating.FOUR,
                List.of(new Photo("client-005", "Reno5.jpg", "image/jpeg", "/uploads/reviews/Reno5.jpg")));

        createReview("client-006", "appointment-006", "Average service, nothing special.", false, Rating.THREE,
                List.of(new Photo("client-006", "Reno1.jpg", "image/jpeg", "/uploads/reviews/Reno1.jpg")));

        createReview("client-007", "appointment-007", "Excellent staff and quick service!", true, Rating.FIVE,
                List.of(new Photo("client-007", "Reno2.jpg", "image/jpeg", "/uploads/reviews/Reno2.jpg")));

        createReview("client-008", "appointment-008", "Decent service, but a bit slow.", true, Rating.FOUR,
                List.of(new Photo("client-008", "Reno3.jpg", "image/jpeg", "/uploads/reviews/Reno3.jpg")));

        createReview("client-009", "appointment-009", "Very disappointed, would not recommend.", false, Rating.ONE,
                List.of(new Photo("client-009", "Reno4.jpg", "image/jpeg", "/uploads/reviews/Reno4.jpg")));

        createReview("client-010", "appointment-010", "Loved the experience! Highly professional.", true, Rating.FIVE,
                List.of(new Photo("client-010", "Reno5.jpg", "image/jpeg", "/uploads/reviews/Reno5.jpg")));

        log.info("Sample review data appended successfully. Total reviews: {}", reviewRepository.count());

    }

    ////////////////////////////////////////////////// add functions like (createProject() as an example or whatever below)

    // ///////////////////////////////////////////////////////////
    // FUNCTION TO CREATE PROJECT
    // ///////////////////////////////////////////////////////////
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

    // ///////////////////////////////////////////////////////////
    // FUNCTION TO CREATE REVIEW
    // ///////////////////////////////////////////////////////////
    private void createReview(String clientId,
                              String appointmentId,
                              String comment,
                              Boolean visible,
                              Rating rating,
                              List<Photo> photos) {
        try {
            Review review = new Review(clientId, appointmentId, comment, visible, rating, photos);
            reviewRepository.save(review);
            log.debug("Created review for clientId: {} and appointmentId: {}", clientId, appointmentId);
        } catch (Exception e) {
            log.error("Error creating review record: {}", e.getMessage(), e);
        }
    }
}
