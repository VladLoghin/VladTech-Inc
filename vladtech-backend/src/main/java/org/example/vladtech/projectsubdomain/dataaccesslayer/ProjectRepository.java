package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

        Optional<Project> findByProjectIdentifier(String projectIdentifier);

        List<Project> findByAssignedEmployeeIdsContains(String employeeId);

        @org.springframework.data.mongodb.repository.Query("{ " +
                        "'$and': [ " +
                        " { $expr: { $cond: { if: { $eq: [ ?0, '' ] }, then: true, else: { $regexMatch: { input: '$name', regex: ?0, options: 'i' } } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?1, '' ] }, then: true, else: { $regexMatch: { input: '$projectIdentifier', regex: ?1, options: 'i' } } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?2, '' ] }, then: true, else: { $regexMatch: { input: '$clientName', regex: ?2, options: 'i' } } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?3, null ] }, then: true, else: { $eq: [ '$status', ?3 ] } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?4, null ] }, then: true, else: { $eq: [ '$state', ?4 ] } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?5, null ] }, then: true, else: { $eq: [ '$priority', ?5 ] } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?6, null ] }, then: true, else: { $gte: [ '$startDate', ?6 ] } } } }, "
                        +
                        " { $expr: { $cond: { if: { $eq: [ ?7, null ] }, then: true, else: { $lte: [ '$dueDate', ?7 ] } } } }, "
                        +
                        " { $or: [ { $expr: { $eq: [ ?8, null ] } }, { 'projectType.type': ?8 } ] }, "
                        +

                        " { $or: [ " +
                        "   { $expr: { $eq: [ ?9, '' ] } }, " +
                        "   { $and: [ " +
                        "       { $expr: { $eq: [ ?9, 'HAS_PRICE' ] } }, " +
                        "       { $expr: { $ne: [ '$estimatedCost', null ] } }, " +
                        "       { $expr: { $gt: [ '$estimatedCost', 0 ] } } " +
                        "   ] }, " +
                        "   { $and: [ " +
                        "       { $expr: { $eq: [ ?9, 'NO_PRICE' ] } }, " +
                        "       { $expr: { $or: [ " +
                        "           { $eq: [ '$estimatedCost', null ] }, " +
                        "           { $lte: [ '$estimatedCost', 0 ] } " +
                        "       ] } } " +
                        "   ] } " +
                        " ] }, " +
                        " { $expr: { $cond: { if: { $eq: [ ?10, '' ] }, then: true, else: { $gt: [ { $size: { $filter: { input: { $ifNull: ['$assignedEmployeeIds', []] }, as: 'id', cond: { $regexMatch: { input: '$$id', regex: ?10, options: 'i' } } } } }, 0 ] } } } } "
                        +
                        "] " +
                        "}")
        org.springframework.data.domain.Page<Project> searchProjects(
                        String name,
                        String projectIdentifier,
                        String clientName,
                        org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus status,
                        org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectState state,
                        org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority priority,
                        java.time.LocalDate startDate,
                        java.time.LocalDate dueDate,
                        org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType.ProjectTypeEnum projectType,
                        String costStatus,
                        String assignedEmployeeId,
                        org.springframework.data.domain.Pageable pageable);
}