package com.api.playpal.court.infrastructure;

import com.api.playpal.court.application.CourtService;
import com.api.playpal.court.domain.Court;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    private final CourtService courtService;
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllCourts(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String city
    ) {
        try {
            Page<Court> courts = courtService.findAll(page, sport, city);
            return ResponseEntity.ok().body(courts);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message",e.getMessage().toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourt(@PathVariable String id) {
        try {
            Court court = courtService.findById(id).orElseThrow(()-> new RuntimeException("Court not found"));
            return ResponseEntity.ok().body(court);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message",e.getMessage().toString()));
        }
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveCourt(
            @RequestParam("sports") String sports,
            @RequestParam("type") String type,
            @RequestParam("number") Integer number,
            @RequestParam("price") Long price,
            @RequestParam("description") String description,
            @RequestParam("hours") String hours,
            @RequestParam(required = false) MultipartFile img,
            @RequestParam("branch") String branchId
    ) {
        try {
            Court court = courtService.save(sports, type, number, price, description, hours, branchId, img);
            return ResponseEntity.ok().body(court);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage().toString());
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateCourt(
            @PathVariable String id,
            @RequestParam(required = false) String sports,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer number,
            @RequestParam(required = false) Long price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String hours,
            @RequestParam(required = false) MultipartFile img
    ) {
        try {
            return ResponseEntity.status(200).body(courtService.update(id, sports, type, number, price, description, hours, img));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message",e.getMessage().toString()));
        }
    }

    @DeleteMapping(value = "/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> deleteCourt(@RequestParam String id) {
        courtService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
