package org.example.vladtech.auth.presentation;

import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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


}