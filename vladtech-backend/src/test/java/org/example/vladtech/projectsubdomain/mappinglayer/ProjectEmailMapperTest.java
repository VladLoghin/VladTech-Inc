package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProjectEmailMapperTest {

    private ProjectEmailMapper mapper;
    private Project project;

    @BeforeEach
    void setUp() {
        mapper = new ProjectEmailMapper();

        project = new Project();
        project.setProjectIdentifier("PROJ-1");
        project.setName("Test Project");
        project.setClientName("John Doe");
        project.setClientEmail("john.doe@example.com");
        project.setDescription("Test Description");
        project.setStartDate(LocalDate.of(2025, 1, 15));
        project.setDueDate(LocalDate.of(2025, 3, 30));

        Address address = new Address("123 Main St", "Montreal", "Quebec", "Canada", "H1A1A1");
        project.setAddress(address);

        ProjectType projectType = new ProjectType();
        projectType.setType(ProjectType.ProjectTypeEnum.SCHEDULED);
        project.setProjectType(projectType);
    }

    @Test
    void toProjectNotificationEmail_ShouldMapAllFields() {
        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Created");

        assertNotNull(email);
        assertEquals("john.doe@example.com", email.getRecipientEmail());
        assertEquals("Project Created: Test Project", email.getSubject());
        assertEquals("PROJ-1", email.getProjectIdentifier());
        assertEquals("Test Project", email.getProjectName());
        assertEquals("John Doe", email.getClientName());
        assertEquals("Test Description", email.getDescription());
        assertEquals(LocalDate.of(2025, 1, 15), email.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 30), email.getDueDate());
        assertEquals("SCHEDULED", email.getProjectType());
        assertEquals("Created", email.getOperation());
        assertNotNull(email.getSentDate());
        assertTrue(email.getAddress().contains("123 Main St"));
        assertTrue(email.getAddress().contains("Montreal"));
    }

    @Test
    void toProjectNotificationEmail_ShouldReturnNull_WhenClientEmailIsNull() {
        project.setClientEmail(null);

        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Created");

        assertNull(email);
    }

    @Test
    void toProjectNotificationEmail_ShouldReturnNull_WhenClientEmailIsBlank() {
        project.setClientEmail("   ");

        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Created");

        assertNull(email);
    }

    @Test
    void toProjectNotificationEmail_ShouldHandleNullAddress() {
        project.setAddress(null);

        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Updated");

        assertNotNull(email);
        assertNull(email.getAddress());
    }

    @Test
    void toProjectNotificationEmail_ShouldHandleNullProjectType() {
        project.setProjectType(null);

        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Created");

        assertNotNull(email);
        assertNull(email.getProjectType());
    }

    @Test
    void toProjectNotificationEmail_ShouldFormatAddressCorrectly() {
        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Created");

        assertNotNull(email.getAddress());
        assertTrue(email.getAddress().contains("123 Main St"));
        assertTrue(email.getAddress().contains("Montreal"));
        assertTrue(email.getAddress().contains("Quebec"));
        assertTrue(email.getAddress().contains("H1A1A1"));
        assertTrue(email.getAddress().contains("Canada"));
    }

    @Test
    void toProjectNotificationEmail_ShouldHandlePartialAddress() {
        Address partialAddress = new Address(null, "Montreal", "Quebec", null, null);
        project.setAddress(partialAddress);

        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Updated");

        assertNotNull(email.getAddress());
        assertTrue(email.getAddress().contains("Montreal"));
        assertTrue(email.getAddress().contains("Quebec"));
        assertFalse(email.getAddress().contains("null"));
    }

    @Test
    void toProjectNotificationEmail_ShouldHandleUpdateOperation() {
        ProjectNotificationEmail email = mapper.toProjectNotificationEmail(project, "Updated");

        assertNotNull(email);
        assertEquals("Project Updated: Test Project", email.getSubject());
        assertEquals("Updated", email.getOperation());
    }
}