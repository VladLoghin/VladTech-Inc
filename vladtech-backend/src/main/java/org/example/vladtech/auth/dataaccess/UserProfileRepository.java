package org.example.vladtech.auth.dataaccess;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    UserProfile findUserProfileByAuth0Sub(String auth0Sub);
}
