package org.example.vladtech.projectsubdomain.businesslayer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectRepository;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectRequestMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectResponseMapper;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequestMapper projectRequestMapper;
    private final ProjectResponseMapper projectResponseMapper;

    @Override
    public List<ProjectResponseModel> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projectResponseMapper.entityListToResponseModelList(projects);
    }

    @Override
    public ProjectResponseModel getProjectByIdentifier(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectIdentifier));
        return projectResponseMapper.entityToResponseModel(project);
    }
    @Override
    public ProjectResponseModel createProject(ProjectRequestModel projectRequestModel) {
        Project project = projectRequestMapper.requestModelToEntity(projectRequestModel);

        //project.setProjectIdentifier(UUID.randomUUID().toString());

        long count = projectRepository.count();
        project.setProjectIdentifier("PROJ-" + (count + 1));

        Project savedProject = projectRepository.save(project);
        return projectResponseMapper.entityToResponseModel(savedProject);
    }

    @Override
    public ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel projectRequestModel) {
        Project existingProject = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectIdentifier));

        existingProject.setName(projectRequestModel.getName());
        existingProject.setDescription(projectRequestModel.getDescription());
        existingProject.setStartDate(projectRequestModel.getStartDate());
        existingProject.setDueDate(projectRequestModel.getDueDate());

        if (projectRequestModel.getAddress() != null) {
            existingProject.setAddress(new Address(
                    projectRequestModel.getAddress().getStreetAddress(),
                    projectRequestModel.getAddress().getCity(),
                    projectRequestModel.getAddress().getProvince(),
                    projectRequestModel.getAddress().getCountry(),
                    projectRequestModel.getAddress().getPostalCode()
            ));
        }

        if (projectRequestModel.getProjectType() != null) {
            ProjectType projectType = new ProjectType();
            projectType.setType(ProjectType.ProjectTypeEnum.valueOf(projectRequestModel.getProjectType().toUpperCase()));
            existingProject.setProjectType(projectType);
        }

        Project updatedProject = projectRepository.save(existingProject);
        return projectResponseMapper.entityToResponseModel(updatedProject);
    }

    /////////////////////////////////////////////////////////////////////////////////////// FILL THE OTHER ONES OUT IN OTHER TICKETS
    @Override
    public void deleteProject(String projectIdentifier) {
    }

    @Override
    public ProjectResponseModel assignEmployee(String projectIdentifier, String employeeId) {
        return null;
    }

    @Override
    public List<PhotoResponseModel> getProjectPhotos(String projectIdentifier) {
        return null;
    }

    @Override
    public ProjectResponseModel addProjectPhoto(String projectIdentifier, PhotoResponseModel photoResponseModel) {
        return null;
    }

    @Override
    public void deleteProjectPhoto(String projectIdentifier, String photoId) {
    }

    @Override
    public long getProjectCount() {
        return projectRepository.count();
    }


}