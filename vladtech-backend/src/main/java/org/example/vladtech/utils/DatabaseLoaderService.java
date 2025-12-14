package org.example.vladtech.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final PortfolioRepository portfolioRepository;

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
        portfolioRepository.deleteAll();

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
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 3, 30),
                ProjectType.ProjectTypeEnum.SCHEDULED
        );

        createProject(
                "PROJ-2",
                "Bathroom Repair",
                "456 Oak Ave", "Montreal", "Quebec", "Canada", "H2B 2B2",
                "Emergency plumbing repair and tile replacement",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 15),
                ProjectType.ProjectTypeEnum.APPOINTMENT
        );

        ////////////////////////////////////////////////// add Reviews subdomain sample data
        log.info("Appending sample review data to MongoDB...");

        createReview("client-001", "appointment-001", "Roger",  "Amazing service! Highly recommend.", true, Rating.FIVE,
                List.of(new Photo("client-001", "Reno1.jpg", "image/jpeg", "/uploads/reviews/Reno1.jpg")));

        createReview("client-002", "appointment-002", "Karen", "Good, but could be faster.", true, Rating.FOUR,
                List.of(new Photo("client-002", "Reno2.jpg", "image/jpeg", "/uploads/reviews/Reno2.jpg")));

        createReview("client-003", "appointment-003", "Josh", "Not satisfied with the quality.", false, Rating.TWO,
                List.of(new Photo("client-003", "Reno3.jpg", "image/jpeg", "/uploads/reviews/Reno3.jpg")));

        createReview("client-004", "appointment-004", "Reed Richards","Fantastic experience, will definitely come back!", true, Rating.FIVE,
                List.of(new Photo("client-004", "Reno4.jpg", "image/jpeg", "/uploads/reviews/Reno4.jpg")));

        createReview("client-005", "appointment-005", "Raymond","Pretty good, but room for improvement.", true, Rating.FOUR,
                List.of(new Photo("client-005", "Reno5.jpg", "image/jpeg", "/uploads/reviews/Reno5.jpg")));

        createReview("client-006", "appointment-006", "John", "Average service, nothing special.", false, Rating.THREE,
                List.of(new Photo("client-006", "Reno1.jpg", "image/jpeg", "/uploads/reviews/Reno1.jpg")));

        createReview("client-007", "appointment-007", "Isabelle","Excellent staff and quick service!", true, Rating.FIVE,
                List.of(new Photo("client-007", "Reno2.jpg", "image/jpeg", "/uploads/reviews/Reno2.jpg")));

        createReview("client-008", "appointment-008", "Joshua","Decent service, but a bit slow.", true, Rating.FOUR,
                List.of(new Photo("client-008", "Reno3.jpg", "image/jpeg", "/uploads/reviews/Reno3.jpg")));

        createReview("client-009", "appointment-009", "Peter","Very disappointed, would not recommend.", false, Rating.ONE,
                List.of(new Photo("client-009", "Reno4.jpg", "image/jpeg", "/uploads/reviews/Reno4.jpg")));

        createReview("client-010", "appointment-010", "Simon","Loved the experience! Highly professional.", true, Rating.FIVE,
                List.of(new Photo("client-010", "Reno5.jpg", "image/jpeg", "/uploads/reviews/Reno5.jpg")));

        log.info("Sample review data appended successfully. Total reviews: {}", reviewRepository.count());

        ////////////////////////////////////////////////// add Portfolio subdomain sample data
        log.info("Appending sample portfolio data to MongoDB...");

        Instant now = Instant.now();

        createPortfolioItem("Modern Kitchen Counter", "/images/Reno1.jpg", 4.9,
                List.of(
                        new PortfolioComment("Sarah M.", "sample-user-1", now.minusSeconds(10800), "Beautiful countertop! The finish is perfect."),
                        new PortfolioComment("John D.", "sample-user-2", now.minusSeconds(3600), "Love the modern design and clean look.")
                ));

        createPortfolioItem("Complete Kitchen Remodel", "/images/Reno2.jpg", 5.0,
                List.of(
                        new PortfolioComment("Emma L.", "sample-user-3", now.minusSeconds(18000), "Amazing transformation! Best kitchen renovation I've seen."),
                        new PortfolioComment("Michael R.", "sample-user-4", now.minusSeconds(7200), "The attention to detail is outstanding.")
                ));

        createPortfolioItem("Luxury Bathroom Renovation", "/images/Reno3.jpg", 4.8,
                List.of(
                        new PortfolioComment("Lisa K.", "sample-user-5", now.minusSeconds(14400), "Stunning bathroom design. Very elegant!"),
                        new PortfolioComment("David P.", "sample-user-6", now.minusSeconds(21600), "The tile work is absolutely beautiful.")
                ));

        createPortfolioItem("Contemporary Office Space", "/images/Reno4.jpg", 4.7,
                List.of(
                        new PortfolioComment("Anna S.", "sample-user-7", now.minusSeconds(10800), "Great use of space and natural lighting."),
                        new PortfolioComment("Tom W.", "sample-user-8", now.minusSeconds(28800), "Very professional and modern office design.")
                ));

        createPortfolioItem("Custom Shower Installation", "/images/Reno5.jpg", 4.9,
                List.of(
                        new PortfolioComment("Rachel B.", "sample-user-9", now.minusSeconds(86400), "Perfect execution! Love the glass work."),
                        new PortfolioComment("Chris M.", "sample-user-10", now.minusSeconds(43200), "High-quality shower installation.")
                ));

        createPortfolioItem("Entertainment Center & TV Setup", "/images/Reno1.jpg", 4.6,
                List.of(
                        new PortfolioComment("Mark H.", "sample-user-11", now.minusSeconds(172800), "Clean TV mounting and cable management."),
                        new PortfolioComment("Jennifer L.", "sample-user-12", now.minusSeconds(18000), "Great entertainment center design!")
                ));

        log.info("Sample portfolio data appended successfully. Total portfolio items: {}", portfolioRepository.count());

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
                              String clientName,
                              String comment,
                              Boolean visible,
                              Rating rating,
                              List<Photo> photos) {
        try {
            Review review = new Review(clientId, appointmentId, clientName, comment, visible, rating, photos);
            reviewRepository.save(review);
            log.debug("Created review for clientId: {} and appointmentId: {}", clientId, appointmentId);
        } catch (Exception e) {
            log.error("Error creating review record: {}", e.getMessage(), e);
        }
    }

    // ///////////////////////////////////////////////////////////
    // FUNCTION TO CREATE PORTFOLIO ITEM
    // ///////////////////////////////////////////////////////////
    private void createPortfolioItem(String title,
                                     String imageUrl,
                                     Double rating,
                                     List<PortfolioComment> comments) {
        try {
            PortfolioItem portfolioItem = new PortfolioItem(title, imageUrl, rating, comments);
            portfolioRepository.save(portfolioItem);
            log.debug("Created portfolio item: {}", title);
        } catch (Exception e) {
            log.error("Error creating portfolio item: {}", e.getMessage(), e);
        }
    }
}
