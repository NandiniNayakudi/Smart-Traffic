// ===== MONITORING JAVASCRIPT =====

// Global variables
let volumeTrendsChart, speedDistributionChart;
let liveDataUpdateInterval;
let isPaused = false;

// Initialize monitoring when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeMonitoring();
    initializeCharts();
    initializeEventListeners();
    startLiveUpdates();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeMonitoring() {
    // Initialize sidebar functionality (reuse from dashboard)
    if (window.TrafficDashboard) {
        // Reuse sidebar functionality
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
    }
    
    // Initialize intersection click handlers
    const intersections = document.querySelectorAll('.intersection');
    intersections.forEach(intersection => {
        intersection.addEventListener('click', function() {
            showIntersectionDetails(this.dataset.id);
        });
    });
}

function initializeEventListeners() {
    // Filter controls
    const applyFiltersBtn = document.getElementById('applyFilters');
    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener('click', applyFilters);
    }
    
    // Refresh data button
    const refreshDataBtn = document.getElementById('refreshData');
    if (refreshDataBtn) {
        refreshDataBtn.addEventListener('click', refreshAllData);
    }
    
    // Export data button
    const exportDataBtn = document.getElementById('exportData');
    if (exportDataBtn) {
        exportDataBtn.addEventListener('click', exportData);
    }
    
    // Pause live data button
    const pauseLiveDataBtn = document.getElementById('pauseLiveData');
    if (pauseLiveDataBtn) {
        pauseLiveDataBtn.addEventListener('click', toggleLiveData);
    }
    
    // Map control buttons
    const mapControlBtns = document.querySelectorAll('.map-control-btn');
    mapControlBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            switchMapLayer(this.dataset.layer);
        });
    });
    
    // Chart controls
    const volumeChartPeriod = document.getElementById('volumeChartPeriod');
    if (volumeChartPeriod) {
        volumeChartPeriod.addEventListener('change', updateVolumeChart);
    }
    
    const refreshSpeedChart = document.getElementById('refreshSpeedChart');
    if (refreshSpeedChart) {
        refreshSpeedChart.addEventListener('click', updateSpeedChart);
    }
    
    // Modal controls
    const closeIntersectionModal = document.getElementById('closeIntersectionModal');
    if (closeIntersectionModal) {
        closeIntersectionModal.addEventListener('click', closeModal);
    }
    
    const closeModal = document.getElementById('closeModal');
    if (closeModal) {
        closeModal.addEventListener('click', closeModal);
    }
    
    const optimizeIntersection = document.getElementById('optimizeIntersection');
    if (optimizeIntersection) {
        optimizeIntersection.addEventListener('click', optimizeSelectedIntersection);
    }
}

// ===== CHARTS INITIALIZATION =====
function initializeCharts() {
    initializeVolumeChart();
    initializeSpeedChart();
}

function initializeVolumeChart() {
    const ctx = document.getElementById('volumeTrendsChart');
    if (!ctx) return;
    
    volumeTrendsChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: generateTimeLabels(24),
            datasets: [{
                label: 'Traffic Volume',
                data: generateVolumeData(24),
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
                    position: 'top',
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Vehicle Count'
                    }
                }
            }
        }
    });
}

function initializeSpeedChart() {
    const ctx = document.getElementById('speedDistributionChart');
    if (!ctx) return;
    
    speedDistributionChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['0-20 mph', '21-35 mph', '36-50 mph', '51-65 mph', '65+ mph'],
            datasets: [{
                label: 'Vehicle Count',
                data: [15, 45, 65, 35, 10],
                backgroundColor: [
                    '#ef4444',
                    '#f59e0b',
                    '#10b981',
                    '#3b82f6',
                    '#8b5cf6'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Number of Vehicles'
                    }
                }
            }
        }
    });
}

// ===== DATA GENERATION =====
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
    const baseVolume = 50;
    
    for (let i = 0; i < points; i++) {
        const hour = (new Date().getHours() - points + i + 24) % 24;
        let multiplier = 1;
        
        if (hour >= 7 && hour <= 9) multiplier = 2.2; // Morning rush
        else if (hour >= 17 && hour <= 19) multiplier = 2.5; // Evening rush
        else if (hour >= 22 || hour <= 6) multiplier = 0.3; // Night time
        
        const randomVariation = 0.8 + Math.random() * 0.4;
        data.push(Math.round(baseVolume * multiplier * randomVariation));
    }
    
    return data;
}

// ===== LIVE DATA UPDATES =====
function startLiveUpdates() {
    updateLiveDataTable();
    updateAlertsTable();
    
    // Update every 30 seconds
    liveDataUpdateInterval = setInterval(() => {
        if (!isPaused) {
            updateLiveDataTable();
            updateAlertsTable();
            updateIntersectionStatus();
        }
    }, 30000);
}

function updateLiveDataTable() {
    const tableBody = document.getElementById('liveDataTableBody');
    if (!tableBody) return;
    
    const locations = [
        'Main St & 1st Ave',
        'Broadway & 5th St',
        'Park Ave & Central',
        'Tech Blvd & Innovation Dr',
        'Commerce St & Market Ave',
        'University Ave & College St'
    ];
    
    const densities = ['LOW', 'MEDIUM', 'HIGH'];
    const statuses = ['optimal', 'warning', 'critical'];
    
    let html = '';
    
    for (let i = 0; i < 6; i++) {
        const location = locations[i];
        const density = densities[Math.floor(Math.random() * densities.length)];
        const vehicleCount = Math.floor(Math.random() * 100) + 20;
        const avgSpeed = (Math.random() * 40 + 15).toFixed(1);
        const lastUpdated = Math.floor(Math.random() * 5) + 1;
        const status = statuses[Math.floor(Math.random() * statuses.length)];
        
        html += `
            <tr>
                <td>${location}</td>
                <td><span class="status-badge ${density.toLowerCase()}">${density}</span></td>
                <td>${vehicleCount}</td>
                <td>${avgSpeed} mph</td>
                <td>${lastUpdated} min ago</td>
                <td><span class="status-badge ${status}">${status.toUpperCase()}</span></td>
            </tr>
        `;
    }
    
    tableBody.innerHTML = html;
}

function updateAlertsTable() {
    const tableBody = document.getElementById('alertsTableBody');
    if (!tableBody) return;
    
    const alertTypes = ['Congestion', 'Signal Fault', 'Incident', 'Maintenance'];
    const locations = ['Downtown', 'Tech District', 'Business Center', 'Residential'];
    const severities = ['Low', 'Medium', 'High', 'Critical'];
    
    let html = '';
    const alertCount = Math.floor(Math.random() * 5) + 3;
    
    for (let i = 0; i < alertCount; i++) {
        const type = alertTypes[Math.floor(Math.random() * alertTypes.length)];
        const location = locations[Math.floor(Math.random() * locations.length)];
        const severity = severities[Math.floor(Math.random() * severities.length)];
        const time = Math.floor(Math.random() * 60) + 1;
        
        html += `
            <tr>
                <td>${type}</td>
                <td>${location}</td>
                <td><span class="status-badge ${severity.toLowerCase()}">${severity}</span></td>
                <td>${time} min ago</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="resolveAlert(${i})">
                        Resolve
                    </button>
                </td>
            </tr>
        `;
    }
    
    tableBody.innerHTML = html;
    
    // Update alert count
    const alertCountElement = document.getElementById('alertCount');
    if (alertCountElement) {
        alertCountElement.textContent = alertCount;
    }
}

// ===== FILTER FUNCTIONALITY =====
function applyFilters() {
    const locationFilter = document.getElementById('locationFilter').value;
    const timeRangeFilter = document.getElementById('timeRangeFilter').value;
    const densityFilter = document.getElementById('densityFilter').value;
    
    console.log('Applying filters:', { locationFilter, timeRangeFilter, densityFilter });
    
    // Show loading state
    const btn = document.getElementById('applyFilters');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Applying...';
    btn.disabled = true;
    
    // Simulate API call
    setTimeout(() => {
        // Update data based on filters
        updateLiveDataTable();
        updateAlertsTable();
        updateVolumeChart();
        
        // Reset button
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        // Show success message
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Filters applied successfully!', 'success');
        }
    }, 1500);
}

// ===== CHART UPDATES =====
function updateVolumeChart() {
    const period = document.getElementById('volumeChartPeriod').value;
    let hours;
    
    switch (period) {
        case 'hour': hours = 1; break;
        case 'week': hours = 7 * 24; break;
        default: hours = 24;
    }
    
    if (volumeTrendsChart) {
        volumeTrendsChart.data.labels = generateTimeLabels(Math.min(hours, 24));
        volumeTrendsChart.data.datasets[0].data = generateVolumeData(Math.min(hours, 24));
        volumeTrendsChart.update();
    }
}

function updateSpeedChart() {
    if (speedDistributionChart) {
        const newData = [
            Math.floor(Math.random() * 30) + 10,
            Math.floor(Math.random() * 50) + 30,
            Math.floor(Math.random() * 70) + 40,
            Math.floor(Math.random() * 40) + 20,
            Math.floor(Math.random() * 20) + 5
        ];
        
        speedDistributionChart.data.datasets[0].data = newData;
        speedDistributionChart.update();
        
        // Show refresh feedback
        const refreshBtn = document.getElementById('refreshSpeedChart');
        const originalText = refreshBtn.innerHTML;
        refreshBtn.innerHTML = '<i class="fas fa-check"></i>';
        refreshBtn.classList.add('btn-success');
        
        setTimeout(() => {
            refreshBtn.innerHTML = originalText;
            refreshBtn.classList.remove('btn-success');
        }, 2000);
    }
}

// ===== UTILITY FUNCTIONS =====
function refreshAllData() {
    const btn = document.getElementById('refreshData');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Refreshing...';
    btn.disabled = true;
    
    // Refresh all data
    setTimeout(() => {
        updateLiveDataTable();
        updateAlertsTable();
        updateVolumeChart();
        updateSpeedChart();
        updateIntersectionStatus();
        
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Data refreshed successfully!', 'success');
        }
    }, 2000);
}

function toggleLiveData() {
    isPaused = !isPaused;
    const btn = document.getElementById('pauseLiveData');
    
    if (isPaused) {
        btn.innerHTML = '<i class="fas fa-play"></i> Resume';
        btn.classList.add('btn-warning');
    } else {
        btn.innerHTML = '<i class="fas fa-pause"></i> Pause';
        btn.classList.remove('btn-warning');
    }
}

function exportData() {
    const btn = document.getElementById('exportData');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Exporting...';
    btn.disabled = true;
    
    // Simulate export
    setTimeout(() => {
        // Create mock CSV data
        const csvData = "Location,Density,Vehicle Count,Avg Speed,Status\n" +
                       "Main St & 1st Ave,HIGH,45,25.5,OPTIMAL\n" +
                       "Broadway & 5th St,MEDIUM,32,35.2,WARNING\n";
        
        // Create download link
        const blob = new Blob([csvData], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'traffic_data_export.csv';
        a.click();
        window.URL.revokeObjectURL(url);
        
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Data exported successfully!', 'success');
        }
    }, 1500);
}

// Reuse sidebar functions from dashboard
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
    if (liveDataUpdateInterval) {
        clearInterval(liveDataUpdateInterval);
    }
});

// ===== EXPORT MONITORING FUNCTIONS =====
window.TrafficMonitoring = {
    refreshAllData,
    toggleLiveData,
    exportData,
    applyFilters,
    updateVolumeChart,
    updateSpeedChart
};
