package org.example.vladtech.auth.dataaccess;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    UserProfile findUserProfileByAuth0Sub(String auth0Sub);

}
