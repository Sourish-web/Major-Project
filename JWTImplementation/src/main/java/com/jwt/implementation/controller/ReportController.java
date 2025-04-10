package com.jwt.implementation.controller;

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
    public String exportAsPDF(@RequestParam String user) throws Exception {
        return reportService.exportAsPDF(user);
    }

    @GetMapping("/export/csv")
    @CrossOrigin(origins = "http://localhost:3000")
    public String exportAsCSV(@RequestParam String user) throws Exception {
        return reportService.exportAsCSV(user);
    }

    @GetMapping("/breakdown/category")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<String, BigDecimal> getCategoryBreakdown() {
        return reportService.breakdownByCategory();
    }

    @GetMapping("/breakdown/month")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<Integer, BigDecimal> getMonthBreakdown() {
        return reportService.breakdownByMonth();
    }

    @GetMapping("/tax-summary")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getTaxSummary() {
        return reportService.taxSummary();
    }
}
