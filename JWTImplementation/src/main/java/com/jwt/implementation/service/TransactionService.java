package com.jwt.implementation.service;

import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.TransactionRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the currently authenticated user from the SecurityContext.
     *
     * @return User object of the authenticated user.
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    /**
     * Adds a new transaction for the currently authenticated user.
     */
    public Transaction addTransaction(Transaction transaction) {
        User currentUser = getCurrentUser();
        transaction.setUser(currentUser);
        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves all transactions for the currently authenticated user.
     */
    public List<Transaction> getAllTransaction() {
        User currentUser = getCurrentUser();
        return transactionRepository.findByUser(currentUser);
    }

    /**
     * Updates a transaction if it belongs to the currently authenticated user.
     */
    public Transaction updateTransaction(Transaction updatedTransaction) {
        Transaction existingTransaction = transactionRepository.findById(updatedTransaction.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(existingTransaction.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this transaction.");
        }

        existingTransaction.setAmount(updatedTransaction.getAmount());
        existingTransaction.setCategory(updatedTransaction.getCategory());
        existingTransaction.setDescription(updatedTransaction.getDescription());
        existingTransaction.setTransactionDate(updatedTransaction.getTransactionDate());

        return transactionRepository.save(existingTransaction);
    }

    /**
     * Deletes a transaction if it belongs to the currently authenticated user.
     */
    public Boolean deleteTransaction(int id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(transaction.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this transaction.");
        }

        transactionRepository.delete(transaction);
        return true;
    }
}
