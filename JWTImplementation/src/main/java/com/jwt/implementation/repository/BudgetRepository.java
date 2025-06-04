package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByUser(User user);

    List<Budget> findByUserAndStartDateGreaterThanEqual(User user, LocalDate startDate);

    List<Budget> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate);
}