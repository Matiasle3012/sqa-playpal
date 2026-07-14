package com.api.playpal.court.application;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.utils.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
public class CourtService {

    private final CourtRepositoryImp courtRepository;
    private final BranchRepositoryImp branchRepository;
    private final StorageService storageService;

    public CourtService(CourtRepositoryImp courtRepository, BranchRepositoryImp branchRepository, StorageService storageService) {
        this.courtRepository = courtRepository;
        this.branchRepository = branchRepository;
        this.storageService = storageService;
    }

    public Optional<Court> findById(String id) {
        return courtRepository.findById(id);
    }

    public Page<Court> findAll(int page, String sport, String city) {
        Pageable pageable = PageRequest.of(page, 10);

        if (sport != null && !sport.isEmpty() && city != null && !city.isEmpty()) {
            return courtRepository.findBySportsContainsAndBranchCity(sport, city, pageable);
        }
        else if (sport != null && !sport.isEmpty()) {
            return courtRepository.findBySportsContains(sport, pageable);
        }
        else if (city != null && !city.isEmpty()) {
            return courtRepository.findByBranchCity(city, pageable);
        }
        return courtRepository.findAll(pageable);
    }

    public Court save(String sports, String type, Integer number, Long price, String description, String hours, String branchId, MultipartFile file) throws IOException {
        Branch branch = branchRepository.findById(branchId).orElseThrow(()-> new RuntimeException("Branch not found"));

        Court court = new Court(type, sports, number, price, description, hours);
        if (file != null && !file.isEmpty()) {
            court.setThumbnail_url(storageService.saveImage(file));
        }
        court.setBranch(branch);
        Court savedCourt = courtRepository.save(court);
        branch.getCourts().add(savedCourt);
        branchRepository.save(branch);
        return courtRepository.save(court);
    }

    public Court update(String id, String sports, String type, Integer number, Long price, String description, String hours, MultipartFile file) throws IOException {
        Court court = findById(id).orElseThrow(()-> new RuntimeException("Court not found"));
        if (sports != null && !sports.isEmpty()) {
            court.setSports(sports);
        }
        if (type != null && !type.isEmpty()) {
            court.setType(type);
        }
        if (number != null && !number.toString().isEmpty()) {
            court.setNumber(number);
        }
        if (price != null && !price.toString().isEmpty()) {
            court.setPrice(price);
        }
        if (description != null && !description.isEmpty()) {
            court.setDescription(description);
        }
        if (hours != null && !hours.isEmpty()) {
            court.setHours(hours);
        }
        if (file != null && !file.isEmpty()) {
            storageService.deleteImage(court.getThumbnail_url());
            court.setThumbnail_url(storageService.saveImage(file));
        }
        return courtRepository.save(court);
    }

    public void deleteById(String id) {
        courtRepository.deleteById(id);
    }
}
