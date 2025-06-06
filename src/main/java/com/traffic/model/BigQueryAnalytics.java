package com.traffic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Entity for BigQuery analytics and query performance tracking
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 * Tracks BigQuery operations for real-time traffic data analysis
 */
@Entity
@Table(name = "bigquery_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigQueryAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Query name is required")
    @Column(name = "query_name", nullable = false)
    private String queryName;
    
    @NotBlank(message = "Dataset is required")
    @Column(name = "dataset_name", nullable = false)
    private String datasetName;
    
    @NotBlank(message = "Table name is required")
    @Column(name = "table_name", nullable = false)
    private String tableName;
    
    @Column(name = "query_sql", columnDefinition = "TEXT")
    private String querySql;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false)
    private QueryType queryType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "query_status", nullable = false)
    private QueryStatus queryStatus;
    
    @Column(name = "job_id")
    private String jobId;
    
    @Column(name = "rows_processed")
    private Long rowsProcessed;
    
    @Column(name = "bytes_processed")
    private Long bytesProcessed;
    
    @Column(name = "bytes_billed")
    private Long bytesBilled;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "slot_ms")
    private Long slotMs;
    
    @Column(name = "total_cost_usd")
    private Double totalCostUsd;
    
    @Column(name = "cache_hit")
    private Boolean cacheHit;
    
    @Column(name = "result_rows")
    private Long resultRows;
    
    @Column(name = "result_size_bytes")
    private Long resultSizeBytes;
    
    @Column(name = "query_priority")
    private String queryPriority;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "project_id")
    private String projectId;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "labels", columnDefinition = "TEXT")
    private String labels; // JSON string
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "warning_messages", columnDefinition = "TEXT")
    private String warningMessages;
    
    @Column(name = "query_parameters", columnDefinition = "TEXT")
    private String queryParameters; // JSON string
    
    @Column(name = "destination_table")
    private String destinationTable;
    
    @Column(name = "write_disposition")
    private String writeDisposition;
    
    @Column(name = "create_disposition")
    private String createDisposition;
    
    @Column(name = "use_legacy_sql")
    private Boolean useLegacySql;
    
    @Column(name = "use_query_cache")
    private Boolean useQueryCache;
    
    @Column(name = "maximum_bytes_billed")
    private Long maximumBytesBilled;
    
    @Column(name = "dry_run")
    private Boolean dryRun;
    
    @Column(name = "clustering_fields")
    private String clusteringFields;
    
    @Column(name = "partitioning_field")
    private String partitioningField;
    
    @Column(name = "schema_update_options")
    private String schemaUpdateOptions;
    
    @Column(name = "time_partitioning_type")
    private String timePartitioningType;
    
    @Column(name = "range_partitioning", columnDefinition = "TEXT")
    private String rangePartitioning; // JSON string
    
    @Column(name = "materialized_view")
    private Boolean materializedView;
    
    @Column(name = "external_data_config", columnDefinition = "TEXT")
    private String externalDataConfig; // JSON string
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "query_started_at")
    private LocalDateTime queryStartedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "query_completed_at")
    private LocalDateTime queryCompletedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * BigQuery query types for traffic analytics
     */
    public enum QueryType {
        TRAFFIC_VOLUME_ANALYSIS("Traffic volume analysis and trends"),
        CONGESTION_HOTSPOT_DETECTION("Congestion hotspot identification"),
        ROUTE_OPTIMIZATION_DATA("Route optimization data processing"),
        PREDICTIVE_MODELING("Predictive modeling data preparation"),
        REAL_TIME_MONITORING("Real-time traffic monitoring"),
        HISTORICAL_ANALYSIS("Historical traffic pattern analysis"),
        ENVIRONMENTAL_IMPACT("Environmental impact assessment"),
        PERFORMANCE_METRICS("System performance metrics"),
        DATA_QUALITY_CHECK("Data quality and validation"),
        AGGREGATION("Data aggregation and summarization"),
        EXPORT("Data export operations"),
        STREAMING_INSERT("Streaming data insertion");
        
        private final String description;
        
        QueryType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Query execution status
     */
    public enum QueryStatus {
        PENDING("Query is pending execution"),
        RUNNING("Query is currently running"),
        DONE("Query completed successfully"),
        FAILED("Query execution failed"),
        CANCELLED("Query was cancelled"),
        TIMEOUT("Query execution timed out");
        
        private final String description;
        
        QueryStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Calculate query efficiency score
     */
    public Double calculateEfficiencyScore() {
        if (executionTimeMs == null || bytesProcessed == null) {
            return 0.0;
        }
        
        // Lower execution time and bytes processed = higher efficiency
        double timeScore = Math.max(0, 100 - (executionTimeMs / 1000.0)); // Penalty for seconds
        double dataScore = Math.max(0, 100 - (bytesProcessed / 1_000_000.0)); // Penalty for MB
        
        return (timeScore + dataScore) / 2.0;
    }
    
    /**
     * Calculate cost efficiency
     */
    public Double calculateCostEfficiency() {
        if (totalCostUsd == null || resultRows == null || resultRows == 0) {
            return 0.0;
        }
        
        // Cost per result row (lower is better)
        double costPerRow = totalCostUsd / resultRows;
        return Math.max(0, 100 - (costPerRow * 1000)); // Scale for readability
    }
    
    /**
     * Check if query was optimized
     */
    public boolean isOptimized() {
        return cacheHit != null && cacheHit && 
               executionTimeMs != null && executionTimeMs < 10000 && // Less than 10 seconds
               totalCostUsd != null && totalCostUsd < 0.01; // Less than 1 cent
    }
    
    /**
     * Get data processing rate (rows per second)
     */
    public Double getDataProcessingRate() {
        if (rowsProcessed == null || executionTimeMs == null || executionTimeMs == 0) {
            return 0.0;
        }
        
        return (rowsProcessed.doubleValue() / executionTimeMs.doubleValue()) * 1000.0;
    }
    
    /**
     * Calculate query complexity score
     */
    public Integer getQueryComplexityScore() {
        if (querySql == null) return 0;
        
        int complexity = 0;
        String sql = querySql.toLowerCase();
        
        // Count complex operations
        if (sql.contains("join")) complexity += 2;
        if (sql.contains("subquery") || sql.contains("with")) complexity += 3;
        if (sql.contains("window")) complexity += 2;
        if (sql.contains("group by")) complexity += 1;
        if (sql.contains("order by")) complexity += 1;
        if (sql.contains("having")) complexity += 2;
        
        return complexity;
    }
}
