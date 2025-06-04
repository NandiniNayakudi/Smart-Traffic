package com.traffic.service;

import com.traffic.model.TrafficData;
import com.traffic.repository.TrafficDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficIngestionServiceTest {

    @Mock
    private TrafficDataRepository trafficDataRepository;

    @InjectMocks
    private TrafficIngestionService trafficIngestionService;

    private TrafficData sampleTrafficData;

    @BeforeEach
    void setUp() {
        sampleTrafficData = new TrafficData();
        sampleTrafficData.setId(1L);
        sampleTrafficData.setLocation("Test Location");
        sampleTrafficData.setLatitude(16.5062);
        sampleTrafficData.setLongitude(80.6480);
        sampleTrafficData.setTrafficDensity(TrafficData.TrafficDensity.MODERATE);
        sampleTrafficData.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testIngestTrafficData_Success() {
        // Given
        when(trafficDataRepository.save(any(TrafficData.class))).thenReturn(sampleTrafficData);

        // When
        TrafficData result = trafficIngestionService.ingestTrafficData(sampleTrafficData);

        // Then
        assertNotNull(result);
        assertEquals(sampleTrafficData.getLocation(), result.getLocation());
        assertEquals(sampleTrafficData.getTrafficDensity(), result.getTrafficDensity());
        verify(trafficDataRepository, times(1)).save(any(TrafficData.class));
    }

    @Test
    void testIngestTrafficData_WithNullTimestamp() {
        // Given
        sampleTrafficData.setTimestamp(null);
        when(trafficDataRepository.save(any(TrafficData.class))).thenReturn(sampleTrafficData);

        // When
        TrafficData result = trafficIngestionService.ingestTrafficData(sampleTrafficData);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        verify(trafficDataRepository, times(1)).save(any(TrafficData.class));
    }

    @Test
    void testIngestTrafficData_InvalidLatitude() {
        // Given
        sampleTrafficData.setLatitude(100.0); // Invalid latitude

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            trafficIngestionService.ingestTrafficData(sampleTrafficData);
        });
    }

    @Test
    void testIngestTrafficData_InvalidLongitude() {
        // Given
        sampleTrafficData.setLongitude(200.0); // Invalid longitude

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            trafficIngestionService.ingestTrafficData(sampleTrafficData);
        });
    }

    @Test
    void testIngestTrafficData_NullLocation() {
        // Given
        sampleTrafficData.setLocation(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            trafficIngestionService.ingestTrafficData(sampleTrafficData);
        });
    }

    @Test
    void testGetTrafficData_WithLocation() {
        // Given
        String location = "Test Location";
        Integer limit = 10;
        List<TrafficData> expectedData = Arrays.asList(sampleTrafficData);
        
        when(trafficDataRepository.findByLocationContainingIgnoreCaseOrderByTimestampDesc(
                eq(location), any(PageRequest.class))).thenReturn(expectedData);

        // When
        List<TrafficData> result = trafficIngestionService.getTrafficData(location, limit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleTrafficData.getLocation(), result.get(0).getLocation());
        verify(trafficDataRepository, times(1))
                .findByLocationContainingIgnoreCaseOrderByTimestampDesc(eq(location), any(PageRequest.class));
    }

    @Test
    void testGetTrafficData_WithoutLocation() {
        // Given
        Integer limit = 10;
        List<TrafficData> expectedData = Arrays.asList(sampleTrafficData);
        
        when(trafficDataRepository.findAllByOrderByTimestampDesc(any(PageRequest.class)))
                .thenReturn(expectedData);

        // When
        List<TrafficData> result = trafficIngestionService.getTrafficData(null, limit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trafficDataRepository, times(1))
                .findAllByOrderByTimestampDesc(any(PageRequest.class));
    }

    @Test
    void testGetRecentTrafficData() {
        // Given
        Double latitude = 16.5062;
        Double longitude = 80.6480;
        int hours = 24;
        List<TrafficData> expectedData = Arrays.asList(sampleTrafficData);
        
        when(trafficDataRepository.findByLatitudeBetweenAndLongitudeBetweenAndTimestampAfter(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any(LocalDateTime.class)))
                .thenReturn(expectedData);

        // When
        List<TrafficData> result = trafficIngestionService.getRecentTrafficData(latitude, longitude, hours);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trafficDataRepository, times(1))
                .findByLatitudeBetweenAndLongitudeBetweenAndTimestampAfter(
                        anyDouble(), anyDouble(), anyDouble(), anyDouble(), any(LocalDateTime.class));
    }

    @Test
    void testBatchIngestTrafficData() {
        // Given
        TrafficData data1 = new TrafficData();
        data1.setLocation("Location 1");
        data1.setLatitude(16.5062);
        data1.setLongitude(80.6480);
        data1.setTrafficDensity(TrafficData.TrafficDensity.LOW);

        TrafficData data2 = new TrafficData();
        data2.setLocation("Location 2");
        data2.setLatitude(16.5063);
        data2.setLongitude(80.6481);
        data2.setTrafficDensity(TrafficData.TrafficDensity.HIGH);

        List<TrafficData> inputData = Arrays.asList(data1, data2);
        when(trafficDataRepository.saveAll(anyList())).thenReturn(inputData);

        // When
        List<TrafficData> result = trafficIngestionService.batchIngestTrafficData(inputData);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(trafficDataRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testBatchIngestTrafficData_WithRepositoryException() {
        // Given
        List<TrafficData> inputData = Arrays.asList(sampleTrafficData);
        when(trafficDataRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            trafficIngestionService.batchIngestTrafficData(inputData);
        });
    }
}
