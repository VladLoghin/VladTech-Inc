package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByProjectIdentifier(String projectIdentifier);
}