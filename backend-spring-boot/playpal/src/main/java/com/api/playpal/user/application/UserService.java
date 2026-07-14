package com.api.playpal.user.application;

import com.api.playpal.user.domain.User;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import com.api.playpal.utils.StorageService;
import org.bson.types.Binary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepositoryImp userRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public UserService(UserRepositoryImp userRepository, StorageService storageService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    public User save(User user, MultipartFile file) throws IOException {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        if (!validData(user)) {
            throw new RuntimeException("Invalid data");
        }
        if (file != null && !file.isEmpty()) {
            user.setThumbnail_url(storageService.saveImage(file));
        }
        return userRepository.save(user);
    }

    private boolean validData(User user) {
        if (!user.getRole().equals("user") && !user.getRole().equals("provider")) {
            throw new RuntimeException("Invalid data");
        }
        return true;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }


    public User update(String id, String username, String password, MultipartFile file) throws IOException {
        User user = findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (file != null && !file.isEmpty()) {
            storageService.deleteImage(user.getThumbnail_url());
            user.setThumbnail_url(storageService.saveImage(file));
        }
        return userRepository.save(user);
    }


    public boolean deleteById(String id) {
        userRepository.deleteById(id);
        return true;
    }
}
