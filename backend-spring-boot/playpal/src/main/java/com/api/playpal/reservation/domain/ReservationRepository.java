package com.api.playpal.reservation.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> findById(String id);
    Reservation save(Reservation branch);
    List<Reservation> findReservationsByUser(String userId);
    List<Reservation> findReservationsByCourt(String courtId);
    void deleteById(String id);
}
