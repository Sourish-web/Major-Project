package com.jwt.implementation.controller;

import com.jwt.implementation.entity.Category;
import com.jwt.implementation.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Exports the transaction report as a PDF for the currently authenticated user.
     */
    @GetMapping("/export/pdf")
    @CrossOrigin(origins = "http://localhost:3000")
    public String exportAsPDF() throws Exception {
        return reportService.exportAsPDF();
    }

    /**
     * Exports the transaction report as a CSV for the currently authenticated user.
     */
    @GetMapping("/export/csv")
    @CrossOrigin(origins = "http://localhost:3000")
    public String exportAsCSV() throws Exception {
        return reportService.exportAsCSV();
    }

    /**
     * Retrieves a breakdown of transactions by category for the currently authenticated user.
     */
    @GetMapping("/breakdown/category")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Category, BigDecimal> getCategoryBreakdown() {
        return reportService.breakdownByCategory();
    }

    /**
     * Retrieves a breakdown of transactions by month for the currently authenticated user.
     */
    @GetMapping("/breakdown/month")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Integer, BigDecimal> getMonthBreakdown() {
        return reportService.breakdownByMonth();
    }

    /**
     * Retrieves the tax summary for the currently authenticated user.
     */
    @GetMapping("/tax-summary")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getTaxSummary() {
        return reportService.taxSummary();
    }
}
