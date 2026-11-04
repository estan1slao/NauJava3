package ru.stanislav.NauJava3.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.repository.AppUserRepository;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(
            String username,
            String email,
            String fullName,
            String rawPassword) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Укажите логин");
        }

        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать не менее 8 символов"
            );
        }

        if (appUserRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException(
                    "Пользователь с таким логином уже существует"
            );
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(
                username.trim(),
                email,
                encodedPassword,
                fullName
        );

        user.setRole("USER");

        appUserRepository.save(user);
    }
}