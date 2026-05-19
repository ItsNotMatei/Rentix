package com.example.demo.repository;

import com.example.demo.model.ListingReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingReportRepository extends JpaRepository<ListingReport, Long> {

    List<ListingReport> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);

    boolean existsByUserIdAndAnuntIdAndStatus(Long userId, Long anuntId, String status);
}
