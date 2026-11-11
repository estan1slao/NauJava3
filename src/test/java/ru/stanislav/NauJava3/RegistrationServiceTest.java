package ru.stanislav.NauJava3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.repository.AppUserRepository;
import ru.stanislav.NauJava3.service.RegistrationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(appUserRepository, passwordEncoder);
    }

    @Test
    void registerCreatesUserAndEncodesPassword() {
        when(appUserRepository.findByUsername("student")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strongPassword123")).thenReturn("encoded-password");

        registrationService.register("  student  ", "student@example.com", "Test Student", "strongPassword123");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();
        assertEquals("student", savedUser.getUsername());
        assertEquals("student@example.com", savedUser.getEmail());
        assertEquals("Test Student", savedUser.getFullName());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertEquals("USER", savedUser.getRole());
        verify(passwordEncoder).encode("strongPassword123");
    }

    @Test
    void registerRejectsBlankUsername() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register("   ", "student@example.com", "Test", "password123")
        );

        assertEquals("Укажите логин", error.getMessage());
        verifyNoInteractions(appUserRepository, passwordEncoder);
    }

    @Test
    void registerRejectsNullUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register(null, "student@example.com", "Test", "password123"));
        verifyNoInteractions(appUserRepository, passwordEncoder);
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("student", "student@example.com", "Test", "1234567"));
        verifyNoInteractions(appUserRepository, passwordEncoder);
    }

    @Test
    void registerRejectsNullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("student", "student@example.com", "Test", null));
        verifyNoInteractions(appUserRepository, passwordEncoder);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        AppUser existing = new AppUser("student", "old@example.com", "hash", "Existing User");
        when(appUserRepository.findByUsername("student")).thenReturn(Optional.of(existing));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register("student", "new@example.com", "New User", "password123")
        );

        assertEquals("Пользователь с таким логином уже существует", error.getMessage());
        verify(appUserRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}
