package com.api.playpal.reservation.aplication;

import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.reservation.infrastructure.ReservationRepositoryImp;
import com.api.playpal.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationRepositoryImp reservationRepository;
    private final CourtRepositoryImp courtRepository;
    private final long advanceHours;

    public ReservationService(ReservationRepositoryImp reservationRepository, CourtRepositoryImp courtRepository,
                              @Value("${reservation.advance-hours:12}") long advanceHours) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.advanceHours = advanceHours;
    }

    public List<Reservation> findByCourt(String courtId) {
        return reservationRepository.findReservationsByCourt(courtId);
    }

    public Optional<Reservation> findById(String id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findByUser(String userId) {
        return reservationRepository.findReservationsByUser(userId);
    }

    // TODO: Validar si ya existe una reserva a esa hora
    public Reservation save(String start, String end, String date, String courtid, User user) {
        Court court = courtRepository.findById(courtid).orElseThrow(()-> new RuntimeException("Court not founded"));
        try {
            LocalDate.parse(date);
            if (LocalDateTime.parse(date + "T" + String.format("%02d", Integer.parseInt(start))+":00").isBefore(LocalDateTime.now().plusHours(advanceHours))) {
                throw new RuntimeException("Invalid date (must be " + advanceHours + " hours before minimum)");
            }
            if (Integer.parseInt(end) <= Integer.parseInt(start)) {
                throw new RuntimeException("End must be greater than start");
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Please use YYYY-MM-DD.");
        }
        Reservation reservation = new Reservation(start, end, court.getPrice()*(Integer.parseInt(end)-Integer.parseInt(start)), LocalDate.parse(date), court, user);
        return reservationRepository.save(reservation);
    }

    public Reservation update(String id, boolean active) {
        Reservation reservation = findById(id).orElseThrow(()-> new RuntimeException("Reservation not founded"));
        if (reservation.isActive() && !active) {
            reservation.setActive(active);
        }
        return reservationRepository.save(reservation);
    }

    public void deleteById(String id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(()-> new RuntimeException("Reservation not founded"));
        if (!reservation.isActive() || LocalDate.now().isAfter(reservation.getDate())) {
            reservationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Reservation is still active");
        }

    }
}
