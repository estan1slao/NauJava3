package ru.stanislav.NauJava3.repository.criteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import ru.stanislav.NauJava3.entity.Category;
import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class FinancialTransactionCriteriaRepositoryImpl
        implements FinancialTransactionCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<FinancialTransaction> findByTypeAndAmountBetweenCriteria(
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FinancialTransaction> query =
                cb.createQuery(FinancialTransaction.class);

        Root<FinancialTransaction> transaction =
                query.from(FinancialTransaction.class);

        Predicate typePredicate =
                cb.equal(transaction.get("type"), type);

        Predicate amountPredicate = cb.between(
                transaction.get("amount"), minAmount, maxAmount
        );

        query.select(transaction).where(
                cb.and(typePredicate, amountPredicate)
        );

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<FinancialTransaction> findByCategoryNameAndUsernameCriteria(
            String categoryName,
            String username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FinancialTransaction> query =
                cb.createQuery(FinancialTransaction.class);

        Root<FinancialTransaction> transaction =
                query.from(FinancialTransaction.class);

        Join<FinancialTransaction, Category> category =
                transaction.join("category");

        Join<FinancialTransaction, AppUser> user =
                transaction.join("user");

        Predicate categoryPredicate =
                cb.equal(category.get("name"), categoryName);

        Predicate usernamePredicate =
                cb.equal(user.get("username"), username);

        query.select(transaction).where(
                cb.and(categoryPredicate, usernamePredicate)
        );

        return entityManager.createQuery(query).getResultList();
    }
}
