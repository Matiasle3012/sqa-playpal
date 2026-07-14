package com.api.playpal.user.infrastructure;

import com.api.playpal.branch.domain.Branch;
import com.api.playpal.branch.infrastructure.BranchRepositoryImp;
import com.api.playpal.user.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserDtoIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepositoryImp userRepository;

    @MockitoBean
    private BranchRepositoryImp branchRepository;

    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    @BeforeEach
    void stubInMemoryPersistence() {
        userStore.clear();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId("user-" + UUID.randomUUID());
            }
            userStore.put(user.getId(), user);
            return user;
        });
        when(userRepository.findByEmail(anyString())).thenAnswer(invocation ->
                userStore.values().stream()
                        .filter(user -> user.getEmail().equals(invocation.getArgument(0)))
                        .findFirst());
        when(userRepository.findById(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(userStore.get(invocation.<String>getArgument(0))));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> {
            Branch branch = invocation.getArgument(0);
            if (branch.getId() == null) {
                branch.setId("branch-" + UUID.randomUUID());
            }
            return branch;
        });
    }

    @Test
    void bothRolesAreServedByTheSameUserJsonSchema() throws Exception {
        register("ana", "ana@mail.com", "user");
        register("prov", "prov@mail.com", "provider");
        String userToken = login("ana@mail.com");
        String providerToken = login("prov@mail.com");
        createBranch(providerToken, "Sede Centro");

        JsonNode userJson = fetchOwnProfile(userToken);
        JsonNode providerJson = fetchOwnProfile(providerToken);

        assertThat(fieldNames(providerJson)).isEqualTo(fieldNames(userJson));

        User plainUser = objectMapper.treeToValue(userJson, User.class);
        User provider = objectMapper.treeToValue(providerJson, User.class);

        assertThat(plainUser).isInstanceOf(User.class);
        assertThat(provider).isInstanceOf(User.class);
        assertThat(plainUser.getRole()).isEqualTo("user");
        assertThat(provider.getRole()).isEqualTo("provider");
        assertThat(plainUser.getBranches()).isEmpty();
        assertThat(provider.getBranches()).hasSize(1);
        assertThat(provider.getBranches().get(0).getName()).isEqualTo("Sede Centro");
    }

    private void register(String username, String email, String role) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("email", email);
        form.add("password", "secret123");
        form.add("role", role);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", multipartEntity(form), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private String login(String email) throws Exception {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("email", email);
        form.add("password", "secret123");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", multipartEntity(form), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private void createBranch(String token, String name) {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/branches/?name=" + name + "&city=Cordoba&street=Calle 1",
                HttpMethod.POST,
                new HttpEntity<>(null, bearerHeaders(token)),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private JsonNode fetchOwnProfile(String token) throws Exception {
        ResponseEntity<String> current = restTemplate.exchange(
                "/api/auth/current", HttpMethod.GET, new HttpEntity<>(null, bearerHeaders(token)), String.class);
        assertThat(current.getStatusCode().value()).isEqualTo(200);
        String userId = objectMapper.readTree(current.getBody()).get("id").asText();

        ResponseEntity<String> profile = restTemplate.exchange(
                "/api/users/" + userId, HttpMethod.GET, new HttpEntity<>(null, bearerHeaders(token)), String.class);
        assertThat(profile.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(profile.getBody());
    }

    private HttpEntity<MultiValueMap<String, Object>> multipartEntity(MultiValueMap<String, Object> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(form, headers);
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new TreeSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
