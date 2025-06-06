package com.traffic.repository;

import com.traffic.model.TrafficData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TrafficData entity
 */
@Repository
public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {

    /**
     * Find traffic data by location containing the given string (case insensitive)
     */
    List<TrafficData> findByLocationContainingIgnoreCaseOrderByTimestampDesc(String location, Pageable pageable);

    /**
     * Find all traffic data ordered by timestamp descending
     */
    List<TrafficData> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find traffic data by location and timestamp after a given date
     */
    List<TrafficData> findByLocationContainingIgnoreCaseAndTimestampAfterOrderByTimestampDesc(
            String location, LocalDateTime timestamp);

    /**
     * Find traffic data after a given timestamp
     */
    List<TrafficData> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);

    /**
     * Find traffic data within a geographic bounding box and after a given timestamp
     */
    List<TrafficData> findByLatitudeBetweenAndLongitudeBetweenAndTimestampAfter(
            Double minLatitude, Double maxLatitude,
            Double minLongitude, Double maxLongitude,
            LocalDateTime timestamp);

    /**
     * Find traffic data by traffic density
     */
    List<TrafficData> findByTrafficDensityOrderByTimestampDesc(TrafficData.TrafficDensity trafficDensity);

    /**
     * Find recent traffic data for a specific location
     */
    @Query("SELECT t FROM TrafficData t WHERE t.location LIKE %:location% AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<TrafficData> findRecentTrafficDataByLocation(@Param("location") String location, 
                                                     @Param("since") LocalDateTime since);

    /**
     * Get traffic data count by density for a location
     */
    @Query("SELECT t.trafficDensity, COUNT(t) FROM TrafficData t WHERE t.location LIKE %:location% GROUP BY t.trafficDensity")
    List<Object[]> getTrafficDensityCountByLocation(@Param("location") String location);

    /**
     * Get average vehicle count by hour for a location
     */
    @Query("SELECT HOUR(t.timestamp), AVG(t.vehicleCount) FROM TrafficData t WHERE t.location LIKE %:location% AND t.vehicleCount IS NOT NULL GROUP BY HOUR(t.timestamp) ORDER BY HOUR(t.timestamp)")
    List<Object[]> getAverageVehicleCountByHour(@Param("location") String location);

    /**
     * Get traffic data for the last N hours
     */
    @Query("SELECT t FROM TrafficData t WHERE t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<TrafficData> findTrafficDataSince(@Param("since") LocalDateTime since);

    /**
     * Delete old traffic data before a given timestamp
     */
    void deleteByTimestampBefore(LocalDateTime timestamp);

    /**
     * Count traffic data points for a location
     */
    long countByLocationContainingIgnoreCase(String location);

    /**
     * Find traffic data by coordinates within a radius (approximate)
     */
    @Query("SELECT t FROM TrafficData t WHERE " +
           "ABS(t.latitude - :lat) <= :radius AND " +
           "ABS(t.longitude - :lon) <= :radius " +
           "ORDER BY t.timestamp DESC")
    List<TrafficData> findTrafficDataNearCoordinates(@Param("lat") Double latitude,
                                                    @Param("lon") Double longitude,
                                                    @Param("radius") Double radius);

    /**
     * Count distinct locations (for total intersections)
     */
    @Query("SELECT COUNT(DISTINCT t.location) FROM TrafficData t")
    long countDistinctLocations();

    /**
     * Count traffic data by density types and timestamp after
     */
    long countByTrafficDensityInAndTimestampAfter(List<TrafficData.TrafficDensity> densities, LocalDateTime timestamp);

    /**
     * Get traffic data by density and timestamp after
     */
    List<TrafficData> findByTrafficDensityInAndTimestampAfterOrderByTimestampDesc(
            List<TrafficData.TrafficDensity> densities, LocalDateTime timestamp);

    /**
     * Get latest traffic data for each location
     */
    @Query("SELECT t FROM TrafficData t WHERE t.timestamp = " +
           "(SELECT MAX(t2.timestamp) FROM TrafficData t2 WHERE t2.location = t.location)")
    List<TrafficData> findLatestTrafficDataPerLocation();

    /**
     * Get traffic statistics by location
     */
    @Query("SELECT t.location, COUNT(t), AVG(t.vehicleCount), AVG(t.averageSpeed) " +
           "FROM TrafficData t WHERE t.timestamp >= :since " +
           "GROUP BY t.location ORDER BY COUNT(t) DESC")
    List<Object[]> getTrafficStatsByLocation(@Param("since") LocalDateTime since);
}
