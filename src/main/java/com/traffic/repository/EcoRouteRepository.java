package com.traffic.repository;

import com.traffic.model.EcoRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for EcoRoute entities
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 */
@Repository
public interface EcoRouteRepository extends JpaRepository<EcoRoute, Long> {
    
    /**
     * Find routes by origin and destination
     */
    List<EcoRoute> findByOriginAndDestinationOrderByEcoScoreDesc(String origin, String destination);
    
    /**
     * Find routes by route type
     */
    List<EcoRoute> findByRouteTypeOrderByEcoScoreDesc(EcoRoute.RouteType routeType);
    
    /**
     * Find routes by optimization priority
     */
    List<EcoRoute> findByOptimizationPriorityOrderByEcoScoreDesc(EcoRoute.OptimizationPriority priority);
    
    /**
     * Find eco-friendly routes with high eco score
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.ecoScore >= :minScore ORDER BY er.ecoScore DESC")
    List<EcoRoute> findEcoFriendlyRoutes(@Param("minScore") Double minScore);
    
    /**
     * Find routes with low CO2 emissions
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.co2EmissionsKg <= :maxEmissions ORDER BY er.co2EmissionsKg ASC")
    List<EcoRoute> findLowEmissionRoutes(@Param("maxEmissions") Double maxEmissions);
    
    /**
     * Find routes within distance range
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.distanceKm BETWEEN :minDistance AND :maxDistance ORDER BY er.ecoScore DESC")
    List<EcoRoute> findRoutesByDistanceRange(@Param("minDistance") Double minDistance, 
                                           @Param("maxDistance") Double maxDistance);
    
    /**
     * Find routes within duration range
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.estimatedDurationMinutes BETWEEN :minDuration AND :maxDuration ORDER BY er.ecoScore DESC")
    List<EcoRoute> findRoutesByDurationRange(@Param("minDuration") Integer minDuration, 
                                           @Param("maxDuration") Integer maxDuration);
    
    /**
     * Find routes within geographic bounds
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.originLat BETWEEN :minLat AND :maxLat AND er.originLng BETWEEN :minLng AND :maxLng")
    List<EcoRoute> findRoutesInBounds(@Param("minLat") Double minLat, 
                                    @Param("maxLat") Double maxLat,
                                    @Param("minLng") Double minLng, 
                                    @Param("maxLng") Double maxLng);
    
    /**
     * Find routes that contribute to SDG 11
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.sdg11ContributionScore >= :minScore ORDER BY er.sdg11ContributionScore DESC")
    List<EcoRoute> findSDG11ContributingRoutes(@Param("minScore") Double minScore);
    
    /**
     * Find routes with high air quality impact
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.airQualityImpact >= :minImpact ORDER BY er.airQualityImpact DESC")
    List<EcoRoute> findHighAirQualityRoutes(@Param("minImpact") Double minImpact);
    
    /**
     * Get average eco score by route type
     */
    @Query("SELECT er.routeType, AVG(er.ecoScore) FROM EcoRoute er GROUP BY er.routeType")
    List<Object[]> getAverageEcoScoreByType();
    
    /**
     * Get total CO2 savings
     */
    @Query("SELECT SUM(er.co2EmissionsKg) FROM EcoRoute er WHERE er.calculatedAt >= :since")
    Optional<Double> getTotalCO2Emissions(@Param("since") LocalDateTime since);
    
    /**
     * Find most efficient routes
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.trafficEfficiencyScore >= :minEfficiency ORDER BY er.trafficEfficiencyScore DESC")
    List<EcoRoute> findMostEfficientRoutes(@Param("minEfficiency") Double minEfficiency);
    
    /**
     * Find routes by Google Maps route ID
     */
    Optional<EcoRoute> findByGoogleMapsRouteId(String routeId);
    
    /**
     * Find recent routes
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.calculatedAt >= :since ORDER BY er.calculatedAt DESC")
    List<EcoRoute> findRecentRoutes(@Param("since") LocalDateTime since);
    
    /**
     * Get route statistics
     */
    @Query("SELECT COUNT(er), AVG(er.ecoScore), AVG(er.co2EmissionsKg), AVG(er.distanceKm) FROM EcoRoute er")
    List<Object[]> getRouteStatistics();
    
    /**
     * Find routes with alternative options
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.alternativeRoutesCount > 0 ORDER BY er.alternativeRoutesCount DESC")
    List<EcoRoute> findRoutesWithAlternatives();
    
    /**
     * Find routes by fuel consumption range
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.fuelConsumptionLiters BETWEEN :minFuel AND :maxFuel ORDER BY er.fuelConsumptionLiters ASC")
    List<EcoRoute> findRoutesByFuelConsumption(@Param("minFuel") Double minFuel, 
                                             @Param("maxFuel") Double maxFuel);
    
    /**
     * Get environmental impact distribution
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN er.ecoScore >= 80 THEN 'EXCELLENT' " +
           "WHEN er.ecoScore >= 60 THEN 'GOOD' " +
           "WHEN er.ecoScore >= 40 THEN 'MODERATE' " +
           "ELSE 'POOR' END, " +
           "COUNT(er) " +
           "FROM EcoRoute er GROUP BY " +
           "CASE " +
           "WHEN er.ecoScore >= 80 THEN 'EXCELLENT' " +
           "WHEN er.ecoScore >= 60 THEN 'GOOD' " +
           "WHEN er.ecoScore >= 40 THEN 'MODERATE' " +
           "ELSE 'POOR' END")
    List<Object[]> getEnvironmentalImpactDistribution();
    
    /**
     * Find top eco-friendly routes
     */
    @Query("SELECT er FROM EcoRoute er ORDER BY er.ecoScore DESC")
    List<EcoRoute> findTopEcoRoutes();
    
    /**
     * Get route count by optimization priority
     */
    @Query("SELECT er.optimizationPriority, COUNT(er) FROM EcoRoute er GROUP BY er.optimizationPriority")
    List<Object[]> getRouteCountByPriority();
    
    /**
     * Find routes with weather impact
     */
    @Query("SELECT er FROM EcoRoute er WHERE er.weatherImpactFactor IS NOT NULL ORDER BY er.weatherImpactFactor DESC")
    List<EcoRoute> findRoutesWithWeatherImpact();
    
    /**
     * Delete old route calculations
     */
    @Query("DELETE FROM EcoRoute er WHERE er.calculatedAt < :cutoffTime")
    void deleteOldRoutes(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find routes by sustainability score range
     */
    @Query("SELECT er FROM EcoRoute er WHERE " +
           "(er.ecoScore * 0.6 + er.trafficEfficiencyScore * 0.4) BETWEEN :minScore AND :maxScore " +
           "ORDER BY (er.ecoScore * 0.6 + er.trafficEfficiencyScore * 0.4) DESC")
    List<EcoRoute> findRoutesBySustainabilityScore(@Param("minScore") Double minScore, 
                                                  @Param("maxScore") Double maxScore);
}
