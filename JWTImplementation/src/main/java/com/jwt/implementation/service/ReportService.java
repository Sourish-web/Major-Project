package com.jwt.implementation.service;

import com.jwt.implementation.dto.ReportDataDTO;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Category;
import com.jwt.implementation.entity.Period;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.TransactionRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser;
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            String email = userDetails.getUsername();
            System.out.println(">> Fetching authenticated user by email: " + email);
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
        } else if (principal instanceof User user) {
            currentUser = user;
            System.out.println(">> Principal is User entity with ID: " + currentUser.getId());
        } else if (principal instanceof String str && str.equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated.");
        } else {
            System.out.println(">> Unexpected principal type: " + principal.getClass().getName());
            throw new RuntimeException("Unexpected principal type: " + principal.getClass());
        }
        System.out.println(">> Current User ID: " + currentUser.getId());
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    public String exportAsPDF() throws Exception {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        List<Budget> budgets = budgetRepository.findByUser(currentUser);

        System.out.println(">> Exporting PDF for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

        Document document = new Document();
        String fileName = "transactions_budgets_" + System.currentTimeMillis() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.add(new Paragraph("Financial Report"));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        // Add Transactions
        document.add(new Paragraph("Transactions:"));
        for (Transaction transaction : transactions) {
            document.add(new Paragraph("Category: " + transaction.getCategory().name() +
                    ", Amount: " + transaction.getAmount() +
                    ", Description: " + transaction.getDescription() +
                    ", Date: " + transaction.getTransactionDate()));
        }
        document.add(new Paragraph(" "));

        // Add Budgets
        document.add(new Paragraph("Budgets:"));
        for (Budget budget : budgets) {
            BigDecimal remaining = budget.getAmount().subtract(budget.getSpent());
            document.add(new Paragraph("Category: " + budget.getCategory().name() +
                    ", Budget Amount: " + budget.getAmount() +
                    ", Spent: " + budget.getSpent() +
                    ", Remaining: " + remaining +
                    ", Period: " + budget.getPeriod() +
                    ", Start Date: " + budget.getStartDate() +
                    ", End Date: " + budget.getEndDate()));
        }

        document.close();
        return fileName;
    }

    public String exportAsCSV() throws IOException {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        List<Budget> budgets = budgetRepository.findByUser(currentUser);

        System.out.println(">> Exporting CSV for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

        String fileName = "transactions_budgets_" + System.currentTimeMillis() + ".csv";
        FileWriter writer = new FileWriter(fileName);
        CSVWriter csvWriter = new CSVWriter(writer);

        // Transaction Headers
        String[] transactionHeader = {"Type", "ID", "Description", "Amount", "Date", "Category"};
        csvWriter.writeNext(transactionHeader);

        // Write Transactions
        for (Transaction transaction : transactions) {
            csvWriter.writeNext(new String[]{
                    "Transaction",
                    String.valueOf(transaction.getId()),
                    transaction.getDescription(),
                    transaction.getAmount().toString(),
                    transaction.getTransactionDate().toString(),
                    transaction.getCategory().name()
            });
        }

        // Budget Headers
        String[] budgetHeader = {"Type", "ID", "Category", "Amount", "Spent", "Remaining", "Period", "Start Date", "End Date"};
        csvWriter.writeNext(new String[]{});
        csvWriter.writeNext(budgetHeader);

        // Write Budgets
        for (Budget budget : budgets) {
            BigDecimal remaining = budget.getAmount().subtract(budget.getSpent());
            csvWriter.writeNext(new String[]{
                    "Budget",
                    String.valueOf(budget.getId()),
                    budget.getCategory().name(),
                    budget.getAmount().toString(),
                    budget.getSpent().toString(),
                    remaining.toString(),
                    budget.getPeriod().toString(),
                    budget.getStartDate().toString(),
                    budget.getEndDate().toString()
            });
        }

        csvWriter.close();
        return fileName;
    }

    public Map<Category, ReportDataDTO.CategoryBreakdown> breakdownByCategory() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        List<Budget> budgets = budgetRepository.findByUser(currentUser);

        System.out.println(">> Category breakdown for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

        Map<Category, BigDecimal> transactionTotals = transactions.stream().collect(
                Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );

        Map<Category, BigDecimal> budgetLimits = budgets.stream().collect(
                Collectors.groupingBy(Budget::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Budget::getAmount, BigDecimal::add))
        );

        return transactionTotals.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ReportDataDTO.CategoryBreakdown(
                        entry.getValue(),
                        budgetLimits.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                )
        ));
    }

    public Map<Integer, ReportDataDTO.MonthlyBreakdown> breakdownByMonth() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        List<Budget> budgets = budgetRepository.findByUser(currentUser);

        System.out.println(">> Monthly breakdown for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

        Map<Integer, BigDecimal> transactionTotals = transactions.stream().collect(
                Collectors.groupingBy(t -> t.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );

        Map<Integer, BigDecimal> budgetTotals = budgets.stream()
                .filter(b -> b.getPeriod() == Period.MONTHLY)
                .collect(Collectors.groupingBy(
                        b -> b.getStartDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Budget::getAmount, BigDecimal::add)
                ));

        return transactionTotals.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ReportDataDTO.MonthlyBreakdown(
                        entry.getValue(),
                        budgetTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                )
        ));
    }

    public ReportDataDTO getReportData() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        List<Budget> budgets = budgetRepository.findByUser(currentUser);

        System.out.println(">> Generating report data for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

        Map<Category, BigDecimal> transactionTotals = transactions.stream().collect(
                Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );

        Map<Category, BigDecimal> budgetLimits = budgets.stream().collect(
                Collectors.groupingBy(Budget::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Budget::getAmount, BigDecimal::add))
        );

        Map<Integer, BigDecimal> monthlyTotals = transactions.stream().collect(
                Collectors.groupingBy(t -> t.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );

        BigDecimal taxSummary = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(t -> t.getAmount().multiply(new BigDecimal("0.1")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println(">> Transaction Totals: " + transactionTotals);
        System.out.println(">> Budget Limits: " + budgetLimits);
        System.out.println(">> Monthly Totals: " + monthlyTotals);

        return new ReportDataDTO(transactions, budgets, transactionTotals, budgetLimits, monthlyTotals, taxSummary);
    }

    public BigDecimal taxSummary() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        System.out.println(">> Calculating tax summary for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
        System.out.println(">> Found " + transactions.size() + " transactions.");

        return transactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(transaction -> transaction.getAmount().multiply(new BigDecimal("0.1")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}