package ru.stanislav.NauJava3.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.stanislav.NauJava3.config.AppConfig;
import ru.stanislav.NauJava3.dao.TransactionRepository;
import ru.stanislav.NauJava3.entity.Transaction;
import ru.stanislav.NauJava3.entity.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppConfig appConfig;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AppConfig appConfig
    ) {
        this.transactionRepository = transactionRepository;
        this.appConfig = appConfig;
    }

    @Override
    public void createTransaction(
            Long id,
            String type,
            BigDecimal amount,
            String category,
            String description
    ) {
        validateAmount(amount);

        Transaction transaction = new Transaction();

        transaction.setId(id);
        transaction.setType(parseTransactionType(type));
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setDescription(description);

        transactionRepository.create(transaction);
    }

    @Override
    public Transaction findById(Long id) {
        return transactionRepository.read(id);
    }

    @Override
    public void updateTransaction(
            Long id,
            BigDecimal amount,
            String category,
            String description
    ) {
        validateAmount(amount);

        Transaction transaction = transactionRepository.read(id);

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Операция с ID " + id + " не найдена."
            );
        }

        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setDescription(description);

        transactionRepository.update(transaction);
    }

    @Override
    public void deleteById(Long id) {
        transactionRepository.delete(id);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAll();
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategory() {
        return transactionRepository.getAll()
                .stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.EXPENSE
                )
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    @Override
    public BigDecimal getTotalIncome() {
        return transactionRepository.getAll()
                .stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.INCOME
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalExpenses() {
        return transactionRepository.getAll()
                .stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.EXPENSE
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    private TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Тип операции должен быть INCOME или EXPENSE."
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Сумма должна быть больше нуля."
            );
        }
    }

    @PostConstruct
    public void init() {
        System.out.println();
        System.out.println("======================================");
        System.out.println("Сервис приложения инициализирован.");
        System.out.println("Приложение: " + appConfig.getAppName());
        System.out.println("Версия: " + appConfig.getAppVersion());
        System.out.println("======================================");
        System.out.println();
    }
}
