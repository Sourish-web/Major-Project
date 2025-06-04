package com.jwt.implementation.service;

import com.jwt.implementation.dto.SpendingOverviewDTO;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpendingOverviewService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<SpendingOverviewDTO> getSpendingOverview(User user) {
        if (user == null) {
            System.out.println("User is null in SpendingOverviewService");
            return Collections.emptyList();
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(6);

        List<Transaction> transactions = transactionRepository.findByUser(user);
        System.out.println("Transactions found for user " + user.getEmail() + ": " + transactions.size());

        Map<String, Double> monthlySpending = transactions.stream()
            .filter(t -> {
                LocalDate date = t.getTransactionDate();
                return date != null && !date.isBefore(start) && !date.isAfter(end);
            })
            .collect(Collectors.groupingBy(
                t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("MMM"))
                    .toLowerCase().replaceFirst("^.", Character.toString(Character.toUpperCase(t.getTransactionDate().format(DateTimeFormatter.ofPattern("MMM")).charAt(0)))),
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        List<SpendingOverviewDTO> result = monthlySpending.entrySet().stream()
            .map(entry -> new SpendingOverviewDTO(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
                return LocalDate.parse("01 " + a.getMonth(), formatter)
                    .compareTo(LocalDate.parse("01 " + b.getMonth(), formatter));
            })
            .toList();

        System.out.println("Spending overview result: " + result);
        return result;
    }
}