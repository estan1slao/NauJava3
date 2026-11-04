package ru.stanislav.NauJava3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.stanislav.NauJava3.repository.FinancialTransactionRepository;

import ru.stanislav.NauJava3.entity.FinancialTransaction;

import java.util.List;
import java.util.stream.StreamSupport;

@Controller
public class FinancialTransactionPageController {

    private final FinancialTransactionRepository transactionRepository;

    public FinancialTransactionPageController(
            FinancialTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transactions-page")
    public String showTransactions(Model model) {
        List<FinancialTransaction> transactions =
                StreamSupport.stream(
                        transactionRepository.findAll().spliterator(),
                        false
                ).toList();

        model.addAttribute("transactions", transactions);

        return "transactions";
    }
}