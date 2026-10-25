
package ru.stanislav.NauJava3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.entity.Category;
import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.Wallet;
import ru.stanislav.NauJava3.entity.enums.TransactionType;
import ru.stanislav.NauJava3.entity.enums.WalletType;
import ru.stanislav.NauJava3.repository.AppUserRepository;
import ru.stanislav.NauJava3.repository.BudgetRepository;
import ru.stanislav.NauJava3.repository.CategoryRepository;
import ru.stanislav.NauJava3.repository.FinancialTransactionRepository;
import ru.stanislav.NauJava3.repository.WalletRepository;
import ru.stanislav.NauJava3.service.TransactionWorkflowService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "app.test-mode=true")
class FinancialTransactionIntegrationTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Autowired
    private TransactionWorkflowService workflowService;

    private AppUser user;
    private Wallet wallet;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        walletRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                new AppUser(
                        "testuser",
                        "testuser@example.com",
                        "test-hash",
                        "Test User"
                )
        );

        wallet = walletRepository.save(
                new Wallet(
                        user,
                        "Основной кошелёк",
                        "RUB",
                        WalletType.CARD,
                        "Тестовый кошелёк",
                        new BigDecimal("10000.00")
                )
        );

        foodCategory = categoryRepository.save(
                new Category(
                        user,
                        "Еда",
                        TransactionType.EXPENSE,
                        "Продукты и питание",
                        "#336699"
                )
        );

        transactionRepository.save(
                new FinancialTransaction(
                        user, wallet, foodCategory,
                        new BigDecimal("500.00"),
                        TransactionType.EXPENSE,
                        "Покупка продуктов",
                        LocalDate.now()
                )
        );

        transactionRepository.save(
                new FinancialTransaction(
                        user, wallet, foodCategory,
                        new BigDecimal("1500.00"),
                        TransactionType.EXPENSE,
                        "Покупка для дома",
                        LocalDate.now()
                )
        );

        transactionRepository.save(
                new FinancialTransaction(
                        user, wallet, foodCategory,
                        new BigDecimal("2000.00"),
                        TransactionType.INCOME,
                        "Доход",
                        LocalDate.now()
                )
        );
    }

    @Test
    void derivedQueryFindsExpensesInAmountRange() {
        List<FinancialTransaction> result =
                transactionRepository.findByTypeAndAmountBetween(
                        TransactionType.EXPENSE,
                        new BigDecimal("100.00"),
                        new BigDecimal("1000.00")
                );

        assertEquals(1, result.size());
        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).getAmount()
        );
    }

    @Test
    void jpqlFindsTransactionsByCategoryAndUsername() {
        List<FinancialTransaction> result =
                transactionRepository.findByCategoryNameAndUsername(
                        "Еда", "testuser"
                );

        assertEquals(3, result.size());
    }

    @Test
    void criteriaFindsExpensesInAmountRange() {
        List<FinancialTransaction> result =
                transactionRepository.findByTypeAndAmountBetweenCriteria(
                        TransactionType.EXPENSE,
                        new BigDecimal("100.00"),
                        new BigDecimal("1000.00")
                );

        assertEquals(1, result.size());
        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).getAmount()
        );
    }

    @Test
    void criteriaFindsTransactionsByCategoryAndUsername() {
        List<FinancialTransaction> result =
                transactionRepository
                        .findByCategoryNameAndUsernameCriteria(
                                "Еда", "testuser"
                        );

        assertEquals(3, result.size());
    }

    @Test
    void transactionCreatesCategoryAndOperationTogether() {
        long categoriesBefore = categoryRepository.count();
        long transactionsBefore = transactionRepository.count();

        FinancialTransaction result =
                workflowService.createCategoryAndTransaction(
                        user,
                        wallet,
                        "Транспорт",
                        new BigDecimal("300.00"),
                        TransactionType.EXPENSE,
                        "Поездка"
                );

        assertNotNull(result.getId());
        assertEquals(categoriesBefore + 1, categoryRepository.count());
        assertEquals(transactionsBefore + 1, transactionRepository.count());
    }

    @Test
    void transactionRollsBackCategoryWhenOperationFails() {
        long categoriesBefore = categoryRepository.count();
        long transactionsBefore = transactionRepository.count();

        assertThrows(RuntimeException.class, () ->
                workflowService.createCategoryAndTransaction(
                        user,
                        wallet,
                        "Категория для отката",
                        null,
                        TransactionType.EXPENSE,
                        "Операция с недопустимой суммой"
                )
        );

        assertEquals(categoriesBefore, categoryRepository.count());
        assertEquals(transactionsBefore, transactionRepository.count());
    }
}
