package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.Wallet;

public interface WalletRepository
        extends CrudRepository<Wallet, Long> {
}
