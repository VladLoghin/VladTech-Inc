package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponseModel {
    private long totalProjects;
    private long pendingCount;
    private long inProgressCount;
    private long completedCount;
    private long activeCount;
    private long archivedCount;
    private long overdueCount;
}
