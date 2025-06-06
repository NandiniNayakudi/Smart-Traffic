package com.traffic.repository;

import com.traffic.model.MLModelMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for MLModelMetrics entities
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 */
@Repository
public interface MLModelMetricsRepository extends JpaRepository<MLModelMetrics, Long> {
    
    /**
     * Find metrics by model name
     */
    List<MLModelMetrics> findByModelNameOrderByCreatedAtDesc(String modelName);
    
    /**
     * Find metrics by model type
     */
    List<MLModelMetrics> findByModelTypeOrderByAccuracyScoreDesc(MLModelMetrics.ModelType modelType);
    
    /**
     * Find metrics by model status
     */
    List<MLModelMetrics> findByModelStatusOrderByCreatedAtDesc(MLModelMetrics.ModelStatus status);
    
    /**
     * Find active models
     */
    List<MLModelMetrics> findByModelStatusOrderByAccuracyScoreDesc(MLModelMetrics.ModelStatus status);
    
    /**
     * Find models with high accuracy
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.accuracyScore >= :minAccuracy ORDER BY mm.accuracyScore DESC")
    List<MLModelMetrics> findHighAccuracyModels(@Param("minAccuracy") Double minAccuracy);
    
    /**
     * Find production-ready models
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.modelStatus = 'DEPLOYED' AND mm.accuracyScore >= 85.0 AND mm.inferenceLatencyMs <= 100.0")
    List<MLModelMetrics> findProductionReadyModels();
    
    /**
     * Find latest version of each model
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.createdAt = (SELECT MAX(mm2.createdAt) FROM MLModelMetrics mm2 WHERE mm2.modelName = mm.modelName)")
    List<MLModelMetrics> findLatestVersionOfEachModel();
    
    /**
     * Find models by Google Cloud model ID
     */
    Optional<MLModelMetrics> findByGoogleCloudModelId(String modelId);
    
    /**
     * Find models by BigQuery dataset
     */
    List<MLModelMetrics> findByBigqueryDatasetOrderByCreatedAtDesc(String dataset);
    
    /**
     * Get average accuracy by model type
     */
    @Query("SELECT mm.modelType, AVG(mm.accuracyScore) FROM MLModelMetrics mm GROUP BY mm.modelType")
    List<Object[]> getAverageAccuracyByType();
    
    /**
     * Get model performance statistics
     */
    @Query("SELECT COUNT(mm), AVG(mm.accuracyScore), MIN(mm.accuracyScore), MAX(mm.accuracyScore) FROM MLModelMetrics mm WHERE mm.modelStatus = 'DEPLOYED'")
    List<Object[]> getModelPerformanceStats();
    
    /**
     * Find models with training in progress
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.modelStatus IN ('TRAINING', 'VALIDATING') ORDER BY mm.createdAt DESC")
    List<MLModelMetrics> findModelsInTraining();
    
    /**
     * Find models by training duration range
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.trainingDurationMinutes BETWEEN :minDuration AND :maxDuration ORDER BY mm.trainingDurationMinutes ASC")
    List<MLModelMetrics> findModelsByTrainingDuration(@Param("minDuration") Integer minDuration, 
                                                     @Param("maxDuration") Integer maxDuration);
    
    /**
     * Find models with high prediction rate
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.predictionsPerMinute >= :minRate ORDER BY mm.predictionsPerMinute DESC")
    List<MLModelMetrics> findHighThroughputModels(@Param("minRate") Integer minRate);
    
    /**
     * Find models by F1 score range
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.f1Score BETWEEN :minF1 AND :maxF1 ORDER BY mm.f1Score DESC")
    List<MLModelMetrics> findModelsByF1Score(@Param("minF1") Double minF1, 
                                           @Param("maxF1") Double maxF1);
    
    /**
     * Get training progress for active training jobs
     */
    @Query("SELECT mm.modelName, mm.epochsCompleted, mm.totalEpochs FROM MLModelMetrics mm WHERE mm.modelStatus = 'TRAINING'")
    List<Object[]> getTrainingProgress();
    
    /**
     * Find models with low latency
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.inferenceLatencyMs <= :maxLatency ORDER BY mm.inferenceLatencyMs ASC")
    List<MLModelMetrics> findLowLatencyModels(@Param("maxLatency") Double maxLatency);
    
    /**
     * Find models by deployment environment
     */
    List<MLModelMetrics> findByDeploymentEnvironmentOrderByCreatedAtDesc(String environment);
    
    /**
     * Get model count by status
     */
    @Query("SELECT mm.modelStatus, COUNT(mm) FROM MLModelMetrics mm GROUP BY mm.modelStatus")
    List<Object[]> getModelCountByStatus();
    
    /**
     * Find models with recent predictions
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.lastPredictionAt >= :since ORDER BY mm.lastPredictionAt DESC")
    List<MLModelMetrics> findModelsWithRecentPredictions(@Param("since") LocalDateTime since);
    
    /**
     * Find models by memory usage range
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.memoryUsageMb BETWEEN :minMemory AND :maxMemory ORDER BY mm.memoryUsageMb ASC")
    List<MLModelMetrics> findModelsByMemoryUsage(@Param("minMemory") Double minMemory, 
                                                @Param("maxMemory") Double maxMemory);
    
    /**
     * Get efficiency scores for all deployed models
     */
    @Query("SELECT mm.modelName, (mm.accuracyScore - LEAST(50.0, mm.inferenceLatencyMs / 2.0)) FROM MLModelMetrics mm WHERE mm.modelStatus = 'DEPLOYED'")
    List<Object[]> getModelEfficiencyScores();
    
    /**
     * Find models by ROC AUC score
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.rocAucScore >= :minAuc ORDER BY mm.rocAucScore DESC")
    List<MLModelMetrics> findModelsByRocAuc(@Param("minAuc") Double minAuc);
    
    /**
     * Find models with cross-validation
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.crossValidationFolds > 0 ORDER BY mm.crossValidationFolds DESC")
    List<MLModelMetrics> findModelsWithCrossValidation();
    
    /**
     * Get training data size statistics
     */
    @Query("SELECT AVG(mm.trainingDataSize), MIN(mm.trainingDataSize), MAX(mm.trainingDataSize) FROM MLModelMetrics mm")
    List<Object[]> getTrainingDataSizeStats();
    
    /**
     * Find models by optimizer
     */
    List<MLModelMetrics> findByOptimizerOrderByAccuracyScoreDesc(String optimizer);
    
    /**
     * Find models by learning rate range
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.learningRate BETWEEN :minRate AND :maxRate ORDER BY mm.accuracyScore DESC")
    List<MLModelMetrics> findModelsByLearningRate(@Param("minRate") Double minRate, 
                                                 @Param("maxRate") Double maxRate);
    
    /**
     * Delete old model metrics
     */
    @Query("DELETE FROM MLModelMetrics mm WHERE mm.createdAt < :cutoffTime AND mm.modelStatus NOT IN ('DEPLOYED', 'TRAINING')")
    void deleteOldMetrics(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find models with early stopping
     */
    @Query("SELECT mm FROM MLModelMetrics mm WHERE mm.earlyStoppingPatience > 0 ORDER BY mm.accuracyScore DESC")
    List<MLModelMetrics> findModelsWithEarlyStopping();
    
    /**
     * Get model size distribution
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN mm.modelSizeMb < 10 THEN 'SMALL' " +
           "WHEN mm.modelSizeMb < 100 THEN 'MEDIUM' " +
           "WHEN mm.modelSizeMb < 1000 THEN 'LARGE' " +
           "ELSE 'VERY_LARGE' END, " +
           "COUNT(mm) " +
           "FROM MLModelMetrics mm GROUP BY " +
           "CASE " +
           "WHEN mm.modelSizeMb < 10 THEN 'SMALL' " +
           "WHEN mm.modelSizeMb < 100 THEN 'MEDIUM' " +
           "WHEN mm.modelSizeMb < 1000 THEN 'LARGE' " +
           "ELSE 'VERY_LARGE' END")
    List<Object[]> getModelSizeDistribution();
}
