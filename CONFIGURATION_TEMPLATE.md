# 🔧 Configuration Template for Google Cloud Integration

## Smart City Traffic Optimization System Using Cloud-Based AI
**Research by:** Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi

---

## 📋 **BEFORE YOU START**

**⚠️ IMPORTANT SECURITY NOTE:**
- Never commit your actual Google Cloud project credentials to version control
- Always use environment variables for sensitive configuration
- Keep your API keys and service account files secure
- Use different projects for development, staging, and production

---

## 🔧 **STEP 1: UPDATE PROJECT CONFIGURATION**

### **1.1 Update setup-google-cloud.sh**
```bash
# Edit setup-google-cloud.sh and replace these values:
PROJECT_ID="your-actual-google-cloud-project-id"
PROJECT_NUMBER="your-actual-project-number"
```

### **1.2 Update .env.google-cloud**
```bash
# Edit .env.google-cloud and replace these values:
GOOGLE_CLOUD_PROJECT_ID=your-actual-google-cloud-project-id
GOOGLE_CLOUD_PROJECT_NUMBER=your-actual-project-number
GOOGLE_MAPS_API_KEY=your-actual-google-maps-api-key
```

### **1.3 Update application.yml (Optional)**
```yaml
# The application.yml uses environment variables by default
# You can override defaults if needed:
google:
  cloud:
    project-id: ${GOOGLE_CLOUD_PROJECT_ID:your-actual-project-id}
    project-number: ${GOOGLE_CLOUD_PROJECT_NUMBER:your-actual-project-number}
```

---

## 🚀 **STEP 2: GET YOUR GOOGLE CLOUD PROJECT DETAILS**

### **2.1 Find Your Project ID**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown at the top
3. Your Project ID is shown in the project list
4. Or run: `gcloud config get-value project`

### **2.2 Find Your Project Number**
1. In Google Cloud Console, go to "IAM & Admin" > "Settings"
2. Your Project Number is displayed at the top
3. Or run: `gcloud projects describe YOUR_PROJECT_ID --format="value(projectNumber)"`

### **2.3 Get Google Maps API Key**
1. Go to [Google Cloud Console APIs & Credentials](https://console.cloud.google.com/apis/credentials)
2. Click "Create Credentials" > "API Key"
3. Restrict the key to these APIs:
   - Maps JavaScript API
   - Directions API
   - Distance Matrix API
   - Places API
4. Copy the API key

---

## 🔐 **STEP 3: SECURE CONFIGURATION**

### **3.1 Create Environment File**
```bash
# Create a secure environment file (never commit this!)
cp .env.google-cloud .env.local

# Edit .env.local with your actual values
nano .env.local
```

### **3.2 Update .gitignore**
```bash
# Add to .gitignore to prevent committing sensitive data
echo ".env.local" >> .gitignore
echo "google-cloud-key.json" >> .gitignore
echo "*.json" >> .gitignore
```

### **3.3 Use Environment Variables**
```bash
# Load your configuration
source .env.local

# Or export individually
export GOOGLE_CLOUD_PROJECT_ID="your-actual-project-id"
export GOOGLE_CLOUD_PROJECT_NUMBER="your-actual-project-number"
export GOOGLE_MAPS_API_KEY="your-actual-api-key"
```

---

## 🛠️ **STEP 4: SETUP AND DEPLOYMENT**

### **4.1 Run Setup Script**
```bash
# Make sure you've updated the PROJECT_ID and PROJECT_NUMBER in the script
./setup-google-cloud.sh
```

### **4.2 Start Application**
```bash
# With environment file
source .env.local
mvn spring-boot:run

# Or with direct environment variables
GOOGLE_CLOUD_PROJECT_ID=your-project-id mvn spring-boot:run
```

### **4.3 Verify Configuration**
```bash
# Test the integration
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/google-cloud/project/config
```

---

## 📝 **STEP 5: CONFIGURATION EXAMPLES**

### **5.1 Development Environment**
```bash
# .env.development
GOOGLE_CLOUD_PROJECT_ID=my-dev-project
GOOGLE_CLOUD_PROJECT_NUMBER=123456789012
GOOGLE_MAPS_API_KEY=AIza...dev-key
BIGQUERY_DATASET=traffic_analytics_dev
PUBSUB_TOPIC=traffic-updates-dev
CLOUD_STORAGE_BUCKET=smart-traffic-data-dev
```

### **5.2 Production Environment**
```bash
# .env.production
GOOGLE_CLOUD_PROJECT_ID=my-prod-project
GOOGLE_CLOUD_PROJECT_NUMBER=987654321098
GOOGLE_MAPS_API_KEY=AIza...prod-key
BIGQUERY_DATASET=traffic_analytics
PUBSUB_TOPIC=traffic-updates
CLOUD_STORAGE_BUCKET=smart-traffic-data-prod
```

### **5.3 Docker Environment**
```dockerfile
# In your Dockerfile or docker-compose.yml
ENV GOOGLE_CLOUD_PROJECT_ID=your-project-id
ENV GOOGLE_CLOUD_PROJECT_NUMBER=your-project-number
ENV GOOGLE_MAPS_API_KEY=your-api-key
```

---

## ✅ **STEP 6: VERIFICATION CHECKLIST**

### **Before Running:**
- [ ] Updated PROJECT_ID in setup-google-cloud.sh
- [ ] Updated PROJECT_NUMBER in setup-google-cloud.sh
- [ ] Created .env.local with your actual values
- [ ] Added sensitive files to .gitignore
- [ ] Obtained Google Maps API key
- [ ] Enabled required Google Cloud APIs

### **After Setup:**
- [ ] Service account created successfully
- [ ] BigQuery dataset and tables exist
- [ ] Pub/Sub topic and subscription created
- [ ] Cloud Storage bucket created
- [ ] Application starts without errors
- [ ] API endpoints return your project configuration

---

## 🔍 **TROUBLESHOOTING**

### **Common Issues:**

1. **"Project not found" error**
   - Verify your PROJECT_ID is correct
   - Ensure you have access to the project
   - Check if billing is enabled

2. **"Permission denied" error**
   - Verify service account has required roles
   - Check if APIs are enabled
   - Ensure service account key is valid

3. **"API key invalid" error**
   - Verify API key is correct
   - Check if API key restrictions are properly set
   - Ensure required APIs are enabled for the key

4. **Application won't start**
   - Check if all environment variables are set
   - Verify google-cloud-key.json exists and is valid
   - Check application logs for specific errors

---

## 📞 **SUPPORT**

If you encounter issues:
1. Check the application logs
2. Verify your Google Cloud Console settings
3. Ensure all environment variables are correctly set
4. Review the Google Cloud documentation
5. Check the project's GitHub issues

---

## 🔒 **SECURITY BEST PRACTICES**

1. **Never commit credentials** to version control
2. **Use different projects** for different environments
3. **Rotate API keys** regularly
4. **Monitor API usage** and set up billing alerts
5. **Use least privilege** for service account permissions
6. **Enable audit logging** for production environments
7. **Use VPC Service Controls** for additional security

---

**Last Updated:** June 2025  
**Version:** 1.0  
**Status:** Ready for Production
