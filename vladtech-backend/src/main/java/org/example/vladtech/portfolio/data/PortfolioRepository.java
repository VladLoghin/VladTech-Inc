package org.example.vladtech.portfolio.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends MongoRepository<PortfolioItem, String> {
    List<PortfolioItem> findByType(String type);

    @Query("{ $or: [ { 'archived': false }, { 'archived': { $exists: false } } ] }")
    List<PortfolioItem> findByArchivedFalse();

    @Query("{ 'type': ?0, $or: [ { 'archived': false }, { 'archived': { $exists: false } } ] }")
    List<PortfolioItem> findByTypeAndArchivedFalse(String type);

    @Query("{ 'archived': true }")
    List<PortfolioItem> findByArchivedTrue();
}

