package com.api.playpal.branch.domain;

import java.util.List;
import java.util.Optional;

public interface BranchRepository {
    Optional<Branch> findById(String id);
    Branch save(Branch branch);
    List<Branch> findBranchesByProviderId(String providerId);
    void deleteById(String id);
}
