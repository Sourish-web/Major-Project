package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jwt.implementation.entity.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

}
