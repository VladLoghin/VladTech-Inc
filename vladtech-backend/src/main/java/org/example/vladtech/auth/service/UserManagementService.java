package org.example.vladtech.auth.service;

import org.example.vladtech.auth.presentation.EmployeeSummaryResponseModel;
import java.util.Map;
import java.util.List;

public interface UserManagementService {
    Map<String, Object> getUsersByRole(String roleId, int page, int perPage);
    Map<String, Object> searchUsers(String query, String roleName, int page, int perPage);
    Map<String, Object> getUsersWithoutRole(String roleId, int page, int perPage);
    List<EmployeeSummaryResponseModel> getAllEmployees(int page, int perPage);
}