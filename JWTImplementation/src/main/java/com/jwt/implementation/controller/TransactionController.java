package com.jwt.implementation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.service.TransactionService;

@RestController
public class TransactionController {
	
	@Autowired 
	private  TransactionService  transactionService;

	@PostMapping("/addTransaction")
	 @CrossOrigin(origins = "http://localhost:3000")
	public Transaction addTransaction(@RequestBody Transaction transaction ) {
		return  transactionService.addTransaction(transaction);
	}
	
	@GetMapping("/getTransaction")
	 @CrossOrigin(origins = "http://localhost:3000")
		public List<Transaction>getAllTransaction(){
		  return transactionService.getAllTransaction();
	}
	
	@PostMapping("/updateTransaction")
	 @CrossOrigin(origins = "http://localhost:3000")
	public Transaction updateTransaction(@RequestBody Transaction transaction) {
		return transactionService.updateTransaction(transaction);
		
	}
		@GetMapping("/deleteTransaction/{id}")
		 @CrossOrigin(origins = "http://localhost:3000")
		public Boolean deleteTransaction (@PathVariable int id) {
			return transactionService.deleteTransaction(id);
			
		}
}
		
	
	

