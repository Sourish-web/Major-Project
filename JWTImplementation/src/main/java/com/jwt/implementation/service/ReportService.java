package com.jwt.implementation.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.jwt.implementation.entity.Report;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.repository.ReportRepository;
import com.jwt.implementation.repository.TransactionRepository;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReportRepository reportRepository;

    public String exportAsPDF(String username) throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        Document document = new Document();
        String fileName = "transactions_" + System.currentTimeMillis() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.add(new Paragraph("Transaction Report"));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));
        for (Transaction t : transactions) {
            document.add(new Paragraph("Date: " + t.getTransactionDate() +
                    ", Amount: " + t.getAmount() +
                    ", Category: " + t.getCategory() +
                    ", Description: " + t.getDescription()));
        }
        document.close();

        Report report = new Report(null, "PDF", "All", LocalDate.now(), username, fileName);
        reportRepository.save(report);

        return fileName;
    }

    public String exportAsCSV(String username) throws IOException {
        List<Transaction> transactions = transactionRepository.findAll();
        String fileName = "transactions_" + System.currentTimeMillis() + ".csv";
        FileWriter writer = new FileWriter(fileName);
        CSVWriter csvWriter = new CSVWriter(writer);
        String[] header = {"ID", "Description", "Amount", "Date", "Category"};
        csvWriter.writeNext(header);

        for (Transaction t : transactions) {
            csvWriter.writeNext(new String[]{
                    String.valueOf(t.getId()),
                    t.getDescription(),
                    String.valueOf(t.getAmount()),
                    t.getTransactionDate().toString(),
                    t.getCategory()
            });
        }
        csvWriter.close();

        Report report = new Report(null, "CSV", "All", LocalDate.now(), username, fileName);
        reportRepository.save(report);

        return fileName;
    }

    public Map<String, BigDecimal> breakdownByCategory() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream().collect(
                Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );
    }

    public Map<Integer, BigDecimal> breakdownByMonth() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream().collect(
                Collectors.groupingBy(t -> t.getTransactionDate().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add))
        );
    }

    public BigDecimal taxSummary() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(t -> t.getAmount().multiply(new BigDecimal("0.1"))) // 10% tax
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
