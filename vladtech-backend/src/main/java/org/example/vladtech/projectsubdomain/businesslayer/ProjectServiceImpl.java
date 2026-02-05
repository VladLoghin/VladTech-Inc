package org.example.vladtech.projectsubdomain.businesslayer;

import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectRepository;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectEmailSender;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.example.vladtech.projectsubdomain.exceptions.InvalidEmployeeIdException;
import org.example.vladtech.projectsubdomain.exceptions.InvalidProjectDataException;
import org.example.vladtech.projectsubdomain.exceptions.ProjectNotFoundException;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectRequestMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectResponseMapper;
import org.example.vladtech.projectsubdomain.mappinglayer.ProjectEmailMapper;
import org.example.vladtech.projectsubdomain.presentationlayer.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectState;
import org.example.vladtech.projectsubdomain.exceptions.ProjectArchivedException;
import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequestMapper projectRequestMapper;
    private final ProjectResponseMapper projectResponseMapper;
    private final ProjectEmailMapper projectEmailMapper;
    private final ProjectEmailSender projectEmailSender;
    private final UserManagementService userManagementService;
    private final FileStorageService fileStorageService;
    private final org.example.vladtech.portfolio.data.PortfolioRepository portfolioRepository;
    private final org.example.vladtech.portfolio.mapperlayer.PortfolioMapper portfolioMapper;

    @Lazy
    @Autowired
    ProjectService self;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
            ProjectRequestMapper projectRequestMapper,
            ProjectResponseMapper projectResponseMapper,
            ProjectEmailMapper projectEmailMapper,
            ProjectEmailSender projectEmailSender,
            UserManagementService userManagementService,
            FileStorageService fileStorageService,
            org.example.vladtech.portfolio.data.PortfolioRepository portfolioRepository,
            org.example.vladtech.portfolio.mapperlayer.PortfolioMapper portfolioMapper) {
        this.projectRepository = projectRepository;
        this.projectRequestMapper = projectRequestMapper;
        this.projectResponseMapper = projectResponseMapper;
        this.projectEmailMapper = projectEmailMapper;
        this.projectEmailSender = projectEmailSender;
        this.userManagementService = userManagementService;
        this.fileStorageService = fileStorageService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    public List<ProjectResponseModel> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projectResponseMapper.entityListToResponseModelList(projects);
    }

    @Override
    public ProjectResponseModel getProjectByIdentifier(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));
        return projectResponseMapper.entityToResponseModel(project);
    }

    @Override
    public ProjectResponseModel createProject(ProjectRequestModel projectRequestModel) {
        validateProjectRequest(projectRequestModel, true);
        
        Project project = projectRequestMapper.requestModelToEntity(projectRequestModel);

        long count = projectRepository.count();
        project.setProjectIdentifier("PROJ-" + (count + 1));
        project.setStatus(ProjectStatus.PENDING);

        Project savedProject = projectRepository.save(project);

        self.sendEmailNotificationAsync(savedProject, "Created");

        return projectResponseMapper.entityToResponseModel(savedProject);
    }

    @Override
    public ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel projectRequestModel) {
        Project existingProject = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        if (existingProject.getState() == ProjectState.COMPLETE) {
            throw new ProjectArchivedException(projectIdentifier);
        }
        
        validateProjectRequest(projectRequestModel, false);

        existingProject.setName(projectRequestModel.getName());
        existingProject.setClientId(projectRequestModel.getClientId());
        existingProject.setClientName(projectRequestModel.getClientName());
        existingProject.setClientEmail(projectRequestModel.getClientEmail());
        existingProject.setDescription(projectRequestModel.getDescription());
        existingProject.setStartDate(projectRequestModel.getStartDate());
        existingProject.setDueDate(projectRequestModel.getDueDate());

        // keep cost fields (teammate work)
        existingProject.setEstimatedCost(projectRequestModel.getEstimatedCost());
        existingProject.setEstimatedCostCurrency(projectRequestModel.getEstimatedCostCurrency());

        if (projectRequestModel.getAddress() != null) {
            existingProject.setAddress(new Address(
                    projectRequestModel.getAddress().getStreetAddress(),
                    projectRequestModel.getAddress().getCity(),
                    projectRequestModel.getAddress().getProvince(),
                    projectRequestModel.getAddress().getCountry(),
                    projectRequestModel.getAddress().getPostalCode()));
        }

        if (projectRequestModel.getProjectType() != null && !projectRequestModel.getProjectType().isEmpty()) {
            ProjectType projectType = new ProjectType();
            projectType.setType(ProjectType.ProjectTypeEnum.valueOf(
                    projectRequestModel.getProjectType().toUpperCase()));
            existingProject.setProjectType(projectType);
        }

        if (projectRequestModel.getPriority() != null && !projectRequestModel.getPriority().isEmpty()) {
            existingProject.setPriority(
                    org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority
                            .valueOf(projectRequestModel.getPriority().toUpperCase()));
        }

        if (projectRequestModel.getAssignedEmployeeIds() != null) {
            existingProject.setAssignedEmployeeIds(projectRequestModel.getAssignedEmployeeIds());
        }

        Project updatedProject = projectRepository.save(existingProject);

        self.sendEmailNotificationAsync(updatedProject, "Updated");

        return projectResponseMapper.entityToResponseModel(updatedProject);
    }

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
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        log.info("[ASSIGN] called projectIdentifier={} employeeId={}", projectIdentifier, employeeId);
        log.info("[ASSIGN] before list={}", project.getAssignedEmployeeIds());

        if (employeeId == null || employeeId.isBlank()) {
            throw new InvalidEmployeeIdException("employeeId cannot be null or blank");
        }

        if (project.getAssignedEmployeeIds() == null) {
            project.setAssignedEmployeeIds(new ArrayList<>());
        }

        if (!project.getAssignedEmployeeIds().contains(employeeId)) {
            project.getAssignedEmployeeIds().add(employeeId);
            project = projectRepository.save(project);

            log.info("[ASSIGN] saved project. now list={}", project.getAssignedEmployeeIds());

            String employeeEmail = userManagementService.getUserEmailById(employeeId);
            log.info("[ASSIGN] resolved employeeEmail='{}'", employeeEmail);

            if (employeeEmail != null && !employeeEmail.isBlank()) {
                if (project.getAssignedEmployeeEmails() == null) {
                    project.setAssignedEmployeeEmails(new ArrayList<>());
                }
                if (!project.getAssignedEmployeeEmails().contains(employeeEmail)) {
                    project.getAssignedEmployeeEmails().add(employeeEmail);
                    project = projectRepository.save(project);
                }
            }

            self.sendEmployeeAssignedEmailAsync(project, employeeEmail);
            log.info("[ASSIGN] triggered async send");
        } else {
            log.info("[ASSIGN] employee already assigned, skipping email");
        }

        return projectResponseMapper.entityToResponseModel(project);
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
            locationSummary = String.format("%s, %s",
                    address.getCity(),
                    address.getProvince());
        }

        return new ProjectCalendarEntryResponseModel(
                project.getProjectIdentifier(),
                project.getName(),
                locationSummary,
                project.getStartDate(),
                project.getDueDate(),
                project.getStatus() != null ? project.getStatus().name() : "PENDING",
                project.getState() != null ? project.getState().name() : "ACTIVE",
                project.getProjectType() != null ? project.getProjectType().getType().name() : null,
                project.getPriority() != null ? project.getPriority().name() : "MEDIUM");
    }

    @Override
    public List<ProjectResponseModel> getProjectsForEmployee(String employeeId) {
        List<Project> projects = projectRepository.findByAssignedEmployeeIdsContains(employeeId);
        
        // Defensive filter: only return ACTIVE projects where this employee is explicitly assigned
        List<Project> filteredProjects = projects.stream()
                .filter(p -> p.getState() == ProjectState.ACTIVE)
                .filter(p -> p.getAssignedEmployeeIds() != null 
                        && p.getAssignedEmployeeIds().contains(employeeId))
                .toList();
        
        // Populate assignedEmployeeEmails for each project
        filteredProjects.forEach(this::enrichProjectWithEmployeeEmails);
        
        return projectResponseMapper.entityListToResponseModelList(filteredProjects);
    }
    
    /**
     * Enriches a project by populating assignedEmployeeEmails from assignedEmployeeIds
     */
    private void enrichProjectWithEmployeeEmails(Project project) {
        if (project.getAssignedEmployeeIds() == null || project.getAssignedEmployeeIds().isEmpty()) {
            project.setAssignedEmployeeEmails(new ArrayList<>());
            return;
        }
        
        // If assignedEmployeeEmails already has values, use them
        if (project.getAssignedEmployeeEmails() != null && !project.getAssignedEmployeeEmails().isEmpty()) {
            return;
        }
        
        List<String> emails = project.getAssignedEmployeeIds().stream()
                .map(id -> {
                    try {
                        String email = userManagementService.getUserEmailById(id);
                        return (email != null && !email.isBlank()) ? email : null;
                    } catch (Exception e) {
                        log.warn("Failed to get email for employee ID: {}", id, e);
                        return null;
                    }
                })
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toList());
        if (emails.isEmpty()) {
            return;
        }

        project.setAssignedEmployeeEmails(emails);
        projectRepository.save(project);
    }

    @Override
    public ProjectResponseModel completeProject(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        project.setState(ProjectState.COMPLETE);
        project.setArchivedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);
        return projectResponseMapper.entityToResponseModel(savedProject);
    }

    @Override
    public ProjectResponseModel reactivateProject(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        project.setState(ProjectState.ACTIVE);
        project.setArchivedAt(null);

        Project savedProject = projectRepository.save(project);
        return projectResponseMapper.entityToResponseModel(savedProject);
    }

    @Override
    public ProjectStatsResponseModel getProjectStats() {
        return ProjectStatsResponseModel.builder()
                .totalProjects(projectRepository.count())
                .pendingCount(projectRepository.countByStatusAndStateNot(ProjectStatus.PENDING, ProjectState.COMPLETE))
                .inProgressCount(projectRepository.countByStatusAndStateNot(ProjectStatus.IN_PROGRESS, ProjectState.COMPLETE))
                .completedCount(projectRepository.countByStatusAndStateNot(ProjectStatus.COMPLETED, ProjectState.COMPLETE))
                .activeCount(projectRepository.countByStateNot(ProjectState.COMPLETE)) 
                .archivedCount(projectRepository.countByState(ProjectState.COMPLETE))
                .overdueCount(projectRepository.countByDueDateBeforeAndStatusNotAndStateNot(LocalDate.now(), ProjectStatus.COMPLETED, ProjectState.COMPLETE))
                .build();
    }

    @Override
    public List<ProjectResponseModel> getActiveProjects() {
        List<Project> allProjects = projectRepository.findAll();
        List<Project> activeProjects = allProjects.stream()
                .filter(p -> p.getState() == null || p.getState() == ProjectState.ACTIVE)
                .collect(Collectors.toList());
        return projectResponseMapper.entityListToResponseModelList(activeProjects);
    }

    @Override
    public List<ProjectResponseModel> getArchivedProjects() {
        List<Project> allProjects = projectRepository.findAll();
        List<Project> archivedProjects = allProjects.stream()
                .filter(p -> p.getState() == ProjectState.COMPLETE)
                .collect(Collectors.toList());
        return projectResponseMapper.entityListToResponseModelList(archivedProjects);
    }

    @Async
    public void sendEmployeeAssignedEmailAsync(Project project, String employeeEmail) {
        log.info("[MAIL] entered sendEmployeeAssignedEmailAsync. to='{}'", employeeEmail);

        if (employeeEmail == null || employeeEmail.isBlank()) {
            return;
        }

        try {
            ProjectNotificationEmail email = projectEmailMapper.toEmployeeAssignedEmail(project, employeeEmail);

            if (email != null) {
                projectEmailSender.send(email);
                log.info("Employee assignment email sent to {} for project {}",
                        employeeEmail, project.getProjectIdentifier());
            }
        } catch (Exception e) {
            log.error("Failed to send employee assignment email for project {}: {}",
                    project.getProjectIdentifier(), e.getMessage());
        }
    }

    @Override
    public ProjectResponseModel updateProjectStatusForEmployee(
            String projectIdentifier,
            String employeeId,
            ProjectStatus newStatus) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        if (employeeId == null || employeeId.isBlank()) {
            throw new InvalidEmployeeIdException("employeeId cannot be null or blank");
        }

        List<String> assigned = project.getAssignedEmployeeIds();
        if (assigned == null || !assigned.contains(employeeId)) {
            throw new RuntimeException("Not allowed: employee is not assigned to this project");
        }

        ProjectStatus current = (project.getStatus() == null) ? ProjectStatus.PENDING : project.getStatus();

        if (!isValidStatusTransition(current, newStatus)) {
            throw new RuntimeException("Invalid status transition: " + current + " -> " + newStatus);
        }

        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return projectResponseMapper.entityToResponseModel(saved);
    }

    private boolean isValidStatusTransition(ProjectStatus current, ProjectStatus next) {
        if (current == next)
            return true;

        return (current == ProjectStatus.PENDING && next == ProjectStatus.IN_PROGRESS)
                || (current == ProjectStatus.IN_PROGRESS && next == ProjectStatus.COMPLETED);
    }

    @Override
    public ProjectResponseModel uploadLatestPhotoForEmployee(
            String projectIdentifier,
            String employeeId,
            MultipartFile photo,
            String comment) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (employeeId == null || employeeId.isBlank()) {
            throw new InvalidEmployeeIdException("employeeId cannot be null or blank");
        }

        if (project.getAssignedEmployeeIds() == null || !project.getAssignedEmployeeIds().contains(employeeId)) {
            throw new RuntimeException("You are not assigned to this project");
        }

        String trimmedComment = (comment == null) ? "" : comment.trim();
        boolean hasComment = !trimmedComment.isEmpty();
        boolean hasPhoto = (photo != null && !photo.isEmpty());

        // ✅ must have at least one
        if (!hasPhoto && !hasComment) {
            throw new RuntimeException("Must provide a photo, a comment, or both");
        }

        if (project.getPhotos() == null) {
            project.setPhotos(new ArrayList<>());
        }

        // CASE 1: Photo uploaded (with optional comment)
        if (hasPhoto) {
            // delete old photo only when a new photo is uploaded
            if (!project.getPhotos().isEmpty() && project.getPhotos().get(0) != null) {
                String oldId = project.getPhotos().get(0).getPhotoId();
                if (oldId != null && !oldId.isBlank()) {
                    try {
                        fileStorageService.delete(oldId);
                    } catch (Exception ignored) {
                    }
                }
            }

            String photoId;
            try {
                photoId = fileStorageService.save(photo);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload photo", e);
            }

            String url = "/api/uploads/projects/" + photoId;

            ProjectPhoto latest = new ProjectPhoto(photoId, url, hasComment ? trimmedComment : "");

            project.getPhotos().clear();
            project.getPhotos().add(latest);

            Project saved = projectRepository.save(project);
            return projectResponseMapper.entityToResponseModel(saved);
        }

        // CASE 2: Comment only (no photo)
        if (!project.getPhotos().isEmpty() && project.getPhotos().get(0) != null) {
            // update existing latest entry description, keep photo
            ProjectPhoto existing = project.getPhotos().get(0);
            ProjectPhoto updated = new ProjectPhoto(
                    existing.getPhotoId(),
                    existing.getPhotoUrl(),
                    trimmedComment);

            project.getPhotos().set(0, updated);
        } else {
            // no existing photo, store a "note-only" entry with no photoUrl
            String noteId = "note-" + System.currentTimeMillis();
            ProjectPhoto note = new ProjectPhoto(noteId, "", trimmedComment);

            project.getPhotos().clear();
            project.getPhotos().add(note);
        }

        Project saved = projectRepository.save(project);
        return projectResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public org.springframework.data.domain.Page<ProjectResponseModel> searchProjects(
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
            org.springframework.data.domain.Pageable pageable) {

        log.info("Searching projects. costStatus='{}'", costStatus);

        String safeName = (name == null) ? "" : name.trim();
        String safeId = (projectIdentifier == null) ? "" : projectIdentifier.trim();
        String safeClient = (clientName == null) ? "" : clientName.trim();
        String safeCostStatus = (costStatus == null) ? "" : costStatus.trim();
        String safeEmployeeId = (assignedEmployeeId == null) ? "" : assignedEmployeeId.trim();

        org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType.ProjectTypeEnum safeType = null;
        if (projectType != null && !projectType.trim().isEmpty()) {
            try {
                safeType = org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType.ProjectTypeEnum
                        .valueOf(projectType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                safeType = null;
            }
        }

        org.springframework.data.domain.Page<Project> projects = projectRepository.searchProjects(
                safeName,
                safeId,
                safeClient,
                status,
                state,
                priority,
                startDate,
                dueDate,
                safeType,
                safeCostStatus,
                safeEmployeeId,
                pageable);

        return projects.map(projectResponseMapper::entityToResponseModel);
    }

    @Override
    public org.example.vladtech.portfolio.presentation.PortfolioResponseDto sendProjectToPortfolio(
            String projectIdentifier,
            String type,
            MultipartFile image) {
        
        // Find the project
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException(projectIdentifier));

        // Check if already sent to portfolio
        if (project.isSentToPortfolio()) {
            throw new RuntimeException("This project has already been sent to portfolio");
        }

        // Upload image
        String imageUrl = "";
        if (image != null && !image.isEmpty()) {
            try {
                String fileId = fileStorageService.save(image);
                imageUrl = "/api/uploads/portfolio/" + fileId;
            } catch (IOException e) {
                log.error("Failed to upload image for project {}: {}", projectIdentifier, e.getMessage());
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        // Create portfolio item
        org.example.vladtech.portfolio.data.PortfolioItem portfolioItem = new org.example.vladtech.portfolio.data.PortfolioItem();
        portfolioItem.setPortfolioId(java.util.UUID.randomUUID().toString());
        portfolioItem.setTitle(project.getName());
        portfolioItem.setImageUrl(imageUrl);
        portfolioItem.setType(type);
        portfolioItem.setComments(new java.util.ArrayList<>());

        // Save to portfolio repository
        org.example.vladtech.portfolio.data.PortfolioItem savedItem = portfolioRepository.save(portfolioItem);

        // Mark project as sent to portfolio
        project.setSentToPortfolio(true);
        projectRepository.save(project);

        log.info("Project {} sent to portfolio with type {}", projectIdentifier, type);

        return portfolioMapper.entityToResponseDto(savedItem);
    }

    private void validateProjectRequest(ProjectRequestModel model, boolean isCreate) {
        // Validate name is not empty (only if provided - allow null for partial updates)
        if (model.getName() != null && model.getName().trim().isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }
        
        // For create operations, name is required
        if (isCreate && model.getName() == null) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }

        // Validate estimated cost is non-negative
        if (model.getEstimatedCost() != null && model.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProjectDataException("Estimated cost must be greater than or equal to 0");
        }

        // Validate dates
        if (model.getStartDate() != null && model.getDueDate() != null) {
            if (model.getStartDate().isAfter(model.getDueDate())) {
                throw new InvalidProjectDataException("Start date must be on or before due date");
            }
        }

        // Validate due date is not in the past (only for creation)
        if (isCreate && model.getDueDate() != null) {
            if (model.getDueDate().isBefore(LocalDate.now())) {
                throw new InvalidProjectDataException("Due date cannot be in the past");
            }
        }

        // Validate city if address is provided
        if (model.getAddress() != null) {
            AddressRequestModel addr = model.getAddress();
            boolean hasAddressData = 
                (addr.getStreetAddress() != null && !addr.getStreetAddress().trim().isEmpty()) ||
                (addr.getProvince() != null && !addr.getProvince().trim().isEmpty()) ||
                (addr.getCountry() != null && !addr.getCountry().trim().isEmpty()) ||
                (addr.getPostalCode() != null && !addr.getPostalCode().trim().isEmpty());
            
            if (hasAddressData && (addr.getCity() == null || addr.getCity().trim().isEmpty())) {
                throw new InvalidProjectDataException("City is required when address information is provided");
            }
        }
    }
}
