package com.api.playpal.court.infrastructure;

import com.api.playpal.court.domain.Court;
import com.api.playpal.court.domain.CourtRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourtRepositoryImp extends CourtRepository, MongoRepository<Court, String> {
    @Override
    Court save(Court court);

    @Override
    Optional<Court> findById(String id);

    @Override
    Page<Court> findAll(Pageable pageable);

    @Override
    void deleteById(String id);

    // filtro sport
    Page<Court> findBySportsContains(String sport, Pageable pageable);

    // filtro city
    Page<Court> findByBranchCity(String city, Pageable pageable);

    // both filters
    Page<Court> findBySportsContainsAndBranchCity(String sport, String city, Pageable pageable);
}

