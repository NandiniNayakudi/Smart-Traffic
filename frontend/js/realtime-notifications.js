/**
 * Real-time Notifications System for Smart City Traffic Optimization
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */

// ===== REAL-TIME NOTIFICATION MANAGER =====
class RealTimeNotificationManager {
    constructor() {
        this.notifications = new Map();
        this.container = null;
        this.maxNotifications = 5;
        this.defaultDuration = 5000;
        this.soundEnabled = true;
        this.browserNotificationsEnabled = false;
        
        // Notification types and their configurations
        this.notificationTypes = {
            'traffic-update': {
                icon: 'fas fa-car',
                color: '#3b82f6',
                sound: 'notification.mp3',
                priority: 'normal'
            },
            'alert': {
                icon: 'fas fa-exclamation-triangle',
                color: '#ef4444',
                sound: 'alert.mp3',
                priority: 'high'
            },
            'warning': {
                icon: 'fas fa-exclamation-circle',
                color: '#f59e0b',
                sound: 'warning.mp3',
                priority: 'medium'
            },
            'info': {
                icon: 'fas fa-info-circle',
                color: '#10b981',
                sound: 'info.mp3',
                priority: 'low'
            },
            'success': {
                icon: 'fas fa-check-circle',
                color: '#10b981',
                sound: 'success.mp3',
                priority: 'low'
            },
            'error': {
                icon: 'fas fa-times-circle',
                color: '#ef4444',
                sound: 'error.mp3',
                priority: 'high'
            }
        };
        
        this.init();
    }

    /**
     * Initialize notification system
     */
    init() {
        this.createNotificationContainer();
        this.requestBrowserPermission();
        this.subscribeToRealTimeEvents();
        this.loadUserPreferences();
        
        console.log('🔔 Real-time notification system initialized');
    }

    /**
     * Create notification container
     */
    createNotificationContainer() {
        // Remove existing container if any
        const existing = document.getElementById('realtime-notifications');
        if (existing) {
            existing.remove();
        }

        this.container = document.createElement('div');
        this.container.id = 'realtime-notifications';
        this.container.className = 'realtime-notifications-container';
        this.container.innerHTML = `
            <style>
                .realtime-notifications-container {
                    position: fixed;
                    top: 80px;
                    right: 20px;
                    z-index: 10000;
                    max-width: 400px;
                    pointer-events: none;
                }
                
                .realtime-notification {
                    background: var(--bg-primary, #ffffff);
                    border: 1px solid var(--border-color, #e5e7eb);
                    border-radius: 12px;
                    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
                    margin-bottom: 12px;
                    padding: 16px;
                    pointer-events: auto;
                    transform: translateX(100%);
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    backdrop-filter: blur(10px);
                    border-left: 4px solid;
                }
                
                .realtime-notification.show {
                    transform: translateX(0);
                }
                
                .realtime-notification.hide {
                    transform: translateX(100%);
                    opacity: 0;
                }
                
                .notification-header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    margin-bottom: 8px;
                }
                
                .notification-icon {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    font-weight: 600;
                    font-size: 14px;
                }
                
                .notification-close {
                    background: none;
                    border: none;
                    cursor: pointer;
                    padding: 4px;
                    border-radius: 4px;
                    opacity: 0.6;
                    transition: opacity 0.2s;
                }
                
                .notification-close:hover {
                    opacity: 1;
                    background: rgba(0, 0, 0, 0.1);
                }
                
                .notification-content {
                    font-size: 14px;
                    line-height: 1.4;
                    color: var(--text-secondary, #6b7280);
                }
                
                .notification-meta {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-top: 8px;
                    font-size: 12px;
                    opacity: 0.7;
                }
                
                .notification-actions {
                    display: flex;
                    gap: 8px;
                    margin-top: 12px;
                }
                
                .notification-action {
                    padding: 6px 12px;
                    border: 1px solid var(--border-color, #e5e7eb);
                    border-radius: 6px;
                    background: transparent;
                    cursor: pointer;
                    font-size: 12px;
                    transition: all 0.2s;
                }
                
                .notification-action:hover {
                    background: var(--bg-secondary, #f9fafb);
                }
                
                .notification-action.primary {
                    background: var(--primary-color, #3b82f6);
                    color: white;
                    border-color: var(--primary-color, #3b82f6);
                }
                
                .notification-action.primary:hover {
                    background: var(--primary-dark, #2563eb);
                }
                
                .notification-progress {
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    height: 3px;
                    background: currentColor;
                    border-radius: 0 0 12px 12px;
                    transition: width linear;
                }
                
                @media (max-width: 768px) {
                    .realtime-notifications-container {
                        top: 60px;
                        right: 10px;
                        left: 10px;
                        max-width: none;
                    }
                }
            </style>
        `;
        
        document.body.appendChild(this.container);
    }

    /**
     * Request browser notification permission
     */
    async requestBrowserPermission() {
        if ('Notification' in window) {
            const permission = await Notification.requestPermission();
            this.browserNotificationsEnabled = permission === 'granted';
            
            if (this.browserNotificationsEnabled) {
                console.log('✅ Browser notifications enabled');
            }
        }
    }

    /**
     * Subscribe to real-time events
     */
    subscribeToRealTimeEvents() {
        if (window.realTimeClient) {
            // Traffic updates
            window.realTimeClient.addEventListener('traffic-update', (data) => {
                this.handleTrafficUpdate(data);
            });

            // Alerts
            window.realTimeClient.addEventListener('alert', (data) => {
                this.handleAlert(data);
            });

            // Connection status
            window.realTimeClient.addEventListener('connection-status', (status) => {
                this.handleConnectionStatus(status);
            });
        }
    }

    /**
     * Handle traffic updates
     */
    handleTrafficUpdate(data) {
        if (data.type === 'TRAFFIC_UPDATE' && data.data) {
            const trafficData = data.data;
            
            // Only notify for significant changes
            if (trafficData.trafficDensity === 'HIGH' || trafficData.trafficDensity === 'CRITICAL') {
                this.show({
                    type: 'traffic-update',
                    title: 'Traffic Update',
                    message: `${trafficData.trafficDensity.toLowerCase()} traffic detected at ${trafficData.location}`,
                    data: trafficData,
                    actions: [
                        {
                            label: 'View Details',
                            action: () => this.viewTrafficDetails(trafficData)
                        },
                        {
                            label: 'Find Route',
                            action: () => this.findAlternativeRoute(trafficData),
                            primary: true
                        }
                    ]
                });
            }
        }
    }

    /**
     * Handle alerts
     */
    handleAlert(data) {
        if (data.type === 'ALERT' && data.alert) {
            const alert = data.alert;
            
            this.show({
                type: 'alert',
                title: alert.type.replace('_', ' '),
                message: alert.message,
                data: alert,
                duration: alert.severity === 'CRITICAL' ? 10000 : 7000,
                actions: [
                    {
                        label: 'Dismiss',
                        action: () => {}
                    },
                    {
                        label: 'View Location',
                        action: () => this.viewAlertLocation(alert),
                        primary: true
                    }
                ]
            });
        }
    }

    /**
     * Handle connection status changes
     */
    handleConnectionStatus(status) {
        if (status.connected) {
            this.show({
                type: 'success',
                title: 'Connected',
                message: 'Real-time updates are now active',
                duration: 3000
            });
        } else if (status.maxAttemptsReached) {
            this.show({
                type: 'error',
                title: 'Connection Lost',
                message: 'Unable to connect to real-time updates. Using cached data.',
                duration: 8000,
                actions: [
                    {
                        label: 'Retry',
                        action: () => this.retryConnection(),
                        primary: true
                    }
                ]
            });
        }
    }

    /**
     * Show notification
     */
    show(options) {
        const notification = this.createNotification(options);
        this.addNotification(notification);
        
        // Browser notification for high priority
        if (this.browserNotificationsEnabled && this.isHighPriority(options.type)) {
            this.showBrowserNotification(options);
        }
        
        // Play sound
        if (this.soundEnabled) {
            this.playNotificationSound(options.type);
        }
        
        return notification.id;
    }

    /**
     * Create notification element
     */
    createNotification(options) {
        const id = 'notification-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        const config = this.notificationTypes[options.type] || this.notificationTypes['info'];
        const duration = options.duration || this.defaultDuration;
        
        const notification = {
            id,
            type: options.type,
            title: options.title,
            message: options.message,
            data: options.data,
            actions: options.actions || [],
            duration,
            timestamp: new Date(),
            element: null
        };

        // Create DOM element
        const element = document.createElement('div');
        element.className = 'realtime-notification';
        element.style.borderLeftColor = config.color;
        element.innerHTML = `
            <div class="notification-header">
                <div class="notification-icon" style="color: ${config.color}">
                    <i class="${config.icon}"></i>
                    <span>${notification.title}</span>
                </div>
                <button class="notification-close" onclick="window.realtimeNotifications.hide('${id}')">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="notification-content">${notification.message}</div>
            ${notification.actions.length > 0 ? `
                <div class="notification-actions">
                    ${notification.actions.map((action, index) => `
                        <button class="notification-action ${action.primary ? 'primary' : ''}" 
                                onclick="window.realtimeNotifications.executeAction('${id}', ${index})">
                            ${action.label}
                        </button>
                    `).join('')}
                </div>
            ` : ''}
            <div class="notification-meta">
                <span>${notification.timestamp.toLocaleTimeString()}</span>
                <span>${options.type}</span>
            </div>
            ${duration > 0 ? `<div class="notification-progress" style="width: 100%; animation: shrink ${duration}ms linear;"></div>` : ''}
            <style>
                @keyframes shrink {
                    from { width: 100%; }
                    to { width: 0%; }
                }
            </style>
        `;

        notification.element = element;
        return notification;
    }

    /**
     * Add notification to container
     */
    addNotification(notification) {
        // Remove oldest notifications if at limit
        while (this.notifications.size >= this.maxNotifications) {
            const oldestId = this.notifications.keys().next().value;
            this.hide(oldestId);
        }

        // Add to container
        this.container.appendChild(notification.element);
        this.notifications.set(notification.id, notification);

        // Trigger show animation
        setTimeout(() => {
            notification.element.classList.add('show');
        }, 10);

        // Auto-hide if duration is set
        if (notification.duration > 0) {
            setTimeout(() => {
                this.hide(notification.id);
            }, notification.duration);
        }
    }

    /**
     * Hide notification
     */
    hide(notificationId) {
        const notification = this.notifications.get(notificationId);
        if (!notification) return;

        notification.element.classList.add('hide');
        
        setTimeout(() => {
            if (notification.element.parentNode) {
                notification.element.parentNode.removeChild(notification.element);
            }
            this.notifications.delete(notificationId);
        }, 300);
    }

    /**
     * Execute notification action
     */
    executeAction(notificationId, actionIndex) {
        const notification = this.notifications.get(notificationId);
        if (!notification || !notification.actions[actionIndex]) return;

        const action = notification.actions[actionIndex];
        if (typeof action.action === 'function') {
            action.action(notification.data);
        }

        // Hide notification after action
        this.hide(notificationId);
    }

    /**
     * Show browser notification
     */
    showBrowserNotification(options) {
        if (!this.browserNotificationsEnabled) return;

        const notification = new Notification(options.title, {
            body: options.message,
            icon: '/favicon.ico',
            badge: '/favicon.ico',
            tag: options.type,
            requireInteraction: this.isHighPriority(options.type)
        });

        notification.onclick = () => {
            window.focus();
            notification.close();
        };

        // Auto-close after 5 seconds
        setTimeout(() => {
            notification.close();
        }, 5000);
    }

    /**
     * Play notification sound
     */
    playNotificationSound(type) {
        try {
            const config = this.notificationTypes[type];
            if (config && config.sound) {
                const audio = new Audio(`/sounds/${config.sound}`);
                audio.volume = 0.3;
                audio.play().catch(() => {
                    // Fallback to system beep
                    this.playSystemBeep();
                });
            }
        } catch (error) {
            console.warn('Could not play notification sound:', error);
        }
    }

    /**
     * Play system beep as fallback
     */
    playSystemBeep() {
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.value = 800;
            oscillator.type = 'sine';
            gainNode.gain.value = 0.1;
            
            oscillator.start();
            oscillator.stop(audioContext.currentTime + 0.1);
        } catch (error) {
            console.warn('Could not play system beep:', error);
        }
    }

    /**
     * Check if notification type is high priority
     */
    isHighPriority(type) {
        const config = this.notificationTypes[type];
        return config && (config.priority === 'high' || config.priority === 'critical');
    }

    /**
     * Action handlers
     */
    viewTrafficDetails(trafficData) {
        if (window.TrafficDashboard && window.TrafficDashboard.showTrafficDetails) {
            window.TrafficDashboard.showTrafficDetails(trafficData);
        }
    }

    findAlternativeRoute(trafficData) {
        if (window.RealTimeMap && window.RealTimeMap.centerOn) {
            window.RealTimeMap.centerOn(trafficData.location);
        }
    }

    viewAlertLocation(alert) {
        if (window.RealTimeMap && window.RealTimeMap.centerOn) {
            window.RealTimeMap.centerOn(alert.location);
        }
    }

    retryConnection() {
        if (window.RealTimeTraffic && window.RealTimeTraffic.init) {
            window.RealTimeTraffic.init();
        }
    }

    /**
     * Load user preferences
     */
    loadUserPreferences() {
        try {
            const prefs = localStorage.getItem('notification-preferences');
            if (prefs) {
                const preferences = JSON.parse(prefs);
                this.soundEnabled = preferences.soundEnabled !== false;
                this.maxNotifications = preferences.maxNotifications || 5;
            }
        } catch (error) {
            console.warn('Could not load notification preferences:', error);
        }
    }

    /**
     * Save user preferences
     */
    saveUserPreferences() {
        try {
            const preferences = {
                soundEnabled: this.soundEnabled,
                maxNotifications: this.maxNotifications
            };
            localStorage.setItem('notification-preferences', JSON.stringify(preferences));
        } catch (error) {
            console.warn('Could not save notification preferences:', error);
        }
    }

    /**
     * Toggle sound
     */
    toggleSound() {
        this.soundEnabled = !this.soundEnabled;
        this.saveUserPreferences();
        return this.soundEnabled;
    }

    /**
     * Clear all notifications
     */
    clearAll() {
        this.notifications.forEach((notification, id) => {
            this.hide(id);
        });
    }

    /**
     * Get notification count
     */
    getCount() {
        return this.notifications.size;
    }

    /**
     * Cleanup
     */
    cleanup() {
        this.clearAll();
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
    }
}

// ===== GLOBAL INSTANCE =====
window.realtimeNotifications = new RealTimeNotificationManager();

// ===== UTILITY FUNCTIONS =====
window.RealTimeNotifications = {
    manager: window.realtimeNotifications,
    
    /**
     * Show notification
     */
    show(options) {
        return window.realtimeNotifications.show(options);
    },

    /**
     * Hide notification
     */
    hide(id) {
        window.realtimeNotifications.hide(id);
    },

    /**
     * Clear all notifications
     */
    clearAll() {
        window.realtimeNotifications.clearAll();
    },

    /**
     * Toggle sound
     */
    toggleSound() {
        return window.realtimeNotifications.toggleSound();
    },

    /**
     * Get count
     */
    getCount() {
        return window.realtimeNotifications.getCount();
    }
};
