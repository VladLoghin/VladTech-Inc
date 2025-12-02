package org.example.vladtech.projectsubdomain.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)  // <-- This disables Spring Security for tests
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private ProjectResponseModel responseModel;
    private ProjectRequestModel requestModel;

    @BeforeEach
    void setUp() {
        responseModel = new ProjectResponseModel();
        responseModel.setProjectIdentifier("PROJ-1");
        responseModel.setName("Test Project");
        responseModel.setDescription("Test Description");
        responseModel.setStartDate(LocalDate.now());
        responseModel.setDueDate(LocalDate.now().plusDays(30));
        responseModel.setProjectType("SCHEDULED");
        responseModel.setAssignedEmployeeIds(Collections.emptyList());
        responseModel.setPhotos(Collections.emptyList());

        AddressResponseModel address = new AddressResponseModel("123 Main St", "Montreal", "Quebec", "Canada", "H1A1A1");
        responseModel.setAddress(address);

        requestModel = new ProjectRequestModel();
        requestModel.setName("Test Project");
        requestModel.setDescription("Test Description");
        requestModel.setStartDate(LocalDate.now());
        requestModel.setDueDate(LocalDate.now().plusDays(30));
        requestModel.setProjectType("SCHEDULED");

        AddressRequestModel addressRequest = new AddressRequestModel("123 Main St", "Montreal", "Quebec", "Canada", "H1A1A1");
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
                .andExpect(jsonPath("$[0].name").value("Test Project"));

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
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService, times(1)).getProjectByIdentifier("PROJ-1");
    }

    @Test
    void createProject_ShouldReturnCreatedWithProject() throws Exception {
        // Arrange
        when(projectService.createProject(any(ProjectRequestModel.class))).thenReturn(responseModel);

        // Act & Assert
        mockMvc.perform(post("/api/projects")
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
        when(projectService.updateProject(eq("PROJ-1"), any(ProjectRequestModel.class))).thenReturn(responseModel);

        // Act & Assert
        mockMvc.perform(put("/api/projects/PROJ-1")
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
        mockMvc.perform(delete("/api/projects/PROJ-1"))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject("PROJ-1");
    }

    @Test
    void assignEmployee_ShouldReturnOkWithProject() throws Exception {
        // Arrange
        when(projectService.assignEmployee("PROJ-1", "EMP-1")).thenReturn(responseModel);

        // Act & Assert
        mockMvc.perform(post("/api/projects/PROJ-1/assign/EMP-1"))
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
        when(projectService.addProjectPhoto(eq("PROJ-1"), any(PhotoResponseModel.class))).thenReturn(responseModel);

        // Act & Assert
        mockMvc.perform(post("/api/projects/PROJ-1/photos")
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
        mockMvc.perform(delete("/api/projects/PROJ-1/photos/PHOTO-1"))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProjectPhoto("PROJ-1", "PHOTO-1");
    }
}