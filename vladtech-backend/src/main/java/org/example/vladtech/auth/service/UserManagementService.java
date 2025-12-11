package org.example.vladtech.auth.service;

import java.util.Map;

public interface UserManagementService {
    Map<String, Object> getUsersByRole(String roleId, int page, int perPage);
    Map<String, Object> searchUsers(String query, String roleName, int page, int perPage);
    Map<String, Object> getUsersWithoutRole(String roleId, int page, int perPage);
}