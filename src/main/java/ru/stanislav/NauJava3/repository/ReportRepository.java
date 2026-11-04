package ru.stanislav.NauJava3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.stanislav.NauJava3.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}