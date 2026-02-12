package org.example.vladtech.estimates.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateSettingsRepository extends MongoRepository<EstimateSettings, String> {
}
