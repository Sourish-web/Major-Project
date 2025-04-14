package com.jwt.implementation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.Report;
import com.jwt.implementation.entity.User;
public interface ReportRepository extends JpaRepository<Report, Integer> {
	 List<Report> findByUser(User user);
}
