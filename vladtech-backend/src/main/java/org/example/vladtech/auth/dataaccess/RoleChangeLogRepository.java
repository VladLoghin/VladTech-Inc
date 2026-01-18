package org.example.vladtech.auth.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleChangeLogRepository extends MongoRepository<RoleChangeLog, String> {

    List<RoleChangeLog> findAllByOrderByPerformedAtDesc();

    Page<RoleChangeLog> findAllByOrderByPerformedAtDesc(Pageable pageable);

    List<RoleChangeLog> findByUserIdOrderByPerformedAtDesc(String userId);
}
