package com.jwt.implementation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.User;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioAsset, Integer> {
    List<PortfolioAsset> findByUser(User user);
    List<PortfolioAsset> findByUserAndSymbol(User user, String symbol);
}
