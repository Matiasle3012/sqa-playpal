package com.api.playpal.system;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleBookingRegressionTest extends MongoSystemTestSupport {

    @Test
    void whenOverlappingBooking_currentlySucceeds_butShouldFail() {
        String providerToken = registerAndLogin("prov-solapado", "prov.solapado@mail.com", "provider");
        String branchId = createBranch(providerToken, "Sede Doble Reserva");
        String courtId = createCourt(providerToken, branchId, 1000);
        String firstUserToken = registerAndLogin("user-primero", "user.primero@mail.com", "user");
        String secondUserToken = registerAndLogin("user-segundo", "user.segundo@mail.com", "user");
        LocalDate date = LocalDate.now().plusDays(3);

        int firstBookingStatus = bookCourt(firstUserToken, courtId, date, 18, 20).statusCode();
        int secondBookingStatus = bookCourt(secondUserToken, courtId, date, 18, 20).statusCode();

        assertEquals(200, firstBookingStatus);
        assertEquals(200, secondBookingStatus);

        given()
                .header("Authorization", "Bearer " + firstUserToken)
                .when().get("/api/reservations/court/" + courtId)
                .then().statusCode(200)
                .body("size()", equalTo(2));
    }
}
