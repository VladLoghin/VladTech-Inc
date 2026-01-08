package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.*;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.example.vladtech.projectsubdomain.exceptions.InvalidEmployeeIdException;
import org.example.vladtech.projectsubdomain.exceptions.ProjectNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
                                projectEmailSender);
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

                AddressRequestModel addressRequest = new AddressRequestModel("123 Main St", "Montreal", "Quebec",
                                "Canada", "H1A1A1");
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
        void getProjectByIdentifier_ShouldThrowProjectNotFoundException_WhenNotFound() {
                // Arrange
                when(projectRepository.findByProjectIdentifier("INVALID")).thenReturn(Optional.empty());

                // Act & Assert
                ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
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
        void assignEmployee_shouldAddEmployeeAndReturnMappedResponse() {
                // Arrange
                String projectId = "PROJ-1";
                String employeeId = "EMP-123";

                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(new ArrayList<>());

                Project savedProject = new Project();
                savedProject.setProjectIdentifier(projectId);
                savedProject.setAssignedEmployeeIds(List.of(employeeId));

                ProjectResponseModel mapped = new ProjectResponseModel();

                when(projectRepository.findByProjectIdentifier(projectId))
                                .thenReturn(Optional.of(existing));
                when(projectRepository.save(existing))
                                .thenReturn(savedProject);
                when(projectResponseMapper.entityToResponseModel(savedProject))
                                .thenReturn(mapped);

                // Act
                ProjectResponseModel result = projectService.assignEmployee(projectId, employeeId);

                // Assert
                assertSame(mapped, result);
                assertEquals(1, existing.getAssignedEmployeeIds().size());
                assertEquals(employeeId, existing.getAssignedEmployeeIds().get(0));

                verify(projectRepository).findByProjectIdentifier(projectId);
                verify(projectRepository).save(existing);
                verify(projectResponseMapper).entityToResponseModel(savedProject);
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

        @Test
        void assignEmployee_addsNewEmployeeAndSaves() {
                String projectId = "PROJ-1";
                String employeeId = "auth0|emp-1";

                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(
                                new ArrayList<>(List.of("auth0|old-emp")));

                Project saved = new Project();
                saved.setProjectIdentifier(projectId);
                saved.setAssignedEmployeeIds(
                                new ArrayList<>(List.of("auth0|old-emp", employeeId)));

                ProjectResponseModel mapped = new ProjectResponseModel();

                given(projectRepository.findByProjectIdentifier(projectId))
                                .willReturn(Optional.of(existing));
                given(projectRepository.save(existing)).willReturn(saved);
                given(projectResponseMapper.entityToResponseModel(saved))
                                .willReturn(mapped);

                ProjectResponseModel result = projectService.assignEmployee(projectId, employeeId);

                assertThat(result).isSameAs(mapped);
                verify(projectRepository).save(existing);
                assertThat(existing.getAssignedEmployeeIds())
                                .containsExactlyInAnyOrder("auth0|old-emp", employeeId);
        }

        @Test
        void assignEmployee_doesNotDuplicateExistingEmployee() {
                String projectId = "PROJ-1";
                String employeeId = "auth0|emp-1";

                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(new ArrayList<>(List.of(employeeId)));

                ProjectResponseModel mapped = new ProjectResponseModel();

                given(projectRepository.findByProjectIdentifier(projectId))
                                .willReturn(Optional.of(existing));
                // no need to stub save, it should NOT be called in this scenario
                given(projectResponseMapper.entityToResponseModel(existing))
                                .willReturn(mapped);

                // when
                ProjectResponseModel result = projectService.assignEmployee(projectId, employeeId);

                // then
                assertThat(result).isSameAs(mapped);
                assertThat(existing.getAssignedEmployeeIds())
                                .containsExactly(employeeId); // still only one instance
                verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        void assignEmployee_blankEmployeeIdThrows() {
                String projectId = "PROJ-1";
                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(new ArrayList<>());

                given(projectRepository.findByProjectIdentifier(projectId))
                                .willReturn(Optional.of(existing));

                assertThrows(InvalidEmployeeIdException.class,
                                () -> projectService.assignEmployee(projectId, "   "));
                verify(projectRepository, never()).save(any());
        }

        @Test
        void assignEmployee_projectNotFoundThrows() {
                String projectId = "PROJ-404";
                given(projectRepository.findByProjectIdentifier(projectId))
                                .willReturn(Optional.empty());

                assertThrows(ProjectNotFoundException.class,
                                () -> projectService.assignEmployee(projectId, "auth0|emp-1"));
                verify(projectRepository, never()).save(any());
        }

        // ---------- getProjectsForCalendar / mapToCalendarEntry tests ----------

        @Test
        void getProjectsForCalendar_mapsProjectsToCalendarEntries_withLocation() {
                Project p1 = new Project();
                p1.setProjectIdentifier("PROJ-1");
                p1.setName("Kitchen Renovation");
                p1.setStartDate(LocalDate.of(2026, 1, 15));
                p1.setDueDate(LocalDate.of(2026, 3, 30));
                p1.setAddress(new Address(
                                "123 Main St",
                                "Montreal",
                                "Quebec",
                                "Canada",
                                "H1A 1A1"));

                Project p2 = new Project();
                p2.setProjectIdentifier("PROJ-2");
                p2.setName("Bathroom Remodel");
                p2.setStartDate(LocalDate.of(2026, 2, 1));
                p2.setDueDate(LocalDate.of(2026, 2, 28));
                // address left null on purpose
                p2.setAddress(null);

                given(projectRepository.findAll()).willReturn(List.of(p1, p2));

                List<ProjectCalendarEntryResponseModel> result = projectService.getProjectsForCalendar();

                assertEquals(2, result.size());

                ProjectCalendarEntryResponseModel e1 = result.get(0);
                assertEquals("PROJ-1", e1.getProjectIdentifier());
                assertEquals("Kitchen Renovation", e1.getName());
                assertEquals(LocalDate.of(2026, 1, 15), e1.getStartDate());
                assertEquals(LocalDate.of(2026, 3, 30), e1.getDueDate());
                assertEquals("Montreal, Quebec", e1.getLocationSummary());

                ProjectCalendarEntryResponseModel e2 = result.get(1);
                assertEquals("PROJ-2", e2.getProjectIdentifier());
                assertEquals("Bathroom Remodel", e2.getName());
                assertNull(e2.getLocationSummary());
        }

        @Test
        void updateProject_shouldReplaceAssignedEmployeeIds_whenRequestHasList() {
                // given
                String projectId = "PROJ-1";

                // existing project with OLD employees
                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(
                                new ArrayList<>(List.of("auth0|old-1", "auth0|old-2")));

                // request with NEW employees
                ProjectRequestModel updateRequest = new ProjectRequestModel();
                updateRequest.setAssignedEmployeeIds(
                                List.of("auth0|new-1", "auth0|new-2"));

                Project saved = new Project();
                saved.setProjectIdentifier(projectId);
                saved.setAssignedEmployeeIds(
                                new ArrayList<>(List.of("auth0|new-1", "auth0|new-2")));

                when(projectRepository.findByProjectIdentifier(projectId))
                                .thenReturn(Optional.of(existing));

                // we also assert inside the stub that the list was replaced correctly
                when(projectRepository.save(any(Project.class)))
                                .thenAnswer(invocation -> {
                                        Project toSave = invocation.getArgument(0);
                                        assertEquals(
                                                        List.of("auth0|new-1", "auth0|new-2"),
                                                        toSave.getAssignedEmployeeIds());
                                        return saved;
                                });

                when(projectResponseMapper.entityToResponseModel(saved))
                                .thenReturn(new ProjectResponseModel());

                // when
                ProjectResponseModel result = projectService.updateProject(projectId, updateRequest);

                // then
                assertNotNull(result);
                // existing object in memory should also now have the NEW list
                assertEquals(
                                List.of("auth0|new-1", "auth0|new-2"),
                                existing.getAssignedEmployeeIds());
                verify(projectRepository).findByProjectIdentifier(projectId);
                verify(projectRepository).save(any(Project.class));
        }

        @Test
        void updateProject_shouldKeepAssignedEmployeeIds_whenRequestHasNullList() {
                String projectId = "PROJ-1";

                Project existing = new Project();
                existing.setProjectIdentifier(projectId);
                existing.setAssignedEmployeeIds(
                                new ArrayList<>(List.of("auth0|keep-me")));

                ProjectRequestModel updateRequest = new ProjectRequestModel();
                updateRequest.setAssignedEmployeeIds(null); // important

                when(projectRepository.findByProjectIdentifier(projectId))
                                .thenReturn(Optional.of(existing));
                when(projectRepository.save(any(Project.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(projectResponseMapper.entityToResponseModel(any()))
                                .thenReturn(new ProjectResponseModel());

                ProjectResponseModel result = projectService.updateProject(projectId, updateRequest);

                assertNotNull(result);
                assertEquals(
                                List.of("auth0|keep-me"),
                                existing.getAssignedEmployeeIds());
        }

}
