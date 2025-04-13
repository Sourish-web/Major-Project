package com.jwt.implementation.controller;

import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction") // You can apply this globally to the class
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/add")
    @CrossOrigin(origins = "http://localhost:3000") 
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction savedTransaction = transactionService.addTransaction(transaction);
            return ResponseEntity.ok(savedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/all")
    @CrossOrigin(origins = "http://localhost:3000") 
    public ResponseEntity<List<Transaction>> getAllTransaction() {
        try {
            List<Transaction> transactions = transactionService.getAllTransaction();
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/update")
    @CrossOrigin(origins = "http://localhost:3000") 
    public ResponseEntity<Transaction> updateTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/delete/{id}")
    @CrossOrigin(origins = "http://localhost:3000") 
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id) {
        try {
            boolean deleted = transactionService.deleteTransaction(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}