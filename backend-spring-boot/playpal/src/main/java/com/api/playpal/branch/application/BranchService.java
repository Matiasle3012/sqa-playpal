package com.api.playpal.branch.application;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.user.domain.User;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import com.api.playpal.utils.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BranchService {
    private final BranchRepositoryImp branchRepository;
    private final UserRepositoryImp userRepository;
    private final StorageService storageService;

    public BranchService(BranchRepositoryImp branchRepository, UserRepositoryImp userRepository, StorageService storageService) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    public Optional<Branch> findById(String id) {
        return branchRepository.findById(id);
    }

    public List<Branch> findBranchesByProviderId(String providerId) {
        return branchRepository.findBranchesByProviderId(providerId);
    }

    public Branch save(String name, String city, String street, User user, MultipartFile file) throws Exception {
        if (user.getRole().equals("user")) {
            throw new Exception("User is not a provider");
        }
        Branch branch = new Branch(name, city, street, user);
        if (file != null && !file.isEmpty()) {
            branch.setThumbnail_url(storageService.saveImage(file));
        }
        branchRepository.save(branch);
        user.getBranches().add(branch);
        userRepository.save(user);
        return branch;
    }

    public Branch update(String id, String name, MultipartFile file) throws IOException {
        Branch branch = branchRepository.findById(id).orElseThrow(()-> new RuntimeException("Branch not found"));
        if (name != null && !name.isEmpty()) {
            branch.setName(name);
        }
        if (file != null && !file.isEmpty()) {
            storageService.deleteImage(branch.getThumbnail_url());
            branch.setThumbnail_url(storageService.saveImage(file));
        }
        return branchRepository.save(branch);
    }

    public void deleteById(String id) {
        branchRepository.deleteById(id);
    }
}