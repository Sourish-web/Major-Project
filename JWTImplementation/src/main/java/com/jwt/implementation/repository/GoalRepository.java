package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.Goal;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
}
