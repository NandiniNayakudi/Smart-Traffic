#!/bin/bash

# 🌐 Google Cloud Setup Script for Smart City Traffic Optimization System
# Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
# Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project configuration - UPDATE THESE WITH YOUR VALUES
PROJECT_ID="your-google-cloud-project-id"
PROJECT_NUMBER="your-project-number"
SERVICE_ACCOUNT_NAME="smart-traffic-service"
DATASET_NAME="traffic_analytics"
TOPIC_NAME="traffic-updates"
SUBSCRIPTION_NAME="traffic-updates-subscription"
BUCKET_NAME="smart-traffic-data-${PROJECT_ID}"

echo -e "${BLUE}🌐 Smart City Traffic Optimization System - Google Cloud Setup${NC}"
echo -e "${BLUE}================================================================${NC}"
echo -e "Project ID: ${GREEN}${PROJECT_ID}${NC}"
echo -e "Project Number: ${GREEN}${PROJECT_NUMBER}${NC}"
echo ""

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    print_error "gcloud CLI is not installed. Please install it first."
    echo "Visit: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

print_status "gcloud CLI found"

# Set project
print_info "Setting Google Cloud project..."
gcloud config set project $PROJECT_ID
print_status "Project set to $PROJECT_ID"

# Enable APIs
print_info "Enabling required Google Cloud APIs..."
apis=(
    "bigquery.googleapis.com"
    "pubsub.googleapis.com"
    "storage.googleapis.com"
    "maps-backend.googleapis.com"
    "aiplatform.googleapis.com"
    "compute.googleapis.com"
    "cloudbuild.googleapis.com"
    "monitoring.googleapis.com"
    "logging.googleapis.com"
)

for api in "${apis[@]}"; do
    echo "Enabling $api..."
    gcloud services enable $api
    print_status "$api enabled"
done

# Create service account
print_info "Creating service account..."
if gcloud iam service-accounts describe ${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com &> /dev/null; then
    print_warning "Service account already exists"
else
    gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
        --display-name="Smart Traffic Management Service" \
        --description="Service account for Smart City Traffic Optimization System"
    print_status "Service account created"
fi

# Grant IAM roles
print_info "Granting IAM roles..."
roles=(
    "roles/bigquery.admin"
    "roles/pubsub.admin"
    "roles/storage.admin"
    "roles/aiplatform.user"
    "roles/monitoring.metricWriter"
    "roles/logging.logWriter"
)

for role in "${roles[@]}"; do
    echo "Granting $role..."
    gcloud projects add-iam-policy-binding $PROJECT_ID \
        --member="serviceAccount:${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
        --role="$role"
    print_status "$role granted"
done

# Create service account key
print_info "Creating service account key..."
if [ -f "google-cloud-key.json" ]; then
    print_warning "Service account key already exists"
    read -p "Do you want to create a new key? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        gcloud iam service-accounts keys create google-cloud-key.json \
            --iam-account=${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com
        print_status "New service account key created"
    fi
else
    gcloud iam service-accounts keys create google-cloud-key.json \
        --iam-account=${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com
    print_status "Service account key created: google-cloud-key.json"
fi

# Create BigQuery dataset
print_info "Creating BigQuery dataset..."
if bq ls -d $PROJECT_ID:$DATASET_NAME &> /dev/null; then
    print_warning "BigQuery dataset already exists"
else
    bq mk --dataset --location=US $PROJECT_ID:$DATASET_NAME
    print_status "BigQuery dataset created"
fi

# Create BigQuery tables
print_info "Creating BigQuery tables..."

# Traffic data table
echo "Creating traffic_data_realtime table..."
bq mk --table --if_not_exists $PROJECT_ID:$DATASET_NAME.traffic_data_realtime \
    location:STRING,latitude:FLOAT,longitude:FLOAT,traffic_density:STRING,average_speed:FLOAT,vehicle_count:INTEGER,weather_condition:STRING,timestamp:TIMESTAMP

# Analytics table
echo "Creating traffic_analytics table..."
bq mk --table --if_not_exists $PROJECT_ID:$DATASET_NAME.traffic_analytics \
    analytics_type:STRING,data:JSON,timestamp:TIMESTAMP

print_status "BigQuery tables created"

# Create Pub/Sub topic and subscription
print_info "Creating Pub/Sub topic and subscription..."
if gcloud pubsub topics describe $TOPIC_NAME &> /dev/null; then
    print_warning "Pub/Sub topic already exists"
else
    gcloud pubsub topics create $TOPIC_NAME
    print_status "Pub/Sub topic created"
fi

if gcloud pubsub subscriptions describe $SUBSCRIPTION_NAME &> /dev/null; then
    print_warning "Pub/Sub subscription already exists"
else
    gcloud pubsub subscriptions create $SUBSCRIPTION_NAME --topic=$TOPIC_NAME
    print_status "Pub/Sub subscription created"
fi

# Create Cloud Storage bucket
print_info "Creating Cloud Storage bucket..."
if gsutil ls -b gs://$BUCKET_NAME &> /dev/null; then
    print_warning "Cloud Storage bucket already exists"
else
    gsutil mb -l us-central1 gs://$BUCKET_NAME
    print_status "Cloud Storage bucket created"
fi

# Set bucket permissions
print_info "Setting bucket permissions..."
gsutil iam ch serviceAccount:${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com:objectAdmin gs://$BUCKET_NAME
print_status "Bucket permissions set"

# Create environment file
print_info "Creating environment configuration..."
cat > .env.google-cloud << EOF
# Google Cloud Configuration for Smart City Traffic Optimization System
GOOGLE_CLOUD_PROJECT_ID=$PROJECT_ID
GOOGLE_CLOUD_PROJECT_NUMBER=$PROJECT_NUMBER
GOOGLE_APPLICATION_CREDENTIALS=./google-cloud-key.json
BIGQUERY_DATASET=$DATASET_NAME
PUBSUB_TOPIC=$TOPIC_NAME
PUBSUB_SUBSCRIPTION=$SUBSCRIPTION_NAME
CLOUD_STORAGE_BUCKET=$BUCKET_NAME

# Add your Google Maps API key here
GOOGLE_MAPS_API_KEY=your-maps-api-key-here
EOF

print_status "Environment file created: .env.google-cloud"

# Summary
echo ""
echo -e "${BLUE}🎉 Setup Complete!${NC}"
echo -e "${BLUE}==================${NC}"
echo ""
echo -e "${GREEN}✅ Google Cloud APIs enabled${NC}"
echo -e "${GREEN}✅ Service account created and configured${NC}"
echo -e "${GREEN}✅ BigQuery dataset and tables created${NC}"
echo -e "${GREEN}✅ Pub/Sub topic and subscription created${NC}"
echo -e "${GREEN}✅ Cloud Storage bucket created${NC}"
echo -e "${GREEN}✅ Environment configuration file created${NC}"
echo ""

print_warning "Next Steps:"
echo "1. Get your Google Maps API key from Google Cloud Console"
echo "2. Update GOOGLE_MAPS_API_KEY in .env.google-cloud"
echo "3. Source the environment file: source .env.google-cloud"
echo "4. Restart your application to use real Google Cloud services"
echo ""

print_info "To get Google Maps API key:"
echo "1. Go to https://console.cloud.google.com/apis/credentials"
echo "2. Click 'Create Credentials' > 'API Key'"
echo "3. Restrict the key to Maps APIs"
echo "4. Copy the key to .env.google-cloud"
echo ""

print_info "Test your setup:"
echo "curl -H \"Authorization: Bearer YOUR_JWT_TOKEN\" http://localhost:8081/api/v1/google-cloud/status"
echo ""

print_status "Google Cloud integration is ready for Smart City Traffic Optimization System!"
