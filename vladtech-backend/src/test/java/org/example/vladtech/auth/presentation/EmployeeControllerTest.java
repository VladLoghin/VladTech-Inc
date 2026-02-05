package org.example.vladtech.auth.presentation;

import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserManagementService userManagementService;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void uploadLatestProjectPhoto_withPhotoAndComments_returnsOk_andCallsServiceWithJwtSubject() throws Exception {
        String projectId = "PROJ-1";
        String employeeId = "emp-123";

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "work.jpg",
                "image/jpeg",
                "img".getBytes()
        );

        when(projectService.uploadLatestPhotoForEmployee(eq(projectId), eq(employeeId), any(), eq("hello")))
                .thenReturn(mock(ProjectResponseModel.class));

        mockMvc.perform(multipart("/api/employee/projects/{projectIdentifier}/photo", projectId)
                        .file(photo)
                        .param("comments", "hello")
                        .with(jwt().jwt(j -> j.subject(employeeId)).authorities(() -> "Employee")))
                .andExpect(status().isOk());

        verify(projectService).uploadLatestPhotoForEmployee(eq(projectId), eq(employeeId), any(), eq("hello"));
    }

    @Test
    void uploadLatestProjectPhoto_withoutComments_stillOk_andCallsService() throws Exception {
        String projectId = "PROJ-1";
        String employeeId = "emp-123";

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "work.jpg",
                "image/jpeg",
                "img".getBytes()
        );

        when(projectService.uploadLatestPhotoForEmployee(eq(projectId), eq(employeeId), any(), any()))
                .thenReturn(mock(ProjectResponseModel.class));

        mockMvc.perform(multipart("/api/employee/projects/{projectIdentifier}/photo", projectId)
                        .file(photo)
                        .with(jwt().jwt(j -> j.subject(employeeId)).authorities(() -> "Employee")))
                .andExpect(status().isOk());

        verify(projectService).uploadLatestPhotoForEmployee(eq(projectId), eq(employeeId), any(), any());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void getAllEmployees_returnsEmployeeList() throws Exception {
        when(userManagementService.getAllEmployees(0, 50))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employee/list")
                        .param("page", "0")
                        .param("perPage", "50"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());

        verify(userManagementService).getAllEmployees(0, 50);
    }

    @Test
    void getMyProjects_returnsProjectList() throws Exception {
        when(projectService.getProjectsForEmployee("employee123"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employee/projects")
                        .with(jwt().jwt(j -> j.subject("employee123")).authorities(() -> "Employee")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());

        verify(projectService).getProjectsForEmployee("employee123");
    }

    @Test
    void updateMyProjectStatus_updatesProjectStatus() throws Exception {
        ProjectResponseModel responseModel = new ProjectResponseModel();
        when(projectService.updateProjectStatusForEmployee("project123", "employee123", ProjectStatus.IN_PROGRESS))
                .thenReturn(responseModel);

        mockMvc.perform(put("/api/employee/projects/project123/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}")
                        .with(jwt().jwt(j -> j.subject("employee123")).authorities(() -> "Employee")))
                .andExpect(status().isOk());

        verify(projectService).updateProjectStatusForEmployee("project123", "employee123", ProjectStatus.IN_PROGRESS);
    }

    @Test
    void uploadLatestProjectPhoto_uploadsPhoto() throws Exception {
        ProjectResponseModel responseModel = new ProjectResponseModel();
        when(projectService.uploadLatestPhotoForEmployee(eq("project123"), eq("employee123"), any(), eq("Great work")))
                .thenReturn(responseModel);

        mockMvc.perform(multipart("/api/employee/projects/project123/photo")
                        .file("photo", "test-photo-content".getBytes())
                        .param("comments", "Great work")
                        .with(jwt().jwt(j -> j.subject("employee123")).authorities(() -> "Employee")))
                .andExpect(status().isOk());

        verify(projectService).uploadLatestPhotoForEmployee(eq("project123"), eq("employee123"), any(), eq("Great work"));
    }

}