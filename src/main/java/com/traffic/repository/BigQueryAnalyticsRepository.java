package com.traffic.repository;

import com.traffic.model.BigQueryAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BigQueryAnalytics entities
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 */
@Repository
public interface BigQueryAnalyticsRepository extends JpaRepository<BigQueryAnalytics, Long> {
    
    /**
     * Find analytics by query name
     */
    List<BigQueryAnalytics> findByQueryNameOrderByQueryStartedAtDesc(String queryName);
    
    /**
     * Find analytics by query type
     */
    List<BigQueryAnalytics> findByQueryTypeOrderByQueryStartedAtDesc(BigQueryAnalytics.QueryType queryType);
    
    /**
     * Find analytics by query status
     */
    List<BigQueryAnalytics> findByQueryStatusOrderByQueryStartedAtDesc(BigQueryAnalytics.QueryStatus status);
    
    /**
     * Find analytics by dataset
     */
    List<BigQueryAnalytics> findByDatasetNameOrderByQueryStartedAtDesc(String datasetName);
    
    /**
     * Find analytics by table
     */
    List<BigQueryAnalytics> findByTableNameOrderByQueryStartedAtDesc(String tableName);
    
    /**
     * Find analytics by job ID
     */
    Optional<BigQueryAnalytics> findByJobId(String jobId);
    
    /**
     * Find successful queries
     */
    List<BigQueryAnalytics> findByQueryStatusOrderByExecutionTimeMsAsc(BigQueryAnalytics.QueryStatus status);
    
    /**
     * Find queries with cache hits
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.cacheHit = true ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findCacheHitQueries();
    
    /**
     * Find queries by execution time range
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.executionTimeMs BETWEEN :minTime AND :maxTime ORDER BY bqa.executionTimeMs ASC")
    List<BigQueryAnalytics> findQueriesByExecutionTime(@Param("minTime") Long minTime, 
                                                      @Param("maxTime") Long maxTime);
    
    /**
     * Find queries by cost range
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.totalCostUsd BETWEEN :minCost AND :maxCost ORDER BY bqa.totalCostUsd ASC")
    List<BigQueryAnalytics> findQueriesByCost(@Param("minCost") Double minCost, 
                                            @Param("maxCost") Double maxCost);
    
    /**
     * Find queries by bytes processed range
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.bytesProcessed BETWEEN :minBytes AND :maxBytes ORDER BY bqa.bytesProcessed ASC")
    List<BigQueryAnalytics> findQueriesByBytesProcessed(@Param("minBytes") Long minBytes, 
                                                       @Param("maxBytes") Long maxBytes);
    
    /**
     * Find queries by user
     */
    List<BigQueryAnalytics> findByUserEmailOrderByQueryStartedAtDesc(String userEmail);
    
    /**
     * Find queries by project
     */
    List<BigQueryAnalytics> findByProjectIdOrderByQueryStartedAtDesc(String projectId);
    
    /**
     * Find queries by location
     */
    List<BigQueryAnalytics> findByLocationOrderByQueryStartedAtDesc(String location);
    
    /**
     * Find recent queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.queryStartedAt >= :since ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findRecentQueries(@Param("since") LocalDateTime since);
    
    /**
     * Find failed queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.queryStatus = 'FAILED' ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findFailedQueries();
    
    /**
     * Find long-running queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.executionTimeMs > :threshold ORDER BY bqa.executionTimeMs DESC")
    List<BigQueryAnalytics> findLongRunningQueries(@Param("threshold") Long threshold);
    
    /**
     * Find expensive queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.totalCostUsd > :threshold ORDER BY bqa.totalCostUsd DESC")
    List<BigQueryAnalytics> findExpensiveQueries(@Param("threshold") Double threshold);
    
    /**
     * Get query statistics by type
     */
    @Query("SELECT bqa.queryType, COUNT(bqa), AVG(bqa.executionTimeMs), AVG(bqa.totalCostUsd) FROM BigQueryAnalytics bqa GROUP BY bqa.queryType")
    List<Object[]> getQueryStatsByType();
    
    /**
     * Get query count by status
     */
    @Query("SELECT bqa.queryStatus, COUNT(bqa) FROM BigQueryAnalytics bqa GROUP BY bqa.queryStatus")
    List<Object[]> getQueryCountByStatus();
    
    /**
     * Get average execution time by dataset
     */
    @Query("SELECT bqa.datasetName, AVG(bqa.executionTimeMs) FROM BigQueryAnalytics bqa GROUP BY bqa.datasetName")
    List<Object[]> getAvgExecutionTimeByDataset();
    
    /**
     * Get total cost by project
     */
    @Query("SELECT bqa.projectId, SUM(bqa.totalCostUsd) FROM BigQueryAnalytics bqa GROUP BY bqa.projectId")
    List<Object[]> getTotalCostByProject();
    
    /**
     * Find optimized queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.cacheHit = true AND bqa.executionTimeMs < 10000 AND bqa.totalCostUsd < 0.01")
    List<BigQueryAnalytics> findOptimizedQueries();
    
    /**
     * Get cache hit rate
     */
    @Query("SELECT (COUNT(CASE WHEN bqa.cacheHit = true THEN 1 END) * 100.0 / COUNT(*)) FROM BigQueryAnalytics bqa WHERE bqa.queryStartedAt >= :since")
    Optional<Double> getCacheHitRate(@Param("since") LocalDateTime since);
    
    /**
     * Get data processing rate statistics
     */
    @Query("SELECT AVG(bqa.rowsProcessed / (bqa.executionTimeMs / 1000.0)), MIN(bqa.rowsProcessed / (bqa.executionTimeMs / 1000.0)), MAX(bqa.rowsProcessed / (bqa.executionTimeMs / 1000.0)) FROM BigQueryAnalytics bqa WHERE bqa.executionTimeMs > 0")
    List<Object[]> getDataProcessingRateStats();
    
    /**
     * Find queries with warnings
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.warningMessages IS NOT NULL ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findQueriesWithWarnings();
    
    /**
     * Find dry run queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.dryRun = true ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findDryRunQueries();
    
    /**
     * Find queries with materialized views
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.materializedView = true ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findMaterializedViewQueries();
    
    /**
     * Find queries by slot usage range
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.slotMs BETWEEN :minSlots AND :maxSlots ORDER BY bqa.slotMs ASC")
    List<BigQueryAnalytics> findQueriesBySlotUsage(@Param("minSlots") Long minSlots, 
                                                  @Param("maxSlots") Long maxSlots);
    
    /**
     * Get hourly query volume
     */
    @Query("SELECT HOUR(bqa.queryStartedAt), COUNT(bqa) FROM BigQueryAnalytics bqa WHERE bqa.queryStartedAt >= :since GROUP BY HOUR(bqa.queryStartedAt) ORDER BY HOUR(bqa.queryStartedAt)")
    List<Object[]> getHourlyQueryVolume(@Param("since") LocalDateTime since);
    
    /**
     * Find queries by priority
     */
    List<BigQueryAnalytics> findByQueryPriorityOrderByQueryStartedAtDesc(String priority);
    
    /**
     * Get efficiency scores for queries
     */
    @Query("SELECT bqa.queryName, ((100 - (bqa.executionTimeMs / 1000.0)) + (100 - (bqa.bytesProcessed / 1000000.0))) / 2.0 FROM BigQueryAnalytics bqa WHERE bqa.executionTimeMs IS NOT NULL AND bqa.bytesProcessed IS NOT NULL")
    List<Object[]> getQueryEfficiencyScores();
    
    /**
     * Find queries with external data
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.externalDataConfig IS NOT NULL ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findExternalDataQueries();
    
    /**
     * Get cost efficiency by query type
     */
    @Query("SELECT bqa.queryType, AVG(bqa.totalCostUsd / NULLIF(bqa.resultRows, 0)) FROM BigQueryAnalytics bqa WHERE bqa.resultRows > 0 GROUP BY bqa.queryType")
    List<Object[]> getCostEfficiencyByType();
    
    /**
     * Find queries by clustering
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.clusteringFields IS NOT NULL ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findClusteredQueries();
    
    /**
     * Find partitioned queries
     */
    @Query("SELECT bqa FROM BigQueryAnalytics bqa WHERE bqa.partitioningField IS NOT NULL ORDER BY bqa.queryStartedAt DESC")
    List<BigQueryAnalytics> findPartitionedQueries();
    
    /**
     * Delete old analytics data
     */
    @Query("DELETE FROM BigQueryAnalytics bqa WHERE bqa.queryStartedAt < :cutoffTime")
    void deleteOldAnalytics(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Get query complexity distribution
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN LENGTH(bqa.querySql) < 500 THEN 'SIMPLE' " +
           "WHEN LENGTH(bqa.querySql) < 2000 THEN 'MEDIUM' " +
           "WHEN LENGTH(bqa.querySql) < 5000 THEN 'COMPLEX' " +
           "ELSE 'VERY_COMPLEX' END, " +
           "COUNT(bqa) " +
           "FROM BigQueryAnalytics bqa WHERE bqa.querySql IS NOT NULL GROUP BY " +
           "CASE " +
           "WHEN LENGTH(bqa.querySql) < 500 THEN 'SIMPLE' " +
           "WHEN LENGTH(bqa.querySql) < 2000 THEN 'MEDIUM' " +
           "WHEN LENGTH(bqa.querySql) < 5000 THEN 'COMPLEX' " +
           "ELSE 'VERY_COMPLEX' END")
    List<Object[]> getQueryComplexityDistribution();
}
