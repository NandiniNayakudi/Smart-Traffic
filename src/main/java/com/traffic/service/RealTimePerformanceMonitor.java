package com.traffic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Real-time Performance Monitoring Service
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@Slf4j
public class RealTimePerformanceMonitor {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Performance metrics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalWebSocketConnections = new AtomicLong(0);
    private final AtomicLong totalTrafficDataProcessed = new AtomicLong(0);
    private final AtomicLong totalAlertsGenerated = new AtomicLong(0);
    
    // Response time tracking
    private final Map<String, Long> requestTimes = new ConcurrentHashMap<>();
    private final Map<String, Double> averageResponseTimes = new ConcurrentHashMap<>();
    
    // System metrics
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    // Performance thresholds
    private static final double HIGH_CPU_THRESHOLD = 80.0;
    private static final double HIGH_MEMORY_THRESHOLD = 85.0;
    private static final long HIGH_RESPONSE_TIME_THRESHOLD = 2000; // 2 seconds
    
    /**
     * Record API request
     */
    public void recordRequest(String endpoint) {
        totalRequests.incrementAndGet();
        requestTimes.put(endpoint + "-" + System.currentTimeMillis(), System.currentTimeMillis());
    }

    /**
     * Record request completion
     */
    public void recordRequestCompletion(String endpoint, long startTime) {
        long responseTime = System.currentTimeMillis() - startTime;
        
        // Update average response time
        averageResponseTimes.compute(endpoint, (key, currentAvg) -> {
            if (currentAvg == null) {
                return (double) responseTime;
            } else {
                return (currentAvg * 0.9) + (responseTime * 0.1); // Exponential moving average
            }
        });
        
        // Check for performance issues
        if (responseTime > HIGH_RESPONSE_TIME_THRESHOLD) {
            log.warn("High response time detected for {}: {}ms", endpoint, responseTime);
            broadcastPerformanceAlert("HIGH_RESPONSE_TIME", 
                String.format("High response time for %s: %dms", endpoint, responseTime));
        }
    }

    /**
     * Record WebSocket connection
     */
    public void recordWebSocketConnection() {
        totalWebSocketConnections.incrementAndGet();
    }

    /**
     * Record WebSocket disconnection
     */
    public void recordWebSocketDisconnection() {
        totalWebSocketConnections.decrementAndGet();
    }

    /**
     * Record traffic data processing
     */
    public void recordTrafficDataProcessed() {
        totalTrafficDataProcessed.incrementAndGet();
    }

    /**
     * Record alert generation
     */
    public void recordAlertGenerated() {
        totalAlertsGenerated.incrementAndGet();
    }

    /**
     * Get current performance metrics
     */
    public Map<String, Object> getCurrentMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Request metrics
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("activeWebSocketConnections", totalWebSocketConnections.get());
        metrics.put("totalTrafficDataProcessed", totalTrafficDataProcessed.get());
        metrics.put("totalAlertsGenerated", totalAlertsGenerated.get());
        
        // Response time metrics
        metrics.put("averageResponseTimes", new HashMap<>(averageResponseTimes));
        
        // System metrics
        metrics.put("systemMetrics", getSystemMetrics());
        
        // Performance status
        metrics.put("performanceStatus", getPerformanceStatus());
        
        metrics.put("timestamp", LocalDateTime.now());
        
        return metrics;
    }

    /**
     * Get system metrics
     */
    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> systemMetrics = new HashMap<>();
        
        // Memory metrics
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        systemMetrics.put("memoryUsed", usedMemory);
        systemMetrics.put("memoryMax", maxMemory);
        systemMetrics.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        // CPU metrics (if available)
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;
            systemMetrics.put("cpuUsagePercent", Math.round(cpuUsage * 100.0) / 100.0);
        }
        
        // JVM metrics
        systemMetrics.put("availableProcessors", osBean.getAvailableProcessors());
        systemMetrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        return systemMetrics;
    }

    /**
     * Get performance status
     */
    private String getPerformanceStatus() {
        Map<String, Object> systemMetrics = getSystemMetrics();
        
        double memoryUsage = (Double) systemMetrics.get("memoryUsagePercent");
        Double cpuUsage = (Double) systemMetrics.get("cpuUsagePercent");
        
        // Check for critical issues
        if (memoryUsage > HIGH_MEMORY_THRESHOLD) {
            return "CRITICAL";
        }
        
        if (cpuUsage != null && cpuUsage > HIGH_CPU_THRESHOLD) {
            return "CRITICAL";
        }
        
        // Check for warnings
        if (memoryUsage > 70.0 || (cpuUsage != null && cpuUsage > 60.0)) {
            return "WARNING";
        }
        
        // Check response times
        boolean hasHighResponseTime = averageResponseTimes.values().stream()
            .anyMatch(time -> time > HIGH_RESPONSE_TIME_THRESHOLD);
        
        if (hasHighResponseTime) {
            return "WARNING";
        }
        
        return "HEALTHY";
    }

    /**
     * Broadcast performance metrics to connected clients
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void broadcastPerformanceMetrics() {
        try {
            Map<String, Object> metrics = getCurrentMetrics();
            
            Map<String, Object> broadcast = Map.of(
                "type", "PERFORMANCE_METRICS",
                "timestamp", System.currentTimeMillis(),
                "data", metrics
            );
            
            messagingTemplate.convertAndSend("/topic/performance", broadcast);
            
            // Log performance status
            String status = (String) metrics.get("performanceStatus");
            if (!"HEALTHY".equals(status)) {
                log.warn("Performance status: {} - Metrics: {}", status, metrics);
            }
            
        } catch (Exception e) {
            log.error("Error broadcasting performance metrics", e);
        }
    }

    /**
     * Monitor system health and send alerts
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorSystemHealth() {
        try {
            Map<String, Object> systemMetrics = getSystemMetrics();
            
            double memoryUsage = (Double) systemMetrics.get("memoryUsagePercent");
            Double cpuUsage = (Double) systemMetrics.get("cpuUsagePercent");
            
            // Memory usage alert
            if (memoryUsage > HIGH_MEMORY_THRESHOLD) {
                broadcastPerformanceAlert("HIGH_MEMORY_USAGE", 
                    String.format("High memory usage detected: %.1f%%", memoryUsage));
            }
            
            // CPU usage alert
            if (cpuUsage != null && cpuUsage > HIGH_CPU_THRESHOLD) {
                broadcastPerformanceAlert("HIGH_CPU_USAGE", 
                    String.format("High CPU usage detected: %.1f%%", cpuUsage));
            }
            
            // WebSocket connection monitoring
            long connections = totalWebSocketConnections.get();
            if (connections > 1000) { // Threshold for high connection count
                broadcastPerformanceAlert("HIGH_CONNECTION_COUNT", 
                    String.format("High WebSocket connection count: %d", connections));
            }
            
        } catch (Exception e) {
            log.error("Error monitoring system health", e);
        }
    }

    /**
     * Broadcast performance alert
     */
    private void broadcastPerformanceAlert(String alertType, String message) {
        Map<String, Object> alert = Map.of(
            "type", "PERFORMANCE_ALERT",
            "alertType", alertType,
            "message", message,
            "timestamp", System.currentTimeMillis(),
            "severity", "WARNING"
        );
        
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        log.warn("Performance alert: {} - {}", alertType, message);
    }

    /**
     * Get performance summary for dashboard
     */
    public Map<String, Object> getPerformanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Key metrics
        summary.put("totalRequests", totalRequests.get());
        summary.put("activeConnections", totalWebSocketConnections.get());
        summary.put("dataProcessed", totalTrafficDataProcessed.get());
        summary.put("alertsGenerated", totalAlertsGenerated.get());
        
        // System health
        Map<String, Object> systemMetrics = getSystemMetrics();
        summary.put("memoryUsage", systemMetrics.get("memoryUsagePercent"));
        summary.put("cpuUsage", systemMetrics.get("cpuUsagePercent"));
        summary.put("status", getPerformanceStatus());
        
        // Average response times
        if (!averageResponseTimes.isEmpty()) {
            double avgResponseTime = averageResponseTimes.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            summary.put("averageResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
        }
        
        return summary;
    }

    /**
     * Reset metrics (for testing or maintenance)
     */
    public void resetMetrics() {
        totalRequests.set(0);
        totalTrafficDataProcessed.set(0);
        totalAlertsGenerated.set(0);
        requestTimes.clear();
        averageResponseTimes.clear();
        
        log.info("Performance metrics reset");
    }

    /**
     * Get detailed performance report
     */
    public Map<String, Object> getDetailedReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Current metrics
        report.put("currentMetrics", getCurrentMetrics());
        
        // Performance trends (simplified)
        report.put("trends", Map.of(
            "requestTrend", "STABLE", // Could be calculated from historical data
            "responseTrend", "STABLE",
            "memoryTrend", "STABLE"
        ));
        
        // Recommendations
        report.put("recommendations", getPerformanceRecommendations());
        
        return report;
    }

    /**
     * Get performance recommendations
     */
    private Map<String, Object> getPerformanceRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        
        String status = getPerformanceStatus();
        Map<String, Object> systemMetrics = getSystemMetrics();
        
        if ("CRITICAL".equals(status)) {
            recommendations.put("priority", "HIGH");
            recommendations.put("actions", java.util.List.of(
                "Consider scaling up server resources",
                "Review and optimize database queries",
                "Implement caching strategies",
                "Monitor for memory leaks"
            ));
        } else if ("WARNING".equals(status)) {
            recommendations.put("priority", "MEDIUM");
            recommendations.put("actions", java.util.List.of(
                "Monitor resource usage trends",
                "Consider implementing connection pooling",
                "Review slow API endpoints",
                "Optimize real-time data processing"
            ));
        } else {
            recommendations.put("priority", "LOW");
            recommendations.put("actions", java.util.List.of(
                "Continue monitoring",
                "Regular performance reviews",
                "Maintain current optimization strategies"
            ));
        }
        
        return recommendations;
    }
}
