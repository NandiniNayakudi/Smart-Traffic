package com.traffic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.dto.*;
import com.traffic.model.TrafficData;
import com.traffic.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrafficController.class)
class TrafficControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrafficIngestionService trafficIngestionService;

    @MockBean
    private PredictionService predictionService;

    @MockBean
    private RouteService routeService;

    @MockBean
    private SignalOptimizationService signalOptimizationService;

    @MockBean
    private TrendAnalysisService trendAnalysisService;

    @MockBean
    private AuthService authService;

    private TrafficData sampleTrafficData;
    private PredictionResponse samplePredictionResponse;
    private RouteResponse sampleRouteResponse;

    @BeforeEach
    void setUp() {
        sampleTrafficData = new TrafficData();
        sampleTrafficData.setId(1L);
        sampleTrafficData.setLocation("Test Location");
        sampleTrafficData.setLatitude(16.5062);
        sampleTrafficData.setLongitude(80.6480);
        sampleTrafficData.setTrafficDensity(TrafficData.TrafficDensity.MODERATE);
        sampleTrafficData.setTimestamp(LocalDateTime.now());

        samplePredictionResponse = new PredictionResponse(
                TrafficData.TrafficDensity.MODERATE, 0.85);

        sampleRouteResponse = new RouteResponse(
                Arrays.asList("NH65", "Benz Circle", "PNBS Bus Stand"),
                "12 mins",
                "0.17 kg CO₂"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIngestTrafficData_Success() throws Exception {
        // Given
        when(trafficIngestionService.ingestTrafficData(any(TrafficData.class)))
                .thenReturn(sampleTrafficData);

        // When & Then
        mockMvc.perform(post("/traffic/ingest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTrafficData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Test Location"))
                .andExpect(jsonPath("$.trafficDensity").value("MODERATE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testIngestTrafficData_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/traffic/ingest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTrafficData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testPredictTraffic_Success() throws Exception {
        // Given
        when(predictionService.predictTraffic(any(PredictionRequest.class)))
                .thenReturn(samplePredictionResponse);

        // When & Then
        mockMvc.perform(get("/traffic/predict")
                .param("lat", "16.5062")
                .param("lon", "80.6480")
                .param("timestamp", "2025-06-04T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").value("MODERATE"))
                .andExpect(jsonPath("$.confidence").value(0.85));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetRouteRecommendation_Success() throws Exception {
        // Given
        when(routeService.getOptimalRoute(any(RouteRequest.class)))
                .thenReturn(sampleRouteResponse);

        // When & Then
        mockMvc.perform(get("/traffic/route")
                .param("source", "Vijayawada Junction")
                .param("destination", "PNBS Bus Stand")
                .param("eco", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedTime").value("12 mins"))
                .andExpect(jsonPath("$.carbonSaved").value("0.17 kg CO₂"));
    }

    @Test
    @WithMockUser(roles = "TRAFFIC_MANAGER")
    void testOptimizeSignal_Success() throws Exception {
        // Given
        SignalOptimizationRequest request = new SignalOptimizationRequest(
                "INT-112", 50, 30, 10, 20, null, null, null);
        
        SignalOptimizationResponse.SignalTimings timings = 
                new SignalOptimizationResponse.SignalTimings(40, 35, 15, 25);
        SignalOptimizationResponse response = new SignalOptimizationResponse(timings);
        
        when(signalOptimizationService.optimizeSignal(any(SignalOptimizationRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/traffic/signal/optimize")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalTimings.north").value(40))
                .andExpect(jsonPath("$.signalTimings.south").value(35));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTrafficTrends_Success() throws Exception {
        // Given
        TrendAnalysisResponse trendResponse = new TrendAnalysisResponse();
        trendResponse.setLocation("Vijayawada");
        trendResponse.setPeriod("monthly");
        
        when(trendAnalysisService.getTrafficTrends(anyString(), anyString()))
                .thenReturn(trendResponse);

        // When & Then
        mockMvc.perform(get("/traffic/trends")
                .param("location", "Vijayawada")
                .param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Vijayawada"))
                .andExpect(jsonPath("$.period").value("monthly"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTrainModel_Success() throws Exception {
        // Given
        ModelTrainingResponse trainingResponse = new ModelTrainingResponse(
                "Training Started", "v123", "Model training initiated successfully");
        
        when(predictionService.triggerModelTraining()).thenReturn(trainingResponse);

        // When & Then
        mockMvc.perform(post("/traffic/train")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Training Started"))
                .andExpect(jsonPath("$.modelId").value("v123"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testTrainModel_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/traffic/train")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTrafficData_Success() throws Exception {
        // Given
        List<TrafficData> trafficDataList = Arrays.asList(sampleTrafficData);
        when(trafficIngestionService.getTrafficData(anyString(), any(Integer.class)))
                .thenReturn(trafficDataList);

        // When & Then
        mockMvc.perform(get("/traffic/data")
                .param("location", "Test Location")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("Test Location"));
    }

    @Test
    void testIngestTrafficData_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/traffic/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTrafficData)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIngestTrafficData_InvalidInput() throws Exception {
        // Given
        TrafficData invalidData = new TrafficData();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/traffic/ingest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testPredictTraffic_MissingParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/traffic/predict")
                .param("lat", "16.5062"))
                // Missing lon and timestamp parameters
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TRAFFIC_MANAGER")
    void testOptimizeSignal_InvalidInput() throws Exception {
        // Given
        SignalOptimizationRequest invalidRequest = new SignalOptimizationRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/traffic/signal/optimize")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
