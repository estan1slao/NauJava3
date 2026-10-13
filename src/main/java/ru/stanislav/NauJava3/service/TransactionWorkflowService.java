package ru.stanislav.NauJava3.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.entity.Category;
import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.Wallet;
import ru.stanislav.NauJava3.entity.enums.TransactionType;
import ru.stanislav.NauJava3.repository.CategoryRepository;
import ru.stanislav.NauJava3.repository.FinancialTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TransactionWorkflowService {

    private final CategoryRepository categoryRepository;
    private final FinancialTransactionRepository transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionWorkflowService(
            CategoryRepository categoryRepository,
            FinancialTransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public FinancialTransaction createCategoryAndTransaction(
            AppUser user,
            Wallet wallet,
            String categoryName,
            BigDecimal amount,
            TransactionType type,
            String description) {

        Category category = new Category(
                user,
                categoryName,
                type,
                "Категория создана вместе с операцией",
                "#336699"
        );

        categoryRepository.save(category);

        FinancialTransaction transaction = new FinancialTransaction(
                user,
                wallet,
                category,
                amount,
                type,
                description,
                LocalDate.now()
        );

        FinancialTransaction saved = transactionRepository.save(transaction);

        entityManager.flush();

        return saved;
    }
}
