package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectEmailMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectRequestMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectResponseMapper;
import org.example.vladtech.projectsubdomain.presentationlayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectRequestMapper projectRequestMapper;

    @Mock
    private ProjectResponseMapper projectResponseMapper;

    @Mock
    private ProjectEmailMapper projectEmailMapper;

    @Mock
    private ProjectEmailSender projectEmailSender;

    @Mock
    private ProjectService projectServiceMock;

    private ProjectServiceImpl projectService;

    private Project project;
    private ProjectRequestModel requestModel;
    private ProjectResponseModel responseModel;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(
                projectRepository,
                projectRequestMapper,
                projectResponseMapper,
                projectEmailMapper,
                projectEmailSender
        );
        projectService.self = projectServiceMock;

        project = new Project();
        project.setId("1");
        project.setProjectIdentifier("PROJ-1");
        project.setName("Test Project");
        project.setClientId("CLIENT-123");
        project.setClientName("John Doe");
        project.setClientEmail("john.doe@example.com");
        project.setDescription("Test Description");
        project.setStartDate(LocalDate.now());
        project.setDueDate(LocalDate.now().plusDays(30));

        Address address = new Address("123 Main St", "Montreal", "Quebec", "Canada", "H1A1A1");
        project.setAddress(address);

        ProjectType projectType = new ProjectType();
        projectType.setType(ProjectType.ProjectTypeEnum.SCHEDULED);
        project.setProjectType(projectType);

        requestModel = new ProjectRequestModel();
        requestModel.setName("Test Project");
        requestModel.setClientId("CLIENT-123");
        requestModel.setClientName("John Doe");
        requestModel.setClientEmail("john.doe@example.com");
        requestModel.setDescription("Test Description");
        requestModel.setStartDate(LocalDate.now());
        requestModel.setDueDate(LocalDate.now().plusDays(30));
        requestModel.setProjectType("SCHEDULED");

        AddressRequestModel addressRequest = new AddressRequestModel("123 Main St", "Montreal", "Quebec", "Canada", "H1A1A1");
        requestModel.setAddress(addressRequest);

        responseModel = new ProjectResponseModel();
        responseModel.setProjectIdentifier("PROJ-1");
        responseModel.setName("Test Project");
        responseModel.setClientId("CLIENT-123");
        responseModel.setClientName("John Doe");
        responseModel.setClientEmail("john.doe@example.com");
        responseModel.setDescription("Test Description");
    }

    @Test
    void getAllProjects_ShouldReturnListOfProjects() {
        // Arrange
        List<Project> projects = Arrays.asList(project);
        List<ProjectResponseModel> responseModels = Arrays.asList(responseModel);

        when(projectRepository.findAll()).thenReturn(projects);
        when(projectResponseMapper.entityListToResponseModelList(projects)).thenReturn(responseModels);

        // Act
        List<ProjectResponseModel> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAll();
        verify(projectResponseMapper, times(1)).entityListToResponseModelList(projects);
    }

    @Test
    void getAllProjects_ShouldReturnEmptyList_WhenNoProjects() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());
        when(projectResponseMapper.entityListToResponseModelList(any())).thenReturn(Collections.emptyList());

        // Act
        List<ProjectResponseModel> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProjectByIdentifier_ShouldReturnProject_WhenFound() {
        // Arrange
        when(projectRepository.findByProjectIdentifier("PROJ-1")).thenReturn(Optional.of(project));
        when(projectResponseMapper.entityToResponseModel(project)).thenReturn(responseModel);

        // Act
        ProjectResponseModel result = projectService.getProjectByIdentifier("PROJ-1");

        // Assert
        assertNotNull(result);
        assertEquals("PROJ-1", result.getProjectIdentifier());
        verify(projectRepository, times(1)).findByProjectIdentifier("PROJ-1");
    }

    @Test
    void getProjectByIdentifier_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(projectRepository.findByProjectIdentifier("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> projectService.getProjectByIdentifier("INVALID"));

        assertTrue(exception.getMessage().contains("Project not found"));
        verify(projectRepository, times(1)).findByProjectIdentifier("INVALID");
    }

    @Test
    void createProject_ShouldCreateAndReturnProject() {
        // Arrange
        when(projectRepository.count()).thenReturn(5L);
        when(projectRequestMapper.requestModelToEntity(requestModel)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectResponseMapper.entityToResponseModel(project)).thenReturn(responseModel);

        // Act
        ProjectResponseModel result = projectService.createProject(requestModel);

        // Assert
        assertNotNull(result);
        verify(projectRepository, times(1)).count();
        verify(projectRequestMapper, times(1)).requestModelToEntity(requestModel);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectResponseMapper, times(1)).entityToResponseModel(project);
        verify(projectServiceMock, times(1)).sendEmailNotificationAsync(any(Project.class), eq("Created"));
    }

    @Test
    void createProject_ShouldSetCorrectProjectIdentifier() {
        // Arrange
        when(projectRepository.count()).thenReturn(10L);
        when(projectRequestMapper.requestModelToEntity(requestModel)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            assertEquals("PROJ-11", savedProject.getProjectIdentifier());
            return savedProject;
        });
        when(projectResponseMapper.entityToResponseModel(any())).thenReturn(responseModel);

        // Act
        projectService.createProject(requestModel);

        // Assert
        verify(projectRepository, times(1)).count();
    }

    @Test
    void createProject_ShouldSendEmailNotification_WhenClientEmailPresent() {
        when(projectRepository.count()).thenReturn(5L);
        when(projectRequestMapper.requestModelToEntity(requestModel)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectResponseMapper.entityToResponseModel(project)).thenReturn(responseModel);

        projectService.createProject(requestModel);

        verify(projectServiceMock, times(1)).sendEmailNotificationAsync(project, "Created");
    }

    @Test
    void updateProject_ShouldUpdateAndReturnProject() {
        // Arrange
        when(projectRepository.findByProjectIdentifier("PROJ-1"))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);
        when(projectResponseMapper.entityToResponseModel(project))
                .thenReturn(responseModel);

        // Act
        ProjectResponseModel result = projectService.updateProject("PROJ-1", requestModel);

        // Assert
        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository, times(1)).findByProjectIdentifier("PROJ-1");
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectServiceMock, times(1)).sendEmailNotificationAsync(any(Project.class), eq("Updated"));
    }

    @Test
    void updateProject_ShouldSendEmailNotification_WhenClientEmailPresent() {
        when(projectRepository.findByProjectIdentifier("PROJ-1"))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);
        when(projectResponseMapper.entityToResponseModel(project))
                .thenReturn(responseModel);

        projectService.updateProject("PROJ-1", requestModel);

        verify(projectServiceMock, times(1)).sendEmailNotificationAsync(project, "Updated");
    }

    @Test
    void sendEmailNotificationAsync_ShouldSendEmail_WhenClientEmailPresent() {
        ProjectNotificationEmail email = mock(ProjectNotificationEmail.class);
        when(projectEmailMapper.toProjectNotificationEmail(project, "Created")).thenReturn(email);

        projectService.sendEmailNotificationAsync(project, "Created");

        verify(projectEmailMapper, times(1)).toProjectNotificationEmail(project, "Created");
        verify(projectEmailSender, times(1)).send(email);
    }

    @Test
    void sendEmailNotificationAsync_ShouldNotSendEmail_WhenClientEmailIsNull() {
        project.setClientEmail(null);

        projectService.sendEmailNotificationAsync(project, "Created");

        verify(projectEmailMapper, never()).toProjectNotificationEmail(any(), any());
        verify(projectEmailSender, never()).send(any());
    }

    @Test
    void sendEmailNotificationAsync_ShouldNotSendEmail_WhenClientEmailIsBlank() {
        project.setClientEmail("   ");

        projectService.sendEmailNotificationAsync(project, "Created");

        verify(projectEmailMapper, never()).toProjectNotificationEmail(any(), any());
        verify(projectEmailSender, never()).send(any());
    }

    @Test
    void sendEmailNotificationAsync_ShouldHandleException_WhenEmailSendingFails() {
        ProjectNotificationEmail email = mock(ProjectNotificationEmail.class);
        when(projectEmailMapper.toProjectNotificationEmail(project, "Created")).thenReturn(email);
        doThrow(new RuntimeException("Email sending failed")).when(projectEmailSender).send(email);

        assertDoesNotThrow(() -> projectService.sendEmailNotificationAsync(project, "Created"));

        verify(projectEmailSender, times(1)).send(email);
    }

    @Test
    void sendEmailNotificationAsync_ShouldNotSendEmail_WhenMapperReturnsNull() {
        when(projectEmailMapper.toProjectNotificationEmail(project, "Created")).thenReturn(null);

        projectService.sendEmailNotificationAsync(project, "Created");

        verify(projectEmailMapper, times(1)).toProjectNotificationEmail(project, "Created");
        verify(projectEmailSender, never()).send(any());
    }

    @Test
    void deleteProject_ShouldDoNothing() {
        // Act
        assertDoesNotThrow(() -> projectService.deleteProject("PROJ-1"));
    }

    @Test
    void assignEmployee_ShouldReturnNull() {
        // Act
        ProjectResponseModel result = projectService.assignEmployee("PROJ-1", "EMP-1");

        // Assert
        assertNull(result);
    }

    @Test
    void getProjectPhotos_ShouldReturnNull() {
        // Act
        List<PhotoResponseModel> result = projectService.getProjectPhotos("PROJ-1");

        // Assert
        assertNull(result);
    }

    @Test
    void addProjectPhoto_ShouldReturnNull() {
        // Arrange
        PhotoResponseModel photoResponse = new PhotoResponseModel("PHOTO-1", "url", "desc");

        // Act
        ProjectResponseModel result = projectService.addProjectPhoto("PROJ-1", photoResponse);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteProjectPhoto_ShouldDoNothing() {
        // Act
        assertDoesNotThrow(() -> projectService.deleteProjectPhoto("PROJ-1", "PHOTO-1"));
    }
    @Test
    void getProjectCount_ShouldReturnRepositoryCount() {
        // Arrange
        when(projectRepository.count()).thenReturn(5L);

        // Act
        long result = projectService.getProjectCount();

        // Assert
        assertEquals(5L, result);
        verify(projectRepository, times(1)).count();
    }

}