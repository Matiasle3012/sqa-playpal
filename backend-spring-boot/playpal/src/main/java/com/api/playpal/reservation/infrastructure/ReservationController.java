package com.api.playpal.reservation.infrastructure;

import com.api.playpal.reservation.aplication.ReservationService;
import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.user.domain.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable String id) {
        Optional<Reservation> reservation = reservationService.findById(id);
        return ResponseEntity.ok(reservation);
    }


    @GetMapping("/")
    public ResponseEntity<?> getReservationsByUser() {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Reservation> reservations = reservationService.findByUser(user.getId());
            return ResponseEntity.ok(reservations);
        } catch (Exception e ) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/court/{courtId}")
    public ResponseEntity<?> getReservationByCourt(@PathVariable String courtId) {
        try {
            return ResponseEntity.ok(reservationService.findByCourt(courtId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReservation(
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam("date") String date,
            @RequestParam("court") String courtId
            ) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok().body(reservationService.save(start, end, date, courtId, user));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateReservation(
        @PathVariable String id,
        @RequestParam(required = false) boolean active
    ) {
        try {
            return ResponseEntity.ok().body(reservationService.update(id, active));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping(value = "/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> deleteReservation(@RequestParam String id) {
        try {
            reservationService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}
