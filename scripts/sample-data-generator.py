#!/usr/bin/env python3
"""
Sample Data Generator for Traffic Management System
Generates realistic traffic data for testing and demonstration
"""

import json
import random
import requests
from datetime import datetime, timedelta
import time

# Configuration
API_BASE_URL = "http://localhost:8080/api/v1"
ADMIN_CREDENTIALS = {"username": "admin", "password": "secure123"}

# Indian cities and their coordinates
CITIES = {
    "Vijayawada": {
        "locations": [
            {"name": "Vijayawada Junction", "lat": 16.5062, "lon": 80.6480},
            {"name": "Benz Circle", "lat": 16.5070, "lon": 80.6490},
            {"name": "PNBS Bus Stand", "lat": 16.5080, "lon": 80.6500},
            {"name": "Ramavarappadu Junction", "lat": 16.5090, "lon": 80.6510},
            {"name": "NH65 Highway", "lat": 16.5100, "lon": 80.6520},
        ]
    },
    "Bangalore": {
        "locations": [
            {"name": "Electronic City", "lat": 12.8456, "lon": 77.6603},
            {"name": "Silk Board Junction", "lat": 12.9176, "lon": 77.6227},
            {"name": "Koramangala", "lat": 12.9352, "lon": 77.6245},
            {"name": "Outer Ring Road", "lat": 12.9698, "lon": 77.7500},
            {"name": "Whitefield", "lat": 12.9698, "lon": 77.7500},
        ]
    },
    "Hyderabad": {
        "locations": [
            {"name": "HITEC City", "lat": 17.4435, "lon": 78.3772},
            {"name": "Gachibowli", "lat": 17.4399, "lon": 78.3482},
            {"name": "Madhapur", "lat": 17.4474, "lon": 78.3914},
            {"name": "Kondapur", "lat": 17.4616, "lon": 78.3570},
            {"name": "Jubilee Hills", "lat": 17.4239, "lon": 78.4738},
        ]
    }
}

TRAFFIC_DENSITIES = ["LOW", "MODERATE", "HIGH", "CRITICAL"]
WEATHER_CONDITIONS = ["CLEAR", "RAIN", "FOG", "CLOUDY"]

def get_auth_token():
    """Get authentication token"""
    try:
        response = requests.post(
            f"{API_BASE_URL}/auth/login",
            json=ADMIN_CREDENTIALS,
            headers={"Content-Type": "application/json"}
        )
        if response.status_code == 200:
            return response.json()["token"]
        else:
            print(f"Authentication failed: {response.status_code}")
            return None
    except Exception as e:
        print(f"Error getting auth token: {e}")
        return None

def generate_traffic_density_by_hour(hour):
    """Generate realistic traffic density based on hour"""
    if 7 <= hour <= 9 or 17 <= hour <= 19:  # Rush hours
        return random.choices(
            TRAFFIC_DENSITIES, 
            weights=[5, 15, 50, 30], 
            k=1
        )[0]
    elif 22 <= hour or hour <= 6:  # Night time
        return random.choices(
            TRAFFIC_DENSITIES, 
            weights=[70, 25, 5, 0], 
            k=1
        )[0]
    else:  # Regular hours
        return random.choices(
            TRAFFIC_DENSITIES, 
            weights=[20, 50, 25, 5], 
            k=1
        )[0]

def generate_vehicle_count(density):
    """Generate vehicle count based on density"""
    density_ranges = {
        "LOW": (5, 20),
        "MODERATE": (21, 50),
        "HIGH": (51, 80),
        "CRITICAL": (81, 120)
    }
    min_count, max_count = density_ranges[density]
    return random.randint(min_count, max_count)

def generate_average_speed(density):
    """Generate average speed based on density"""
    speed_ranges = {
        "LOW": (40, 60),
        "MODERATE": (20, 40),
        "HIGH": (10, 25),
        "CRITICAL": (2, 15)
    }
    min_speed, max_speed = speed_ranges[density]
    return round(random.uniform(min_speed, max_speed), 1)

def generate_traffic_data(location, timestamp):
    """Generate a single traffic data point"""
    hour = timestamp.hour
    density = generate_traffic_density_by_hour(hour)
    
    # Weekend adjustment
    if timestamp.weekday() >= 5:  # Saturday = 5, Sunday = 6
        if density == "CRITICAL":
            density = "HIGH"
        elif density == "HIGH" and random.random() < 0.5:
            density = "MODERATE"
    
    # Weather impact
    weather = random.choice(WEATHER_CONDITIONS)
    if weather in ["RAIN", "FOG"]:
        if density == "LOW":
            density = "MODERATE"
        elif density == "MODERATE" and random.random() < 0.3:
            density = "HIGH"
    
    return {
        "location": location["name"],
        "latitude": location["lat"] + random.uniform(-0.001, 0.001),  # Small variation
        "longitude": location["lon"] + random.uniform(-0.001, 0.001),
        "trafficDensity": density,
        "timestamp": timestamp.strftime("%Y-%m-%dT%H:%M:%S"),
        "vehicleCount": generate_vehicle_count(density),
        "averageSpeed": generate_average_speed(density),
        "weatherCondition": weather
    }

def ingest_traffic_data(token, traffic_data):
    """Send traffic data to the API"""
    try:
        response = requests.post(
            f"{API_BASE_URL}/traffic/ingest",
            json=traffic_data,
            headers={
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json"
            }
        )
        return response.status_code == 201
    except Exception as e:
        print(f"Error ingesting data: {e}")
        return False

def generate_historical_data(token, days=7):
    """Generate historical traffic data for the past N days"""
    print(f"Generating historical data for {days} days...")
    
    end_time = datetime.now()
    start_time = end_time - timedelta(days=days)
    
    total_records = 0
    successful_records = 0
    
    # Generate data for each hour in the time range
    current_time = start_time
    while current_time <= end_time:
        # Generate data for all locations
        for city, city_data in CITIES.items():
            for location in city_data["locations"]:
                traffic_data = generate_traffic_data(location, current_time)
                
                if ingest_traffic_data(token, traffic_data):
                    successful_records += 1
                    if successful_records % 50 == 0:
                        print(f"Ingested {successful_records} records...")
                
                total_records += 1
                
                # Small delay to avoid overwhelming the server
                time.sleep(0.1)
        
        # Move to next hour
        current_time += timedelta(hours=1)
    
    print(f"Historical data generation complete: {successful_records}/{total_records} records ingested")

def generate_real_time_data(token, duration_minutes=60):
    """Generate real-time traffic data for testing"""
    print(f"Generating real-time data for {duration_minutes} minutes...")
    
    end_time = datetime.now() + timedelta(minutes=duration_minutes)
    
    while datetime.now() < end_time:
        current_time = datetime.now()
        
        # Generate data for random locations
        city = random.choice(list(CITIES.keys()))
        location = random.choice(CITIES[city]["locations"])
        
        traffic_data = generate_traffic_data(location, current_time)
        
        if ingest_traffic_data(token, traffic_data):
            print(f"Real-time data ingested: {location['name']} - {traffic_data['trafficDensity']}")
        
        # Wait before next data point
        time.sleep(random.uniform(5, 15))  # 5-15 seconds between updates

def test_api_endpoints(token):
    """Test various API endpoints with sample data"""
    print("Testing API endpoints...")
    
    # Test prediction
    print("Testing traffic prediction...")
    response = requests.get(
        f"{API_BASE_URL}/traffic/predict",
        params={
            "lat": 16.5062,
            "lon": 80.6480,
            "timestamp": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    if response.status_code == 200:
        print(f"Prediction result: {response.json()}")
    
    # Test route recommendation
    print("Testing route recommendation...")
    response = requests.get(
        f"{API_BASE_URL}/traffic/route",
        params={
            "source": "Vijayawada Junction",
            "destination": "PNBS Bus Stand",
            "eco": "true"
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    if response.status_code == 200:
        print(f"Route recommendation: {response.json()}")
    
    # Test signal optimization
    print("Testing signal optimization...")
    signal_request = {
        "intersectionId": "INT-TEST-001",
        "north": 45,
        "south": 35,
        "east": 25,
        "west": 30
    }
    response = requests.post(
        f"{API_BASE_URL}/traffic/signal/optimize",
        json=signal_request,
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
    )
    if response.status_code == 200:
        print(f"Signal optimization: {response.json()}")
    
    # Test traffic trends
    print("Testing traffic trends...")
    response = requests.get(
        f"{API_BASE_URL}/traffic/trends",
        params={
            "location": "Vijayawada",
            "period": "daily"
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    if response.status_code == 200:
        trends = response.json()
        print(f"Traffic trends for Vijayawada: {len(trends.get('dailyTrend', []))} data points")

def main():
    """Main function"""
    print("Traffic Management System - Sample Data Generator")
    print("=" * 50)
    
    # Get authentication token
    token = get_auth_token()
    if not token:
        print("Failed to authenticate. Please check the API server and credentials.")
        return
    
    print("Authentication successful!")
    
    # Menu
    while True:
        print("\nOptions:")
        print("1. Generate historical data (7 days)")
        print("2. Generate real-time data (60 minutes)")
        print("3. Test API endpoints")
        print("4. Generate custom historical data")
        print("5. Exit")
        
        choice = input("\nEnter your choice (1-5): ").strip()
        
        if choice == "1":
            generate_historical_data(token, days=7)
        elif choice == "2":
            generate_real_time_data(token, duration_minutes=60)
        elif choice == "3":
            test_api_endpoints(token)
        elif choice == "4":
            days = int(input("Enter number of days: "))
            generate_historical_data(token, days=days)
        elif choice == "5":
            print("Goodbye!")
            break
        else:
            print("Invalid choice. Please try again.")

if __name__ == "__main__":
    main()
