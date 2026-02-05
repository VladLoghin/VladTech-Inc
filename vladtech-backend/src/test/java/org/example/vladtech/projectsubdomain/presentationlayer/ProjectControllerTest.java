package org.example.vladtech.projectsubdomain.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.example.vladtech.projectsubdomain.businesslayer.UserProjectPinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(authorities = "Admin")
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc
class ProjectControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ProjectService projectService;

        @MockitoBean
        private UserProjectPinService userProjectPinService;

        private ProjectResponseModel responseModel;
        private ProjectRequestModel requestModel;

        @BeforeEach
        void setUp() {
                responseModel = new ProjectResponseModel();
                responseModel.setState("ACTIVE");
                responseModel.setId("mongo-id-123");
                responseModel.setProjectIdentifier("PROJ-1");
                responseModel.setName("Test Project");
                responseModel.setClientId("CLIENT-123");
                responseModel.setClientName("John Doe");
                responseModel.setClientEmail("john.doe@example.com");
                responseModel.setDescription("Test Description");
                responseModel.setStartDate(LocalDate.now());
                responseModel.setDueDate(LocalDate.now().plusDays(30));
                responseModel.setProjectType("SCHEDULED");
                responseModel.setAssignedEmployeeIds(Collections.emptyList());
                responseModel.setPhotos(Collections.emptyList());

                AddressResponseModel address = new AddressResponseModel("123 Main St", "Montreal", "Quebec", "Canada",
                                "H1A1A1");
                responseModel.setAddress(address);

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
                                "Canada",
                                "H1A1A1");
                requestModel.setAddress(addressRequest);
        }

        @Test
        void getAllProjects_ShouldReturnOkWithProjectList() throws Exception {
                // Arrange
                List<ProjectResponseModel> projects = Arrays.asList(responseModel);
                when(projectService.getAllProjects()).thenReturn(projects);

                // Act & Assert
                mockMvc.perform(get("/api/projects"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$[0].name").value("Test Project"))
                                .andExpect(jsonPath("$[0].clientId").value("CLIENT-123"))
                                .andExpect(jsonPath("$[0].clientName").value("John Doe"))
                                .andExpect(jsonPath("$[0].clientEmail").value("john.doe@example.com"));

                verify(projectService, times(1)).getAllProjects();
        }

        @Test
        void getProjectByIdentifier_ShouldReturnOkWithProject() throws Exception {
                // Arrange
                when(projectService.getProjectByIdentifier("PROJ-1")).thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(get("/api/projects/PROJ-1"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$.name").value("Test Project"))
                                .andExpect(jsonPath("$.clientId").value("CLIENT-123"))
                                .andExpect(jsonPath("$.clientName").value("John Doe"))
                                .andExpect(jsonPath("$.clientEmail").value("john.doe@example.com"));

                verify(projectService, times(1)).getProjectByIdentifier("PROJ-1");
        }

        @Test
        void createProject_ShouldReturnCreatedWithProject() throws Exception {
                // Arrange
                when(projectService.createProject(any(ProjectRequestModel.class))).thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(post("/api/projects")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestModel)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"));

                verify(projectService, times(1)).createProject(any(ProjectRequestModel.class));
        }

        @Test
        void updateProject_ShouldReturnOkWithUpdatedProject() throws Exception {
                // Arrange
                when(projectService.updateProject(eq("PROJ-1"), any(ProjectRequestModel.class)))
                                .thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(put("/api/projects/PROJ-1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestModel)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"));

                verify(projectService, times(1)).updateProject(eq("PROJ-1"), any(ProjectRequestModel.class));
        }

        @Test
        void deleteProject_ShouldReturnNoContent() throws Exception {
                // Arrange
                doNothing().when(projectService).deleteProject("PROJ-1");

                // Act & Assert
                mockMvc.perform(delete("/api/projects/PROJ-1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(projectService, times(1)).deleteProject("PROJ-1");
        }

        @Test
        void assignEmployee_ShouldReturnOkWithProject() throws Exception {
                // Arrange
                when(projectService.assignEmployee("PROJ-1", "EMP-1")).thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(post("/api/projects/PROJ-1/assign/EMP-1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"));

                verify(projectService, times(1)).assignEmployee("PROJ-1", "EMP-1");
        }

        @Test
        void getProjectPhotos_ShouldReturnOkWithPhotoList() throws Exception {
                // Arrange
                PhotoResponseModel photo = new PhotoResponseModel("PHOTO-1", "url", "desc");
                List<PhotoResponseModel> photos = Arrays.asList(photo);
                when(projectService.getProjectPhotos("PROJ-1")).thenReturn(photos);

                // Act & Assert
                mockMvc.perform(get("/api/projects/PROJ-1/photos"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].photoId").value("PHOTO-1"));

                verify(projectService, times(1)).getProjectPhotos("PROJ-1");
        }

        @Test
        void addProjectPhoto_ShouldReturnCreatedWithProject() throws Exception {
                // Arrange
                PhotoResponseModel photo = new PhotoResponseModel("PHOTO-1", "url", "desc");
                when(projectService.addProjectPhoto(eq("PROJ-1"), any(PhotoResponseModel.class)))
                                .thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(post("/api/projects/PROJ-1/photos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(photo)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                verify(projectService, times(1)).addProjectPhoto(eq("PROJ-1"), any(PhotoResponseModel.class));
        }

        @Test
        void deleteProjectPhoto_ShouldReturnNoContent() throws Exception {
                // Arrange
                doNothing().when(projectService).deleteProjectPhoto("PROJ-1", "PHOTO-1");

                // Act & Assert
                mockMvc.perform(delete("/api/projects/PROJ-1/photos/PHOTO-1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(projectService, times(1)).deleteProjectPhoto("PROJ-1", "PHOTO-1");
        }

        @Test
        void getProjectCount_ShouldReturnOkWithCount() throws Exception {
                // Arrange
                when(projectService.getProjectCount()).thenReturn(7L);

                // Act & Assert
                mockMvc.perform(get("/api/projects/count"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("7"));

                verify(projectService, times(1)).getProjectCount();
        }

        @Test
        void getProjectStats_ShouldReturnOkWithStats() throws Exception {
                // Arrange
                ProjectStatsResponseModel stats = ProjectStatsResponseModel.builder()
                                .totalProjects(10)
                                .pendingCount(2)
                                .inProgressCount(3)
                                .completedCount(5)
                                .activeCount(5)
                                .archivedCount(5)
                                .overdueCount(1)
                                .build();
                when(projectService.getProjectStats()).thenReturn(stats);

                // Act & Assert
                mockMvc.perform(get("/api/projects/stats"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.totalProjects").value(10))
                                .andExpect(jsonPath("$.pendingCount").value(2))
                                .andExpect(jsonPath("$.inProgressCount").value(3));

                verify(projectService, times(1)).getProjectStats();
        }

        // ---------- Archive Feature Tests ----------

        @Test
        void completeProject_ShouldReturnOkWithCompletedProject() throws Exception {
                // Arrange
                responseModel.setState("COMPLETE");
                when(projectService.completeProject("PROJ-1")).thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(put("/api/projects/PROJ-1/complete")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$.state").value("COMPLETE"));

                verify(projectService, times(1)).completeProject("PROJ-1");
        }

        @Test
        void getActiveProjects_ShouldReturnOkWithActiveProjectList() throws Exception {
                // Arrange
                responseModel.setState("ACTIVE");
                List<ProjectResponseModel> activeProjects = Arrays.asList(responseModel);
                when(projectService.getActiveProjects()).thenReturn(activeProjects);

                // Act & Assert
                mockMvc.perform(get("/api/projects/active"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$[0].state").value("ACTIVE"));

                verify(projectService, times(1)).getActiveProjects();
        }

        @Test
        void getArchivedProjects_ShouldReturnOkWithArchivedProjectList() throws Exception {
                // Arrange
                responseModel.setState("COMPLETE");
                List<ProjectResponseModel> archivedProjects = Arrays.asList(responseModel);
                when(projectService.getArchivedProjects()).thenReturn(archivedProjects);

                // Act & Assert
                mockMvc.perform(get("/api/projects/archived"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$[0].state").value("COMPLETE"));

                verify(projectService, times(1)).getArchivedProjects();
        }

        @Test
        void reactivateProject_ShouldReturnOkWithReactivatedProject() throws Exception {
                // Arrange
                responseModel.setState("ACTIVE");
                when(projectService.reactivateProject("PROJ-1")).thenReturn(responseModel);

                // Act & Assert
                mockMvc.perform(put("/api/projects/PROJ-1/reactivate")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$.state").value("ACTIVE"));

                verify(projectService, times(1)).reactivateProject("PROJ-1");
        }

        @Test
        void getProjectsForCalendar_ShouldReturnOkWithCalendarEntries() throws Exception {
                // Arrange
                ProjectCalendarEntryResponseModel calendarEntry = new ProjectCalendarEntryResponseModel();
                calendarEntry.setProjectIdentifier("PROJ-1");
                calendarEntry.setName("Test Project");
                calendarEntry.setLocationSummary("Montreal, Quebec");
                calendarEntry.setStartDate(LocalDate.now());
                calendarEntry.setDueDate(LocalDate.now().plusDays(30));
                calendarEntry.setState("ACTIVE");
                calendarEntry.setPriority("MEDIUM");
                List<ProjectCalendarEntryResponseModel> calendarEntries = Arrays.asList(calendarEntry);
                when(projectService.getProjectsForCalendar()).thenReturn(calendarEntries);

                // Act & Assert
                mockMvc.perform(get("/api/projects/calendar"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].projectIdentifier").value("PROJ-1"))
                                .andExpect(jsonPath("$[0].name").value("Test Project"))
                                .andExpect(jsonPath("$[0].locationSummary").value("Montreal, Quebec"))
                                .andExpect(jsonPath("$[0].state").value("ACTIVE"));

                verify(projectService, times(1)).getProjectsForCalendar();
        }
        @Test
        void searchProjects_ShouldReturnOkWithProjectList() throws Exception {
                // Arrange
                org.springframework.data.domain.Page<ProjectResponseModel> page = new org.springframework.data.domain.PageImpl<>(
                                Collections.singletonList(responseModel));

                when(projectService.searchProjects(
                                any(), any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any()))
                                .thenReturn(page);

                // Act & Assert
                mockMvc.perform(get("/api/projects/search")
                                .param("name", "Test")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.content[0].projectIdentifier").value("PROJ-1"));

                verify(projectService, times(1)).searchProjects(
                                any(), any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any());
        }

        // ========================================
        // Tests for sendProjectToPortfolio
        // ========================================

        @Test
        void sendProjectToPortfolio_WithImage_ShouldReturnOk() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String type = "Kitchen";
                
                org.example.vladtech.portfolio.presentation.PortfolioResponseDto portfolioResponseDto = 
                        new org.example.vladtech.portfolio.presentation.PortfolioResponseDto();
                portfolioResponseDto.setPortfolioId("portfolio-123");
                portfolioResponseDto.setTitle("Test Project");
                portfolioResponseDto.setImageUrl("/uploads/portfolio/test.jpg");
                portfolioResponseDto.setType(type);

                when(projectService.sendProjectToPortfolio(eq(projectIdentifier), eq(type), any()))
                        .thenReturn(portfolioResponseDto);

                // Act & Assert
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/projects/" + projectIdentifier + "/send-to-portfolio")
                                .file("image", "test image content".getBytes())
                                .param("type", type)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.portfolioId").value("portfolio-123"))
                        .andExpect(jsonPath("$.title").value("Test Project"))
                        .andExpect(jsonPath("$.type").value(type));

                verify(projectService, times(1)).sendProjectToPortfolio(eq(projectIdentifier), eq(type), any());
        }

        @Test
        void sendProjectToPortfolio_WithoutImage_ShouldReturnOk() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String type = "Interior";
                
                org.example.vladtech.portfolio.presentation.PortfolioResponseDto portfolioResponseDto = 
                        new org.example.vladtech.portfolio.presentation.PortfolioResponseDto();
                portfolioResponseDto.setPortfolioId("portfolio-456");
                portfolioResponseDto.setTitle("Test Project");
                portfolioResponseDto.setImageUrl("");
                portfolioResponseDto.setType(type);

                when(projectService.sendProjectToPortfolio(eq(projectIdentifier), eq(type), eq(null)))
                        .thenReturn(portfolioResponseDto);

                // Act & Assert
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/projects/" + projectIdentifier + "/send-to-portfolio")
                                .param("type", type)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.portfolioId").value("portfolio-456"))
                        .andExpect(jsonPath("$.type").value(type));

                verify(projectService, times(1)).sendProjectToPortfolio(eq(projectIdentifier), eq(type), any());
        }

        @Test
        void sendProjectToPortfolio_ProjectNotFound_ShouldReturnBadRequest() throws Exception {
                // Arrange
                String projectIdentifier = "NONEXISTENT";
                String type = "Bathroom";

                when(projectService.sendProjectToPortfolio(eq(projectIdentifier), eq(type), any()))
                        .thenThrow(new RuntimeException("Project not found"));

                // Act & Assert
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/projects/" + projectIdentifier + "/send-to-portfolio")
                                .param("type", type)
                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                verify(projectService, times(1)).sendProjectToPortfolio(eq(projectIdentifier), eq(type), any());
        }

        @Test
        void sendProjectToPortfolio_AllPortfolioTypes_ShouldReturnOk() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String[] types = {"Interior", "Kitchen", "Bathroom", "Exterior/Yard"};

                for (String type : types) {
                        org.example.vladtech.portfolio.presentation.PortfolioResponseDto portfolioResponseDto = 
                                new org.example.vladtech.portfolio.presentation.PortfolioResponseDto();
                        portfolioResponseDto.setPortfolioId("portfolio-" + type);
                        portfolioResponseDto.setType(type);

                        when(projectService.sendProjectToPortfolio(eq(projectIdentifier), eq(type), any()))
                                .thenReturn(portfolioResponseDto);

                        // Act & Assert
                        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .multipart("/api/projects/" + projectIdentifier + "/send-to-portfolio")
                                        .param("type", type)
                                        .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value(type));
                }

                verify(projectService, times(types.length)).sendProjectToPortfolio(eq(projectIdentifier), any(), any());
        }

        // ========================================
        // Tests for Pin Project Feature
        // ========================================

        @Test
        void pinProject_ShouldReturnOk() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String userId = "user-123";
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(responseModel);
                doNothing().when(userProjectPinService).pinProject(userId, "mongo-id-123");

                // Act & Assert
                mockMvc.perform(post("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk());

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).pinProject(userId, "mongo-id-123");
        }

        @Test
        void unpinProject_ShouldReturnNoContent() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String userId = "user-123";
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(responseModel);
                doNothing().when(userProjectPinService).unpinProject(userId, "mongo-id-123");

                // Act & Assert
                mockMvc.perform(delete("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isNoContent());

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).unpinProject(userId, "mongo-id-123");
        }

        @Test
        void isProjectPinned_WhenPinned_ShouldReturnTrue() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String userId = "user-123";
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(responseModel);
                when(userProjectPinService.isProjectPinned(userId, "mongo-id-123")).thenReturn(true);

                // Act & Assert
                mockMvc.perform(get("/api/projects/" + projectIdentifier + "/is-pinned")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).isProjectPinned(userId, "mongo-id-123");
        }

        @Test
        void isProjectPinned_WhenNotPinned_ShouldReturnFalse() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String userId = "user-123";
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(responseModel);
                when(userProjectPinService.isProjectPinned(userId, "mongo-id-123")).thenReturn(false);

                // Act & Assert
                mockMvc.perform(get("/api/projects/" + projectIdentifier + "/is-pinned")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk())
                                .andExpect(content().string("false"));

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).isProjectPinned(userId, "mongo-id-123");
        }

        @Test
        void pinProject_WithDifferentProjectId_ShouldReturnOk() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-2";
                String userId = "user-456";
                ProjectResponseModel project2 = new ProjectResponseModel();
                project2.setId("mongo-id-456");
                project2.setProjectIdentifier("PROJ-2");
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(project2);
                doNothing().when(userProjectPinService).pinProject(userId, "mongo-id-456");

                // Act & Assert
                mockMvc.perform(post("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk());

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).pinProject(userId, "mongo-id-456");
        }

        @Test
        void unpinProject_WithDifferentProjectId_ShouldReturnNoContent() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-3";
                String userId = "user-789";
                ProjectResponseModel project3 = new ProjectResponseModel();
                project3.setId("mongo-id-789");
                project3.setProjectIdentifier("PROJ-3");
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(project3);
                doNothing().when(userProjectPinService).unpinProject(userId, "mongo-id-789");

                // Act & Assert
                mockMvc.perform(delete("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isNoContent());

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(1)).unpinProject(userId, "mongo-id-789");
        }

        @Test
        void isProjectPinned_MultipleProjects_ShouldReturnCorrectStatus() throws Exception {
                // Test first project is pinned
                String projectIdentifier1 = "PROJ-1";
                String userId = "user-123";
                when(projectService.getProjectByIdentifier(projectIdentifier1)).thenReturn(responseModel);
                when(userProjectPinService.isProjectPinned(userId, "mongo-id-123")).thenReturn(true);

                mockMvc.perform(get("/api/projects/" + projectIdentifier1 + "/is-pinned")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));

                // Test second project is not pinned
                String projectIdentifier2 = "PROJ-2";
                ProjectResponseModel project2 = new ProjectResponseModel();
                project2.setId("mongo-id-456");
                project2.setProjectIdentifier("PROJ-2");
                when(projectService.getProjectByIdentifier(projectIdentifier2)).thenReturn(project2);
                when(userProjectPinService.isProjectPinned(userId, "mongo-id-456")).thenReturn(false);

                mockMvc.perform(get("/api/projects/" + projectIdentifier2 + "/is-pinned")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId))))
                                .andExpect(status().isOk())
                                .andExpect(content().string("false"));

                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier1);
                verify(projectService, times(1)).getProjectByIdentifier(projectIdentifier2);
                verify(userProjectPinService, times(1)).isProjectPinned(userId, "mongo-id-123");
                verify(userProjectPinService, times(1)).isProjectPinned(userId, "mongo-id-456");
        }

        @Test
        void pinProject_MultipleUsers_ShouldPinCorrectly() throws Exception {
                // Arrange
                String projectIdentifier = "PROJ-1";
                String userId1 = "user-111";
                String userId2 = "user-222";
                when(projectService.getProjectByIdentifier(projectIdentifier)).thenReturn(responseModel);
                doNothing().when(userProjectPinService).pinProject(anyString(), eq("mongo-id-123"));

                // Act - User 1 pins project
                mockMvc.perform(post("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId1))))
                                .andExpect(status().isOk());

                // Act - User 2 pins same project
                mockMvc.perform(post("/api/projects/" + projectIdentifier + "/pin")
                                .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))
                                                .jwt(jwtBuilder -> jwtBuilder.claim("sub", userId2))))
                                .andExpect(status().isOk());

                // Assert
                verify(projectService, times(2)).getProjectByIdentifier(projectIdentifier);
                verify(userProjectPinService, times(2)).pinProject(anyString(), eq("mongo-id-123"));
        }
}