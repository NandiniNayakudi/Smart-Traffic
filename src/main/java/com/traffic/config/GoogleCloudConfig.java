package com.traffic.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.maps.GeoApiContext;
import com.google.pubsub.v1.ProjectTopicName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Google Cloud Platform Configuration
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Configuration
@Slf4j
public class GoogleCloudConfig {

    @Value("${google.cloud.project-id:smart-traffic-demo}")
    private String projectId;

    @Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    @Value("${google.cloud.credentials-path:}")
    private String credentialsPath;

    @Value("${google.cloud.bigquery.dataset:traffic_data}")
    private String bigQueryDataset;

    @Value("${google.cloud.pubsub.topic:traffic-updates}")
    private String pubSubTopic;

    @Value("${google.cloud.storage.bucket:smart-traffic-data}")
    private String storageBucket;

    /**
     * Google Credentials Bean
     */
    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            if (credentialsPath != null && !credentialsPath.isEmpty()) {
                log.info("Loading Google Cloud credentials from: {}", credentialsPath);
                return GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            } else {
                log.info("Using default Google Cloud credentials");
                return GoogleCredentials.getApplicationDefault()
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            }
        } catch (IOException e) {
            log.warn("Failed to load Google Cloud credentials, using mock credentials for development: {}", e.getMessage());
            // Return null for development mode - services will use mock implementations
            return null;
        }
    }

    /**
     * Google Maps API Context
     */
    @Bean
    public GeoApiContext geoApiContext() {
        if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
            log.warn("Google Maps API key not configured. Real-time traffic data will be simulated.");
            return null;
        }

        return new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build();
    }

    /**
     * BigQuery Client
     */
    @Bean
    @ConditionalOnProperty(name = "google.cloud.bigquery.enabled", havingValue = "true", matchIfMissing = false)
    public BigQuery bigQuery() {
        try {
            GoogleCredentials credentials = googleCredentials();
            if (credentials != null) {
                return BigQueryOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(credentials)
                        .build()
                        .getService();
            } else {
                log.warn("BigQuery client not available - using mock implementation");
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to create BigQuery client: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cloud Storage Client
     */
    @Bean
    @ConditionalOnProperty(name = "google.cloud.storage.enabled", havingValue = "true", matchIfMissing = false)
    public Storage cloudStorage() {
        try {
            GoogleCredentials credentials = googleCredentials();
            if (credentials != null) {
                return StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(credentials)
                        .build()
                        .getService();
            } else {
                log.warn("Cloud Storage client not available - using mock implementation");
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to create Cloud Storage client: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Pub/Sub Publisher
     */
    @Bean
    @ConditionalOnProperty(name = "google.cloud.pubsub.enabled", havingValue = "true", matchIfMissing = false)
    public Publisher pubSubPublisher() {
        try {
            GoogleCredentials credentials = googleCredentials();
            if (credentials != null) {
                ProjectTopicName topicName = ProjectTopicName.of(projectId, pubSubTopic);
                return Publisher.newBuilder(topicName)
                        .setCredentialsProvider(() -> credentials)
                        .build();
            } else {
                log.warn("Pub/Sub Publisher not available - using mock implementation");
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to create Pub/Sub Publisher: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Configuration Properties Bean
     */
    @Bean
    public GoogleCloudProperties googleCloudProperties() {
        GoogleCloudProperties properties = new GoogleCloudProperties();
        properties.setProjectId(projectId);
        properties.setGoogleMapsApiKey(googleMapsApiKey);
        properties.setBigQueryDataset(bigQueryDataset);
        properties.setPubSubTopic(pubSubTopic);
        properties.setStorageBucket(storageBucket);
        properties.setCredentialsPath(credentialsPath);
        return properties;
    }

    /**
     * Google Cloud Properties Class
     */
    public static class GoogleCloudProperties {
        private String projectId;
        private String googleMapsApiKey;
        private String bigQueryDataset;
        private String pubSubTopic;
        private String storageBucket;
        private String credentialsPath;

        // Getters and Setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getGoogleMapsApiKey() { return googleMapsApiKey; }
        public void setGoogleMapsApiKey(String googleMapsApiKey) { this.googleMapsApiKey = googleMapsApiKey; }

        public String getBigQueryDataset() { return bigQueryDataset; }
        public void setBigQueryDataset(String bigQueryDataset) { this.bigQueryDataset = bigQueryDataset; }

        public String getPubSubTopic() { return pubSubTopic; }
        public void setPubSubTopic(String pubSubTopic) { this.pubSubTopic = pubSubTopic; }

        public String getStorageBucket() { return storageBucket; }
        public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }

        public String getCredentialsPath() { return credentialsPath; }
        public void setCredentialsPath(String credentialsPath) { this.credentialsPath = credentialsPath; }
    }
}
