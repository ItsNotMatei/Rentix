package com.example.demo.repository;

import com.example.demo.model.ModerationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, Long> {
    List<ModerationReport> findByStatusOrderByCreatedAtDesc(String status);
    long countByStatus(String status);
}
