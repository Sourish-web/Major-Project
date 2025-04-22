package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.GoalInvitation;
import com.jwt.implementation.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalInvitationRepository extends JpaRepository<GoalInvitation, Integer> {

    // Find all invitations sent to a specific user
    List<GoalInvitation> findByInvitedUser(User invitedUser);

    // Find invitations for a goal
    List<GoalInvitation> findByGoal(Goal goal);

    // Optional: Check if a specific user has already been invited to a goal
    Optional<GoalInvitation> findByGoalAndInvitedUser(Goal goal, User invitedUser);
}
