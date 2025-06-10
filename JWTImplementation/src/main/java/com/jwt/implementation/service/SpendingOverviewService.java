package com.jwt.implementation.service;

import com.jwt.implementation.dto.SpendingOverviewDTO;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
                t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)),
                Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
            ));

        List<SpendingOverviewDTO> result = monthlySpending.entrySet().stream()
            .map(entry -> new SpendingOverviewDTO(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
                Month monthA = Month.from(formatter.parse(a.getMonth()));
                Month monthB = Month.from(formatter.parse(b.getMonth()));
                return monthA.compareTo(monthB);
            })
            .toList();

        System.out.println("Spending overview result: " + result);
        return result.isEmpty() ? List.of(new SpendingOverviewDTO("No Data", 0.0)) : result;
    }
}