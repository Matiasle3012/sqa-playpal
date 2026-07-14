package com.api.playpal.reservation.aplication;

import com.api.playpal.court.domain.Court;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.reservation.domain.Reservation;
import com.api.playpal.reservation.infrastructure.ReservationRepositoryImp;
import com.api.playpal.user.domain.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceAdvanceHoursTest {

    private static Court someCourt() {
        Court court = new Court("futbol 5", "futbol", 1, 1000L, "Cancha techada", "08:00-22:00");
        court.setId("court-1");
        return court;
    }

    private static User someUser() {
        User user = new User("tester", "tester@mail.com", "secret", "user");
        user.setId("user-1");
        return user;
    }

    private static Reservation saveReservationHoursAhead(ReservationService reservationService, long hoursAhead) {
        LocalDateTime target = LocalDateTime.now().plusHours(hoursAhead);
        return reservationService.save(
                String.valueOf(target.getHour()),
                String.valueOf(target.getHour() + 2),
                target.toLocalDate().toString(),
                "court-1",
                someUser());
    }

    @Nested
    @SpringBootTest
    class TwelveHourThresholdFromApplicationProperties {

        @Autowired
        private ReservationService reservationService;

        @MockitoBean
        private ReservationRepositoryImp reservationRepository;

        @MockitoBean
        private CourtRepositoryImp courtRepository;

        @Test
        void acceptsReservationFourteenHoursAhead() {
            when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Reservation reservation = saveReservationHoursAhead(reservationService, 14);

            assertNotNull(reservation);
            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        void rejectsReservationEightHoursAhead() {
            when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> saveReservationHoursAhead(reservationService, 8));

            assertEquals("Invalid date (must be 12 hours before minimum)", exception.getMessage());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "reservation.advance-hours=6")
    class SixHourThresholdFromTestPropertySource {

        @Autowired
        private ReservationService reservationService;

        @MockitoBean
        private ReservationRepositoryImp reservationRepository;

        @MockitoBean
        private CourtRepositoryImp courtRepository;

        @Test
        void acceptsReservationEightHoursAhead() {
            when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Reservation reservation = saveReservationHoursAhead(reservationService, 8);

            assertNotNull(reservation);
            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        void rejectsReservationThreeHoursAhead() {
            when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> saveReservationHoursAhead(reservationService, 3));

            assertEquals("Invalid date (must be 6 hours before minimum)", exception.getMessage());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    @SpringBootTest
    class SixHourThresholdFromDynamicPropertySource {

        @DynamicPropertySource
        static void reservationAdvanceHours(DynamicPropertyRegistry registry) {
            registry.add("reservation.advance-hours", () -> "6");
        }

        @Autowired
        private ReservationService reservationService;

        @MockitoBean
        private ReservationRepositoryImp reservationRepository;

        @MockitoBean
        private CourtRepositoryImp courtRepository;

        @Test
        void acceptsReservationEightHoursAheadWithRuntimeConfiguredThreshold() {
            when(courtRepository.findById("court-1")).thenReturn(Optional.of(someCourt()));
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Reservation reservation = saveReservationHoursAhead(reservationService, 8);

            assertNotNull(reservation);
            verify(reservationRepository).save(any(Reservation.class));
        }
    }
}
