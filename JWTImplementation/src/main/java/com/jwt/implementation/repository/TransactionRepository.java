package com.jwt.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jwt.implementation.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {
	
	

}
