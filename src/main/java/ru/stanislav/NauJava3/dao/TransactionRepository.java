package ru.stanislav.NauJava3.dao;

import org.springframework.stereotype.Component;
import ru.stanislav.NauJava3.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionRepository implements CrudRepository<Transaction, Long> {

    private final List<Transaction> transactionContainer;

    public TransactionRepository(List<Transaction> transactionContainer) {
        this.transactionContainer = transactionContainer;
    }

    @Override
    public void create(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Операция не может быть null.");
        }

        if (transaction.getId() == null) {
            throw new IllegalArgumentException("ID операции не может быть null.");
        }

        if (transactionContainer.stream()
                .anyMatch(existing -> existing.getId().equals(transaction.getId()))) {
            throw new IllegalArgumentException(
                    "Операция с ID " + transaction.getId() + " уже существует."
            );
        }

        transactionContainer.add(transaction);
    }

    @Override
    public Transaction read(Long id) {
        return transactionContainer.stream()
                .filter(transaction -> transaction.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Операция не может быть null.");
        }

        for (int i = 0; i < transactionContainer.size(); i++) {
            if (transactionContainer.get(i).getId().equals(transaction.getId())) {
                transactionContainer.set(i, transaction);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Операция с ID " + transaction.getId() + " не найдена."
        );
    }

    @Override
    public void delete(Long id) {
        boolean removed = transactionContainer.removeIf(
                transaction -> transaction.getId().equals(id)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                    "Операция с ID " + id + " не найдена."
            );
        }
    }

    @Override
    public List<Transaction> getAll() {
        return new ArrayList<>(transactionContainer);
    }
}