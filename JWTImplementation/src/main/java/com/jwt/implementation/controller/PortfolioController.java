package com.jwt.implementation.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jwt.implementation.dto.AssetAllocationDTO;
import com.jwt.implementation.dto.PortfolioSummaryDTO;
import com.jwt.implementation.dto.PriceHistoryDTO;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.PortfolioSnapshot;
import com.jwt.implementation.service.PortfolioService;

@RestController
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/addAsset")
    @CrossOrigin(origins = "http://localhost:3000")
    public PortfolioAsset addAsset(@RequestBody PortfolioAsset asset) {
        return portfolioService.addAsset(asset);
    }

    @GetMapping("/getAssets")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<PortfolioAsset> getAllAssets() {
        return portfolioService.getAllAssets();
    }

    @PostMapping("/updateAsset")
    @CrossOrigin(origins = "http://localhost:3000")
    public PortfolioAsset updateAsset(@RequestBody PortfolioAsset asset) {
        return portfolioService.updateAsset(asset);
    }

    @GetMapping("/deleteAsset/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Boolean deleteAsset(@PathVariable int id) {
        return portfolioService.deleteAsset(id);
    }
    
    @GetMapping("/performance")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<PriceHistoryDTO>> getAssetPerformance(
            @RequestParam String symbol,
            @RequestParam String type) {
        List<PriceHistoryDTO> history = portfolioService.getAssetPerformance(symbol, type);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/portfolio/summary")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary() {
        PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/trend")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<PortfolioSnapshot>> getUserPortfolioTrend() {
        List<PortfolioSnapshot> trend = portfolioService.getUserPortfolioTrend();
        return ResponseEntity.ok(trend);
    }
    
    @GetMapping("/test-snapshot")
    @CrossOrigin(origins = "http://localhost:3000")
    public String testSnapshot() {
        portfolioService.takeDailySnapshot();
        return "Snapshot triggered";
    }
    
    @GetMapping("/portfolio/asset-allocation")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<AssetAllocationDTO>> getAssetAllocation() {
        try {
            List<AssetAllocationDTO> allocation = portfolioService.getAssetAllocation();
            if (allocation.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
            return ResponseEntity.ok(allocation);
        } catch (RuntimeException e) {
            System.out.println("Error in getAssetAllocation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
  





    
}
