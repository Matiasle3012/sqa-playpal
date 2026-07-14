package com.api.playpal.system;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

class ReservationFullFlowSystemTest extends MongoSystemTestSupport {

    @Test
    @Timeout(15)
    void fullReservationFlowFromProviderSetupToPriceVerification() {
        String providerToken = registerAndLogin("prov-flujo", "prov.flujo@mail.com", "provider");
        String branchId = createBranch(providerToken, "Sede Flujo Completo");
        String courtId = createCourt(providerToken, branchId, 1500);

        given()
                .when().get("/api/courts/")
                .then().statusCode(200)
                .body("content.id", hasItem(courtId));

        String userToken = registerAndLogin("user-flujo", "user.flujo@mail.com", "user");
        LocalDate date = LocalDate.now().plusDays(2);

        Response booking = bookCourt(userToken, courtId, date, 10, 13);
        booking.then().statusCode(200).body("id", notNullValue());
        String reservationId = booking.jsonPath().getString("id");

        given()
                .header("Authorization", "Bearer " + userToken)
                .when().get("/api/reservations/" + reservationId)
                .then().statusCode(200)
                .body("id", equalTo(reservationId))
                .body("totalPrice", equalTo(4500))
                .body("date", equalTo(date.toString()))
                .body("active", equalTo(true));
    }
}
