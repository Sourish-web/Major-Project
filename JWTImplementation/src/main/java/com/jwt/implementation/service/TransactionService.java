package com.jwt.implementation.service;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.GoalRepository;
import com.jwt.implementation.repository.TransactionRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository; // Added for contributeToGoal

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

    /**
     * Links a transaction to a goal and updates the goal's currentAmount.
     */
    public Transaction contributeToGoal(Integer transactionId, Integer goalId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(transaction.getUser().getId(), currentUser.getId()) ||
            !Objects.equals(goal.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to link transaction to goal.");
        }

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transaction amount must be positive to contribute to a goal.");
        }

        transaction.setGoalId(goalId);
        goal.setCurrentAmount(goal.getCurrentAmount().add(transaction.getAmount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("Completed");
        } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
            goal.setStatus("Missed");
        }

        goalRepository.save(goal);
        return transactionRepository.save(transaction);
    }
}