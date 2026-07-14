package com.api.playpal.user.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
    User save(User user);
    void deleteById(String id);
}
