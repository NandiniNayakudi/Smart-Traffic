// ===== DASHBOARD JAVASCRIPT =====

// Global variables
let trafficFlowChart, signalPerformanceChart;
let liveDataInterval, statsUpdateInterval;

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard DOM loaded');

    // Check if TrafficAuth is available
    if (typeof TrafficAuth === 'undefined') {
        console.error('TrafficAuth not available - auth.js may not be loaded');
        return;
    }

    console.log('Checking authentication...');

    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        console.log('Authentication failed - stopping dashboard initialization');
        return;
    }

    console.log('Authentication passed - initializing dashboard');

    try {
        initializeDashboard();
        initializeCharts();
        initializeEventListeners();
        startLiveUpdates();
        loadUserInfo();
        console.log('Dashboard initialization complete');
    } catch (error) {
        console.error('Error initializing dashboard:', error);
    }
});

// ===== INITIALIZATION =====
function initializeDashboard() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const logoutBtn = document.getElementById('logoutBtn');
    
    // Sidebar toggle
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', toggleSidebar);
    }
    
    // Mobile menu toggle
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', toggleMobileSidebar);
    }
    
    // Logout button
    if (logoutBtn) {
        logoutBtn.addEventListener('click', TrafficAuth.logout);
    }
    
    // Close mobile sidebar when clicking outside
    document.addEventListener('click', function(e) {
        if (window.innerWidth <= 768) {
            const sidebar = document.getElementById('sidebar');
            const mobileMenuToggle = document.getElementById('mobileMenuToggle');
            
            if (!sidebar.contains(e.target) && !mobileMenuToggle.contains(e.target)) {
                sidebar.classList.remove('mobile-open');
            }
        }
    });
}

function initializeEventListeners() {
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
    
    // Chart controls
    const flowTimeRange = document.getElementById('flowTimeRange');
    if (flowTimeRange) {
        flowTimeRange.addEventListener('change', updateTrafficFlowChart);
    }
    
    const refreshSignals = document.getElementById('refreshSignals');
    if (refreshSignals) {
        refreshSignals.addEventListener('click', updateSignalPerformanceChart);
    }
    
    // Theme toggle
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            // Use the main theme toggle function
            if (window.toggleTheme) {
                window.toggleTheme();
            }
        });
    }
}

// ===== SIDEBAR FUNCTIONALITY =====
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('collapsed');
    
    // Save preference
    localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
}

function toggleMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('mobile-open');
}

// ===== USER INFO =====
function loadUserInfo() {
    const userInfo = TrafficAuth.getUserInfo();
    
    if (userInfo) {
        const userName = document.getElementById('userName');
        const userRole = document.getElementById('userRole');
        
        if (userName) {
            userName.textContent = userInfo.username;
        }
        
        if (userRole) {
            userRole.textContent = userInfo.role;
        }
    }
}

// ===== CHARTS INITIALIZATION =====
function initializeCharts() {
    initializeTrafficFlowChart();
    initializeSignalPerformanceChart();
}

function initializeTrafficFlowChart() {
    const ctx = document.getElementById('trafficFlowChart');
    if (!ctx) return;
    
    trafficFlowChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: generateTimeLabels(24),
            datasets: [{
                label: 'Vehicle Count',
                data: generateTrafficData(24),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }, {
                label: 'Average Speed',
                data: generateSpeedData(24),
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Vehicle Count'
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'Speed (mph)'
                    },
                    grid: {
                        drawOnChartArea: false,
                    },
                }
            }
        }
    });
}

function initializeSignalPerformanceChart() {
    const ctx = document.getElementById('signalPerformanceChart');
    if (!ctx) return;
    
    signalPerformanceChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Optimal', 'Good', 'Needs Attention', 'Critical'],
            datasets: [{
                data: [45, 32, 18, 5],
                backgroundColor: [
                    '#10b981',
                    '#3b82f6',
                    '#f59e0b',
                    '#ef4444'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                },
                title: {
                    display: false
                }
            }
        }
    });
}

// ===== CHART DATA GENERATORS =====
function generateTimeLabels(hours) {
    const labels = [];
    const now = new Date();
    
    for (let i = hours - 1; i >= 0; i--) {
        const time = new Date(now.getTime() - (i * 60 * 60 * 1000));
        labels.push(time.toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
        }));
    }
    
    return labels;
}

function generateTrafficData(points) {
    const data = [];
    const baseTraffic = 100;
    
    for (let i = 0; i < points; i++) {
        // Simulate traffic patterns (higher during rush hours)
        const hour = (new Date().getHours() - points + i + 24) % 24;
        let multiplier = 1;
        
        if (hour >= 7 && hour <= 9) multiplier = 1.8; // Morning rush
        else if (hour >= 17 && hour <= 19) multiplier = 2.0; // Evening rush
        else if (hour >= 22 || hour <= 6) multiplier = 0.3; // Night time
        
        const randomVariation = 0.8 + Math.random() * 0.4;
        data.push(Math.round(baseTraffic * multiplier * randomVariation));
    }
    
    return data;
}

function generateSpeedData(points) {
    const data = [];
    const baseSpeed = 35;
    
    for (let i = 0; i < points; i++) {
        const hour = (new Date().getHours() - points + i + 24) % 24;
        let multiplier = 1;
        
        if (hour >= 7 && hour <= 9) multiplier = 0.6; // Morning rush - slower
        else if (hour >= 17 && hour <= 19) multiplier = 0.5; // Evening rush - slower
        else if (hour >= 22 || hour <= 6) multiplier = 1.2; // Night time - faster
        
        const randomVariation = 0.9 + Math.random() * 0.2;
        data.push(Math.round(baseSpeed * multiplier * randomVariation));
    }
    
    return data;
}

// ===== CHART UPDATES =====
function updateTrafficFlowChart() {
    const timeRange = document.getElementById('flowTimeRange').value;
    let hours;
    
    switch (timeRange) {
        case '7d': hours = 7 * 24; break;
        case '30d': hours = 30 * 24; break;
        default: hours = 24;
    }
    
    if (trafficFlowChart) {
        trafficFlowChart.data.labels = generateTimeLabels(Math.min(hours, 24));
        trafficFlowChart.data.datasets[0].data = generateTrafficData(Math.min(hours, 24));
        trafficFlowChart.data.datasets[1].data = generateSpeedData(Math.min(hours, 24));
        trafficFlowChart.update();
    }
}

function updateSignalPerformanceChart() {
    if (signalPerformanceChart) {
        // Simulate new data
        const newData = [
            40 + Math.random() * 10,
            30 + Math.random() * 10,
            15 + Math.random() * 10,
            5 + Math.random() * 5
        ];
        
        signalPerformanceChart.data.datasets[0].data = newData;
        signalPerformanceChart.update();
        
        // Show refresh feedback
        const refreshBtn = document.getElementById('refreshSignals');
        const originalText = refreshBtn.innerHTML;
        refreshBtn.innerHTML = '<i class="fas fa-check"></i> Updated';
        refreshBtn.classList.add('btn-success');
        
        setTimeout(() => {
            refreshBtn.innerHTML = originalText;
            refreshBtn.classList.remove('btn-success');
        }, 2000);
    }
}

// ===== LIVE DATA UPDATES =====
function startLiveUpdates() {
    // Initialize real-time WebSocket connection
    initializeRealTimeConnection();

    // Fallback: Update live traffic feed every 30 seconds (if WebSocket fails)
    liveDataInterval = setInterval(updateLiveTrafficFeed, 30000);

    // Update stats every 60 seconds
    statsUpdateInterval = setInterval(updateStats, 60000);

    // Initial updates
    updateLiveTrafficFeed();
    updateStats();
    loadRealTimeData();
}

// ===== REAL-TIME WEBSOCKET INTEGRATION =====
async function initializeRealTimeConnection() {
    try {
        console.log('🚀 Initializing real-time connection...');

        // Initialize real-time client
        const connected = await RealTimeTraffic.init();

        if (connected) {
            console.log('✅ Real-time connection established');

            // Subscribe to real-time updates
            RealTimeTraffic.subscribeToAll({
                traffic: handleRealTimeTrafficUpdate,
                alerts: handleRealTimeAlert,
                analytics: handleRealTimeAnalytics,
                connectionStatus: handleConnectionStatus
            });

            // Show real-time indicator
            showRealTimeStatus(true);

        } else {
            console.warn('⚠️ Real-time connection failed, using polling fallback');
            showRealTimeStatus(false);
        }

        // Initialize Google Cloud integration
        await initializeGoogleCloudIntegration();

    } catch (error) {
        console.error('❌ Error initializing real-time connection:', error);
        showRealTimeStatus(false);
    }
}

// ===== GOOGLE CLOUD INTEGRATION =====
async function initializeGoogleCloudIntegration() {
    try {
        console.log('🌐 Initializing Google Cloud integration...');

        // Initialize Google Cloud services
        const initialized = await GoogleCloud.init();

        if (initialized) {
            console.log('✅ Google Cloud integration initialized');

            // Get initial analytics data
            await GoogleCloud.getAnalytics();

            // Show Google Cloud status
            showGoogleCloudStatus(true);

        } else {
            console.warn('⚠️ Google Cloud integration running in demo mode');
            showGoogleCloudStatus(false);
        }

    } catch (error) {
        console.error('❌ Error initializing Google Cloud integration:', error);
        showGoogleCloudStatus(false);
    }
}

// ===== REAL-TIME EVENT HANDLERS =====
function handleRealTimeTrafficUpdate(data) {
    console.log('📊 Real-time traffic update:', data);

    if (data.type === 'TRAFFIC_UPDATE' && data.data) {
        // Update live traffic feed with new data
        updateLiveTrafficWithRealData(data.data);

        // Update charts if needed
        updateTrafficCharts();

        // Show notification for significant changes
        if (data.data.trafficDensity === 'HIGH') {
            showTrafficNotification(data.data);
        }
    } else if (data.type === 'TRAFFIC_SNAPSHOT' && data.locations) {
        // Handle initial snapshot
        updateLiveTrafficFeed(data.locations);
    }
}

function handleRealTimeAlert(data) {
    console.log('🚨 Real-time alert:', data);

    if (data.type === 'ALERT' && data.alert) {
        // Show alert notification
        showAlertNotification(data.alert);

        // Update alerts section
        updateAlertsSection(data.alert);
    }
}

function handleRealTimeAnalytics(data) {
    console.log('📈 Real-time analytics:', data);

    if (data.type === 'ANALYTICS_UPDATE' && data.data) {
        // Update dashboard stats with real-time data
        updateStatsWithRealTimeData(data.data);
    }
}

function handleConnectionStatus(status) {
    console.log('🔌 Connection status:', status);
    showRealTimeStatus(status.connected);

    if (!status.connected && status.maxAttemptsReached) {
        showError('Real-time connection lost. Using cached data.');
    }
}

// ===== REAL API INTEGRATION =====
async function loadRealTimeData() {
    try {
        console.log('Loading real-time data from backend...');

        // Try to get real traffic data from backend
        if (TrafficAuth.isAuthenticated()) {
            await loadTrafficStats();
            await loadCurrentTrafficData();
        }
    } catch (error) {
        console.warn('Failed to load real data, using simulated data:', error);
        // Fallback to simulated data
        updateStats();
        updateLiveTrafficFeed();
    }
}

async function loadTrafficStats() {
    try {
        const response = await fetch(`${TrafficAuth.API_BASE_URL}/traffic/stats`, {
            headers: {
                'Authorization': `Bearer ${TrafficAuth.getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const stats = await response.json();
            updateStatsFromAPI(stats);
            console.log('Traffic stats loaded from API');
        } else {
            throw new Error(`API returned ${response.status}`);
        }
    } catch (error) {
        console.warn('Failed to load traffic stats from API:', error);
        // Use simulated data as fallback
        updateStats();
    }
}

async function loadCurrentTrafficData() {
    try {
        const response = await fetch(`${TrafficAuth.API_BASE_URL}/traffic/current`, {
            headers: {
                'Authorization': `Bearer ${TrafficAuth.getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const trafficData = await response.json();
            updateTrafficFeedFromAPI(trafficData);
            console.log('Current traffic data loaded from API');
        } else {
            throw new Error(`API returned ${response.status}`);
        }
    } catch (error) {
        console.warn('Failed to load current traffic data from API:', error);
        // Use simulated data as fallback
        updateLiveTrafficFeed();
    }
}

function updateLiveTrafficFeed() {
    const feedContainer = document.getElementById('liveTrafficFeed');
    if (!feedContainer) return;
    
    // Simulate new traffic data
    const locations = [
        'Main St & 1st Ave',
        'Broadway & 5th St',
        'Park Ave & Central',
        'Tech Blvd & Innovation Dr',
        'Commerce St & Market Ave',
        'University Ave & College St'
    ];
    
    const statuses = ['high', 'medium', 'low'];
    const statusLabels = ['High Density', 'Medium Density', 'Low Density'];
    
    let html = '';
    
    for (let i = 0; i < 3; i++) {
        const location = locations[Math.floor(Math.random() * locations.length)];
        const statusIndex = Math.floor(Math.random() * statuses.length);
        const status = statuses[statusIndex];
        const statusLabel = statusLabels[statusIndex];
        const timeAgo = Math.floor(Math.random() * 10) + 1;
        
        html += `
            <div class="traffic-item">
                <div class="traffic-location">${location}</div>
                <div class="traffic-status ${status}">${statusLabel}</div>
                <div class="traffic-time">${timeAgo} min ago</div>
            </div>
        `;
    }
    
    feedContainer.innerHTML = html;
}

function updateStats() {
    // Simulate stat updates (fallback when API is not available)
    const stats = {
        totalIntersections: 156 + Math.floor(Math.random() * 10) - 5,
        congestionAlerts: 23 + Math.floor(Math.random() * 10) - 5,
        avgTravelTime: (18.5 + (Math.random() * 4) - 2).toFixed(1),
        vehicleCount: (12.4 + (Math.random() * 2) - 1).toFixed(1) + 'K'
    };

    Object.keys(stats).forEach(key => {
        const element = document.getElementById(key);
        if (element) {
            element.textContent = stats[key];
        }
    });

    // Show that we're using simulated data
    showAPIConnectionStatus(false);
}

// ===== API INTEGRATION FUNCTIONS =====
function updateStatsFromAPI(apiStats) {
    console.log('Updating stats from API:', apiStats);

    // Map API data to dashboard stats
    const stats = {
        totalIntersections: apiStats.totalIntersections || 156,
        congestionAlerts: apiStats.congestionAlerts || 23,
        avgTravelTime: (apiStats.avgTravelTime || 18.5) + ' min',
        vehicleCount: (apiStats.vehicleCount || 12.4) + 'K'
    };

    Object.keys(stats).forEach(key => {
        const element = document.getElementById(key);
        if (element) {
            element.textContent = stats[key];
        }
    });

    // Show that we're using real API data
    showAPIConnectionStatus(true);
}

function showAPIConnectionStatus(connected) {
    // Add visual indicator for API connection
    let indicator = document.getElementById('apiStatus');

    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'apiStatus';
        indicator.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 8px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
            z-index: 1000;
            transition: all 0.3s ease;
        `;
        document.body.appendChild(indicator);
    }

    if (connected) {
        indicator.className = 'api-status connected';
        indicator.innerHTML = '<i class="fas fa-wifi"></i> Live Data';
        indicator.style.backgroundColor = '#10b981';
        indicator.style.color = 'white';
        indicator.title = 'Connected to backend API - showing real data';
    } else {
        indicator.className = 'api-status disconnected';
        indicator.innerHTML = '<i class="fas fa-wifi-slash"></i> Simulated';
        indicator.style.backgroundColor = '#f59e0b';
        indicator.style.color = 'white';
        indicator.title = 'Using simulated data - backend API not available';
    }
}

// ===== SEARCH FUNCTIONALITY =====
function handleSearch(event) {
    const query = event.target.value.toLowerCase();
    
    if (query.length < 2) {
        return;
    }
    
    // Simulate search results
    console.log('Searching for:', query);
    
    // In a real application, this would make an API call
    // For demo purposes, we'll just show a notification
    if (query.length >= 3) {
        showSearchResults(query);
    }
}

function showSearchResults(query) {
    // This would typically show search results in a dropdown
    // For now, we'll just log it
    console.log(`Search results for: ${query}`);
}

// ===== QUICK ACTIONS =====
function optimizeSignals() {
    // Show loading state
    const actionBtn = event.target.closest('.action-btn');
    const originalContent = actionBtn.innerHTML;
    
    actionBtn.innerHTML = '<div class="spinner spinner-sm"></div><span>Optimizing...</span>';
    actionBtn.style.pointerEvents = 'none';
    
    // Simulate API call
    setTimeout(() => {
        actionBtn.innerHTML = '<i class="fas fa-check"></i><span>Optimized!</span>';
        actionBtn.style.backgroundColor = 'var(--success-color)';
        actionBtn.style.color = 'white';
        
        // Reset after 3 seconds
        setTimeout(() => {
            actionBtn.innerHTML = originalContent;
            actionBtn.style.backgroundColor = '';
            actionBtn.style.color = '';
            actionBtn.style.pointerEvents = '';
        }, 3000);
        
        // Show notification
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Traffic signals optimized successfully!', 'success');
        }
    }, 2000);
}

// ===== UTILITY FUNCTIONS =====
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// ===== CLEANUP =====
window.addEventListener('beforeunload', function() {
    if (liveDataInterval) {
        clearInterval(liveDataInterval);
    }
    
    if (statsUpdateInterval) {
        clearInterval(statsUpdateInterval);
    }
});

// ===== RESPONSIVE HANDLING =====
window.addEventListener('resize', function() {
    if (window.innerWidth > 768) {
        const sidebar = document.getElementById('sidebar');
        sidebar.classList.remove('mobile-open');
    }
});

// ===== REAL-TIME HELPER FUNCTIONS =====
function updateLiveTrafficWithRealData(trafficData) {
    const feedContainer = document.getElementById('liveTrafficFeed');
    if (!feedContainer) return;

    // Create or update traffic item
    const existingItem = feedContainer.querySelector(`[data-location="${trafficData.location}"]`);
    const trafficItem = createTrafficFeedItem(trafficData);

    if (existingItem) {
        existingItem.replaceWith(trafficItem);
    } else {
        feedContainer.insertBefore(trafficItem, feedContainer.firstChild);
    }

    // Limit to 10 items
    const items = feedContainer.querySelectorAll('.traffic-feed-item');
    if (items.length > 10) {
        items[items.length - 1].remove();
    }
}

function createTrafficFeedItem(trafficData) {
    const item = document.createElement('div');
    item.className = 'traffic-item traffic-feed-item';
    item.setAttribute('data-location', trafficData.location);

    const densityClass = trafficData.trafficDensity.toLowerCase();
    const timeAgo = Math.floor((Date.now() - new Date(trafficData.timestamp).getTime()) / 60000);

    item.innerHTML = `
        <div class="traffic-location">${trafficData.location}</div>
        <div class="traffic-status ${densityClass}">${trafficData.trafficDensity} Density</div>
        <div class="traffic-time">${timeAgo} min ago</div>
    `;

    return item;
}

function updateStatsWithRealTimeData(analyticsData) {
    // Update total intersections
    if (analyticsData.totalLocations !== undefined) {
        updateStatCard('totalIntersections', analyticsData.totalLocations);
    }

    // Update active alerts
    if (analyticsData.activeAlerts !== undefined) {
        updateStatCard('congestionAlerts', analyticsData.activeAlerts);
    }

    // Update average speed
    if (analyticsData.averageSpeed !== undefined) {
        updateStatCard('avgTravelTime', analyticsData.averageSpeed + ' km/h');
    }

    // Update total vehicles
    if (analyticsData.totalVehicles !== undefined) {
        const vehicleCount = (analyticsData.totalVehicles / 1000).toFixed(1) + 'K';
        updateStatCard('vehicleCount', vehicleCount);
    }
}

function updateStatCard(cardId, value) {
    const card = document.getElementById(cardId);
    if (card) {
        card.textContent = value;

        // Add update animation
        card.classList.add('stat-updated');
        setTimeout(() => card.classList.remove('stat-updated'), 1000);
    }
}

function showTrafficNotification(trafficData) {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(
            `High traffic detected at ${trafficData.location}`,
            'warning'
        );
    }
}

function showAlertNotification(alert) {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(alert.message, alert.severity.toLowerCase());
    }
}

function showRealTimeStatus(connected) {
    let statusIndicator = document.getElementById('realTimeStatus');
    if (!statusIndicator) {
        createRealTimeStatusIndicator();
        statusIndicator = document.getElementById('realTimeStatus');
    }

    if (connected) {
        statusIndicator.className = 'real-time-status connected';
        statusIndicator.innerHTML = '<i class="fas fa-circle"></i> Live';
        statusIndicator.style.color = '#10b981';
    } else {
        statusIndicator.className = 'real-time-status disconnected';
        statusIndicator.innerHTML = '<i class="fas fa-circle"></i> Offline';
        statusIndicator.style.color = '#ef4444';
    }
}

function showGoogleCloudStatus(connected) {
    let statusIndicator = document.getElementById('googleCloudStatus');
    if (!statusIndicator) {
        createGoogleCloudStatusIndicator();
        statusIndicator = document.getElementById('googleCloudStatus');
    }

    const status = GoogleCloud.getStatus();
    const connectedServices = Object.values(status.services).filter(Boolean).length;
    const totalServices = Object.keys(status.services).length;

    if (connected && connectedServices > 0) {
        statusIndicator.className = 'google-cloud-status connected';
        statusIndicator.innerHTML = `
            <i class="fab fa-google"></i>
            Google Cloud (${connectedServices}/${totalServices})
        `;
        statusIndicator.style.color = '#10b981';
    } else {
        statusIndicator.className = 'google-cloud-status disconnected';
        statusIndicator.innerHTML = '<i class="fab fa-google"></i> Demo Mode';
        statusIndicator.style.color = '#f59e0b';
    }
}

function createRealTimeStatusIndicator() {
    const navbar = document.querySelector('.navbar .nav-actions');
    if (!navbar) return;

    const statusIndicator = document.createElement('div');
    statusIndicator.id = 'realTimeStatus';
    statusIndicator.className = 'real-time-status';
    statusIndicator.style.cssText = `
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.875rem;
        font-weight: 500;
        margin-right: 1rem;
    `;
    statusIndicator.innerHTML = '<i class="fas fa-circle"></i> Connecting...';

    navbar.insertBefore(statusIndicator, navbar.firstChild);
}

function createGoogleCloudStatusIndicator() {
    const navbar = document.querySelector('.navbar .nav-actions');
    if (!navbar) return;

    const statusIndicator = document.createElement('div');
    statusIndicator.id = 'googleCloudStatus';
    statusIndicator.className = 'google-cloud-status';
    statusIndicator.style.cssText = `
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.875rem;
        font-weight: 500;
        margin-right: 1rem;
        padding: 0.5rem 1rem;
        border-radius: 20px;
        background: var(--bg-secondary);
        border: 1px solid var(--border-color);
    `;
    statusIndicator.innerHTML = '<i class="fab fa-google"></i> Initializing...';

    navbar.insertBefore(statusIndicator, navbar.firstChild);
}

function updateTrafficCharts() {
    // Update charts with real-time data if needed
    if (trafficFlowChart) {
        // Add new data point to the chart
        const now = new Date();
        const timeLabel = now.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });

        // Add new label and remove oldest
        trafficFlowChart.data.labels.push(timeLabel);
        if (trafficFlowChart.data.labels.length > 24) {
            trafficFlowChart.data.labels.shift();
        }

        // Add new data points
        trafficFlowChart.data.datasets.forEach(dataset => {
            const newValue = dataset.data[dataset.data.length - 1] + (Math.random() * 20 - 10);
            dataset.data.push(Math.max(0, newValue));
            if (dataset.data.length > 24) {
                dataset.data.shift();
            }
        });

        trafficFlowChart.update('none');
    }
}

// ===== EXPORT DASHBOARD FUNCTIONS =====
window.TrafficDashboard = {
    optimizeSignals,
    updateTrafficFlowChart,
    updateSignalPerformanceChart,
    updateLiveTrafficFeed,
    updateStats,
    initializeRealTime: initializeRealTimeConnection,
    cleanup: () => {
        if (liveDataInterval) clearInterval(liveDataInterval);
        if (statsUpdateInterval) clearInterval(statsUpdateInterval);
        if (window.RealTimeTraffic) {
            RealTimeTraffic.cleanup();
        }
    }
};
