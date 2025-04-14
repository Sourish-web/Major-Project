package com.jwt.implementation.repository;

import com.jwt.implementation.entity.Subscription;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    List<Subscription> findByUser(User user);
    List<Subscription> findByUserAndRenewalDateBetween(User user, LocalDate start, LocalDate end);
}
