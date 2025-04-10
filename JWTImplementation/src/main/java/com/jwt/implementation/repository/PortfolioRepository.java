package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.PortfolioAsset;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioAsset, Integer> {
}
