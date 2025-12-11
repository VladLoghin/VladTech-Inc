package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.presentationlayer.AddressRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRequestMapperTest {

    private ProjectRequestMapper mapper;
    private ProjectRequestModel requestModel;

    @BeforeEach
    void setUp() {
        mapper = new ProjectRequestMapper();

        requestModel = new ProjectRequestModel();
        requestModel.setName("Test Project");
        requestModel.setClientId("CLIENT-123");
        requestModel.setClientName("John Doe");
        requestModel.setClientEmail("john.doe@example.com");
        requestModel.setDescription("Test Description");
        requestModel.setStartDate(LocalDate.of(2025, 1, 15));
        requestModel.setDueDate(LocalDate.of(2025, 3, 30));
        requestModel.setProjectType("SCHEDULED");

        AddressRequestModel address = new AddressRequestModel();
        address.setStreetAddress("123 Main St");
        address.setCity("Montreal");
        address.setProvince("Quebec");
        address.setCountry("Canada");
        address.setPostalCode("H1A1A1");
        requestModel.setAddress(address);
    }

    @Test
    void requestModelToEntity_ShouldMapAllFields() {
        Project project = mapper.requestModelToEntity(requestModel);

        assertNotNull(project);
        assertEquals("Test Project", project.getName());
        assertEquals("CLIENT-123", project.getClientId());
        assertEquals("John Doe", project.getClientName());
        assertEquals("john.doe@example.com", project.getClientEmail());
        assertEquals("Test Description", project.getDescription());
        assertEquals(LocalDate.of(2025, 1, 15), project.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 30), project.getDueDate());
        assertNotNull(project.getProjectType());
        assertEquals(ProjectType.ProjectTypeEnum.SCHEDULED, project.getProjectType().getType());
        assertNotNull(project.getAddress());
        assertEquals("123 Main St", project.getAddress().getStreetAddress());
    }

    @Test
    void requestModelToEntity_ShouldReturnNull_WhenInputIsNull() {
        // Act
        Project project = mapper.requestModelToEntity(null);

        // Assert
        assertNull(project);
    }

    @Test
    void requestModelToEntity_ShouldHandleNullAddress() {
        // Arrange
        requestModel.setAddress(null);

        // Act
        Project project = mapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(project);
        assertNull(project.getAddress());
    }

    @Test
    void requestModelToEntity_ShouldHandleNullProjectType() {
        // Arrange
        requestModel.setProjectType(null);

        // Act
        Project project = mapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(project);
        assertNull(project.getProjectType());
    }

    @Test
    void requestModelToEntity_ShouldMapAppointmentType() {
        // Arrange
        requestModel.setProjectType("APPOINTMENT");

        // Act
        Project project = mapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(project);
        assertNotNull(project.getProjectType());
        assertEquals(ProjectType.ProjectTypeEnum.APPOINTMENT, project.getProjectType().getType());
    }

    @Test
    void requestModelToEntity_ShouldHandleLowercaseProjectType() {
        // Arrange
        requestModel.setProjectType("scheduled");

        // Act
        Project project = mapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(project);
        assertNotNull(project.getProjectType());
        assertEquals(ProjectType.ProjectTypeEnum.SCHEDULED, project.getProjectType().getType());
    }

    @Test
    void requestModelToEntity_ShouldMapAddressWithAllFields() {
        // Act
        Project project = mapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(project.getAddress());
        assertEquals("123 Main St", project.getAddress().getStreetAddress());
        assertEquals("Montreal", project.getAddress().getCity());
        assertEquals("Quebec", project.getAddress().getProvince());
        assertEquals("Canada", project.getAddress().getCountry());
        assertEquals("H1A1A1", project.getAddress().getPostalCode());
    }
}