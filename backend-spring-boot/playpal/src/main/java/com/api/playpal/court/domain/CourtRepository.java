package com.api.playpal.court.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CourtRepository {
    Court save(Court court);
    Optional<Court> findById(String id);
    Page<Court> findAll(Pageable pageable);
}
