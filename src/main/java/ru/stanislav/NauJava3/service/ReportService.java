package ru.stanislav.NauJava3.service;

import org.springframework.stereotype.Service;
import ru.stanislav.NauJava3.entity.FinancialTransaction;
import ru.stanislav.NauJava3.entity.Report;
import ru.stanislav.NauJava3.entity.ReportStatus;
import ru.stanislav.NauJava3.repository.AppUserRepository;
import ru.stanislav.NauJava3.repository.FinancialTransactionRepository;
import ru.stanislav.NauJava3.repository.ReportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final AppUserRepository appUserRepository;
    private final FinancialTransactionRepository transactionRepository;

    public ReportService(
            ReportRepository reportRepository,
            AppUserRepository appUserRepository,
            FinancialTransactionRepository transactionRepository) {
        this.reportRepository = reportRepository;
        this.appUserRepository = appUserRepository;
        this.transactionRepository = transactionRepository;
    }

    // Создание отчёта со статусом CREATED
    public Long createReport() {
        Report report = new Report(ReportStatus.CREATED, null);
        Report savedReport = reportRepository.save(report);

        return savedReport.getId();
    }

    // Получение отчёта по идентификатору
    public Report getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Отчёт с ID " + id + " не найден"));
    }

    // Асинхронное формирование отчёта
    public CompletableFuture<Void> generateReportAsync(Long reportId) {

        return CompletableFuture.runAsync(() -> {

            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Отчёт с ID " + reportId + " не найден"));

            long totalStartTime = System.currentTimeMillis();

            AtomicLong usersCount = new AtomicLong();
            AtomicLong usersTime = new AtomicLong();

            AtomicReference<List<FinancialTransaction>> transactions =
                    new AtomicReference<>(new ArrayList<>());

            AtomicLong transactionsTime = new AtomicLong();

            AtomicReference<Throwable> error = new AtomicReference<>();

            try {
                // Первый поток: подсчёт пользователей
                Thread usersThread = new Thread(() -> {
                    long start = System.currentTimeMillis();

                    try {
                        long count = appUserRepository.count();
                        usersCount.set(count);
                    } catch (Throwable exception) {
                        error.compareAndSet(null, exception);
                    } finally {
                        usersTime.set(
                                System.currentTimeMillis() - start);
                    }
                }, "report-users-thread");

                // Второй поток: получение финансовых операций
                Thread transactionsThread = new Thread(() -> {
                    long start = System.currentTimeMillis();

                    try {
                        List<FinancialTransaction> result =
                                new ArrayList<>();

                        transactionRepository.findAll()
                                .forEach(result::add);

                        transactions.set(result);
                    } catch (Throwable exception) {
                        error.compareAndSet(null, exception);
                    } finally {
                        transactionsTime.set(
                                System.currentTimeMillis() - start);
                    }
                }, "report-transactions-thread");

                usersThread.start();
                transactionsThread.start();

                usersThread.join();
                transactionsThread.join();

                if (error.get() != null) {
                    throw new RuntimeException(
                            "Ошибка при сборе статистики",
                            error.get());
                }

                long totalTime =
                        System.currentTimeMillis() - totalStartTime;

                String html = buildHtmlReport(
                        usersCount.get(),
                        transactions.get(),
                        usersTime.get(),
                        transactionsTime.get(),
                        totalTime
                );

                report.setContent(html);
                report.setStatus(ReportStatus.COMPLETED);

                reportRepository.save(report);

            } catch (Exception exception) {

                report.setStatus(ReportStatus.ERROR);
                report.setContent(
                        "Ошибка формирования отчёта: "
                                + exception.getMessage());

                reportRepository.save(report);
            }
        });
    }

    // Формирование HTML-содержимого отчёта
    private String buildHtmlReport(
            long usersCount,
            List<FinancialTransaction> transactions,
            long usersTime,
            long transactionsTime,
            long totalTime) {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <title>Отчёт приложения</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 30px;
                            background: #f5f5f5;
                            color: #222;
                        }
                        .container {
                            max-width: 1000px;
                            margin: auto;
                            background: white;
                            padding: 25px;
                            border-radius: 10px;
                        }
                        .stat {
                            padding: 12px;
                            margin: 10px 0;
                            background: #eef3f8;
                            border-radius: 6px;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 20px;
                        }
                        th, td {
                            padding: 10px;
                            border: 1px solid #ddd;
                            text-align: left;
                        }
                        th {
                            background: #e8edf2;
                        }
                        h1, h2 {
                            color: #243447;
                        }
                    </style>
                </head>
                <body>
                <div class="container">
                    <h1>Статистический отчёт приложения</h1>

                    <h2>Общая статистика</h2>
                """);

        html.append("<div class='stat'>")
                .append("<b>Количество зарегистрированных пользователей:</b> ")
                .append(usersCount)
                .append("</div>");

        html.append("<div class='stat'>")
                .append("<b>Количество финансовых операций:</b> ")
                .append(transactions.size())
                .append("</div>");

        html.append("<h2>Время выполнения</h2>");

        html.append("<div class='stat'>")
                .append("<b>Подсчёт пользователей:</b> ")
                .append(usersTime)
                .append(" мс</div>");

        html.append("<div class='stat'>")
                .append("<b>Получение финансовых операций:</b> ")
                .append(transactionsTime)
                .append(" мс</div>");

        html.append("<div class='stat'>")
                .append("<b>Общее время формирования отчёта:</b> ")
                .append(totalTime)
                .append(" мс</div>");

        html.append("""
                    <h2>Список финансовых операций</h2>
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Сумма</th>
                                <th>Тип</th>
                                <th>Дата операции</th>
                            </tr>
                        </thead>
                        <tbody>
                """);

        for (FinancialTransaction transaction : transactions) {

            html.append("<tr>");

            html.append("<td>")
                    .append(transaction.getId())
                    .append("</td>");

            html.append("<td>")
                    .append(transaction.getAmount())
                    .append("</td>");

            html.append("<td>")
                    .append(transaction.getType())
                    .append("</td>");

            html.append("<td>")
                    .append(transaction.getTransactionDate())
                    .append("</td>");

            html.append("</tr>");
        }

        html.append("""
                        </tbody>
                    </table>
                </div>
                </body>
                </html>
                """);

        return html.toString();
    }
}