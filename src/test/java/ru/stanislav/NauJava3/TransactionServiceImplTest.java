package ru.stanislav.NauJava3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.stanislav.NauJava3.config.AppConfig;
import ru.stanislav.NauJava3.dao.TransactionRepository;
import ru.stanislav.NauJava3.entity.Transaction;
import ru.stanislav.NauJava3.entity.TransactionType;
import ru.stanislav.NauJava3.service.TransactionServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AppConfig appConfig;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, appConfig);
    }

    @Test
    void createTransactionSavesValidIncome() {
        transactionService.createTransaction(1L, "income", new BigDecimal("1500.00"), "Зарплата", "Доход");

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).create(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(Long.valueOf(1), saved.getId());
        assertEquals(TransactionType.INCOME, saved.getType());
        assertEquals(new BigDecimal("1500.00"), saved.getAmount());
        assertEquals("Зарплата", saved.getCategory());
    }

    @Test
    void createTransactionRejectsZeroAmount() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, "EXPENSE", BigDecimal.ZERO, "Еда", "Покупка"));

        assertEquals("Сумма должна быть больше нуля.", error.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void createTransactionRejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, "EXPENSE", new BigDecimal("-100"), "Еда", "Покупка"));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void createTransactionRejectsNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, "EXPENSE", null, "Еда", "Покупка"));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void createTransactionRejectsUnknownType() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, "TRANSFER", new BigDecimal("100"), "Еда", "Покупка"));

        assertEquals("Тип операции должен быть INCOME или EXPENSE.", error.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void updateTransactionRejectsUnknownId() {
        when(transactionRepository.read(99L)).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> transactionService.updateTransaction(99L, new BigDecimal("100"), "Еда", "Обед"));

        assertEquals("Операция с ID 99 не найдена.", error.getMessage());
        verify(transactionRepository, never()).update(any());
    }

    @Test
    void updateTransactionSavesChanges() {
        Transaction existing = transaction(3L, TransactionType.EXPENSE, "200.00", "Еда");
        when(transactionRepository.read(3L)).thenReturn(existing);

        transactionService.updateTransaction(3L, new BigDecimal("250.00"), "Транспорт", "Такси");

        verify(transactionRepository).update(existing);
        assertEquals(new BigDecimal("250.00"), existing.getAmount());
        assertEquals("Транспорт", existing.getCategory());
        assertEquals("Такси", existing.getDescription());
    }

    @Test
    void balanceSubtractsExpensesFromIncome() {
        when(transactionRepository.getAll()).thenReturn(List.of(
                transaction(1L, TransactionType.INCOME, "2000.00", "Зарплата"),
                transaction(2L, TransactionType.EXPENSE, "300.00", "Еда"),
                transaction(3L, TransactionType.EXPENSE, "50.00", "Транспорт")
        ));

        assertEquals(new BigDecimal("1650.00"), transactionService.getBalance());
    }

    @Test
    void expensesByCategoryGroupsOnlyExpenses() {
        when(transactionRepository.getAll()).thenReturn(List.of(
                transaction(1L, TransactionType.EXPENSE, "100.00", "Еда"),
                transaction(2L, TransactionType.EXPENSE, "50.00", "Еда"),
                transaction(3L, TransactionType.INCOME, "800.00", "Еда")
        ));

        Map<String, BigDecimal> expenses = transactionService.getExpensesByCategory();

        assertEquals(1, expenses.size());
        assertEquals(new BigDecimal("150.00"), expenses.get("Еда"));
    }

    @Test
    void deleteTransactionDelegatesToRepository() {
        transactionService.deleteById(10L);
        verify(transactionRepository).delete(10L);
    }

    private Transaction transaction(Long id, TransactionType type, String amount, String category) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCategory(category);
        return transaction;
    }
}
