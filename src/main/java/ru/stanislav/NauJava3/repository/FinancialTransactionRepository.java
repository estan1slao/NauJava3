package ru.stanislav.NauJava3.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.enums.TransactionType;
import ru.stanislav.NauJava3.repository.criteria.FinancialTransactionCriteriaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface FinancialTransactionRepository
        extends CrudRepository<FinancialTransaction, Long>,
        FinancialTransactionCriteriaRepository {

    List<FinancialTransaction> findByTypeAndAmountBetween(
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount
    );

    @Query("""
        SELECT t
        FROM FinancialTransaction t
        JOIN t.category c
        JOIN t.user u
        WHERE c.name = :categoryName
          AND u.username = :username
        """)
    List<FinancialTransaction> findByCategoryNameAndUsername(
            @Param("categoryName") String categoryName,
            @Param("username") String username
    );
}
