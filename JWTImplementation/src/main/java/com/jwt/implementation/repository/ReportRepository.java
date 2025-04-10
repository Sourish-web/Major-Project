package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jwt.implementation.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
