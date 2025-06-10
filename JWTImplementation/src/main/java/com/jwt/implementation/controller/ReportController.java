package com.jwt.implementation.controller;

import com.jwt.implementation.dto.ReportDataDTO;
import com.jwt.implementation.entity.Category;
import com.jwt.implementation.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/export/pdf")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<byte[]> exportAsPDF(@RequestParam(value = "quarter", required = false) String quarter) throws Exception {
        if (quarter != null && !List.of("All", "Q1", "Q2", "Q3", "Q4").contains(quarter)) {
            throw new IllegalArgumentException("Invalid quarter value: " + quarter);
        }
        byte[] pdfBytes = reportService.exportAsPDF(quarter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transactions_budgets_" + System.currentTimeMillis() + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/export/csv")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<byte[]> exportAsCSV(@RequestParam(value = "quarter", required = false) String quarter) throws Exception {
        if (quarter != null && !List.of("All", "Q1", "Q2", "Q3", "Q4").contains(quarter)) {
            throw new IllegalArgumentException("Invalid quarter value: " + quarter);
        }
        byte[] csvBytes = reportService.exportAsCSV(quarter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions_budgets_" + System.currentTimeMillis() + ".csv");
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/breakdown/category")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Category, ReportDataDTO.CategoryBreakdown> getCategoryBreakdown(@RequestParam(value = "quarter", required = false) String quarter) {
        return reportService.breakdownByCategory(quarter);
    }

    @GetMapping("/breakdown/month")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Integer, ReportDataDTO.MonthlyBreakdown> getMonthBreakdown(@RequestParam(value = "quarter", required = false) String quarter) {
        return reportService.breakdownByMonth(quarter);
    }

    @GetMapping("/tax-summary")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getTaxSummary(@RequestParam(value = "quarter", required = false) String quarter) {
        return reportService.taxSummary(quarter);
    }

    @GetMapping("/data")
    @CrossOrigin(origins = "http://localhost:3000")
    public ReportDataDTO getReportData(@RequestParam(value = "quarter", required = false) String quarter) {
        return reportService.getReportData(quarter);
    }
}