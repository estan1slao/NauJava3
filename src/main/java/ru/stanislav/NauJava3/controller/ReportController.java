package ru.stanislav.NauJava3.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stanislav.NauJava3.entity.Report;
import ru.stanislav.NauJava3.entity.ReportStatus;
import ru.stanislav.NauJava3.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Создание отчёта и запуск его формирования
    @PostMapping
    public ResponseEntity<?> createReport() {

        Long reportId = reportService.createReport();

        reportService.generateReportAsync(reportId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new CreateReportResponse(
                        reportId,
                        "Отчёт создан и отправлен на формирование"
                ));
    }

    // Получение отчёта по ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getReport(@PathVariable Long id) {

        Report report;

        try {
            report = reportService.getReport(id);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(exception.getMessage());
        }

        if (report.getStatus() == ReportStatus.CREATED) {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Отчёт ещё формируется. Попробуйте позже.");
        }

        if (report.getStatus() == ReportStatus.ERROR) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("При формировании отчёта произошла ошибка: "
                            + report.getContent());
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(report.getContent());
    }

    // DTO ответа при создании отчёта
    public record CreateReportResponse(
            Long reportId,
            String message
    ) {
    }
}