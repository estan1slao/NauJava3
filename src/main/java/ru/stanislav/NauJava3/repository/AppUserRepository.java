package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository
        extends CrudRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
