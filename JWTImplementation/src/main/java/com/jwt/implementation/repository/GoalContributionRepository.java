package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.GoalContribution;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, Integer> {
    List<GoalContribution> findByGoal(Goal goal);
    List<GoalContribution> findByGoalAndUser(Goal goal, User user);
}