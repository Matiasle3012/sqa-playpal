package com.api.playpal.user.infrastructure;

import com.api.playpal.user.domain.User;
import com.api.playpal.user.domain.UserRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepositoryImp extends UserRepository, MongoRepository<User, String> {
    @Override
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findById(String id);

    @Override
    User save(User user);

    @Override
    void deleteById(String id);
}
