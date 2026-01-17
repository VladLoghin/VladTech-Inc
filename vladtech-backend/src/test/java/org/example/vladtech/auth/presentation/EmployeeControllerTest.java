package org.example.vladtech.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserManagementService userManagementService;
    @MockitoBean ProjectService projectService;

    @Test
    void getAllEmployees_admin_ok() throws Exception {
        when(userManagementService.getAllEmployees(0, 50))
                .thenReturn(List.of(new EmployeeSummaryResponseModel("u1", "John", "john@x.com")));

        mockMvc.perform(get("/api/employee/list")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("u1"));
    }

    @Test
    void getMyProjects_employee_ok() throws Exception {
        when(projectService.getProjectsForEmployee("auth0|emp1"))
                .thenReturn(List.of(new ProjectResponseModel()));

        mockMvc.perform(get("/api/employee/projects")
                        .with(jwt().jwt(j -> j.subject("auth0|emp1"))
                                .authorities(new SimpleGrantedAuthority("Employee"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateMyProjectStatus_employee_ok() throws Exception {
        ProjectStatusUpdateRequest req = new ProjectStatusUpdateRequest();
        req.setStatus("IN_PROGRESS");

        when(projectService.updateProjectStatusForEmployee(
                eq("PROJ-1"), eq("auth0|emp1"), eq(ProjectStatus.IN_PROGRESS)
        )).thenReturn(new ProjectResponseModel());

        mockMvc.perform(put("/api/employee/projects/PROJ-1/status")
                        .with(jwt().jwt(j -> j.subject("auth0|emp1"))
                                .authorities(new SimpleGrantedAuthority("Employee")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
