package com.traffic.repository;

import com.traffic.model.TrafficPrediction;
import com.traffic.model.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TrafficPrediction entities
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 */
@Repository
public interface TrafficPredictionRepository extends JpaRepository<TrafficPrediction, Long> {
    
    /**
     * Find predictions for a specific location
     */
    List<TrafficPrediction> findByLocationOrderByPredictionTimeDesc(String location);
    
    /**
     * Find predictions within a time range
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.predictedForTime BETWEEN :startTime AND :endTime ORDER BY tp.predictionTime DESC")
    List<TrafficPrediction> findPredictionsInTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find high-confidence predictions
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.confidenceScore >= :minConfidence ORDER BY tp.confidenceScore DESC")
    List<TrafficPrediction> findHighConfidencePredictions(@Param("minConfidence") Double minConfidence);
    
    /**
     * Find predictions by traffic density
     */
    List<TrafficPrediction> findByPredictedDensityOrderByPredictionTimeDesc(TrafficData.TrafficDensity density);
    
    /**
     * Find recent predictions for a location
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.location = :location AND tp.predictionTime >= :since ORDER BY tp.predictionTime DESC")
    List<TrafficPrediction> findRecentPredictionsForLocation(@Param("location") String location, 
                                                            @Param("since") LocalDateTime since);
    
    /**
     * Find predictions by model version
     */
    List<TrafficPrediction> findByModelVersionOrderByPredictionTimeDesc(String modelVersion);
    
    /**
     * Find predictions within geographic bounds
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.latitude BETWEEN :minLat AND :maxLat AND tp.longitude BETWEEN :minLng AND :maxLng")
    List<TrafficPrediction> findPredictionsInBounds(@Param("minLat") Double minLat, 
                                                   @Param("maxLat") Double maxLat,
                                                   @Param("minLng") Double minLng, 
                                                   @Param("maxLng") Double maxLng);
    
    /**
     * Find predictions with high congestion probability
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.congestionProbability >= :threshold ORDER BY tp.congestionProbability DESC")
    List<TrafficPrediction> findHighCongestionPredictions(@Param("threshold") Double threshold);
    
    /**
     * Get average confidence score for a model
     */
    @Query("SELECT AVG(tp.confidenceScore) FROM TrafficPrediction tp WHERE tp.modelVersion = :modelVersion")
    Optional<Double> getAverageConfidenceForModel(@Param("modelVersion") String modelVersion);
    
    /**
     * Count predictions by density type
     */
    @Query("SELECT tp.predictedDensity, COUNT(tp) FROM TrafficPrediction tp GROUP BY tp.predictedDensity")
    List<Object[]> countPredictionsByDensity();
    
    /**
     * Find predictions for next hour
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.predictedForTime BETWEEN :now AND :nextHour ORDER BY tp.confidenceScore DESC")
    List<TrafficPrediction> findPredictionsForNextHour(@Param("now") LocalDateTime now, 
                                                       @Param("nextHour") LocalDateTime nextHour);
    
    /**
     * Get prediction accuracy statistics
     */
    @Query("SELECT AVG(tp.confidenceScore), MIN(tp.confidenceScore), MAX(tp.confidenceScore) FROM TrafficPrediction tp WHERE tp.modelVersion = :modelVersion")
    List<Object[]> getPredictionAccuracyStats(@Param("modelVersion") String modelVersion);
    
    /**
     * Find predictions by Google Cloud job ID
     */
    List<TrafficPrediction> findByGoogleCloudJobIdOrderByPredictionTimeDesc(String jobId);
    
    /**
     * Find predictions by BigQuery dataset
     */
    List<TrafficPrediction> findByBigqueryDatasetOrderByPredictionTimeDesc(String dataset);
    
    /**
     * Delete old predictions
     */
    @Query("DELETE FROM TrafficPrediction tp WHERE tp.predictionTime < :cutoffTime")
    void deleteOldPredictions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find latest prediction for each location
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.predictionTime = (SELECT MAX(tp2.predictionTime) FROM TrafficPrediction tp2 WHERE tp2.location = tp.location)")
    List<TrafficPrediction> findLatestPredictionPerLocation();
    
    /**
     * Get prediction count by hour
     */
    @Query("SELECT HOUR(tp.predictionTime), COUNT(tp) FROM TrafficPrediction tp WHERE tp.predictionTime >= :since GROUP BY HOUR(tp.predictionTime) ORDER BY HOUR(tp.predictionTime)")
    List<Object[]> getPredictionCountByHour(@Param("since") LocalDateTime since);
    
    /**
     * Find predictions with specific ML algorithm
     */
    List<TrafficPrediction> findByMlAlgorithmOrderByPredictionTimeDesc(String algorithm);
    
    /**
     * Get average prediction horizon
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, tp.predictionTime, tp.predictedForTime)) FROM TrafficPrediction tp")
    Optional<Double> getAveragePredictionHorizonMinutes();
    
    /**
     * Find predictions that need validation
     */
    @Query("SELECT tp FROM TrafficPrediction tp WHERE tp.predictedForTime <= :now AND tp.confidenceScore >= :minConfidence")
    List<TrafficPrediction> findPredictionsForValidation(@Param("now") LocalDateTime now, 
                                                        @Param("minConfidence") Double minConfidence);
}
