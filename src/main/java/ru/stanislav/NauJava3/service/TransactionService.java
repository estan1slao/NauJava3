package ru.stanislav.NauJava3.service;

import ru.stanislav.NauJava3.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    void createTransaction(
            Long id,
            String type,
            BigDecimal amount,
            String category,
            String description
    );

    Transaction findById(Long id);

    void updateTransaction(
            Long id,
            BigDecimal amount,
            String category,
            String description
    );

    void deleteById(Long id);

    List<Transaction> getAllTransactions();

    Map<String, BigDecimal> getExpensesByCategory();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpenses();

    BigDecimal getBalance();
}
