package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectCalendarEntryResponseModel;
import java.util.List;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.springframework.web.multipart.MultipartFile;


public interface ProjectService {

    List<ProjectResponseModel> getAllProjects();

    ProjectResponseModel getProjectByIdentifier(String projectIdentifier);

    ProjectResponseModel createProject(ProjectRequestModel projectRequestModel);

    ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel projectRequestModel);

    void deleteProject(String projectIdentifier);

    ProjectResponseModel assignEmployee(String projectIdentifier, String employeeId);

    List<PhotoResponseModel> getProjectPhotos(String projectIdentifier);

    ProjectResponseModel addProjectPhoto(String projectIdentifier, PhotoResponseModel photoResponseModel);

    void deleteProjectPhoto(String projectIdentifier, String photoId);

    long getProjectCount();

    List<ProjectCalendarEntryResponseModel> getProjectsForCalendar();

    void sendEmailNotificationAsync(Project project, String operation);

    List<ProjectResponseModel> getProjectsForEmployee(String employeeId);

    ProjectResponseModel completeProject(String projectIdentifier);

    ProjectResponseModel reactivateProject(String projectIdentifier);

    List<ProjectResponseModel> getActiveProjects();

    List<ProjectResponseModel> getArchivedProjects();

    void sendEmployeeAssignedEmailAsync(Project project, String employeeEmail);

    ProjectResponseModel updateProjectStatusForEmployee(String projectIdentifier, String employeeId, ProjectStatus newStatus);

    ProjectResponseModel uploadLatestPhotoForEmployee(String projectIdentifier, String employeeId, MultipartFile photo , String comment);

}