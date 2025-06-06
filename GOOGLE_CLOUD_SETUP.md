# 🌐 Google Cloud Integration Setup Guide

## Smart City Traffic Optimization System Using Cloud-Based AI
**Research by:** Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi

---

## 📋 **PROJECT CONFIGURATION**

- **Project ID:** `your-google-cloud-project-id` (update this)
- **Project Number:** `your-project-number` (update this)
- **Status:** ⚙️ Ready for configuration

---

## 🚀 **QUICK START GUIDE**

### **Step 1: Enable Required APIs**

Run these commands in Google Cloud Shell or with gcloud CLI:

```bash
# Set your project (replace with your actual project ID)
gcloud config set project your-google-cloud-project-id

# Enable required APIs
gcloud services enable bigquery.googleapis.com
gcloud services enable pubsub.googleapis.com
gcloud services enable storage.googleapis.com
gcloud services enable maps-backend.googleapis.com
gcloud services enable aiplatform.googleapis.com
gcloud services enable compute.googleapis.com
```

### **Step 2: Create Service Account**

```bash
# Create service account
gcloud iam service-accounts create smart-traffic-service \
    --display-name="Smart Traffic Management Service" \
    --description="Service account for Smart City Traffic Optimization System"

# Grant necessary roles (replace PROJECT_ID with your actual project ID)
gcloud projects add-iam-policy-binding your-google-cloud-project-id \
    --member="serviceAccount:smart-traffic-service@your-google-cloud-project-id.iam.gserviceaccount.com" \
    --role="roles/bigquery.admin"

gcloud projects add-iam-policy-binding your-google-cloud-project-id \
    --member="serviceAccount:smart-traffic-service@your-google-cloud-project-id.iam.gserviceaccount.com" \
    --role="roles/pubsub.admin"

gcloud projects add-iam-policy-binding your-google-cloud-project-id \
    --member="serviceAccount:smart-traffic-service@your-google-cloud-project-id.iam.gserviceaccount.com" \
    --role="roles/storage.admin"

gcloud projects add-iam-policy-binding your-google-cloud-project-id \
    --member="serviceAccount:smart-traffic-service@your-google-cloud-project-id.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"

# Create and download key
gcloud iam service-accounts keys create google-cloud-key.json \
    --iam-account=smart-traffic-service@your-google-cloud-project-id.iam.gserviceaccount.com
```

### **Step 3: Get Google Maps API Key**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **APIs & Services > Credentials**
3. Click **Create Credentials > API Key**
4. Restrict the key to these APIs:
   - Maps JavaScript API
   - Directions API
   - Distance Matrix API
   - Places API
5. Copy the API key

### **Step 4: Create BigQuery Dataset**

```bash
# Create BigQuery dataset (replace PROJECT_ID with your actual project ID)
bq mk --dataset --location=US your-google-cloud-project-id:traffic_analytics

# Create tables
bq mk --table your-google-cloud-project-id:traffic_analytics.traffic_data_realtime \
    location:STRING,latitude:FLOAT,longitude:FLOAT,traffic_density:STRING,average_speed:FLOAT,vehicle_count:INTEGER,weather_condition:STRING,timestamp:TIMESTAMP

bq mk --table your-google-cloud-project-id:traffic_analytics.traffic_analytics \
    analytics_type:STRING,data:JSON,timestamp:TIMESTAMP
```

### **Step 5: Create Pub/Sub Topics**

```bash
# Create Pub/Sub topic and subscription
gcloud pubsub topics create traffic-updates
gcloud pubsub subscriptions create traffic-updates-subscription --topic=traffic-updates
```

### **Step 6: Create Cloud Storage Bucket**

```bash
# Create storage bucket (replace with your project ID)
gsutil mb -l us-central1 gs://smart-traffic-data-your-project-id
```

---

## ⚙️ **CONFIGURATION**

### **Environment Variables**

Set these environment variables or update `application.yml`:

```bash
export GOOGLE_CLOUD_PROJECT_ID=your-google-cloud-project-id
export GOOGLE_CLOUD_PROJECT_NUMBER=your-project-number
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/google-cloud-key.json
export GOOGLE_MAPS_API_KEY=your-maps-api-key-here
export BIGQUERY_DATASET=traffic_analytics
export PUBSUB_TOPIC=traffic-updates
export CLOUD_STORAGE_BUCKET=smart-traffic-data-your-project-id
```

### **Application Configuration**

The system is already configured with your project details in `application.yml`:

```yaml
google:
  cloud:
    project-id: your-google-cloud-project-id
    project-number: your-project-number
    bigquery:
      dataset: traffic_analytics
    pubsub:
      topic: traffic-updates
    storage:
      bucket: smart-traffic-data-your-project-id
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY}
```

---

## 🧪 **TESTING THE INTEGRATION**

### **1. Test API Endpoints**

```bash
# Test Google Cloud status
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/google-cloud/status

# Test BigQuery analytics
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/google-cloud/analytics/summary

# Test Google Maps directions
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8081/api/v1/google-cloud/directions?origin=Times%20Square&destination=Central%20Park"
```

### **2. Test Real-Time Features**

```bash
# Trigger traffic data fetch
curl -X POST -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/google-cloud/traffic/fetch

# Check Pub/Sub status
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/google-cloud/pubsub/status
```

---

## 📊 **FEATURES ENABLED**

### **✅ Real-Time Traffic Data**
- Google Maps API integration for live traffic conditions
- Distance Matrix API for travel time calculations
- Directions API for route optimization

### **✅ BigQuery Analytics**
- Real-time data streaming to BigQuery
- Advanced traffic analytics queries
- Historical data analysis
- Congestion hotspot detection

### **✅ Pub/Sub Messaging**
- Real-time traffic updates broadcasting
- Event-driven architecture
- Scalable message processing

### **✅ Cloud Storage**
- Traffic data archival
- ML model storage
- Report generation

### **✅ AI/ML Integration**
- Traffic prediction models
- Route optimization algorithms
- Anomaly detection

---

## 🔧 **TROUBLESHOOTING**

### **Common Issues**

1. **Authentication Errors**
   - Ensure service account key is properly configured
   - Check GOOGLE_APPLICATION_CREDENTIALS path
   - Verify service account has required permissions

2. **API Not Enabled**
   - Enable required Google Cloud APIs
   - Check API quotas and limits

3. **Permission Denied**
   - Verify IAM roles are correctly assigned
   - Check project billing is enabled

4. **Network Issues**
   - Ensure firewall allows outbound HTTPS traffic
   - Check VPC/network configuration

### **Monitoring**

- Check application logs for Google Cloud integration status
- Monitor BigQuery job history
- Review Pub/Sub message metrics
- Track API usage in Google Cloud Console

---

## 💰 **COST OPTIMIZATION**

### **Free Tier Usage**
- BigQuery: 1TB queries/month free
- Pub/Sub: 10GB messages/month free
- Cloud Storage: 5GB storage free
- Maps API: $200 credit/month

### **Best Practices**
- Use BigQuery partitioning for large datasets
- Implement Pub/Sub message filtering
- Set up billing alerts
- Monitor API usage regularly

---

## 🚀 **PRODUCTION DEPLOYMENT**

### **Security**
- Use Workload Identity for GKE
- Implement least privilege access
- Enable audit logging
- Use VPC Service Controls

### **Scalability**
- Configure auto-scaling for Pub/Sub
- Use BigQuery slots for consistent performance
- Implement caching strategies
- Set up load balancing

### **Monitoring**
- Set up Cloud Monitoring alerts
- Configure log aggregation
- Implement health checks
- Monitor SLA metrics

---

## 📞 **SUPPORT**

For issues with this integration:
1. Check application logs
2. Review Google Cloud Console
3. Consult Google Cloud documentation
4. Contact system administrators

**Project Contact:** Smart City Traffic Optimization Team
**Last Updated:** June 2025
