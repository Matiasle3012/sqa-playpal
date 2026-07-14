package com.api.playpal.system;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class MongoSystemTestSupport {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    @DynamicPropertySource
    static void overrideMongoUri(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssuredPort() {
        RestAssured.port = port;
    }

    String registerAndLogin(String username, String email, String role) {
        given()
                .multiPart("username", username)
                .multiPart("email", email)
                .multiPart("password", "secret123")
                .multiPart("role", role)
                .when().post("/api/auth/register")
                .then().statusCode(200);

        return given()
                .multiPart("email", email)
                .multiPart("password", "secret123")
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");
    }

    String createBranch(String token, String name) {
        return given()
                .header("Authorization", "Bearer " + token)
                .queryParam("name", name)
                .queryParam("city", "Cordoba")
                .queryParam("street", "Calle 1")
                .when().post("/api/branches/")
                .then().statusCode(200)
                .extract().jsonPath().getString("id");
    }

    String createCourt(String token, String branchId, long pricePerHour) {
        return given()
                .header("Authorization", "Bearer " + token)
                .multiPart("sports", "futbol")
                .multiPart("type", "futbol 5")
                .multiPart("number", "1")
                .multiPart("price", String.valueOf(pricePerHour))
                .multiPart("description", "Cancha techada")
                .multiPart("hours", "08:00-22:00")
                .multiPart("branch", branchId)
                .when().post("/api/courts/")
                .then().statusCode(200)
                .extract().jsonPath().getString("id");
    }

    Response bookCourt(String token, String courtId, LocalDate date, int startHour, int endHour) {
        return given()
                .header("Authorization", "Bearer " + token)
                .multiPart("start", String.valueOf(startHour))
                .multiPart("end", String.valueOf(endHour))
                .multiPart("date", date.toString())
                .multiPart("court", courtId)
                .when().post("/api/reservations/");
    }
}
