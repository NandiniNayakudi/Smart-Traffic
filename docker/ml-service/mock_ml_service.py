#!/usr/bin/env python3
"""
Mock ML Service for Traffic Management System
Simulates a machine learning model for traffic prediction
"""

from flask import Flask, request, jsonify
import random
import logging
from datetime import datetime

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

# Mock ML model responses
TRAFFIC_DENSITIES = ["LOW", "MODERATE", "HIGH", "CRITICAL"]

def predict_traffic(lat, lon, hour, day_of_week, weather, historical_data_points):
    """
    Mock traffic prediction logic
    """
    # Base prediction based on time of day
    if 7 <= hour <= 9 or 17 <= hour <= 19:  # Rush hours
        base_prediction = "HIGH"
        base_confidence = 0.85
    elif 22 <= hour or hour <= 6:  # Night time
        base_prediction = "LOW"
        base_confidence = 0.80
    else:  # Regular hours
        base_prediction = "MODERATE"
        base_confidence = 0.75
    
    # Adjust for day of week
    if day_of_week in ["Saturday", "Sunday"]:
        if base_prediction == "HIGH":
            base_prediction = "MODERATE"
        base_confidence -= 0.05
    
    # Adjust for weather
    if weather in ["RAIN", "SNOW", "FOG"]:
        if base_prediction == "LOW":
            base_prediction = "MODERATE"
        elif base_prediction == "MODERATE":
            base_prediction = "HIGH"
        base_confidence -= 0.10
    
    # Adjust for historical data
    if historical_data_points > 10:
        base_confidence += 0.05
    elif historical_data_points < 3:
        base_confidence -= 0.10
    
    # Add some randomness
    confidence_adjustment = random.uniform(-0.05, 0.05)
    final_confidence = max(0.5, min(0.95, base_confidence + confidence_adjustment))
    
    # Occasionally change prediction for realism
    if random.random() < 0.2:  # 20% chance to adjust prediction
        current_index = TRAFFIC_DENSITIES.index(base_prediction)
        if random.random() < 0.5 and current_index > 0:
            base_prediction = TRAFFIC_DENSITIES[current_index - 1]
        elif current_index < len(TRAFFIC_DENSITIES) - 1:
            base_prediction = TRAFFIC_DENSITIES[current_index + 1]
    
    return base_prediction, final_confidence

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "Traffic ML Service",
        "timestamp": datetime.now().isoformat()
    })

@app.route('/predict', methods=['POST'])
def predict():
    """Main prediction endpoint"""
    try:
        data = request.get_json()
        
        # Validate required fields
        required_fields = ['lat', 'lon', 'hour', 'dayOfWeek']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    "error": f"Missing required field: {field}"
                }), 400
        
        # Extract parameters
        lat = data['lat']
        lon = data['lon']
        hour = data['hour']
        day_of_week = data['dayOfWeek']
        weather = data.get('weather', 'CLEAR')
        historical_data_points = data.get('historicalDataPoints', 0)
        
        # Log the prediction request
        app.logger.info(f"Prediction request: lat={lat}, lon={lon}, hour={hour}, day={day_of_week}")
        
        # Generate prediction
        prediction, confidence = predict_traffic(
            lat, lon, hour, day_of_week, weather, historical_data_points
        )
        
        # Simulate processing time
        import time
        time.sleep(random.uniform(0.1, 0.5))
        
        response = {
            "prediction": prediction,
            "confidence": confidence,
            "model_version": "v1.2.3",
            "timestamp": datetime.now().isoformat(),
            "features_used": {
                "coordinates": [lat, lon],
                "temporal": {"hour": hour, "day_of_week": day_of_week},
                "weather": weather,
                "historical_data_points": historical_data_points
            }
        }
        
        app.logger.info(f"Prediction result: {prediction} (confidence: {confidence:.2f})")
        return jsonify(response)
        
    except Exception as e:
        app.logger.error(f"Prediction error: {str(e)}")
        return jsonify({
            "error": "Internal server error",
            "message": str(e)
        }), 500

@app.route('/model/info', methods=['GET'])
def model_info():
    """Get model information"""
    return jsonify({
        "model_name": "Traffic Prediction Model",
        "version": "v1.2.3",
        "algorithm": "Random Forest + LSTM",
        "training_data_size": "1M+ traffic records",
        "accuracy": "87.5%",
        "last_trained": "2024-01-15T10:30:00Z",
        "supported_features": [
            "coordinates",
            "time_of_day",
            "day_of_week",
            "weather_conditions",
            "historical_patterns"
        ]
    })

@app.route('/model/retrain', methods=['POST'])
def retrain_model():
    """Simulate model retraining"""
    try:
        # Simulate training time
        import time
        time.sleep(2)
        
        new_version = f"v1.2.{random.randint(4, 10)}"
        
        return jsonify({
            "status": "Training completed",
            "new_version": new_version,
            "training_time": "2.3 seconds (simulated)",
            "accuracy_improvement": f"+{random.uniform(0.5, 2.0):.1f}%",
            "timestamp": datetime.now().isoformat()
        })
        
    except Exception as e:
        return jsonify({
            "error": "Training failed",
            "message": str(e)
        }), 500

@app.route('/batch/predict', methods=['POST'])
def batch_predict():
    """Batch prediction endpoint"""
    try:
        data = request.get_json()
        requests = data.get('requests', [])
        
        if not requests:
            return jsonify({"error": "No prediction requests provided"}), 400
        
        results = []
        for req in requests:
            prediction, confidence = predict_traffic(
                req['lat'], req['lon'], req['hour'], 
                req['dayOfWeek'], req.get('weather', 'CLEAR'),
                req.get('historicalDataPoints', 0)
            )
            
            results.append({
                "id": req.get('id', len(results)),
                "prediction": prediction,
                "confidence": confidence
            })
        
        return jsonify({
            "results": results,
            "total_predictions": len(results),
            "timestamp": datetime.now().isoformat()
        })
        
    except Exception as e:
        return jsonify({
            "error": "Batch prediction failed",
            "message": str(e)
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
