/**
 * Real-time Map Integration for Smart City Traffic Optimization System
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */

// ===== REAL-TIME MAP MANAGER =====
class RealTimeMapManager {
    constructor() {
        this.map = null;
        this.markers = new Map();
        this.heatmapLayer = null;
        this.trafficLayer = null;
        this.alertMarkers = new Map();
        this.routePolylines = new Map();
        this.isInitialized = false;
        
        // Map configuration
        this.mapConfig = {
            center: { lat: 40.7128, lng: -74.0060 }, // New York City
            zoom: 12,
            styles: this.getMapStyles()
        };
        
        // Real-time data cache
        this.trafficData = new Map();
        this.alertData = new Map();
    }

    /**
     * Initialize the real-time map
     */
    async initialize(containerId) {
        try {
            // Load Google Maps API if not already loaded
            await this.loadGoogleMapsAPI();
            
            const container = document.getElementById(containerId);
            if (!container) {
                throw new Error(`Map container ${containerId} not found`);
            }

            // Initialize map
            this.map = new google.maps.Map(container, this.mapConfig);
            
            // Initialize layers
            this.initializeTrafficLayer();
            this.initializeHeatmapLayer();
            
            // Set up real-time subscriptions
            this.subscribeToRealTimeUpdates();
            
            this.isInitialized = true;
            console.log('✅ Real-time map initialized successfully');
            
            return true;
        } catch (error) {
            console.error('❌ Failed to initialize real-time map:', error);
            return false;
        }
    }

    /**
     * Load Google Maps API dynamically
     */
    async loadGoogleMapsAPI() {
        if (window.google && window.google.maps) {
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            // Use a demo API key - replace with your actual key
            const apiKey = 'YOUR_GOOGLE_MAPS_API_KEY';
            const script = document.createElement('script');
            script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=visualization,geometry`;
            script.async = true;
            script.defer = true;
            
            script.onload = () => resolve();
            script.onerror = () => {
                // Fallback: Use OpenStreetMap with Leaflet
                this.loadLeafletFallback().then(resolve).catch(reject);
            };
            
            document.head.appendChild(script);
        });
    }

    /**
     * Fallback to Leaflet with OpenStreetMap
     */
    async loadLeafletFallback() {
        console.log('🗺️ Loading Leaflet fallback for maps...');
        
        // Load Leaflet CSS and JS
        const loadCSS = (href) => {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = href;
            document.head.appendChild(link);
        };
        
        const loadJS = (src) => {
            return new Promise((resolve, reject) => {
                const script = document.createElement('script');
                script.src = src;
                script.onload = resolve;
                script.onerror = reject;
                document.head.appendChild(script);
            });
        };

        loadCSS('https://unpkg.com/leaflet@1.9.4/dist/leaflet.css');
        await loadJS('https://unpkg.com/leaflet@1.9.4/dist/leaflet.js');
        await loadJS('https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js');
        
        // Set up Leaflet compatibility layer
        this.setupLeafletCompatibility();
    }

    /**
     * Set up Leaflet compatibility layer
     */
    setupLeafletCompatibility() {
        window.google = {
            maps: {
                Map: class {
                    constructor(element, options) {
                        this.leafletMap = L.map(element).setView(
                            [options.center.lat, options.center.lng], 
                            options.zoom
                        );
                        
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(this.leafletMap);
                        
                        this.markers = [];
                        this.polylines = [];
                    }
                    
                    setCenter(latLng) {
                        this.leafletMap.setView([latLng.lat, latLng.lng]);
                    }
                    
                    setZoom(zoom) {
                        this.leafletMap.setZoom(zoom);
                    }
                },
                
                Marker: class {
                    constructor(options) {
                        this.position = options.position;
                        this.map = options.map;
                        this.title = options.title;
                        this.icon = options.icon;
                        
                        if (this.map && this.map.leafletMap) {
                            this.leafletMarker = L.marker([this.position.lat, this.position.lng])
                                .addTo(this.map.leafletMap);
                            
                            if (this.title) {
                                this.leafletMarker.bindPopup(this.title);
                            }
                        }
                    }
                    
                    setMap(map) {
                        if (map && map.leafletMap) {
                            this.leafletMarker.addTo(map.leafletMap);
                        } else if (this.leafletMarker) {
                            this.leafletMarker.remove();
                        }
                    }
                    
                    setPosition(position) {
                        this.position = position;
                        if (this.leafletMarker) {
                            this.leafletMarker.setLatLng([position.lat, position.lng]);
                        }
                    }
                },
                
                InfoWindow: class {
                    constructor(options) {
                        this.content = options.content;
                        this.isOpen = false;
                    }
                    
                    open(map, marker) {
                        if (marker && marker.leafletMarker) {
                            marker.leafletMarker.bindPopup(this.content).openPopup();
                            this.isOpen = true;
                        }
                    }
                    
                    close() {
                        this.isOpen = false;
                    }
                },
                
                LatLng: class {
                    constructor(lat, lng) {
                        this.lat = lat;
                        this.lng = lng;
                    }
                }
            }
        };
    }

    /**
     * Initialize traffic layer
     */
    initializeTrafficLayer() {
        if (!this.map) return;
        
        // For Google Maps
        if (google.maps.TrafficLayer) {
            this.trafficLayer = new google.maps.TrafficLayer();
        }
        
        console.log('🚦 Traffic layer initialized');
    }

    /**
     * Initialize heatmap layer
     */
    initializeHeatmapLayer() {
        if (!this.map) return;
        
        try {
            // For Google Maps
            if (google.maps.visualization && google.maps.visualization.HeatmapLayer) {
                this.heatmapLayer = new google.maps.visualization.HeatmapLayer({
                    data: [],
                    map: this.map,
                    radius: 50,
                    opacity: 0.6
                });
            }
            // For Leaflet
            else if (this.map.leafletMap && L.heatLayer) {
                this.heatmapLayer = L.heatLayer([], {
                    radius: 50,
                    blur: 25,
                    maxZoom: 17
                }).addTo(this.map.leafletMap);
            }
            
            console.log('🔥 Heatmap layer initialized');
        } catch (error) {
            console.warn('⚠️ Heatmap layer not available:', error);
        }
    }

    /**
     * Subscribe to real-time updates
     */
    subscribeToRealTimeUpdates() {
        if (!window.realTimeClient) {
            console.warn('Real-time client not available');
            return;
        }

        // Subscribe to traffic updates
        window.realTimeClient.addEventListener('traffic-update', (data) => {
            this.handleTrafficUpdate(data);
        });

        // Subscribe to alerts
        window.realTimeClient.addEventListener('alert', (data) => {
            this.handleAlertUpdate(data);
        });

        console.log('📡 Subscribed to real-time map updates');
    }

    /**
     * Handle real-time traffic updates
     */
    handleTrafficUpdate(data) {
        if (!this.isInitialized) return;

        if (data.type === 'TRAFFIC_UPDATE' && data.data) {
            this.updateTrafficMarker(data.data);
            this.updateHeatmap();
        } else if (data.type === 'TRAFFIC_SNAPSHOT' && data.locations) {
            data.locations.forEach(location => this.updateTrafficMarker(location));
            this.updateHeatmap();
        }
    }

    /**
     * Handle real-time alert updates
     */
    handleAlertUpdate(data) {
        if (!this.isInitialized) return;

        if (data.type === 'ALERT' && data.alert) {
            this.addAlertMarker(data.alert);
        }
    }

    /**
     * Update traffic marker on map
     */
    updateTrafficMarker(trafficData) {
        const markerId = trafficData.location;
        const position = new google.maps.LatLng(trafficData.latitude, trafficData.longitude);
        
        // Get marker color based on traffic density
        const markerColor = this.getTrafficColor(trafficData.trafficDensity);
        
        // Update existing marker or create new one
        if (this.markers.has(markerId)) {
            const marker = this.markers.get(markerId);
            marker.setPosition(position);
            marker.setIcon(this.createTrafficIcon(markerColor, trafficData.trafficDensity));
        } else {
            const marker = new google.maps.Marker({
                position: position,
                map: this.map,
                title: `${trafficData.location}\nDensity: ${trafficData.trafficDensity}\nSpeed: ${trafficData.averageSpeed || 'N/A'} km/h`,
                icon: this.createTrafficIcon(markerColor, trafficData.trafficDensity)
            });
            
            // Add info window
            const infoWindow = new google.maps.InfoWindow({
                content: this.createTrafficInfoContent(trafficData)
            });
            
            marker.addListener('click', () => {
                infoWindow.open(this.map, marker);
            });
            
            this.markers.set(markerId, marker);
        }
        
        // Update data cache
        this.trafficData.set(markerId, trafficData);
    }

    /**
     * Add alert marker to map
     */
    addAlertMarker(alertData) {
        if (!alertData.latitude || !alertData.longitude) return;
        
        const alertId = alertData.id;
        const position = new google.maps.LatLng(alertData.latitude, alertData.longitude);
        
        const marker = new google.maps.Marker({
            position: position,
            map: this.map,
            title: alertData.message,
            icon: this.createAlertIcon(alertData.severity),
            animation: google.maps.Animation.BOUNCE
        });
        
        // Stop animation after 3 seconds
        setTimeout(() => {
            marker.setAnimation(null);
        }, 3000);
        
        // Add info window
        const infoWindow = new google.maps.InfoWindow({
            content: this.createAlertInfoContent(alertData)
        });
        
        marker.addListener('click', () => {
            infoWindow.open(this.map, marker);
        });
        
        this.alertMarkers.set(alertId, marker);
        
        // Auto-remove alert after 10 minutes
        setTimeout(() => {
            this.removeAlertMarker(alertId);
        }, 600000);
    }

    /**
     * Update heatmap with current traffic data
     */
    updateHeatmap() {
        if (!this.heatmapLayer) return;
        
        const heatmapData = Array.from(this.trafficData.values()).map(data => {
            const weight = this.getTrafficWeight(data.trafficDensity);
            
            // For Google Maps
            if (google.maps.visualization) {
                return {
                    location: new google.maps.LatLng(data.latitude, data.longitude),
                    weight: weight
                };
            }
            // For Leaflet
            else {
                return [data.latitude, data.longitude, weight];
            }
        });
        
        // Update heatmap data
        if (this.heatmapLayer.setData) {
            this.heatmapLayer.setData(heatmapData);
        } else if (this.heatmapLayer.setLatLngs) {
            this.heatmapLayer.setLatLngs(heatmapData);
        }
    }

    /**
     * Get traffic color based on density
     */
    getTrafficColor(density) {
        const colors = {
            'LOW': '#10b981',      // Green
            'MODERATE': '#f59e0b', // Yellow
            'HIGH': '#ef4444',     // Red
            'CRITICAL': '#7c2d12'  // Dark red
        };
        return colors[density] || '#6b7280';
    }

    /**
     * Get traffic weight for heatmap
     */
    getTrafficWeight(density) {
        const weights = {
            'LOW': 0.2,
            'MODERATE': 0.5,
            'HIGH': 0.8,
            'CRITICAL': 1.0
        };
        return weights[density] || 0.3;
    }

    /**
     * Create traffic icon
     */
    createTrafficIcon(color, density) {
        return {
            path: google.maps.SymbolPath.CIRCLE,
            fillColor: color,
            fillOpacity: 0.8,
            strokeColor: '#ffffff',
            strokeWeight: 2,
            scale: density === 'CRITICAL' ? 12 : density === 'HIGH' ? 10 : 8
        };
    }

    /**
     * Create alert icon
     */
    createAlertIcon(severity) {
        const colors = {
            'INFO': '#3b82f6',
            'WARNING': '#f59e0b',
            'CRITICAL': '#ef4444',
            'EMERGENCY': '#7c2d12'
        };
        
        return {
            path: 'M12 2L1 21h22L12 2zm0 3.5L19.5 19h-15L12 5.5zM11 16v2h2v-2h-2zm0-6v4h2v-4h-2z',
            fillColor: colors[severity] || '#ef4444',
            fillOpacity: 0.9,
            strokeColor: '#ffffff',
            strokeWeight: 2,
            scale: 1.5
        };
    }

    /**
     * Create traffic info content
     */
    createTrafficInfoContent(data) {
        const timeAgo = Math.floor((Date.now() - new Date(data.timestamp).getTime()) / 60000);
        
        return `
            <div class="traffic-info">
                <h3>${data.location}</h3>
                <div class="traffic-details">
                    <p><strong>Density:</strong> <span class="density-${data.trafficDensity.toLowerCase()}">${data.trafficDensity}</span></p>
                    <p><strong>Speed:</strong> ${data.averageSpeed || 'N/A'} km/h</p>
                    <p><strong>Vehicles:</strong> ${data.vehicleCount || 'N/A'}</p>
                    <p><strong>Updated:</strong> ${timeAgo} minutes ago</p>
                </div>
            </div>
        `;
    }

    /**
     * Create alert info content
     */
    createAlertInfoContent(alert) {
        return `
            <div class="alert-info">
                <h3 class="alert-title">${alert.type.replace('_', ' ')}</h3>
                <div class="alert-details">
                    <p class="alert-message">${alert.message}</p>
                    <p><strong>Severity:</strong> <span class="severity-${alert.severity.toLowerCase()}">${alert.severity}</span></p>
                    <p><strong>Location:</strong> ${alert.location}</p>
                    <p><strong>Time:</strong> ${new Date(alert.timestamp).toLocaleString()}</p>
                </div>
            </div>
        `;
    }

    /**
     * Remove alert marker
     */
    removeAlertMarker(alertId) {
        const marker = this.alertMarkers.get(alertId);
        if (marker) {
            marker.setMap(null);
            this.alertMarkers.delete(alertId);
        }
    }

    /**
     * Toggle traffic layer
     */
    toggleTrafficLayer() {
        if (this.trafficLayer) {
            const isVisible = this.trafficLayer.getMap();
            this.trafficLayer.setMap(isVisible ? null : this.map);
            return !isVisible;
        }
        return false;
    }

    /**
     * Toggle heatmap layer
     */
    toggleHeatmapLayer() {
        if (this.heatmapLayer) {
            const isVisible = this.heatmapLayer.getMap ? this.heatmapLayer.getMap() : true;
            if (this.heatmapLayer.setMap) {
                this.heatmapLayer.setMap(isVisible ? null : this.map);
            } else if (this.heatmapLayer.addTo && this.heatmapLayer.remove) {
                if (isVisible) {
                    this.heatmapLayer.remove();
                } else {
                    this.heatmapLayer.addTo(this.map.leafletMap);
                }
            }
            return !isVisible;
        }
        return false;
    }

    /**
     * Get custom map styles
     */
    getMapStyles() {
        return [
            {
                featureType: 'all',
                elementType: 'geometry.fill',
                stylers: [{ color: '#1f2937' }]
            },
            {
                featureType: 'road',
                elementType: 'geometry',
                stylers: [{ color: '#374151' }]
            },
            {
                featureType: 'water',
                elementType: 'geometry',
                stylers: [{ color: '#0f172a' }]
            }
        ];
    }

    /**
     * Center map on location
     */
    centerOnLocation(location) {
        const trafficData = Array.from(this.trafficData.values())
            .find(data => data.location.toLowerCase().includes(location.toLowerCase()));
            
        if (trafficData) {
            const position = new google.maps.LatLng(trafficData.latitude, trafficData.longitude);
            this.map.setCenter(position);
            this.map.setZoom(15);
        }
    }

    /**
     * Cleanup resources
     */
    cleanup() {
        // Clear all markers
        this.markers.forEach(marker => marker.setMap(null));
        this.alertMarkers.forEach(marker => marker.setMap(null));
        
        // Clear data
        this.markers.clear();
        this.alertMarkers.clear();
        this.trafficData.clear();
        this.alertData.clear();
        
        this.isInitialized = false;
    }
}

// ===== GLOBAL INSTANCE =====
window.realTimeMap = new RealTimeMapManager();

// ===== UTILITY FUNCTIONS =====
window.RealTimeMap = {
    manager: window.realTimeMap,
    
    /**
     * Initialize real-time map
     */
    async init(containerId = 'realTimeMap') {
        return await window.realTimeMap.initialize(containerId);
    },

    /**
     * Toggle layers
     */
    toggleTraffic() {
        return window.realTimeMap.toggleTrafficLayer();
    },
    
    toggleHeatmap() {
        return window.realTimeMap.toggleHeatmapLayer();
    },

    /**
     * Center on location
     */
    centerOn(location) {
        window.realTimeMap.centerOnLocation(location);
    },

    /**
     * Cleanup
     */
    cleanup() {
        window.realTimeMap.cleanup();
    }
};
