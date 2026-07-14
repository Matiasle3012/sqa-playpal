package com.api.playpal.branch.infrastructure;

import com.api.playpal.branch.application.BranchService;
import com.api.playpal.user.domain.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable String id) {
        try {
            return ResponseEntity.ok().body(branchService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getBranchesByProviderId(@PathVariable String providerId) {
        try {
            return ResponseEntity.ok().body(branchService.findBranchesByProviderId(providerId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createBranch(
            @RequestParam String name,
            @RequestParam String city,
            @RequestParam String street,
            @RequestParam(required = false) MultipartFile img,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok().body(branchService.save(name, city, street, user, img));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateBranch(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MultipartFile img
    ) {
        try {
            return ResponseEntity.status(200).body(branchService.update(id, name, img));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping(value = "/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> deleteBranch(@RequestParam String id) {
        branchService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
