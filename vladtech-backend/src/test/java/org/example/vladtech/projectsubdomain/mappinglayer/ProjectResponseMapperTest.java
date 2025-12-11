package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectResponseMapperTest {

    private ProjectResponseMapper mapper;
    private Project project;

    @BeforeEach
    void setUp() {
        mapper = new ProjectResponseMapper();

        project = new Project();
        project.setId("1");
        project.setProjectIdentifier("PROJ-1");
        project.setName("Test Project");
        project.setClientId("CLIENT-123");
        project.setClientName("John Doe");
        project.setClientEmail("john.doe@example.com");
        project.setDescription("Test Description");
        project.setStartDate(LocalDate.of(2025, 1, 15));
        project.setDueDate(LocalDate.of(2025, 3, 30));

        Address address = new Address();
        address.setStreetAddress("123 Main St");
        address.setCity("Montreal");
        address.setProvince("Quebec");
        address.setCountry("Canada");
        address.setPostalCode("H1A1A1");
        project.setAddress(address);

        ProjectType projectType = new ProjectType();
        projectType.setType(ProjectType.ProjectTypeEnum.SCHEDULED);
        project.setProjectType(projectType);

        project.setAssignedEmployeeIds(Arrays.asList("EMP-1", "EMP-2"));

        ProjectPhoto photo = new ProjectPhoto();
        photo.setPhotoId("PHOTO-1");
        photo.setPhotoUrl("http://example.com/photo.jpg");
        photo.setDescription("Test Photo");
        project.setPhotos(Collections.singletonList(photo));
    }

    @Test
    void entityToResponseModel_ShouldMapAllFields() {
        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response);
        assertEquals("PROJ-1", response.getProjectIdentifier());
        assertEquals("Test Project", response.getName());
        assertEquals("CLIENT-123", response.getClientId());
        assertEquals("John Doe", response.getClientName());
        assertEquals("john.doe@example.com", response.getClientEmail());
        assertEquals("Test Description", response.getDescription());
        assertEquals(LocalDate.of(2025, 1, 15), response.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 30), response.getDueDate());
        assertEquals("SCHEDULED", response.getProjectType());
        assertNotNull(response.getAddress());
        assertEquals("123 Main St", response.getAddress().getStreetAddress());
        assertEquals(2, response.getAssignedEmployeeIds().size());
        assertEquals(1, response.getPhotos().size());
    }

    @Test
    void entityToResponseModel_ShouldReturnNull_WhenInputIsNull() {
        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(null);

        // Assert
        assertNull(response);
    }

    @Test
    void entityToResponseModel_ShouldHandleNullAddress() {
        // Arrange
        project.setAddress(null);

        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response);
        assertNull(response.getAddress());
    }

    @Test
    void entityToResponseModel_ShouldHandleNullProjectType() {
        // Arrange
        project.setProjectType(null);

        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response);
        assertNull(response.getProjectType());
    }

    @Test
    void entityToResponseModel_ShouldHandleProjectTypeWithNullEnum() {
        // Arrange
        ProjectType projectType = new ProjectType();
        projectType.setType(null);
        project.setProjectType(projectType);

        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response);
        assertNull(response.getProjectType());
    }

    @Test
    void entityToResponseModel_ShouldHandleNullPhotos() {
        // Arrange
        project.setPhotos(null);

        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response);
        assertNull(response.getPhotos());
    }

    @Test
    void entityToResponseModel_ShouldMapAppointmentType() {
        // Arrange
        ProjectType projectType = new ProjectType();
        projectType.setType(ProjectType.ProjectTypeEnum.APPOINTMENT);
        project.setProjectType(projectType);

        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertEquals("APPOINTMENT", response.getProjectType());
    }

    @Test
    void entityToResponseModel_ShouldMapPhotosCorrectly() {
        // Act
        ProjectResponseModel response = mapper.entityToResponseModel(project);

        // Assert
        assertNotNull(response.getPhotos());
        assertEquals(1, response.getPhotos().size());
        PhotoResponseModel photo = response.getPhotos().get(0);
        assertEquals("PHOTO-1", photo.getPhotoId());
        assertEquals("http://example.com/photo.jpg", photo.getPhotoUrl());
        assertEquals("Test Photo", photo.getDescription());
    }

    @Test
    void entityListToResponseModelList_ShouldMapAllProjects() {
        // Arrange
        Project project2 = new Project();
        project2.setProjectIdentifier("PROJ-2");
        project2.setName("Another Project");
        List<Project> projects = Arrays.asList(project, project2);

        // Act
        List<ProjectResponseModel> responses = mapper.entityListToResponseModelList(projects);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("PROJ-1", responses.get(0).getProjectIdentifier());
        assertEquals("PROJ-2", responses.get(1).getProjectIdentifier());
    }

    @Test
    void entityListToResponseModelList_ShouldReturnNull_WhenInputIsNull() {
        // Act
        List<ProjectResponseModel> responses = mapper.entityListToResponseModelList(null);

        // Assert
        assertNull(responses);
    }

    @Test
    void entityListToResponseModelList_ShouldHandleEmptyList() {
        // Act
        List<ProjectResponseModel> responses = mapper.entityListToResponseModelList(Collections.emptyList());

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}