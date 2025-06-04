package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    List<Goal> findByUser(User user);
    List<Goal> findByCollaboratorsContaining(User user);

    long countByUserAndStatusAndCreatedAtGreaterThanEqual(User user, String status, LocalDate startDate);

    long countByUserAndStatusAndCreatedAtBetween(User user, String status, LocalDate startDate, LocalDate endDate);
}