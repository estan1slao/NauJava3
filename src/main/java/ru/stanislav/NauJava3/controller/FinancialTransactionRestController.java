package ru.stanislav.NauJava3.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.enums.TransactionType;
import ru.stanislav.NauJava3.repository.FinancialTransactionRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class FinancialTransactionRestController {

    private final FinancialTransactionRepository transactionRepository;

    public FinancialTransactionRestController(
            FinancialTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/by-type-and-amount")
    public List<FinancialTransaction> findByTypeAndAmountBetween(
            @RequestParam TransactionType type,
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {

        return transactionRepository.findByTypeAndAmountBetween(
                type,
                minAmount,
                maxAmount
        );
    }

    @GetMapping("/by-category-and-user")
    public List<FinancialTransaction> findByCategoryNameAndUsername(
            @RequestParam String categoryName,
            @RequestParam String username) {

        return transactionRepository.findByCategoryNameAndUsername(
                categoryName,
                username
        );
    }
}