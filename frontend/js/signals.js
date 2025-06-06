// ===== SIGNAL CONTROL JAVASCRIPT =====

// Global variables
let signalUpdateInterval;
let emergencyModeActive = false;
let signals = {};

// Initialize signals when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeSignals();
    initializeEventListeners();
    startSignalUpdates();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeSignals() {
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
    
    // Initialize signal data
    initializeSignalData();
}

function initializeEventListeners() {
    // Emergency mode button
    const emergencyModeBtn = document.getElementById('emergencyMode');
    if (emergencyModeBtn) {
        emergencyModeBtn.addEventListener('click', toggleEmergencyMode);
    }
    
    // Optimize all button
    const optimizeAllBtn = document.getElementById('optimizeAll');
    if (optimizeAllBtn) {
        optimizeAllBtn.addEventListener('click', optimizeAllSignals);
    }
    
    // Emergency control buttons
    document.addEventListener('click', function(e) {
        if (e.target.matches('.emergency-controls .btn')) {
            handleEmergencyControl(e.target);
        }
        
        if (e.target.matches('.btn-full') && e.target.textContent.includes('Create Pattern')) {
            createTimingPattern();
        }
    });
}

function initializeSignalData() {
    // Initialize signal states
    signals = {
        1: { phase: 'green', countdown: 28, status: 'optimal', location: 'Main St & 1st Ave' },
        2: { phase: 'red', countdown: 45, status: 'warning', location: 'Broadway & 5th St' },
        3: { phase: 'yellow', countdown: 3, status: 'critical', location: 'Tech Blvd & Innovation Dr' },
        4: { phase: 'green', countdown: 35, status: 'optimal', location: 'Park Ave & Central' },
        5: { phase: 'red', countdown: 52, status: 'warning', location: 'Commerce St & Market' },
        6: { phase: 'green', countdown: 22, status: 'optimal', location: 'University Ave & College' }
    };
}

// ===== SIGNAL UPDATES =====
function startSignalUpdates() {
    updateAllSignals();
    
    // Update every second for countdown timers
    signalUpdateInterval = setInterval(() => {
        if (!emergencyModeActive) {
            updateSignalCountdowns();
            updateSignalPhases();
        }
    }, 1000);
}

function updateAllSignals() {
    Object.keys(signals).forEach(signalId => {
        updateSignalDisplay(signalId);
    });
    
    updateOverviewStats();
}

function updateSignalDisplay(signalId) {
    const signal = signals[signalId];
    const signalCard = document.querySelector(`[data-signal-id="${signalId}"]`);
    
    if (!signalCard) return;
    
    // Update traffic light
    const lights = signalCard.querySelectorAll('.light');
    lights.forEach(light => light.classList.remove('active'));
    
    const activeLight = signalCard.querySelector(`.light.${signal.phase}`);
    if (activeLight) {
        activeLight.classList.add('active');
    }
    
    // Update phase and countdown
    const phaseElement = signalCard.querySelector('.phase');
    const countdownElement = signalCard.querySelector('.countdown');
    
    if (phaseElement) {
        phaseElement.textContent = `${signal.phase.charAt(0).toUpperCase() + signal.phase.slice(1)} Phase`;
    }
    
    if (countdownElement) {
        countdownElement.textContent = `${signal.countdown}s`;
    }
    
    // Update status
    const statusIndicator = signalCard.querySelector('.status-indicator');
    const statusText = signalCard.querySelector('.status-text');
    
    if (statusIndicator) {
        statusIndicator.className = `status-indicator ${signal.status}`;
    }
    
    if (statusText) {
        statusText.textContent = signal.status.charAt(0).toUpperCase() + signal.status.slice(1);
        if (signal.status === 'warning') {
            statusText.textContent = 'Needs Attention';
        } else if (signal.status === 'critical') {
            statusText.textContent = 'Critical';
        }
    }
    
    // Update card class
    signalCard.className = `signal-card ${signal.status}`;
}

function updateSignalCountdowns() {
    Object.keys(signals).forEach(signalId => {
        const signal = signals[signalId];
        
        if (signal.countdown > 0) {
            signal.countdown--;
        } else {
            // Phase transition
            cycleSignalPhase(signalId);
        }
        
        // Update countdown display
        const signalCard = document.querySelector(`[data-signal-id="${signalId}"]`);
        const countdownElement = signalCard?.querySelector('.countdown');
        if (countdownElement) {
            countdownElement.textContent = `${signal.countdown}s`;
        }
    });
}

function cycleSignalPhase(signalId) {
    const signal = signals[signalId];
    
    switch (signal.phase) {
        case 'green':
            signal.phase = 'yellow';
            signal.countdown = 3;
            break;
        case 'yellow':
            signal.phase = 'red';
            signal.countdown = Math.floor(Math.random() * 30) + 30; // 30-60 seconds
            break;
        case 'red':
            signal.phase = 'green';
            signal.countdown = Math.floor(Math.random() * 40) + 20; // 20-60 seconds
            break;
    }
    
    updateSignalDisplay(signalId);
}

function updateSignalPhases() {
    // Randomly update signal status for demonstration
    if (Math.random() < 0.01) { // 1% chance per second
        const signalIds = Object.keys(signals);
        const randomSignalId = signalIds[Math.floor(Math.random() * signalIds.length)];
        const statuses = ['optimal', 'warning', 'critical'];
        const currentStatus = signals[randomSignalId].status;
        const newStatus = statuses[Math.floor(Math.random() * statuses.length)];
        
        if (newStatus !== currentStatus) {
            signals[randomSignalId].status = newStatus;
            updateSignalDisplay(randomSignalId);
            updateOverviewStats();
        }
    }
}

function updateOverviewStats() {
    const stats = {
        total: Object.keys(signals).length,
        optimal: 0,
        warning: 0,
        critical: 0
    };
    
    Object.values(signals).forEach(signal => {
        stats[signal.status]++;
    });
    
    // Update display
    document.getElementById('totalSignals').textContent = stats.total;
    document.getElementById('optimalSignals').textContent = stats.optimal;
    document.getElementById('warningSignals').textContent = stats.warning;
    document.getElementById('criticalSignals').textContent = stats.critical;
}

// ===== SIGNAL CONTROL FUNCTIONS =====
function adjustTiming(signalId) {
    const signal = signals[signalId];
    
    // Simulate timing adjustment
    const adjustments = [
        'Extended green phase by 10 seconds',
        'Reduced red phase by 5 seconds',
        'Optimized yellow phase timing',
        'Applied rush hour pattern'
    ];
    
    const adjustment = adjustments[Math.floor(Math.random() * adjustments.length)];
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(
            `${signal.location}: ${adjustment}`, 
            'success'
        );
    }
    
    // Simulate improvement
    if (signal.status === 'warning' || signal.status === 'critical') {
        setTimeout(() => {
            signal.status = 'optimal';
            updateSignalDisplay(signalId);
            updateOverviewStats();
        }, 2000);
    }
}

function optimizeSignal(signalId) {
    const signal = signals[signalId];
    const button = event.target;
    const originalText = button.innerHTML;
    
    button.innerHTML = '<div class="spinner spinner-sm"></div> Optimizing...';
    button.disabled = true;
    
    setTimeout(() => {
        // Apply optimization
        signal.status = 'optimal';
        signal.countdown = Math.floor(Math.random() * 30) + 20;
        
        updateSignalDisplay(signalId);
        updateOverviewStats();
        
        button.innerHTML = originalText;
        button.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification(
                `${signal.location} optimized successfully!`, 
                'success'
            );
        }
    }, 3000);
}

function emergencyOverride(signalId) {
    const signal = signals[signalId];
    
    // Force green phase for emergency
    signal.phase = 'green';
    signal.countdown = 60;
    signal.status = 'optimal';
    
    updateSignalDisplay(signalId);
    updateOverviewStats();
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(
            `Emergency override activated for ${signal.location}`, 
            'warning'
        );
    }
}

function optimizeAllSignals() {
    const btn = document.getElementById('optimizeAll');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Optimizing...';
    btn.disabled = true;
    
    setTimeout(() => {
        // Optimize all signals
        Object.keys(signals).forEach(signalId => {
            signals[signalId].status = 'optimal';
            updateSignalDisplay(signalId);
        });
        
        updateOverviewStats();
        
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification(
                'All signals optimized successfully!', 
                'success'
            );
        }
    }, 5000);
}

function toggleEmergencyMode() {
    emergencyModeActive = !emergencyModeActive;
    const btn = document.getElementById('emergencyMode');
    
    if (emergencyModeActive) {
        btn.innerHTML = '<i class="fas fa-stop"></i> Exit Emergency';
        btn.classList.add('btn-danger');
        btn.classList.remove('btn-secondary');
        
        // Stop all signals at red
        Object.keys(signals).forEach(signalId => {
            signals[signalId].phase = 'red';
            signals[signalId].countdown = 999;
            updateSignalDisplay(signalId);
        });
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification(
                'Emergency mode activated - All signals stopped', 
                'warning'
            );
        }
    } else {
        btn.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Emergency Mode';
        btn.classList.remove('btn-danger');
        btn.classList.add('btn-secondary');
        
        // Resume normal operation
        Object.keys(signals).forEach(signalId => {
            signals[signalId].countdown = Math.floor(Math.random() * 30) + 10;
        });
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification(
                'Emergency mode deactivated - Normal operation resumed', 
                'success'
            );
        }
    }
}

// ===== EMERGENCY CONTROLS =====
function handleEmergencyControl(button) {
    const buttonText = button.textContent.trim();
    
    switch (buttonText) {
        case 'All Stop':
            allStopControl();
            break;
        case 'Emergency Vehicle Priority':
            emergencyVehiclePriority();
            break;
        case 'Maintenance Mode':
            maintenanceMode();
            break;
    }
}

function allStopControl() {
    Object.keys(signals).forEach(signalId => {
        signals[signalId].phase = 'red';
        signals[signalId].countdown = 999;
        updateSignalDisplay(signalId);
    });
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('All signals stopped for safety', 'warning');
    }
}

function emergencyVehiclePriority() {
    // Simulate emergency vehicle route
    const priorityRoute = [1, 2, 3]; // Signal IDs along emergency route
    
    priorityRoute.forEach(signalId => {
        signals[signalId].phase = 'green';
        signals[signalId].countdown = 120;
        updateSignalDisplay(signalId);
    });
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Emergency vehicle priority activated', 'info');
    }
}

function maintenanceMode() {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Maintenance mode functionality coming soon!', 'info');
    }
}

function createTimingPattern() {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Timing pattern creator coming soon!', 'info');
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
    if (signalUpdateInterval) {
        clearInterval(signalUpdateInterval);
    }
});

// ===== EXPORT SIGNALS FUNCTIONS =====
window.TrafficSignals = {
    adjustTiming,
    optimizeSignal,
    emergencyOverride,
    optimizeAllSignals,
    toggleEmergencyMode
};
