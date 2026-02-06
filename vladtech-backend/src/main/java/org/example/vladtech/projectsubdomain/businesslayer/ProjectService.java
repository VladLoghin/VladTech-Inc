package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectStatsResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectCalendarEntryResponseModel;
import java.util.List;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectState;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectService {

        List<ProjectResponseModel> getAllProjects();

        ProjectResponseModel getProjectByIdentifier(String projectIdentifier);

        ProjectResponseModel createProject(ProjectRequestModel projectRequestModel);

        ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel projectRequestModel);

        void deleteProject(String projectIdentifier);

        ProjectResponseModel assignEmployee(String projectIdentifier, String employeeId);

        ProjectResponseModel assignClient(String projectIdentifier, String clientId);

        List<PhotoResponseModel> getProjectPhotos(String projectIdentifier);

        ProjectResponseModel addProjectPhoto(String projectIdentifier, PhotoResponseModel photoResponseModel);

        void deleteProjectPhoto(String projectIdentifier, String photoId);

        long getProjectCount();

        List<ProjectCalendarEntryResponseModel> getProjectsForCalendar();

        void sendEmailNotificationAsync(Project project, String operation);

        List<ProjectResponseModel> getProjectsForEmployee(String employeeId);

        ProjectResponseModel completeProject(String projectIdentifier);

        ProjectResponseModel reactivateProject(String projectIdentifier);

    ProjectStatsResponseModel getProjectStats();

    List<ProjectResponseModel> getActiveProjects();

        List<ProjectResponseModel> getArchivedProjects();

    List<ProjectResponseModel> getCompletedProjectsByClientId(String clientId);

    org.example.vladtech.portfolio.presentation.PortfolioResponseDto sendProjectToPortfolio(
            String projectIdentifier,
            String type,
            org.springframework.web.multipart.MultipartFile image
    );

        void sendEmployeeAssignedEmailAsync(Project project, String employeeEmail);

        ProjectResponseModel updateProjectStatusForEmployee(String projectIdentifier, String employeeId,
                        ProjectStatus newStatus);

        ProjectResponseModel uploadLatestPhotoForEmployee(String projectIdentifier, String employeeId,
                        MultipartFile photo,
                        String comment);



    List<ProjectResponseModel> getProjectsList(
            String name,
            String projectIdentifier,
            String clientName,
            ProjectStatus status,
            ProjectState state,
            org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority priority,
            java.time.LocalDate startDate,
            java.time.LocalDate dueDate,
            String projectType,
            String costStatus,
            String assignedEmployeeId);

    org.springframework.data.domain.Page<ProjectResponseModel> searchProjects(
            String name,
            String projectIdentifier,
            String clientName,
            ProjectStatus status,
            ProjectState state,
            org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority priority,
            java.time.LocalDate startDate,
            java.time.LocalDate dueDate,
            String projectType,
            String costStatus,
            String assignedEmployeeId,
            org.springframework.data.domain.Pageable pageable);
}