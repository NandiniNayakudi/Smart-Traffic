package com.traffic.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.traffic.config.GoogleCloudConfig;
import com.traffic.model.TrafficData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Google Cloud Pub/Sub Service for Real-Time Traffic Messaging
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@Slf4j
public class GoogleCloudPubSubService {

    private final Publisher publisher;
    private final GoogleCloudConfig.GoogleCloudProperties googleCloudProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleCloudPubSubService(@Autowired(required = false) Publisher publisher,
                                   GoogleCloudConfig.GoogleCloudProperties googleCloudProperties) {
        this.publisher = publisher;
        this.googleCloudProperties = googleCloudProperties;
    }
    
    @Autowired
    @Lazy
    private RealTimeTrafficService realTimeTrafficService;
    
    private Subscriber subscriber;

    /**
     * Initialize Pub/Sub subscriber
     */
    @PostConstruct
    public void initializeSubscriber() {
        if (publisher == null) {
            log.warn("Google Cloud Pub/Sub not configured - using local messaging");
            return;
        }

        try {
            String projectId = googleCloudProperties.getProjectId();
            String subscriptionName = googleCloudProperties.getPubSubTopic() + "-subscription";
            
            ProjectSubscriptionName subscriptionPath = ProjectSubscriptionName.of(projectId, subscriptionName);
            
            // Create subscriber to listen for external traffic updates
            subscriber = Subscriber.newBuilder(subscriptionPath, this::handleIncomingMessage)
                .build();
            
            subscriber.startAsync().awaitRunning();
            log.info("✅ Google Cloud Pub/Sub subscriber started for: {}", subscriptionPath);
            
        } catch (Exception e) {
            log.error("Failed to initialize Pub/Sub subscriber: {}", e.getMessage());
        }
    }

    /**
     * Publish traffic data to Pub/Sub topic
     */
    @Async
    public void publishTrafficData(TrafficData trafficData) {
        if (publisher == null) {
            log.debug("Pub/Sub publisher not available - skipping publish");
            return;
        }

        try {
            // Convert traffic data to JSON
            String jsonData = objectMapper.writeValueAsString(trafficData);
            
            // Create Pub/Sub message
            PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(jsonData))
                .putAttributes("messageType", "TRAFFIC_UPDATE")
                .putAttributes("location", trafficData.getLocation())
                .putAttributes("density", trafficData.getTrafficDensity().toString())
                .putAttributes("timestamp", trafficData.getTimestamp().toString())
                .putAttributes("source", "SMART_TRAFFIC_SYSTEM")
                .build();

            // Publish message
            publisher.publish(message).get(5, TimeUnit.SECONDS);
            log.debug("Published traffic data to Pub/Sub: {}", trafficData.getLocation());
            
        } catch (Exception e) {
            log.error("Error publishing traffic data to Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Publish traffic alert to Pub/Sub topic
     */
    @Async
    public void publishTrafficAlert(Map<String, Object> alert) {
        if (publisher == null) {
            log.debug("Pub/Sub publisher not available - skipping alert publish");
            return;
        }

        try {
            // Convert alert to JSON
            String jsonData = objectMapper.writeValueAsString(alert);
            
            // Create Pub/Sub message
            PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(jsonData))
                .putAttributes("messageType", "TRAFFIC_ALERT")
                .putAttributes("alertType", alert.get("type").toString())
                .putAttributes("severity", alert.get("severity").toString())
                .putAttributes("location", alert.get("location").toString())
                .putAttributes("source", "SMART_TRAFFIC_SYSTEM")
                .build();

            // Publish message
            publisher.publish(message).get(5, TimeUnit.SECONDS);
            log.info("Published traffic alert to Pub/Sub: {}", alert.get("message"));
            
        } catch (Exception e) {
            log.error("Error publishing traffic alert to Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Publish analytics data to Pub/Sub topic
     */
    @Async
    public void publishAnalyticsData(Map<String, Object> analytics) {
        if (publisher == null) {
            log.debug("Pub/Sub publisher not available - skipping analytics publish");
            return;
        }

        try {
            // Convert analytics to JSON
            String jsonData = objectMapper.writeValueAsString(analytics);
            
            // Create Pub/Sub message
            PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(jsonData))
                .putAttributes("messageType", "ANALYTICS_UPDATE")
                .putAttributes("timestamp", System.currentTimeMillis() + "")
                .putAttributes("source", "SMART_TRAFFIC_SYSTEM")
                .build();

            // Publish message
            publisher.publish(message).get(5, TimeUnit.SECONDS);
            log.debug("Published analytics data to Pub/Sub");
            
        } catch (Exception e) {
            log.error("Error publishing analytics data to Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Handle incoming Pub/Sub messages
     */
    private void handleIncomingMessage(PubsubMessage message, com.google.cloud.pubsub.v1.AckReplyConsumer consumer) {
        try {
            String messageType = message.getAttributesMap().get("messageType");
            String jsonData = message.getData().toStringUtf8();
            
            log.debug("Received Pub/Sub message: type={}, data={}", messageType, jsonData);
            
            switch (messageType) {
                case "TRAFFIC_UPDATE":
                    handleTrafficUpdate(jsonData);
                    break;
                case "TRAFFIC_ALERT":
                    handleTrafficAlert(jsonData);
                    break;
                case "EXTERNAL_TRAFFIC_DATA":
                    handleExternalTrafficData(jsonData);
                    break;
                default:
                    log.warn("Unknown message type: {}", messageType);
            }
            
            // Acknowledge the message
            consumer.ack();
            
        } catch (Exception e) {
            log.error("Error processing Pub/Sub message: {}", e.getMessage());
            // Negative acknowledgment - message will be redelivered
            consumer.nack();
        }
    }

    /**
     * Handle traffic update from Pub/Sub
     */
    private void handleTrafficUpdate(String jsonData) {
        try {
            TrafficData trafficData = objectMapper.readValue(jsonData, TrafficData.class);
            
            // Process through real-time service
            if (realTimeTrafficService != null) {
                realTimeTrafficService.processRealTimeTrafficData(trafficData);
            }
            
            log.debug("Processed external traffic update: {}", trafficData.getLocation());
            
        } catch (Exception e) {
            log.error("Error processing traffic update from Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Handle traffic alert from Pub/Sub
     */
    private void handleTrafficAlert(String jsonData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> alert = objectMapper.readValue(jsonData, Map.class);
            
            log.info("Received external traffic alert: {}", alert.get("message"));
            
            // Could forward to real-time alert system
            // realTimeTrafficService.processExternalAlert(alert);
            
        } catch (Exception e) {
            log.error("Error processing traffic alert from Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Handle external traffic data from third-party sources
     */
    private void handleExternalTrafficData(String jsonData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> externalData = objectMapper.readValue(jsonData, Map.class);
            
            log.info("Received external traffic data from: {}", externalData.get("source"));
            
            // Convert external data to internal format and process
            TrafficData trafficData = convertExternalToInternalFormat(externalData);
            
            if (realTimeTrafficService != null && trafficData != null) {
                realTimeTrafficService.processRealTimeTrafficData(trafficData);
            }
            
        } catch (Exception e) {
            log.error("Error processing external traffic data from Pub/Sub: {}", e.getMessage());
        }
    }

    /**
     * Convert external traffic data format to internal TrafficData format
     */
    private TrafficData convertExternalToInternalFormat(Map<String, Object> externalData) {
        try {
            TrafficData trafficData = new TrafficData();
            
            // Map common fields (this would be customized based on external data format)
            trafficData.setLocation((String) externalData.get("location"));
            trafficData.setLatitude(((Number) externalData.get("latitude")).doubleValue());
            trafficData.setLongitude(((Number) externalData.get("longitude")).doubleValue());
            
            // Map traffic density
            String density = (String) externalData.get("trafficLevel");
            trafficData.setTrafficDensity(mapExternalDensity(density));
            
            // Map other fields
            if (externalData.containsKey("averageSpeed")) {
                trafficData.setAverageSpeed(((Number) externalData.get("averageSpeed")).doubleValue());
            }
            
            if (externalData.containsKey("vehicleCount")) {
                trafficData.setVehicleCount(((Number) externalData.get("vehicleCount")).intValue());
            }
            
            trafficData.setTimestamp(java.time.LocalDateTime.now());
            trafficData.setWeatherCondition("CLEAR"); // Default
            
            return trafficData;
            
        } catch (Exception e) {
            log.error("Error converting external traffic data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Map external traffic density to internal format
     */
    private TrafficData.TrafficDensity mapExternalDensity(String externalDensity) {
        if (externalDensity == null) return TrafficData.TrafficDensity.MODERATE;
        
        switch (externalDensity.toUpperCase()) {
            case "HEAVY":
            case "SEVERE":
            case "CRITICAL":
                return TrafficData.TrafficDensity.CRITICAL;
            case "MODERATE":
            case "MEDIUM":
                return TrafficData.TrafficDensity.MODERATE;
            case "LIGHT":
            case "LOW":
                return TrafficData.TrafficDensity.LOW;
            default:
                return TrafficData.TrafficDensity.MODERATE;
        }
    }

    /**
     * Get Pub/Sub connection status
     */
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = Map.of(
            "publisherAvailable", publisher != null,
            "subscriberRunning", subscriber != null && subscriber.isRunning(),
            "projectId", googleCloudProperties.getProjectId(),
            "topic", googleCloudProperties.getPubSubTopic()
        );
        
        return status;
    }

    /**
     * Cleanup resources
     */
    @PreDestroy
    public void cleanup() {
        try {
            if (subscriber != null) {
                subscriber.stopAsync().awaitTerminated(30, TimeUnit.SECONDS);
                log.info("Pub/Sub subscriber stopped");
            }
            
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(30, TimeUnit.SECONDS);
                log.info("Pub/Sub publisher stopped");
            }
        } catch (Exception e) {
            log.error("Error during Pub/Sub cleanup: {}", e.getMessage());
        }
    }
}
