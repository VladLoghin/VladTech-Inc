package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;

import java.util.List;

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
}