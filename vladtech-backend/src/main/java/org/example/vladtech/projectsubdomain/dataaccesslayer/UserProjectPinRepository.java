package org.example.vladtech.projectsubdomain.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserProjectPinRepository extends MongoRepository<UserProjectPin, String> {

    List<UserProjectPin> findByUserId(String userId);

    Optional<UserProjectPin> findByUserIdAndProjectId(String userId, String projectId);

    void deleteByUserIdAndProjectId(String userId, String projectId);

    boolean existsByUserIdAndProjectId(String userId, String projectId);
}
