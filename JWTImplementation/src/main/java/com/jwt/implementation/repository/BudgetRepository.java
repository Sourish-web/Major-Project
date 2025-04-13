package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.User;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByUser(User user); // Custom query to fetch budgets for a specific user
}
