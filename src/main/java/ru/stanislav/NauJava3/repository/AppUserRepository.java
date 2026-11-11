package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.AppUser;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "users")
public interface AppUserRepository
        extends CrudRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
