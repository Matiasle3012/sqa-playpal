package com.api.playpal.branch.infrastructure;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.domain.BranchRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepositoryImp extends BranchRepository, MongoRepository<Branch, String> {
    @Override
    Optional<Branch> findById(String id);

    @Override
    Branch save(Branch branch);

    @Override
    List<Branch> findBranchesByProviderId(String providerId);

    @Override
    void deleteById(String id);
}
