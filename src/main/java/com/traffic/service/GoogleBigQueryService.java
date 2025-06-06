package com.traffic.service;

import com.google.cloud.bigquery.*;
import com.traffic.config.GoogleCloudConfig;
import com.traffic.model.TrafficData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Google BigQuery Service for Real-Time Traffic Analytics
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@Slf4j
public class GoogleBigQueryService {

    private final BigQuery bigQuery;
    private final GoogleCloudConfig.GoogleCloudProperties googleCloudProperties;

    public GoogleBigQueryService(@Autowired(required = false) BigQuery bigQuery,
                                GoogleCloudConfig.GoogleCloudProperties googleCloudProperties) {
        this.bigQuery = bigQuery;
        this.googleCloudProperties = googleCloudProperties;
    }
    
    private TableId trafficDataTable;
    private TableId analyticsTable;

    /**
     * Initialize BigQuery tables
     */
    @PostConstruct
    public void initializeTables() {
        if (bigQuery == null) {
            log.warn("BigQuery not configured - using local analytics");
            return;
        }

        try {
            String dataset = googleCloudProperties.getBigQueryDataset();
            
            // Initialize table references
            trafficDataTable = TableId.of(dataset, "traffic_data_realtime");
            analyticsTable = TableId.of(dataset, "traffic_analytics");
            
            // Create tables if they don't exist
            createTrafficDataTable();
            createAnalyticsTable();
            
            log.info("✅ BigQuery tables initialized: {}.{}", dataset, "traffic_data_realtime");
            
        } catch (Exception e) {
            log.error("Failed to initialize BigQuery tables: {}", e.getMessage());
        }
    }

    /**
     * Stream traffic data to BigQuery in real-time
     */
    @Async
    public CompletableFuture<Void> streamTrafficData(TrafficData trafficData) {
        if (bigQuery == null || trafficDataTable == null) {
            log.debug("BigQuery not available - skipping data streaming");
            return CompletableFuture.completedFuture(null);
        }

        try {
            // Create row data
            Map<String, Object> rowData = createTrafficDataRow(trafficData);
            
            // Insert row
            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(trafficDataTable)
                .addRow(rowData)
                .build();
            
            InsertAllResponse response = bigQuery.insertAll(insertRequest);
            
            if (response.hasErrors()) {
                log.error("BigQuery insert errors: {}", response.getInsertErrors());
            } else {
                log.debug("Streamed traffic data to BigQuery: {}", trafficData.getLocation());
            }
            
        } catch (Exception e) {
            log.error("Error streaming traffic data to BigQuery: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Run real-time analytics queries
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void runRealTimeAnalytics() {
        if (bigQuery == null) {
            log.debug("BigQuery not available - skipping analytics");
            return;
        }

        try {
            // Run various analytics queries
            Map<String, Object> analytics = new HashMap<>();
            
            analytics.put("currentTrafficSummary", getCurrentTrafficSummary());
            analytics.put("congestionHotspots", getCongestionHotspots());
            analytics.put("averageSpeeds", getAverageSpeeds());
            analytics.put("trafficTrends", getTrafficTrends());
            analytics.put("timestamp", LocalDateTime.now());
            
            // Store analytics results
            storeAnalyticsResults(analytics);
            
            log.debug("Completed real-time BigQuery analytics");
            
        } catch (Exception e) {
            log.error("Error running real-time analytics: {}", e.getMessage());
        }
    }

    /**
     * Get current traffic summary from BigQuery
     */
    public Map<String, Object> getCurrentTrafficSummary() {
        if (bigQuery == null) {
            return createMockTrafficSummary();
        }

        try {
            String query = String.format("""
                SELECT 
                    COUNT(*) as total_locations,
                    AVG(average_speed) as avg_speed,
                    SUM(vehicle_count) as total_vehicles,
                    COUNTIF(traffic_density = 'HIGH') as high_traffic_count,
                    COUNTIF(traffic_density = 'CRITICAL') as critical_traffic_count
                FROM `%s.%s.%s`
                WHERE timestamp >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 5 MINUTE)
                """, 
                googleCloudProperties.getProjectId(),
                googleCloudProperties.getBigQueryDataset(),
                "traffic_data_realtime"
            );

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);
            
            Map<String, Object> summary = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                summary.put("totalLocations", row.get("total_locations").getLongValue());
                summary.put("averageSpeed", row.get("avg_speed").getDoubleValue());
                summary.put("totalVehicles", row.get("total_vehicles").getLongValue());
                summary.put("highTrafficCount", row.get("high_traffic_count").getLongValue());
                summary.put("criticalTrafficCount", row.get("critical_traffic_count").getLongValue());
                break; // Only one row expected
            }
            
            return summary;
            
        } catch (Exception e) {
            log.error("Error getting traffic summary from BigQuery: {}", e.getMessage());
            return createMockTrafficSummary();
        }
    }

    /**
     * Get congestion hotspots from BigQuery
     */
    public List<Map<String, Object>> getCongestionHotspots() {
        if (bigQuery == null) {
            return createMockHotspots();
        }

        try {
            String query = String.format("""
                SELECT 
                    location,
                    latitude,
                    longitude,
                    AVG(average_speed) as avg_speed,
                    COUNT(*) as data_points,
                    COUNTIF(traffic_density IN ('HIGH', 'CRITICAL')) as congestion_count
                FROM `%s.%s.%s`
                WHERE timestamp >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 MINUTE)
                GROUP BY location, latitude, longitude
                HAVING congestion_count > 5
                ORDER BY congestion_count DESC, avg_speed ASC
                LIMIT 10
                """, 
                googleCloudProperties.getProjectId(),
                googleCloudProperties.getBigQueryDataset(),
                "traffic_data_realtime"
            );

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);
            
            List<Map<String, Object>> hotspots = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, Object> hotspot = new HashMap<>();
                hotspot.put("location", row.get("location").getStringValue());
                hotspot.put("latitude", row.get("latitude").getDoubleValue());
                hotspot.put("longitude", row.get("longitude").getDoubleValue());
                hotspot.put("averageSpeed", row.get("avg_speed").getDoubleValue());
                hotspot.put("congestionCount", row.get("congestion_count").getLongValue());
                hotspots.add(hotspot);
            }
            
            return hotspots;
            
        } catch (Exception e) {
            log.error("Error getting congestion hotspots from BigQuery: {}", e.getMessage());
            return createMockHotspots();
        }
    }

    /**
     * Get average speeds by location
     */
    public Map<String, Double> getAverageSpeeds() {
        if (bigQuery == null) {
            return createMockAverageSpeeds();
        }

        try {
            String query = String.format("""
                SELECT 
                    location,
                    AVG(average_speed) as avg_speed
                FROM `%s.%s.%s`
                WHERE timestamp >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 15 MINUTE)
                GROUP BY location
                ORDER BY avg_speed ASC
                """, 
                googleCloudProperties.getProjectId(),
                googleCloudProperties.getBigQueryDataset(),
                "traffic_data_realtime"
            );

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);
            
            Map<String, Double> speeds = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                speeds.put(
                    row.get("location").getStringValue(),
                    row.get("avg_speed").getDoubleValue()
                );
            }
            
            return speeds;
            
        } catch (Exception e) {
            log.error("Error getting average speeds from BigQuery: {}", e.getMessage());
            return createMockAverageSpeeds();
        }
    }

    /**
     * Get traffic trends over time
     */
    public List<Map<String, Object>> getTrafficTrends() {
        if (bigQuery == null) {
            return createMockTrafficTrends();
        }

        try {
            String query = String.format("""
                SELECT 
                    TIMESTAMP_TRUNC(timestamp, MINUTE) as time_bucket,
                    AVG(average_speed) as avg_speed,
                    COUNT(*) as data_points,
                    COUNTIF(traffic_density = 'HIGH') as high_traffic_count
                FROM `%s.%s.%s`
                WHERE timestamp >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 2 HOUR)
                GROUP BY time_bucket
                ORDER BY time_bucket DESC
                LIMIT 120
                """, 
                googleCloudProperties.getProjectId(),
                googleCloudProperties.getBigQueryDataset(),
                "traffic_data_realtime"
            );

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);
            
            List<Map<String, Object>> trends = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, Object> trend = new HashMap<>();
                trend.put("timestamp", row.get("time_bucket").getStringValue());
                trend.put("averageSpeed", row.get("avg_speed").getDoubleValue());
                trend.put("dataPoints", row.get("data_points").getLongValue());
                trend.put("highTrafficCount", row.get("high_traffic_count").getLongValue());
                trends.add(trend);
            }
            
            return trends;
            
        } catch (Exception e) {
            log.error("Error getting traffic trends from BigQuery: {}", e.getMessage());
            return createMockTrafficTrends();
        }
    }

    /**
     * Create traffic data table in BigQuery
     */
    private void createTrafficDataTable() {
        try {
            Schema schema = Schema.of(
                Field.of("location", StandardSQLTypeName.STRING),
                Field.of("latitude", StandardSQLTypeName.FLOAT64),
                Field.of("longitude", StandardSQLTypeName.FLOAT64),
                Field.of("traffic_density", StandardSQLTypeName.STRING),
                Field.of("average_speed", StandardSQLTypeName.FLOAT64),
                Field.of("vehicle_count", StandardSQLTypeName.INT64),
                Field.of("weather_condition", StandardSQLTypeName.STRING),
                Field.of("timestamp", StandardSQLTypeName.TIMESTAMP)
            );

            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(trafficDataTable, tableDefinition).build();
            
            bigQuery.create(tableInfo);
            log.info("Created BigQuery table: {}", trafficDataTable);
            
        } catch (BigQueryException e) {
            if (e.getCode() == 409) {
                log.debug("BigQuery table already exists: {}", trafficDataTable);
            } else {
                log.error("Error creating BigQuery table: {}", e.getMessage());
            }
        }
    }

    /**
     * Create analytics table in BigQuery
     */
    private void createAnalyticsTable() {
        try {
            Schema schema = Schema.of(
                Field.of("analytics_type", StandardSQLTypeName.STRING),
                Field.of("data", StandardSQLTypeName.JSON),
                Field.of("timestamp", StandardSQLTypeName.TIMESTAMP)
            );

            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(analyticsTable, tableDefinition).build();
            
            bigQuery.create(tableInfo);
            log.info("Created BigQuery analytics table: {}", analyticsTable);
            
        } catch (BigQueryException e) {
            if (e.getCode() == 409) {
                log.debug("BigQuery analytics table already exists: {}", analyticsTable);
            } else {
                log.error("Error creating BigQuery analytics table: {}", e.getMessage());
            }
        }
    }

    /**
     * Create row data for BigQuery insertion
     */
    private Map<String, Object> createTrafficDataRow(TrafficData trafficData) {
        Map<String, Object> row = new HashMap<>();
        row.put("location", trafficData.getLocation());
        row.put("latitude", trafficData.getLatitude());
        row.put("longitude", trafficData.getLongitude());
        row.put("traffic_density", trafficData.getTrafficDensity().toString());
        row.put("average_speed", trafficData.getAverageSpeed());
        row.put("vehicle_count", trafficData.getVehicleCount());
        row.put("weather_condition", trafficData.getWeatherCondition());
        row.put("timestamp", trafficData.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return row;
    }

    /**
     * Store analytics results in BigQuery
     */
    private void storeAnalyticsResults(Map<String, Object> analytics) {
        try {
            Map<String, Object> row = new HashMap<>();
            row.put("analytics_type", "REAL_TIME_SUMMARY");
            row.put("data", analytics.toString()); // Would use proper JSON serialization in production
            row.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(analyticsTable)
                .addRow(row)
                .build();
            
            bigQuery.insertAll(insertRequest);
            
        } catch (Exception e) {
            log.error("Error storing analytics results: {}", e.getMessage());
        }
    }

    // Mock data methods for when BigQuery is not available
    private Map<String, Object> createMockTrafficSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLocations", 10L);
        summary.put("averageSpeed", 35.5);
        summary.put("totalVehicles", 450L);
        summary.put("highTrafficCount", 3L);
        summary.put("criticalTrafficCount", 1L);
        return summary;
    }

    private List<Map<String, Object>> createMockHotspots() {
        List<Map<String, Object>> hotspots = new ArrayList<>();
        Map<String, Object> hotspot = new HashMap<>();
        hotspot.put("location", "Times Square, New York");
        hotspot.put("latitude", 40.7580);
        hotspot.put("longitude", -73.9855);
        hotspot.put("averageSpeed", 15.2);
        hotspot.put("congestionCount", 8L);
        hotspots.add(hotspot);
        return hotspots;
    }

    private Map<String, Double> createMockAverageSpeeds() {
        Map<String, Double> speeds = new HashMap<>();
        speeds.put("Times Square, New York", 15.2);
        speeds.put("Union Square, New York", 25.8);
        speeds.put("Brooklyn Bridge, New York", 30.5);
        return speeds;
    }

    private List<Map<String, Object>> createMockTrafficTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("timestamp", LocalDateTime.now().minusMinutes(i * 5).toString());
            trend.put("averageSpeed", 30.0 + (Math.random() * 20 - 10));
            trend.put("dataPoints", 15L + (long)(Math.random() * 10));
            trend.put("highTrafficCount", (long)(Math.random() * 5));
            trends.add(trend);
        }
        return trends;
    }
}
