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

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    // Helper method to filter transactions by quarter
    private List<Transaction> filterTransactionsByQuarter(List<Transaction> transactions, String quarter) {
        if (quarter == null || quarter.equals("All")) {
            return transactions;
        }
        return transactions.stream()
                .filter(t -> {
                    int month = t.getTransactionDate().getMonthValue();
                    return switch (quarter) {
                        case "Q1" -> month >= 1 && month <= 3;
                        case "Q2" -> month >= 4 && month <= 6;
                        case "Q3" -> month >= 7 && month <= 9;
                        case "Q4" -> month >= 10 && month <= 12;
                        default -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    // Helper method to filter budgets by quarter
    private List<Budget> filterBudgetsByQuarter(List<Budget> budgets, String quarter) {
        if (quarter == null || quarter.equals("All")) {
            return budgets;
        }
        return budgets.stream()
                .filter(b -> {
                    int month = b.getStartDate().getMonthValue();
                    return switch (quarter) {
                        case "Q1" -> month >= 1 && month <= 3;
                        case "Q2" -> month >= 4 && month <= 6;
                        case "Q3" -> month >= 7 && month <= 9;
                        case "Q4" -> month >= 10 && month <= 12;
                        default -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    public byte[] exportAsPDF(String quarter) throws Exception {
        try {
            User currentUser = getCurrentUser();
            List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);
            List<Budget> budgets = filterBudgetsByQuarter(budgetRepository.findByUser(currentUser), quarter);

            System.out.println(">> Exporting PDF for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
            System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Financial Report"));
            document.add(new Paragraph("Generated on: " + LocalDate.now()));
            document.add(new Paragraph("Period: " + (quarter == null ? "All" : quarter)));
            document.add(new Paragraph(" "));

            // Add Transactions
            document.add(new Paragraph("Transactions:"));
            for (Transaction transaction : transactions) {
                document.add(new Paragraph("Category: " + transaction.getCategory().name() +
                        ", Amount: " + transaction.getAmount() +
                        ", Description: " + (transaction.getDescription() != null ? transaction.getDescription() : "N/A") +
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
            writer.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println(">> Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] exportAsCSV(String quarter) throws IOException {
        try {
            User currentUser = getCurrentUser();
            List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);
            List<Budget> budgets = filterBudgetsByQuarter(budgetRepository.findByUser(currentUser), quarter);

            System.out.println(">> Exporting CSV for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
            System.out.println(">> Found " + transactions.size() + " transactions and " + budgets.size() + " budgets.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(baos));

            // Transaction Headers
            String[] transactionHeader = {"Type", "ID", "Description", "Amount", "Date", "Category"};
            csvWriter.writeNext(transactionHeader);

            // Write Transactions
            for (Transaction transaction : transactions) {
                csvWriter.writeNext(new String[]{
                        "Transaction",
                        String.valueOf(transaction.getId()),
                        transaction.getDescription() != null ? transaction.getDescription() : "N/A",
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

            csvWriter.flush();
            csvWriter.close();
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println(">> Error generating CSV: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    public Map<Category, ReportDataDTO.CategoryBreakdown> breakdownByCategory(String quarter) {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);
        List<Budget> budgets = filterBudgetsByQuarter(budgetRepository.findByUser(currentUser), quarter);

        System.out.println(">> Category breakdown for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
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

    public Map<Integer, ReportDataDTO.MonthlyBreakdown> breakdownByMonth(String quarter) {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);
        List<Budget> budgets = filterBudgetsByQuarter(budgetRepository.findByUser(currentUser), quarter);

        System.out.println(">> Monthly breakdown for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
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

    public ReportDataDTO getReportData(String quarter) {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);
        List<Budget> budgets = filterBudgetsByQuarter(budgetRepository.findByUser(currentUser), quarter);

        System.out.println(">> Generating report data for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
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

    public BigDecimal taxSummary(String quarter) {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = filterTransactionsByQuarter(transactionRepository.findByUser(currentUser), quarter);

        System.out.println(">> Calculating tax summary for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + "), Quarter: " + quarter);
        System.out.println(">> Found " + transactions.size() + " transactions.");

        return transactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(transaction -> transaction.getAmount().multiply(new BigDecimal("0.1")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}