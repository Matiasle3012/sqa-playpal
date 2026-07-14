package com.api.playpal.user.application;

import com.api.playpal.user.domain.User;
import com.api.playpal.user.infrastructure.UserRepositoryImp;
import com.api.playpal.utils.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
class UserServiceRepositoryMockTest {

    @Mock
    private UserRepositoryImp userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserService userService;

    @Test
    void savePersistsNewUserWithoutDatabase() throws IOException {
        User user = new User("ana", "ana@mail.com", "secret", "user");
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.save(user, null);

        assertEquals("ana@mail.com", saved.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void saveRejectsDuplicatedEmail() {
        User existing = new User("ana", "ana@mail.com", "secret", "user");
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.save(new User("otra", "ana@mail.com", "secret", "user"), null));

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByIdDelegatesToRepository() {
        User user = new User("ana", "ana@mail.com", "secret", "user");
        user.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findById("user-1");

        assertTrue(found.isPresent());
        assertEquals("user-1", found.get().getId());
        verify(userRepository).findById("user-1");
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        assertTrue(userService.deleteById("user-1"));
        verify(userRepository).deleteById("user-1");
    }
}
