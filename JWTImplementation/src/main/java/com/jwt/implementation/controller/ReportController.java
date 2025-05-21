package com.jwt.implementation.controller;

import com.jwt.implementation.dto.ReportDataDTO;
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

    @GetMapping("/export/pdf")
    @CrossOrigin(origins = "http://localhost:3000")
    public String exportAsPDF() throws Exception {
        return reportService.exportAsPDF();
    }

    @GetMapping("/export/csv")
    @CrossOrigin(origins = "http://localhost:3000")
    public String exportAsCSV() throws Exception {
        return reportService.exportAsCSV();
    }

    @GetMapping("/breakdown/category")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Category, ReportDataDTO.CategoryBreakdown> getCategoryBreakdown() {
        return reportService.breakdownByCategory();
    }

    @GetMapping("/breakdown/month")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Integer, ReportDataDTO.MonthlyBreakdown> getMonthBreakdown() {
        return reportService.breakdownByMonth();
    }

    @GetMapping("/tax-summary")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getTaxSummary() {
        return reportService.taxSummary();
    }

    @GetMapping("/data")
    @CrossOrigin(origins = "http://localhost:3000")
    public ReportDataDTO getReportData() {
        return reportService.getReportData();
    }
}