package com.api.playpal.branch.infrastructure;

import com.api.playpal.branch.application.BranchService;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("nightly")
class BranchControllerFuzzingTest {

    private static final long SEED = 20260714L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @MockitoBean
    private UserRepositoryImp userRepository;

    static Stream<Arguments> fuzzedRequests() {
        Random random = new Random(SEED);
        List<String[]> endpoints = List.of(
                new String[]{"GET", "/api/branches/branch-1"},
                new String[]{"GET", "/api/branches/provider/prov-1"},
                new String[]{"POST", "/api/branches/"},
                new String[]{"PUT", "/api/branches/branch-1"},
                new String[]{"DELETE", "/api/branches/"});

        List<Arguments> cases = new ArrayList<>();
        for (String[] endpoint : endpoints) {
            String method = endpoint[0];
            String path = endpoint[1];
            cases.add(Arguments.of(method, path, "Authorization", "Bearer " + randomPrintable(random, 24), "token aleatorio"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer ", "token vacio"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer " + "A".repeat(16384), "token de 16KB"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9." + randomAlphanumeric(random, 12), "jwt truncado"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer " + randomJwtShape(random), "jwt con firma corrupta"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer " + randomUnicode(random, 16), "token con encoding invalido"));
            cases.add(Arguments.of(method, path, "Authorization", "bearer " + randomPrintable(random, 24), "esquema en minusculas"));
            cases.add(Arguments.of(method, path, "Authorization", "Basic " + Base64.getEncoder().encodeToString(randomBytes(random, 18)), "esquema basic"));
            cases.add(Arguments.of(method, path, "Authorization", "", "authorization vacio"));
            cases.add(Arguments.of(method, path, "Authorization", "Bearer ..%%$$" + randomPrintable(random, 8) + "..", "token con caracteres corruptos"));
            cases.add(Arguments.of(method, path, "X-Fuzz-" + randomAlphanumeric(random, 6), randomPrintable(random, 8192), "header arbitrario de 8KB sin credencial"));
            cases.add(Arguments.of(method, path, "Content-Type", "application/" + randomAlphanumeric(random, 12), "content-type invalido sin credencial"));
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "[{index}] {4}: {0} {1}")
    @MethodSource("fuzzedRequests")
    void malformedHeadersAreRejectedBeforeReachingBranchServiceAndNeverCause500(
            String method, String path, String headerName, String headerValue, String mutation) throws Exception {
        MvcResult result = mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header(headerName, headerValue))
                .andReturn();

        int status = result.getResponse().getStatus();

        assertThat(status).as("mutacion [%s] sobre %s %s", mutation, method, path).isIn(400, 401, 403);
        verifyNoInteractions(branchService);
    }

    private static String randomPrintable(Random random, int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&'*+-.^_`|~";
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return value.toString();
    }

    private static String randomAlphanumeric(Random random, int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return value.toString();
    }

    private static String randomJwtShape(Random random) {
        return randomAlphanumeric(random, 20) + "." + randomAlphanumeric(random, 40) + "." + randomAlphanumeric(random, 43);
    }

    private static String randomUnicode(Random random, int length) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append((char) (0x00A1 + random.nextInt(0x0200)));
        }
        return value.toString();
    }

    private static byte[] randomBytes(Random random, int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}
