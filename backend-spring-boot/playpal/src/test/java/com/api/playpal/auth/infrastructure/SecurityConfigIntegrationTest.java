package com.api.playpal.auth.infrastructure;

import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.court.infrastructure.CourtRepositoryImp;
import com.api.playpal.reservation.infrastructure.ReservationRepositoryImp;
import com.api.playpal.user.domain.User;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepositoryImp userRepository;

    @MockitoBean
    private CourtRepositoryImp courtRepository;

    @MockitoBean
    private BranchRepositoryImp branchRepository;

    @MockitoBean
    private ReservationRepositoryImp reservationRepository;

    private User persistedUser(String id, String rawPassword) {
        User user = new User("tester", "tester@mail.com", passwordEncoder.encode(rawPassword), "user");
        user.setId(id);
        return user;
    }

    private String bearerTokenFor(String userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(persistedUser(userId, "secret123")));
        return "Bearer " + jwtUtil.generateToken(userId);
    }

    @Test
    void corsPreflightIsPermittedWithoutJwt() throws Exception {
        mockMvc.perform(options("/api/reservations/")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk());
    }

    @Test
    void loginIsPermittedWithoutJwt() throws Exception {
        when(userRepository.findByEmail("tester@mail.com"))
                .thenReturn(Optional.of(persistedUser("user-1", "secret123")));

        mockMvc.perform(multipart("/api/auth/login")
                        .param("email", "tester@mail.com")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registerIsPermittedWithoutJwt() throws Exception {
        when(userRepository.findByEmail("nuevo@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "nuevo")
                        .param("email", "nuevo@mail.com")
                        .param("password", "secret123")
                        .param("role", "user"))
                .andExpect(status().isOk());
    }

    @Test
    void publicCourtCatalogIsPermittedWithoutJwt() throws Exception {
        when(courtRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/courts/"))
                .andExpect(status().isOk());
    }

    @Test
    void staticImagesBypassAuthentication() throws Exception {
        mockMvc.perform(get("/images/missing.png"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/reservations/",
            "GET, /api/reservations/court/court-1",
            "GET, /api/branches/branch-1",
            "POST, /api/branches/",
            "GET, /api/users/user-1",
            "GET, /api/auth/current",
            "DELETE, /api/courts/"
    })
    void protectedEndpointsWithoutJwtAreRejected(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path))
                .andExpect(status().isForbidden());
    }

    @Test
    void reservationsWithValidJwtReturnOk() throws Exception {
        String token = bearerTokenFor("user-1");
        when(reservationRepository.findReservationsByUser("user-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/reservations/").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }

    @Test
    void currentUserWithValidJwtReturnsOk() throws Exception {
        String token = bearerTokenFor("user-1");

        mockMvc.perform(get("/api/auth/current").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"));
    }

    @Test
    void userProfileWithValidJwtReturnsOk() throws Exception {
        String token = bearerTokenFor("user-1");

        mockMvc.perform(get("/api/users/user-1").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }

    @Test
    void publicCourtCatalogIsAlsoAccessibleWithJwt() throws Exception {
        String token = bearerTokenFor("user-1");
        when(courtRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/courts/").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }
}
