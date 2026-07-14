package com.api.playpal.reservation.infrastructure;

import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.reservation.domain.ReservationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryImp extends ReservationRepository, MongoRepository<Reservation, String> {
    @Override
    Optional<Reservation> findById(String id);

    @Override
    Reservation save(Reservation branch);

    @Override
    List<Reservation> findReservationsByUser(String userid);

    @Override
    @Query("{ 'court.id': ?0 }")
    List<Reservation> findReservationsByCourt(String courtId);

    @Override
    void deleteById(String id);
}
