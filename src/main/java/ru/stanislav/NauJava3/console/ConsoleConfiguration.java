package ru.stanislav.NauJava3.console;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
public class ConsoleConfiguration {

    @Value("${app.test-mode:false}")
    private boolean testMode;

    @Bean
    @ConditionalOnProperty(
            name = "app.test-mode",
            havingValue = "false",
            matchIfMissing = true
    )
    public CommandLineRunner commandScanner(
            CommandProcessor commandProcessor
    ) {
        return args -> {

            if (testMode) {
                return;
            }

            try (Scanner scanner = new Scanner(System.in)) {

                System.out.println();
                System.out.println("======================================");
                System.out.println("Учет и анализ личных расходов");
                System.out.println("Введите 'help' для получения списка команд.");
                System.out.println("Введите 'exit' для выхода.");
                System.out.println("======================================");
                System.out.println();

                while (true) {
                    System.out.print("> ");

                    if (!scanner.hasNextLine()) {
                        break;
                    }

                    String input = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(input.trim())) {
                        System.out.println("Выход из программы...");
                        break;
                    }

                    commandProcessor.processCommand(input);
                }
            }
        };
    }
}