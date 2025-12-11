package org.example.vladtech.projectsubdomain.businesslayer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectRepository;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectEmailSender;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectRequestMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectResponseMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectEmailMapper;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectCalendarEntryResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
//@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequestMapper projectRequestMapper;
    private final ProjectResponseMapper projectResponseMapper;
    private final ProjectEmailMapper projectEmailMapper;
    private final ProjectEmailSender projectEmailSender;

    @Lazy
    @Autowired
    ProjectService self;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectRequestMapper projectRequestMapper,
                              ProjectResponseMapper projectResponseMapper,
                              ProjectEmailMapper projectEmailMapper,
                              ProjectEmailSender projectEmailSender) {
        this.projectRepository = projectRepository;
        this.projectRequestMapper = projectRequestMapper;
        this.projectResponseMapper = projectResponseMapper;
        this.projectEmailMapper = projectEmailMapper;
        this.projectEmailSender = projectEmailSender;
    }

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

        self.sendEmailNotificationAsync(savedProject, "Created");

        return projectResponseMapper.entityToResponseModel(savedProject);
    }

    @Override
    public ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel projectRequestModel) {
        Project existingProject = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectIdentifier));

        existingProject.setName(projectRequestModel.getName());
        existingProject.setClientId(projectRequestModel.getClientId());
        existingProject.setClientName(projectRequestModel.getClientName());
        existingProject.setClientEmail(projectRequestModel.getClientEmail());
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

        self.sendEmailNotificationAsync(updatedProject, "Updated");

        return projectResponseMapper.entityToResponseModel(updatedProject);
    }

    /////////////////////////////////////////////////////////////////////////////////////// FILL THE OTHER ONES OUT IN OTHER TICKETS
    @Async
    @Override
    public void sendEmailNotificationAsync(Project project, String operation) {
        if (project.getClientEmail() != null && !project.getClientEmail().isBlank()) {
            try {
                ProjectNotificationEmail email = projectEmailMapper.toProjectNotificationEmail(project, operation);
                if (email != null) {
                    projectEmailSender.send(email);
                    log.info("Project notification email sent to {} for project {}",
                            project.getClientEmail(), project.getProjectIdentifier());
                }
            } catch (Exception e) {
                log.error("Failed to send project notification email for project {}: {}",
                        project.getProjectIdentifier(), e.getMessage());
            }
        }
    }

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

    @Override
    public List<ProjectCalendarEntryResponseModel> getProjectsForCalendar() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .map(this::mapToCalendarEntry)
                .collect(Collectors.toList());
    }

    private ProjectCalendarEntryResponseModel mapToCalendarEntry(Project project) {
        Address address = project.getAddress();

        String locationSummary = null;
        if (address != null) {
            // keep it simple for now, you can tweak later
            // example: "Montreal, QC" or "123 Main St, Montreal"
            locationSummary = String.format("%s, %s",
                    address.getCity(),
                    address.getProvince());
        }

        return new ProjectCalendarEntryResponseModel(
                project.getProjectIdentifier(),
                project.getName(),
                locationSummary,
                project.getStartDate(),
                project.getDueDate()
        );
    }
}