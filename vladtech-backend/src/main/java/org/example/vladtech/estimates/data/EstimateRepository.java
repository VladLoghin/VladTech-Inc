package org.example.vladtech.estimates.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateRepository extends MongoRepository<Estimate, String> {
	java.util.List<Estimate> findByOwnerAuth0Id(String ownerAuth0Id);

	java.util.Optional<Estimate> findByEstimateIdAndOwnerAuth0Id(String estimateId, String ownerAuth0Id);
}
