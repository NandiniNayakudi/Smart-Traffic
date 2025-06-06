/**
 * Real-time WebSocket client for Smart City Traffic Optimization System
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */

// ===== WEBSOCKET CONNECTION MANAGEMENT =====
class RealTimeTrafficClient {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.heartbeatInterval = null;
        
        // Event listeners
        this.eventListeners = {
            'traffic-update': [],
            'alert': [],
            'analytics-update': [],
            'performance-update': [],
            'connection-status': []
        };
    }

    /**
     * Connect to WebSocket server
     */
    connect() {
        if (this.connected) {
            console.log('Already connected to real-time server');
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            try {
                // Load SockJS and STOMP libraries if not already loaded
                this.loadLibraries().then(() => {
                    const socket = new SockJS('/ws');
                    this.stompClient = Stomp.over(socket);
                    
                    // Disable debug logging in production
                    this.stompClient.debug = (msg) => {
                        if (window.location.hostname === 'localhost') {
                            console.log('STOMP:', msg);
                        }
                    };

                    this.stompClient.connect({}, 
                        (frame) => {
                            console.log('✅ Connected to real-time traffic server');
                            this.connected = true;
                            this.reconnectAttempts = 0;
                            this.startHeartbeat();
                            this.emit('connection-status', { connected: true });
                            resolve();
                        },
                        (error) => {
                            console.error('❌ Failed to connect to real-time server:', error);
                            this.connected = false;
                            this.emit('connection-status', { connected: false, error });
                            this.scheduleReconnect();
                            reject(error);
                        }
                    );
                }).catch(reject);
            } catch (error) {
                console.error('Error initializing WebSocket connection:', error);
                reject(error);
            }
        });
    }

    /**
     * Load required libraries dynamically
     */
    async loadLibraries() {
        // Check if libraries are already loaded
        if (window.SockJS && window.Stomp) {
            return Promise.resolve();
        }

        const loadScript = (src) => {
            return new Promise((resolve, reject) => {
                const script = document.createElement('script');
                script.src = src;
                script.onload = resolve;
                script.onerror = reject;
                document.head.appendChild(script);
            });
        };

        try {
            if (!window.SockJS) {
                await loadScript('https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js');
            }
            if (!window.Stomp) {
                await loadScript('https://cdn.jsdelivr.net/npm/@stomp/stompjs@6.1.2/bundles/stomp.umd.min.js');
                window.Stomp = window.StompJs;
            }
        } catch (error) {
            console.error('Failed to load WebSocket libraries:', error);
            throw error;
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect(() => {
                console.log('Disconnected from real-time server');
                this.connected = false;
                this.stopHeartbeat();
                this.emit('connection-status', { connected: false });
            });
        }
    }

    /**
     * Subscribe to traffic updates
     */
    subscribeToTraffic(callback) {
        if (!this.connected) {
            console.warn('Not connected to real-time server');
            return null;
        }

        const subscription = this.stompClient.subscribe('/topic/traffic', (message) => {
            try {
                const data = JSON.parse(message.body);
                this.emit('traffic-update', data);
                if (callback) callback(data);
            } catch (error) {
                console.error('Error parsing traffic update:', error);
            }
        });

        this.subscriptions.set('traffic', subscription);
        
        // Request current snapshot
        this.stompClient.send('/app/traffic/subscribe', {}, '{}');
        
        return subscription;
    }

    /**
     * Subscribe to alerts
     */
    subscribeToAlerts(callback) {
        if (!this.connected) {
            console.warn('Not connected to real-time server');
            return null;
        }

        const subscription = this.stompClient.subscribe('/topic/alerts', (message) => {
            try {
                const data = JSON.parse(message.body);
                this.emit('alert', data);
                if (callback) callback(data);
            } catch (error) {
                console.error('Error parsing alert:', error);
            }
        });

        this.subscriptions.set('alerts', subscription);
        
        // Request current alerts
        this.stompClient.send('/app/alerts/subscribe', {}, '{}');
        
        return subscription;
    }

    /**
     * Subscribe to analytics updates
     */
    subscribeToAnalytics(callback) {
        if (!this.connected) {
            console.warn('Not connected to real-time server');
            return null;
        }

        const subscription = this.stompClient.subscribe('/topic/analytics', (message) => {
            try {
                const data = JSON.parse(message.body);
                this.emit('analytics-update', data);
                if (callback) callback(data);
            } catch (error) {
                console.error('Error parsing analytics update:', error);
            }
        });

        this.subscriptions.set('analytics', subscription);

        // Request current analytics
        this.stompClient.send('/app/analytics/subscribe', {}, '{}');

        return subscription;
    }

    /**
     * Subscribe to performance monitoring
     */
    subscribeToPerformance(callback) {
        if (!this.connected) {
            console.warn('Not connected to real-time server');
            return null;
        }

        const subscription = this.stompClient.subscribe('/topic/performance', (message) => {
            try {
                const data = JSON.parse(message.body);
                this.emit('performance-update', data);
                if (callback) callback(data);
            } catch (error) {
                console.error('Error parsing performance update:', error);
            }
        });

        this.subscriptions.set('performance', subscription);

        // Request current performance metrics
        this.stompClient.send('/app/performance/subscribe', {}, '{}');

        return subscription;
    }

    /**
     * Subscribe to location-specific traffic
     */
    subscribeToLocation(locationId, callback) {
        if (!this.connected) {
            console.warn('Not connected to real-time server');
            return null;
        }

        const topic = `/topic/traffic/location/${locationId}`;
        const subscription = this.stompClient.subscribe(topic, (message) => {
            try {
                const data = JSON.parse(message.body);
                if (callback) callback(data);
            } catch (error) {
                console.error('Error parsing location update:', error);
            }
        });

        this.subscriptions.set(`location-${locationId}`, subscription);
        
        // Request location data
        this.stompClient.send(`/app/traffic/location/${locationId}`, {}, '{}');
        
        return subscription;
    }

    /**
     * Unsubscribe from a topic
     */
    unsubscribe(subscriptionKey) {
        const subscription = this.subscriptions.get(subscriptionKey);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(subscriptionKey);
        }
    }

    /**
     * Add event listener
     */
    addEventListener(event, callback) {
        if (this.eventListeners[event]) {
            this.eventListeners[event].push(callback);
        }
    }

    /**
     * Remove event listener
     */
    removeEventListener(event, callback) {
        if (this.eventListeners[event]) {
            const index = this.eventListeners[event].indexOf(callback);
            if (index > -1) {
                this.eventListeners[event].splice(index, 1);
            }
        }
    }

    /**
     * Emit event to listeners
     */
    emit(event, data) {
        if (this.eventListeners[event]) {
            this.eventListeners[event].forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error(`Error in event listener for ${event}:`, error);
                }
            });
        }
    }

    /**
     * Start heartbeat to keep connection alive
     */
    startHeartbeat() {
        this.heartbeatInterval = setInterval(() => {
            if (this.connected && this.stompClient) {
                try {
                    this.stompClient.send('/app/heartbeat', {}, '{}');
                } catch (error) {
                    console.warn('Heartbeat failed:', error);
                }
            }
        }, 30000); // Every 30 seconds
    }

    /**
     * Stop heartbeat
     */
    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    /**
     * Schedule reconnection attempt
     */
    scheduleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
            
            console.log(`Scheduling reconnect attempt ${this.reconnectAttempts} in ${delay}ms`);
            
            setTimeout(() => {
                console.log(`Reconnect attempt ${this.reconnectAttempts}`);
                this.connect().catch(() => {
                    // Will schedule another reconnect if this fails
                });
            }, delay);
        } else {
            console.error('Max reconnection attempts reached');
            this.emit('connection-status', { connected: false, maxAttemptsReached: true });
        }
    }

    /**
     * Get connection status
     */
    isConnected() {
        return this.connected;
    }
}

// ===== GLOBAL INSTANCE =====
window.realTimeClient = new RealTimeTrafficClient();

// ===== UTILITY FUNCTIONS =====
window.RealTimeTraffic = {
    client: window.realTimeClient,
    
    /**
     * Initialize real-time connection
     */
    async init() {
        try {
            await window.realTimeClient.connect();
            return true;
        } catch (error) {
            console.error('Failed to initialize real-time connection:', error);
            return false;
        }
    },

    /**
     * Subscribe to all real-time updates
     */
    subscribeToAll(callbacks = {}) {
        const client = window.realTimeClient;
        
        if (callbacks.traffic) {
            client.subscribeToTraffic(callbacks.traffic);
        }
        
        if (callbacks.alerts) {
            client.subscribeToAlerts(callbacks.alerts);
        }
        
        if (callbacks.analytics) {
            client.subscribeToAnalytics(callbacks.analytics);
        }
        
        if (callbacks.connectionStatus) {
            client.addEventListener('connection-status', callbacks.connectionStatus);
        }
    },

    /**
     * Cleanup connections
     */
    cleanup() {
        window.realTimeClient.disconnect();
    }
};
