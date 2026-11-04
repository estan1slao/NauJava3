package ru.stanislav.NauJava3.console;

import org.springframework.stereotype.Component;
import ru.stanislav.NauJava3.entity.Transaction;
import ru.stanislav.NauJava3.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class CommandProcessor {

    private final TransactionService transactionService;

    public CommandProcessor(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void processCommand(String input) {
        if (input == null || input.isBlank()) {
            System.out.println("Команда не введена.");
            return;
        }

        try {
            String[] command = parseCommand(input);

            switch (command[0].toLowerCase()) {
                case "create" -> create(command);
                case "read" -> read(command);
                case "update" -> update(command);
                case "delete" -> delete(command);
                case "list" -> list();
                case "category" -> category();
                case "balance" -> balance();
                case "help" -> printHelp();
                default -> System.out.println(
                        "Неизвестная команда. Используйте 'help'."
                );
            }
        } catch (NumberFormatException exception) {
            System.out.println("Ошибка: неверный формат числа.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Ошибка: " + exception.getMessage());
        } catch (Exception exception) {
            System.out.println(
                    "Произошла непредвиденная ошибка: " + exception.getMessage()
            );
        }
    }

    private void create(String[] command) {
        if (command.length < 6) {
            System.out.println(
                    "Использование: create <id> <type> <amount> <category> <description>"
            );
            return;
        }

        Long id = Long.parseLong(command[1]);
        String type = command[2];
        BigDecimal amount = new BigDecimal(command[3]);
        String category = command[4];
        String description = command[5];

        transactionService.createTransaction(
                id,
                type,
                amount,
                category,
                description
        );

        System.out.println("Операция успешно добавлена.");
    }

    private void read(String[] command) {
        if (command.length < 2) {
            System.out.println("Использование: read <id>");
            return;
        }

        Long id = Long.parseLong(command[1]);

        Transaction transaction = transactionService.findById(id);

        if (transaction == null) {
            System.out.println("Операция с ID " + id + " не найдена.");
            return;
        }

        printTransaction(transaction);
    }

    private void update(String[] command) {
        if (command.length < 5) {
            System.out.println(
                    "Использование: update <id> <amount> <category> <description>"
            );
            return;
        }

        Long id = Long.parseLong(command[1]);
        BigDecimal amount = new BigDecimal(command[2]);
        String category = command[3];
        String description = command[4];

        transactionService.updateTransaction(
                id,
                amount,
                category,
                description
        );

        System.out.println("Операция успешно обновлена.");
    }

    private void delete(String[] command) {
        if (command.length < 2) {
            System.out.println("Использование: delete <id>");
            return;
        }

        Long id = Long.parseLong(command[1]);

        transactionService.deleteById(id);

        System.out.println("Операция успешно удалена.");
    }

    private void list() {
        List<Transaction> transactions =
                transactionService.getAllTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Список операций пуст.");
            return;
        }

        System.out.println();
        System.out.println("Список операций:");
        System.out.println("--------------------------------------");

        for (Transaction transaction : transactions) {
            printTransaction(transaction);
        }

        System.out.println("--------------------------------------");
    }

    private void category() {
        Map<String, BigDecimal> expenses =
                transactionService.getExpensesByCategory();

        if (expenses.isEmpty()) {
            System.out.println("Расходов пока нет.");
            return;
        }

        System.out.println();
        System.out.println("Расходы по категориям:");
        System.out.println("--------------------------------------");

        expenses.forEach((category, amount) ->
                System.out.println(category + ": " + amount)
        );

        System.out.println("--------------------------------------");
    }

    private void balance() {
        BigDecimal income = transactionService.getTotalIncome();
        BigDecimal expenses = transactionService.getTotalExpenses();
        BigDecimal balance = transactionService.getBalance();

        System.out.println();
        System.out.println("Финансовый анализ:");
        System.out.println("--------------------------------------");
        System.out.println("Доходы:  " + income);
        System.out.println("Расходы: " + expenses);
        System.out.println("Баланс:  " + balance);
        System.out.println("--------------------------------------");
    }

    private void printTransaction(Transaction transaction) {
        System.out.println(
                "ID: " + transaction.getId()
                        + " | Тип: " + transaction.getType()
                        + " | Сумма: " + transaction.getAmount()
                        + " | Категория: " + transaction.getCategory()
                        + " | Описание: " + transaction.getDescription()
        );
    }

    private void printHelp() {
        System.out.println();
        System.out.println("Доступные команды:");
        System.out.println();
        System.out.println(
                "create <id> <type> <amount> <category> <description>"
        );
        System.out.println("    Создать операцию.");
        System.out.println("    type: INCOME или EXPENSE.");
        System.out.println();
        System.out.println("read <id>");
        System.out.println("    Найти операцию.");
        System.out.println();
        System.out.println(
                "update <id> <amount> <category> <description>"
        );
        System.out.println("    Обновить операцию.");
        System.out.println();
        System.out.println("delete <id>");
        System.out.println("    Удалить операцию.");
        System.out.println();
        System.out.println("list");
        System.out.println("    Показать все операции.");
        System.out.println();
        System.out.println("category");
        System.out.println("    Показать расходы по категориям.");
        System.out.println();
        System.out.println("balance");
        System.out.println("    Показать доходы, расходы и баланс.");
        System.out.println();
        System.out.println("help");
        System.out.println("    Показать справку.");
        System.out.println();
        System.out.println("exit");
        System.out.println("    Выйти из программы.");
        System.out.println();
    }

    private String[] parseCommand(String input) {
        String trimmedInput = input.trim();

        if (trimmedInput.startsWith("create ")) {
            return parseCreateCommand(trimmedInput);
        }

        if (trimmedInput.startsWith("update ")) {
            return parseUpdateCommand(trimmedInput);
        }

        return trimmedInput.split("\\s+");
    }

    private String[] parseCreateCommand(String input) {
        String[] parts = input.split("\\s+", 6);

        if (parts.length == 6) {
            parts[5] = removeQuotes(parts[5]);
        }

        return parts;
    }

    private String[] parseUpdateCommand(String input) {
        String[] parts = input.split("\\s+", 5);

        if (parts.length == 5) {
            parts[4] = removeQuotes(parts[4]);
        }

        return parts;
    }

    private String removeQuotes(String value) {
        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}
