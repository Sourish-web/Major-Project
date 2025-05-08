package com.jwt.implementation.service;

import com.jwt.implementation.entity.Category;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
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

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String str && str.equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated.");
        } else {
            throw new RuntimeException("Unexpected principal type: " + principal.getClass());
        }

        System.out.println(">> Fetching authenticated user by email: " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    public String exportAsPDF() throws Exception {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        System.out.println(">> Exporting PDF for user: " + currentUser.getEmail());
        System.out.println(">> Found " + transactions.size() + " transactions.");

        Document document = new Document();
        String fileName = "transactions_" + System.currentTimeMillis() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.add(new Paragraph("Transaction Report"));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        for (Transaction transaction : transactions) {
            document.add(new Paragraph("Category: " + transaction.getCategory().name() +
                    ", Amount: " + transaction.getAmount() +
                    ", Description: " + transaction.getDescription() +
                    ", Date: " + transaction.getTransactionDate()));
        }

        document.close();
        return fileName;
    }

    public String exportAsCSV() throws IOException {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        System.out.println(">> Exporting CSV for user: " + currentUser.getEmail());
        System.out.println(">> Found " + transactions.size() + " transactions.");

        String fileName = "transactions_" + System.currentTimeMillis() + ".csv";
        FileWriter writer = new FileWriter(fileName);
        CSVWriter csvWriter = new CSVWriter(writer);
        String[] header = {"ID", "Description", "Amount", "Date", "Category"};
        csvWriter.writeNext(header);

        for (Transaction transaction : transactions) {
            csvWriter.writeNext(new String[]{
                    String.valueOf(transaction.getId()),
                    transaction.getDescription(),
                    transaction.getAmount().toString(),
                    transaction.getTransactionDate().toString(),
                    transaction.getCategory().name()
            });
        }
        csvWriter.close();
        return fileName;
    }

    public Map<Category, BigDecimal> breakdownByCategory() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        return transactions.stream().collect(
                Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );
    }

    public Map<Integer, BigDecimal> breakdownByMonth() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        return transactions.stream().collect(
                Collectors.groupingBy(r -> r.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );
    }

    public BigDecimal taxSummary() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);
        return transactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(transaction -> transaction.getAmount().multiply(new BigDecimal("0.1")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}