package com.traffic.controller;

import com.traffic.service.RealTimePerformanceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Performance Monitoring
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@Slf4j
public class PerformanceController {

    private final RealTimePerformanceMonitor performanceMonitor;

    /**
     * Get performance summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        try {
            Map<String, Object> summary = performanceMonitor.getPerformanceSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting performance summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getCurrentMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitor.getCurrentMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting current metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get detailed performance report
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getDetailedReport() {
        try {
            Map<String, Object> report = performanceMonitor.getDetailedReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting detailed report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reset performance metrics (admin only)
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        try {
            performanceMonitor.resetMetrics();
            return ResponseEntity.ok(Map.of("message", "Performance metrics reset successfully"));
        } catch (Exception e) {
            log.error("Error resetting metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
