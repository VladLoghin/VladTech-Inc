package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByProjectIdentifier(String projectIdentifier);

    List<Project> findByAssignedEmployeeIdsContains(String employeeId);

    @org.springframework.data.mongodb.repository.Query("{ " +
            "'$and': [ " +
            " { '$or': [ " +
            " { 'name': { '$regex': ?0, '$options': 'i' } }, " +
            " { 'projectIdentifier': { '$regex': ?0, '$options': 'i' } }, " +
            " { 'clientName': { '$regex': ?0, '$options': 'i' } } " +
            " ] }, " +
            " { $expr: { $cond: { if: { $eq: [ ?1, null ] }, then: true, else: { $eq: [ '$status', ?1 ] } } } }, " + // Status
            " { $expr: { $cond: { if: { $eq: [ ?2, null ] }, then: true, else: { $eq: [ '$state', ?2 ] } } } }, " + // State
            " { $expr: { $cond: { if: { $eq: [ ?3, null ] }, then: true, else: { $eq: [ '$priority', ?3 ] } } } } " + // Priority
            "] " +
            "}")
    org.springframework.data.domain.Page<Project> searchProjects(
            String search,
            org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus status,
            org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectState state,
            org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority priority,
            org.springframework.data.domain.Pageable pageable);
}