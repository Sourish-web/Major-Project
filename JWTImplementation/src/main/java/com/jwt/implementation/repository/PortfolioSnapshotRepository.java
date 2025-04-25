package com.jwt.implementation.repository;

import com.jwt.implementation.entity.PortfolioSnapshot;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    List<PortfolioSnapshot> findByUserOrderBySnapshotDateAsc(User user);
		// TODO Auto-generated method stub
		
     boolean existsByUserAndSnapshotDate(User user, LocalDate date);
		// TODO Auto-generated method stub
		
}
