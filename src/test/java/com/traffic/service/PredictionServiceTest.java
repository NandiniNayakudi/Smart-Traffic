package com.traffic.service;

import com.traffic.dto.ModelTrainingResponse;
import com.traffic.dto.PredictionRequest;
import com.traffic.dto.PredictionResponse;
import com.traffic.model.TrafficData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private TrafficIngestionService trafficIngestionService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private PredictionService predictionService;

    private PredictionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new PredictionRequest();
        sampleRequest.setLat(16.5062);
        sampleRequest.setLon(80.6480);
        sampleRequest.setTimestamp(LocalDateTime.now());
        
        // Set private fields using reflection
        ReflectionTestUtils.setField(predictionService, "mlModelEndpoint", "http://localhost:5000/predict");
        ReflectionTestUtils.setField(predictionService, "mlModelTimeout", 5000);
    }

    @Test
    void testPredictTraffic_WithHistoricalData() {
        // Given
        TrafficData historicalData = new TrafficData();
        historicalData.setTrafficDensity(TrafficData.TrafficDensity.MODERATE);
        historicalData.setTimestamp(LocalDateTime.now().minusHours(1));
        
        List<TrafficData> historicalDataList = Arrays.asList(historicalData);
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(historicalDataList);

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertNotNull(result.getConfidence());
        assertTrue(result.getConfidence() >= 0.5 && result.getConfidence() <= 0.95);
        verify(trafficIngestionService, times(1))
                .getRecentTrafficData(anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void testPredictTraffic_WithoutHistoricalData() {
        // Given
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertNotNull(result.getConfidence());
        assertTrue(result.getConfidence() >= 0.5 && result.getConfidence() <= 0.95);
    }

    @Test
    void testPredictTraffic_PeakHourWeekday() {
        // Given
        LocalDateTime peakTime = LocalDateTime.now().withHour(8); // 8 AM - peak hour
        sampleRequest.setTimestamp(peakTime);
        sampleRequest.setHour(8);
        sampleRequest.setDayOfWeek("Monday");
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        assertEquals(TrafficData.TrafficDensity.HIGH, result.getPrediction());
        assertTrue(result.getConfidence() >= 0.8);
    }

    @Test
    void testPredictTraffic_OffPeakHour() {
        // Given
        LocalDateTime offPeakTime = LocalDateTime.now().withHour(2); // 2 AM - off peak
        sampleRequest.setTimestamp(offPeakTime);
        sampleRequest.setHour(2);
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        assertEquals(TrafficData.TrafficDensity.LOW, result.getPrediction());
    }

    @Test
    void testPredictTraffic_WeatherImpact() {
        // Given
        sampleRequest.setWeatherCondition("RAIN");
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        // Weather should impact prediction (increase traffic density)
        assertNotNull(result.getPrediction());
    }

    @Test
    void testPredictTraffic_Weekend() {
        // Given
        sampleRequest.setDayOfWeek("Saturday");
        sampleRequest.setHour(8); // Peak hour but weekend
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        // Weekend should have different prediction than weekday
        assertNotNull(result.getPrediction());
    }

    @Test
    void testTriggerModelTraining_Success() {
        // When
        ModelTrainingResponse result = predictionService.triggerModelTraining();

        // Then
        assertNotNull(result);
        assertEquals("Training Started", result.getStatus());
        assertNotNull(result.getModelId());
        assertTrue(result.getModelId().startsWith("v"));
        assertEquals("Model training initiated successfully", result.getMessage());
    }

    @Test
    void testPredictTraffic_WithNullRequest() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            predictionService.predictTraffic(null);
        });
    }

    @Test
    void testPredictTraffic_WithInvalidCoordinates() {
        // Given
        sampleRequest.setLat(null);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            predictionService.predictTraffic(sampleRequest);
        });
    }

    @Test
    void testPredictTraffic_ServiceException() {
        // Given
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            predictionService.predictTraffic(sampleRequest);
        });
    }

    @Test
    void testPredictTraffic_HighTrafficHistoricalData() {
        // Given
        TrafficData highTrafficData1 = new TrafficData();
        highTrafficData1.setTrafficDensity(TrafficData.TrafficDensity.HIGH);
        
        TrafficData highTrafficData2 = new TrafficData();
        highTrafficData2.setTrafficDensity(TrafficData.TrafficDensity.CRITICAL);
        
        List<TrafficData> highTrafficHistory = Arrays.asList(highTrafficData1, highTrafficData2);
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(highTrafficHistory);

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        // Should predict higher traffic due to historical data
        assertNotNull(result.getPrediction());
        assertTrue(result.getConfidence() > 0.5);
    }

    @Test
    void testPredictTraffic_LowTrafficHistoricalData() {
        // Given
        TrafficData lowTrafficData = new TrafficData();
        lowTrafficData.setTrafficDensity(TrafficData.TrafficDensity.LOW);
        
        List<TrafficData> lowTrafficHistory = Arrays.asList(lowTrafficData);
        
        when(trafficIngestionService.getRecentTrafficData(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(lowTrafficHistory);

        // When
        PredictionResponse result = predictionService.predictTraffic(sampleRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertTrue(result.getConfidence() > 0.5);
    }
}
