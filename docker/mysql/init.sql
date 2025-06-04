-- Initialize Traffic Management Database

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS traffic_db;
USE traffic_db;

-- Create user if not exists
CREATE USER IF NOT EXISTS 'traffic_user'@'%' IDENTIFIED BY 'traffic_password';
GRANT ALL PRIVILEGES ON traffic_db.* TO 'traffic_user'@'%';
FLUSH PRIVILEGES;

-- Sample data for testing
INSERT INTO traffic_data (location, latitude, longitude, traffic_density, timestamp, vehicle_count, average_speed, weather_condition, created_at, updated_at) VALUES
('Vijayawada Junction', 16.5062, 80.6480, 'HIGH', '2024-01-15 08:30:00', 65, 15.5, 'CLEAR', NOW(), NOW()),
('Benz Circle', 16.5070, 80.6490, 'MODERATE', '2024-01-15 08:35:00', 35, 25.0, 'CLEAR', NOW(), NOW()),
('PNBS Bus Stand', 16.5080, 80.6500, 'LOW', '2024-01-15 08:40:00', 15, 45.0, 'CLEAR', NOW(), NOW()),
('Ramavarappadu Junction', 16.5090, 80.6510, 'MODERATE', '2024-01-15 08:45:00', 40, 20.0, 'CLEAR', NOW(), NOW()),
('NH65 Highway', 16.5100, 80.6520, 'HIGH', '2024-01-15 08:50:00', 70, 12.0, 'CLEAR', NOW(), NOW()),

-- Bangalore data
('Electronic City', 12.8456, 77.6603, 'CRITICAL', '2024-01-15 09:00:00', 95, 8.0, 'CLEAR', NOW(), NOW()),
('Silk Board Junction', 12.9176, 77.6227, 'HIGH', '2024-01-15 09:05:00', 80, 10.5, 'CLEAR', NOW(), NOW()),
('Koramangala', 12.9352, 77.6245, 'MODERATE', '2024-01-15 09:10:00', 45, 22.0, 'CLEAR', NOW(), NOW()),
('Outer Ring Road', 12.9698, 77.7500, 'HIGH', '2024-01-15 09:15:00', 75, 18.0, 'CLEAR', NOW(), NOW()),

-- Hyderabad data
('HITEC City', 17.4435, 78.3772, 'HIGH', '2024-01-15 09:20:00', 85, 14.0, 'CLEAR', NOW(), NOW()),
('Gachibowli', 17.4399, 78.3482, 'MODERATE', '2024-01-15 09:25:00', 50, 28.0, 'CLEAR', NOW(), NOW()),
('Madhapur', 17.4474, 78.3914, 'CRITICAL', '2024-01-15 09:30:00', 100, 5.0, 'RAIN', NOW(), NOW()),

-- Evening rush hour data
('Vijayawada Junction', 16.5062, 80.6480, 'CRITICAL', '2024-01-15 18:30:00', 100, 8.0, 'CLEAR', NOW(), NOW()),
('Benz Circle', 16.5070, 80.6490, 'HIGH', '2024-01-15 18:35:00', 75, 12.0, 'CLEAR', NOW(), NOW()),
('Electronic City', 12.8456, 77.6603, 'HIGH', '2024-01-15 18:40:00', 85, 15.0, 'CLEAR', NOW(), NOW()),

-- Night time data
('Vijayawada Junction', 16.5062, 80.6480, 'LOW', '2024-01-15 23:30:00', 10, 50.0, 'CLEAR', NOW(), NOW()),
('HITEC City', 17.4435, 78.3772, 'LOW', '2024-01-15 23:35:00', 8, 55.0, 'CLEAR', NOW(), NOW()),

-- Weekend data
('Koramangala', 12.9352, 77.6245, 'LOW', '2024-01-13 10:00:00', 20, 40.0, 'CLEAR', NOW(), NOW()),
('Gachibowli', 17.4399, 78.3482, 'MODERATE', '2024-01-13 15:00:00', 35, 30.0, 'CLEAR', NOW(), NOW());

-- Create indexes for better performance
CREATE INDEX idx_traffic_location ON traffic_data(location);
CREATE INDEX idx_traffic_timestamp ON traffic_data(timestamp);
CREATE INDEX idx_traffic_coordinates ON traffic_data(latitude, longitude);
CREATE INDEX idx_traffic_density ON traffic_data(traffic_density);

-- Create a view for recent traffic data
CREATE VIEW recent_traffic_view AS
SELECT 
    location,
    latitude,
    longitude,
    traffic_density,
    vehicle_count,
    average_speed,
    weather_condition,
    timestamp
FROM traffic_data 
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY timestamp DESC;

-- Create a view for traffic summary by location
CREATE VIEW traffic_summary_view AS
SELECT 
    location,
    COUNT(*) as total_records,
    AVG(vehicle_count) as avg_vehicle_count,
    AVG(average_speed) as avg_speed,
    MAX(timestamp) as last_updated
FROM traffic_data 
GROUP BY location
ORDER BY total_records DESC;
