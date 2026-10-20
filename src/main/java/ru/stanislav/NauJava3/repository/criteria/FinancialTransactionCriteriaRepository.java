package ru.stanislav.NauJava3.repository.criteria;

import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface FinancialTransactionCriteriaRepository {

    List<FinancialTransaction> findByTypeAndAmountBetweenCriteria(
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount
    );

    List<FinancialTransaction> findByCategoryNameAndUsernameCriteria(
            String categoryName,
            String username
    );
}
