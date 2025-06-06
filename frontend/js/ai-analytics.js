// ===== AI ANALYTICS JAVASCRIPT =====

// Global variables
let volumeChart, hotspotChart, forecastChart, trendsChart;
let modelUpdateInterval, queryUpdateInterval;

// Initialize AI Analytics when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeAIAnalytics();
    initializeCharts();
    initializeEventListeners();
    startRealTimeUpdates();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeAIAnalytics() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const logoutBtn = document.getElementById('logoutBtn');
    
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', toggleSidebar);
    }
    
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', toggleMobileSidebar);
    }
    
    if (logoutBtn) {
        logoutBtn.addEventListener('click', TrafficAuth.logout);
    }
    
    // Initialize cloud status
    updateCloudStatus();
}

function initializeEventListeners() {
    // Refresh queries button
    const refreshBtn = document.getElementById('refreshQueries');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshBigQueryData);
    }
    
    // New query button
    const newQueryBtn = document.getElementById('newQuery');
    if (newQueryBtn) {
        newQueryBtn.addEventListener('click', createNewQuery);
    }
    
    // Theme toggle
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            if (window.TrafficApp && window.TrafficApp.toggleTheme) {
                window.TrafficApp.toggleTheme();
            }
        });
    }
}

// ===== CHARTS INITIALIZATION =====
function initializeCharts() {
    initializeVolumeChart();
    initializeHotspotChart();
    initializeForecastChart();
    initializeTrendsChart();
}

function initializeVolumeChart() {
    const ctx = document.getElementById('volumeChart');
    if (!ctx) return;
    
    volumeChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: generateTimeLabels(12),
            datasets: [{
                label: 'Traffic Volume',
                data: generateVolumeData(12),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Vehicles/Hour'
                    }
                }
            }
        }
    });
}

function initializeHotspotChart() {
    const ctx = document.getElementById('hotspotChart');
    if (!ctx) return;
    
    hotspotChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Main St', 'Broadway', 'Park Ave', 'Tech Blvd', 'Others'],
            datasets: [{
                data: [35, 25, 20, 15, 5],
                backgroundColor: [
                    '#ef4444',
                    '#f59e0b',
                    '#10b981',
                    '#3b82f6',
                    '#6b7280'
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
                    labels: {
                        fontSize: 12
                    }
                }
            }
        }
    });
}

function initializeForecastChart() {
    const ctx = document.getElementById('forecastChart');
    if (!ctx) return;
    
    forecastChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: generateForecastLabels(6),
            datasets: [{
                label: 'Predicted Traffic',
                data: generateForecastData(6),
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                borderDash: [5, 5]
            }, {
                label: 'Current Trend',
                data: generateCurrentTrendData(6),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                borderWidth: 2,
                fill: false,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Congestion Level'
                    }
                }
            }
        }
    });
}

function initializeTrendsChart() {
    const ctx = document.getElementById('trendsChart');
    if (!ctx) return;
    
    trendsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [{
                label: 'Average Congestion',
                data: [75, 80, 85, 82, 90, 45, 35],
                backgroundColor: [
                    '#3b82f6',
                    '#3b82f6',
                    '#f59e0b',
                    '#3b82f6',
                    '#ef4444',
                    '#10b981',
                    '#10b981'
                ],
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    title: {
                        display: true,
                        text: 'Congestion %'
                    }
                }
            }
        }
    });
}

// ===== DATA GENERATORS =====
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

function generateVolumeData(points) {
    const data = [];
    for (let i = 0; i < points; i++) {
        data.push(Math.floor(Math.random() * 500) + 200);
    }
    return data;
}

function generateForecastLabels(hours) {
    const labels = [];
    const now = new Date();
    
    for (let i = 0; i < hours; i++) {
        const time = new Date(now.getTime() + (i * 60 * 60 * 1000));
        labels.push(time.toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
        }));
    }
    
    return labels;
}

function generateForecastData(points) {
    const data = [];
    let baseValue = 50;
    
    for (let i = 0; i < points; i++) {
        baseValue += (Math.random() - 0.5) * 20;
        baseValue = Math.max(10, Math.min(90, baseValue));
        data.push(Math.round(baseValue));
    }
    
    return data;
}

function generateCurrentTrendData(points) {
    const data = [];
    for (let i = 0; i < points; i++) {
        data.push(Math.floor(Math.random() * 40) + 30);
    }
    return data;
}

// ===== REAL-TIME UPDATES =====
function startRealTimeUpdates() {
    // Update model metrics every 30 seconds
    modelUpdateInterval = setInterval(updateModelMetrics, 30000);
    
    // Update BigQuery data every 60 seconds
    queryUpdateInterval = setInterval(updateBigQueryData, 60000);
    
    // Initial updates
    updateModelMetrics();
    updateBigQueryData();
}

function updateModelMetrics() {
    // Update Traffic Prediction Model
    updateMetric('accuracy', (94.2 + (Math.random() - 0.5) * 2).toFixed(1) + '%');
    updateMetric('predictions', Math.floor(1247 + (Math.random() - 0.5) * 200).toLocaleString());
    
    // Update Route Optimization Model
    updateMetric('efficiency', (87.8 + (Math.random() - 0.5) * 3).toFixed(1) + '%');
    updateMetric('routes', Math.floor(342 + (Math.random() - 0.5) * 50));
    updateMetric('co2-saved', (23.4 + (Math.random() - 0.5) * 2).toFixed(1) + ' tons');
    
    // Update Signal Timing Model (if training)
    const progress = Math.min(100, 78 + Math.random() * 5);
    updateMetric('training-progress', Math.floor(progress) + '%');
}

function updateMetric(metricId, value) {
    const elements = document.querySelectorAll(`[data-metric="${metricId}"]`);
    elements.forEach(element => {
        element.textContent = value;
    });
}

function updateBigQueryData() {
    // Simulate BigQuery data updates
    if (volumeChart) {
        volumeChart.data.datasets[0].data = generateVolumeData(12);
        volumeChart.update('none');
    }
    
    // Update query stats
    updateQueryStats();
}

function updateQueryStats() {
    const stats = {
        rowsProcessed: (2.4 + (Math.random() - 0.5) * 0.5).toFixed(1) + 'M',
        queryTime: (1.2 + (Math.random() - 0.5) * 0.3).toFixed(1) + 's'
    };
    
    Object.keys(stats).forEach(key => {
        const element = document.querySelector(`[data-stat="${key}"]`);
        if (element) {
            element.textContent = stats[key];
        }
    });
}

// ===== CLOUD STATUS =====
function updateCloudStatus() {
    const indicator = document.querySelector('.cloud-indicator');
    if (indicator) {
        // Simulate cloud connection status
        const isConnected = Math.random() > 0.1; // 90% uptime
        
        if (isConnected) {
            indicator.classList.add('connected');
            indicator.querySelector('.status-dot').classList.add('active');
        } else {
            indicator.classList.remove('connected');
            indicator.querySelector('.status-dot').classList.remove('active');
        }
    }
}

// ===== EVENT HANDLERS =====
function refreshBigQueryData() {
    const btn = document.getElementById('refreshQueries');
    const originalText = btn.innerHTML;
    
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Refreshing...';
    btn.disabled = true;
    
    setTimeout(() => {
        updateBigQueryData();
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('BigQuery data refreshed successfully', 'success');
        }
    }, 2000);
}

function createNewQuery() {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Query builder coming soon!', 'info');
    }
}

// ===== UTILITY FUNCTIONS =====
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('collapsed');
    localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
}

function toggleMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('mobile-open');
}

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

// ===== CLEANUP =====
window.addEventListener('beforeunload', function() {
    if (modelUpdateInterval) {
        clearInterval(modelUpdateInterval);
    }
    
    if (queryUpdateInterval) {
        clearInterval(queryUpdateInterval);
    }
});

// ===== EXPORT AI ANALYTICS FUNCTIONS =====
window.TrafficAI = {
    updateModelMetrics,
    refreshBigQueryData,
    createNewQuery,
    updateCloudStatus
};
