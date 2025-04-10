package com.jwt.implementation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.repository.TransactionRepository;


@Service
public class TransactionService {
	
	
	@Autowired
	private TransactionRepository transactionRepository ;

	public Transaction addTransaction (Transaction transaction ) {
		return transactionRepository.save(transaction);
	}
	
	public List<Transaction>getAllTransaction(){
		return transactionRepository.findAll();
		}
	
	public Transaction updateTransaction(Transaction updatedTransaction) {
		Optional<Transaction> transaction1 = transactionRepository.findById(  updatedTransaction.getId());
		Transaction transaction = transaction1.get();
		transaction.setAmount(updatedTransaction.getAmount());
		transaction.setCategory(updatedTransaction.getCategory());
		transaction.setDescription(updatedTransaction.getDescription());
		transaction.setId(updatedTransaction.getId());
		transaction.setTransactionDate(updatedTransaction.getTransactionDate());
		
		return transactionRepository.save(transaction);
		
	}
	
	
	public Boolean deleteTransaction(int id) {
		transactionRepository.deleteById(id);
		return true;
	}
}
