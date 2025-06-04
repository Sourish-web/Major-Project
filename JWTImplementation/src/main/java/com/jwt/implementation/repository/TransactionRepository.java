package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUser(User user);

    long countByUserAndTransactionDateGreaterThanEqual(User user, LocalDate startDate);

    long countByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);
}