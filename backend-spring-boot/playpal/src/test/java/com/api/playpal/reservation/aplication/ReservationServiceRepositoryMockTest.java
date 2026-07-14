package com.api.playpal.reservation.aplication;

import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.reservation.infrastructure.ReservationRepositoryImp;
import com.api.playpal.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
class ReservationServiceRepositoryMockTest {

    @Mock
    private ReservationRepositoryImp reservationRepository;

    @Mock
    private CourtRepositoryImp courtRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, courtRepository, 12);
    }

    private Court someCourt() {
        Court court = new Court("futbol 5", "futbol", 1, 1000L, "Cancha techada", "08:00-22:00");
        court.setId("court-1");
        return court;
    }

    private User someUser() {
        User user = new User("tester", "tester@mail.com", "secret", "user");
        user.setId("user-1");
        return user;
    }

    private Reservation someReservation() {
        return new Reservation("10", "12", 2000L, LocalDate.now().plusDays(30), someCourt(), someUser());
    }

    @Test
    void savePersistsReservationWithoutDatabase() {
        when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate date = LocalDate.now().plusDays(30);

        Reservation saved = reservationService.save("10", "12", date.toString(), "court-1", someUser());

        assertEquals(date, saved.getDate());
        assertEquals(2000L, saved.getTotalPrice());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void findByIdDelegatesToRepository() {
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(someReservation()));

        Optional<Reservation> found = reservationService.findById("res-1");

        assertTrue(found.isPresent());
        verify(reservationRepository).findById("res-1");
    }

    @Test
    void findByUserDelegatesToRepository() {
        when(reservationRepository.findReservationsByUser("user-1")).thenReturn(List.of(someReservation()));

        List<Reservation> reservations = reservationService.findByUser("user-1");

        assertEquals(1, reservations.size());
        verify(reservationRepository).findReservationsByUser("user-1");
    }

    @Test
    void findByCourtDelegatesToRepository() {
        when(reservationRepository.findReservationsByCourt("court-1")).thenReturn(List.of(someReservation()));

        List<Reservation> reservations = reservationService.findByCourt("court-1");

        assertEquals(1, reservations.size());
        verify(reservationRepository).findReservationsByCourt("court-1");
    }

    @Test
    void updateDeactivatesActiveReservation() {
        Reservation reservation = someReservation();
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation updated = reservationService.update("res-1", false);

        assertFalse(updated.isActive());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void updateKeepsReservationActiveWhenFlagIsTrue() {
        Reservation reservation = someReservation();
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation updated = reservationService.update("res-1", true);

        assertTrue(updated.isActive());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void deleteByIdRemovesInactiveReservation() {
        Reservation reservation = someReservation();
        reservation.setActive(false);
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));

        reservationService.deleteById("res-1");

        verify(reservationRepository).deleteById("res-1");
    }

    @Test
    void deleteByIdRejectsActiveFutureReservation() {
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(someReservation()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.deleteById("res-1"));

        assertEquals("Reservation is still active", exception.getMessage());
        verify(reservationRepository, never()).deleteById("res-1");
    }

    @Test
    void updateFailsWhenReservationDoesNotExist() {
        when(reservationRepository.findById("res-404")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.update("res-404", false));

        assertEquals("Reservation not founded", exception.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void deleteByIdFailsWhenReservationDoesNotExist() {
        when(reservationRepository.findById("res-404")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.deleteById("res-404"));

        assertEquals("Reservation not founded", exception.getMessage());
        verify(reservationRepository, never()).deleteById("res-404");
    }
}
