// ===== ANALYTICS JAVASCRIPT =====

// Global variables
let trafficFlowAnalysisChart, peakHoursChart, routeEfficiencyChart, incidentImpactChart;
let currentTimeRange = '7d';
let currentChartView = 'hourly';

// Initialize analytics when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeAnalytics();
    initializeCharts();
    initializeEventListeners();
    loadMetrics();
    loadInsights();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeAnalytics() {
    // Initialize sidebar functionality (reuse from dashboard)
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

function initializeEventListeners() {
    // Time range selector
    const timeRangeSelect = document.getElementById('timeRangeSelect');
    if (timeRangeSelect) {
        timeRangeSelect.addEventListener('change', function() {
            currentTimeRange = this.value;
            updateAllCharts();
            loadMetrics();
        });
    }
    
    // Generate report button
    const generateReportBtn = document.getElementById('generateReport');
    if (generateReportBtn) {
        generateReportBtn.addEventListener('click', generateReport);
    }
    
    // Chart tabs
    const chartTabs = document.querySelectorAll('.chart-tab');
    chartTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            switchChartView(this.dataset.chart);
        });
    });
    
    // Refresh buttons
    const refreshPeakHours = document.getElementById('refreshPeakHours');
    if (refreshPeakHours) {
        refreshPeakHours.addEventListener('click', updatePeakHoursChart);
    }
    
    const refreshInsights = document.getElementById('refreshInsights');
    if (refreshInsights) {
        refreshInsights.addEventListener('click', loadInsights);
    }
    
    // Route selector
    const routeSelect = document.getElementById('routeSelect');
    if (routeSelect) {
        routeSelect.addEventListener('change', updateRouteEfficiencyChart);
    }
    
    // Create custom report button
    const createCustomReport = document.getElementById('createCustomReport');
    if (createCustomReport) {
        createCustomReport.addEventListener('click', showCustomReportModal);
    }
    
    // Insight action buttons
    document.addEventListener('click', function(e) {
        if (e.target.matches('.insight-actions .btn')) {
            handleInsightAction(e.target);
        }
    });
}

// ===== CHARTS INITIALIZATION =====
function initializeCharts() {
    initializeTrafficFlowAnalysisChart();
    initializePeakHoursChart();
    initializeRouteEfficiencyChart();
    initializeIncidentImpactChart();
}

function initializeTrafficFlowAnalysisChart() {
    const ctx = document.getElementById('trafficFlowAnalysisChart');
    if (!ctx) return;
    
    trafficFlowAnalysisChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: generateAnalyticsLabels('hourly'),
            datasets: [{
                label: 'Traffic Volume',
                data: generateAnalyticsData('volume', 'hourly'),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }, {
                label: 'Average Speed',
                data: generateAnalyticsData('speed', 'hourly'),
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                borderWidth: 2,
                fill: false,
                tension: 0.4,
                yAxisID: 'y1'
            }, {
                label: 'Efficiency Score',
                data: generateAnalyticsData('efficiency', 'hourly'),
                borderColor: '#f59e0b',
                backgroundColor: 'rgba(245, 158, 11, 0.1)',
                borderWidth: 2,
                fill: false,
                tension: 0.4,
                yAxisID: 'y2'
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
                        text: 'Volume / Speed'
                    }
                },
                y1: {
                    type: 'linear',
                    display: false,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false,
                    },
                },
                y2: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'Efficiency %'
                    },
                    grid: {
                        drawOnChartArea: false,
                    },
                }
            }
        }
    });
}

function initializePeakHoursChart() {
    const ctx = document.getElementById('peakHoursChart');
    if (!ctx) return;
    
    peakHoursChart = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['6 AM', '9 AM', '12 PM', '3 PM', '6 PM', '9 PM', '12 AM', '3 AM'],
            datasets: [{
                label: 'Traffic Intensity',
                data: [65, 95, 70, 75, 98, 60, 25, 15],
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.2)',
                borderWidth: 2,
                pointBackgroundColor: '#3b82f6',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2
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
                r: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        stepSize: 20
                    }
                }
            }
        }
    });
}

function initializeRouteEfficiencyChart() {
    const ctx = document.getElementById('routeEfficiencyChart');
    if (!ctx) return;
    
    routeEfficiencyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Main St', 'Broadway', 'Park Ave', 'Tech Blvd', 'Commerce St'],
            datasets: [{
                label: 'Efficiency %',
                data: [85, 92, 78, 96, 88],
                backgroundColor: [
                    '#10b981',
                    '#3b82f6',
                    '#f59e0b',
                    '#8b5cf6',
                    '#06b6d4'
                ],
                borderWidth: 0,
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
                        text: 'Efficiency %'
                    }
                }
            }
        }
    });
}

function initializeIncidentImpactChart() {
    const ctx = document.getElementById('incidentImpactChart');
    if (!ctx) return;
    
    incidentImpactChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Minor Delays', 'Moderate Impact', 'Major Disruption', 'No Impact'],
            datasets: [{
                data: [45, 25, 15, 15],
                backgroundColor: [
                    '#10b981',
                    '#f59e0b',
                    '#ef4444',
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
                }
            }
        }
    });
}

// ===== DATA GENERATION =====
function generateAnalyticsLabels(period) {
    const labels = [];
    const now = new Date();
    
    switch (period) {
        case 'hourly':
            for (let i = 23; i >= 0; i--) {
                const time = new Date(now.getTime() - (i * 60 * 60 * 1000));
                labels.push(time.toLocaleTimeString('en-US', { hour: '2-digit', hour12: false }));
            }
            break;
        case 'daily':
            for (let i = 6; i >= 0; i--) {
                const date = new Date(now.getTime() - (i * 24 * 60 * 60 * 1000));
                labels.push(date.toLocaleDateString('en-US', { weekday: 'short' }));
            }
            break;
        case 'weekly':
            for (let i = 11; i >= 0; i--) {
                const date = new Date(now.getTime() - (i * 7 * 24 * 60 * 60 * 1000));
                labels.push(`Week ${Math.ceil((now.getTime() - date.getTime()) / (7 * 24 * 60 * 60 * 1000))}`);
            }
            break;
    }
    
    return labels;
}

function generateAnalyticsData(type, period) {
    const data = [];
    const points = period === 'hourly' ? 24 : period === 'daily' ? 7 : 12;
    
    for (let i = 0; i < points; i++) {
        let value;
        const randomFactor = 0.8 + Math.random() * 0.4;
        
        switch (type) {
            case 'volume':
                value = Math.round((50 + Math.random() * 100) * randomFactor);
                break;
            case 'speed':
                value = Math.round((25 + Math.random() * 30) * randomFactor);
                break;
            case 'efficiency':
                value = Math.round((70 + Math.random() * 25) * randomFactor);
                break;
            default:
                value = Math.round(Math.random() * 100);
        }
        
        data.push(value);
    }
    
    return data;
}

// ===== CHART UPDATES =====
function updateAllCharts() {
    updateTrafficFlowAnalysisChart();
    updatePeakHoursChart();
    updateRouteEfficiencyChart();
    updateIncidentImpactChart();
}

function updateTrafficFlowAnalysisChart() {
    if (trafficFlowAnalysisChart) {
        trafficFlowAnalysisChart.data.labels = generateAnalyticsLabels(currentChartView);
        trafficFlowAnalysisChart.data.datasets[0].data = generateAnalyticsData('volume', currentChartView);
        trafficFlowAnalysisChart.data.datasets[1].data = generateAnalyticsData('speed', currentChartView);
        trafficFlowAnalysisChart.data.datasets[2].data = generateAnalyticsData('efficiency', currentChartView);
        trafficFlowAnalysisChart.update();
    }
}

function updatePeakHoursChart() {
    if (peakHoursChart) {
        const newData = Array.from({length: 8}, () => Math.floor(Math.random() * 100));
        peakHoursChart.data.datasets[0].data = newData;
        peakHoursChart.update();
        
        // Show refresh feedback
        const refreshBtn = document.getElementById('refreshPeakHours');
        if (refreshBtn) {
            const originalText = refreshBtn.innerHTML;
            refreshBtn.innerHTML = '<i class="fas fa-check"></i>';
            refreshBtn.classList.add('btn-success');
            
            setTimeout(() => {
                refreshBtn.innerHTML = originalText;
                refreshBtn.classList.remove('btn-success');
            }, 2000);
        }
    }
}

function updateRouteEfficiencyChart() {
    if (routeEfficiencyChart) {
        const newData = Array.from({length: 5}, () => Math.floor(Math.random() * 30) + 70);
        routeEfficiencyChart.data.datasets[0].data = newData;
        routeEfficiencyChart.update();
    }
}

function updateIncidentImpactChart() {
    if (incidentImpactChart) {
        const total = 100;
        const values = [
            Math.floor(Math.random() * 30) + 30,
            Math.floor(Math.random() * 20) + 15,
            Math.floor(Math.random() * 15) + 5,
        ];
        values.push(total - values.reduce((a, b) => a + b, 0));
        
        incidentImpactChart.data.datasets[0].data = values;
        incidentImpactChart.update();
    }
}

function switchChartView(view) {
    currentChartView = view;
    
    // Update active tab
    document.querySelectorAll('.chart-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelector(`[data-chart="${view}"]`).classList.add('active');
    
    // Update chart
    updateTrafficFlowAnalysisChart();
}

// ===== METRICS LOADING =====
function loadMetrics() {
    const metrics = {
        trafficEfficiency: (90 + Math.random() * 10).toFixed(1) + '%',
        avgWaitTime: (2 + Math.random() * 2).toFixed(1) + ' min',
        fuelSavings: (15 + Math.random() * 10).toFixed(1) + '%',
        signalOptimization: (80 + Math.random() * 15).toFixed(1) + '%'
    };
    
    Object.keys(metrics).forEach(key => {
        const element = document.getElementById(key);
        if (element) {
            element.textContent = metrics[key];
        }
    });
}

// ===== INSIGHTS LOADING =====
function loadInsights() {
    // Simulate loading new insights
    const refreshBtn = document.getElementById('refreshInsights');
    if (refreshBtn) {
        const originalText = refreshBtn.innerHTML;
        refreshBtn.innerHTML = '<div class="spinner spinner-sm"></div> Loading...';
        refreshBtn.disabled = true;
        
        setTimeout(() => {
            refreshBtn.innerHTML = originalText;
            refreshBtn.disabled = false;
            
            if (window.TrafficApp && window.TrafficApp.showNotification) {
                window.TrafficApp.showNotification('Insights refreshed successfully!', 'success');
            }
        }, 2000);
    }
}

// ===== REPORT GENERATION =====
function generateReport() {
    const btn = document.getElementById('generateReport');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Generating...';
    btn.disabled = true;
    
    setTimeout(() => {
        // Simulate report generation
        const reportData = {
            timeRange: currentTimeRange,
            generatedAt: new Date().toISOString(),
            metrics: {
                efficiency: '94.2%',
                waitTime: '2.3 min',
                fuelSavings: '18.7%'
            }
        };
        
        // Create and download report
        const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `traffic_analytics_report_${currentTimeRange}.json`;
        a.click();
        window.URL.revokeObjectURL(url);
        
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Report generated and downloaded!', 'success');
        }
    }, 3000);
}

function showCustomReportModal() {
    // This would open a modal for custom report configuration
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Custom report builder coming soon!', 'info');
    }
}

// ===== INSIGHT ACTIONS =====
function handleInsightAction(button) {
    const action = button.textContent.trim();
    const insightCard = button.closest('.insight-card');
    const insightTitle = insightCard.querySelector('h4').textContent;
    
    button.innerHTML = '<div class="spinner spinner-sm"></div> Processing...';
    button.disabled = true;
    
    setTimeout(() => {
        button.innerHTML = '<i class="fas fa-check"></i> Applied';
        button.classList.add('btn-success');
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification(`${action} applied for: ${insightTitle}`, 'success');
        }
        
        setTimeout(() => {
            button.innerHTML = action;
            button.classList.remove('btn-success');
            button.disabled = false;
        }, 3000);
    }, 2000);
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

// ===== EXPORT ANALYTICS FUNCTIONS =====
window.TrafficAnalytics = {
    generateReport,
    loadMetrics,
    loadInsights,
    updateAllCharts,
    switchChartView
};
