package org.example.vladtech.auth.presentation;

import lombok.Data;

@Data
public class ProjectStatusUpdateRequest {
    private String status; // "PENDING", "IN_PROGRESS", "COMPLETED"
}
