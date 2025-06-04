package com.jwt.implementation.repository;

import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioAsset, Integer> {
    List<PortfolioAsset> findByUser(User user);
    List<PortfolioAsset> findByUserAndSymbol(User user, String symbol);
}