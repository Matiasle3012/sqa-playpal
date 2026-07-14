package com.api.playpal.reservation.aplication;

import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.reservation.infrastructure.ReservationRepositoryImp;
import com.api.playpal.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMcdcTest {

    private static final long ADVANCE_HOURS = 12;

    @Mock
    private ReservationRepositoryImp reservationRepository;

    @Mock
    private CourtRepositoryImp courtRepository;

    private ReservationService reservationService;
    private Court court;
    private User user;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, courtRepository, ADVANCE_HOURS);
        court = new Court("futbol 5", "futbol", 1, 1000L, "Cancha techada", "08:00-22:00");
        court.setId("court-1");
        user = new User("tester", "tester@mail.com", "secret", "user");
        user.setId("user-1");
    }

    @Test
    void allConditionsTrueCreatesReservation() {
        when(courtRepository.findById("court-1")).thenReturn(Optional.of(court));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime target = LocalDateTime.now().plusHours(48);

        Reservation reservation = reservationService.save(
                String.valueOf(target.getHour()),
                String.valueOf(target.getHour() + 2),
                target.toLocalDate().toString(),
                "court-1",
                user);

        assertEquals(target.toLocalDate(), reservation.getDate());
        assertEquals(2000L, reservation.getTotalPrice());
        assertTrue(reservation.isActive());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @ParameterizedTest
    @CsvSource({
            "false, 48, 2, Court not founded",
            "true, 8, 2, Invalid date (must be 12 hours before minimum)",
            "true, 48, 0, End must be greater than start"
    })
    void singleFalseConditionRejectsReservationWithSpecificMessage(
            boolean courtExists, long hoursAhead, int durationHours, String expectedMessage) {
        when(courtRepository.findById("court-1"))
                .thenReturn(courtExists ? Optional.of(court) : Optional.empty());
        LocalDateTime target = LocalDateTime.now().plusHours(hoursAhead);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> reservationService.save(
                String.valueOf(target.getHour()),
                String.valueOf(target.getHour() + durationHours),
                target.toLocalDate().toString(),
                "court-1",
                user));

        assertEquals(expectedMessage, exception.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
