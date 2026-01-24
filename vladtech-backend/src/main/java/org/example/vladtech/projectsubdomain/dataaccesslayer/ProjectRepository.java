package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByProjectIdentifier(String projectIdentifier);
    List<Project> findByAssignedEmployeeIdsContains(String employeeId);

    long countByStatus(ProjectStatus status);

    long countByState(ProjectState state);
    
    // New methods to filter out archived projects (State is NOT COMPLETE)
    long countByStateNot(ProjectState state); // Use with ProjectState.COMPLETE to get Active projects (Null or ACTIVE)
    
    long countByStatusAndStateNot(ProjectStatus status, ProjectState state); // Count specific status among active projects
    
    long countByDueDateBeforeAndStatusNotAndStateNot(java.time.LocalDate date, ProjectStatus status, ProjectState state); // Overdue active projects
}