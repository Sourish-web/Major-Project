package com.jwt.implementation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.User;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    List<Goal> findByUser(User user);
    List<Goal> findByCollaboratorsContaining(User user);
}