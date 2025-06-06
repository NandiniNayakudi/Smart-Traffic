// ===== ROUTE PLANNING JAVASCRIPT =====

// Global variables
let currentRoute = null;
let routeHistory = [];
let waypoints = [];

// Initialize route planning when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeRoutes();
    initializeEventListeners();
    loadRouteHistory();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeRoutes() {
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
    
    // Set default departure time to current time
    const departureTimeInput = document.getElementById('departureTime');
    if (departureTimeInput) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        departureTimeInput.value = now.toISOString().slice(0, 16);
    }
}

function initializeEventListeners() {
    // Route form submission
    const routeForm = document.getElementById('routeForm');
    if (routeForm) {
        routeForm.addEventListener('submit', handleRouteSearch);
    }
    
    // Input autocomplete
    const originInput = document.getElementById('originInput');
    const destinationInput = document.getElementById('destinationInput');
    
    if (originInput) {
        originInput.addEventListener('input', debounce(function() {
            showLocationSuggestions(this, 'originSuggestions');
        }, 300));
    }
    
    if (destinationInput) {
        destinationInput.addEventListener('input', debounce(function() {
            showLocationSuggestions(this, 'destinationSuggestions');
        }, 300));
    }
    
    // Control buttons
    const clearRoutesBtn = document.getElementById('clearRoutes');
    if (clearRoutesBtn) {
        clearRoutesBtn.addEventListener('click', clearAllRoutes);
    }
    
    const optimizeRoutesBtn = document.getElementById('optimizeRoutes');
    if (optimizeRoutesBtn) {
        optimizeRoutesBtn.addEventListener('click', optimizeAllRoutes);
    }
    
    const addWaypointBtn = document.getElementById('addWaypoint');
    if (addWaypointBtn) {
        addWaypointBtn.addEventListener('click', addWaypoint);
    }
    
    const refreshRoutesBtn = document.getElementById('refreshRoutes');
    if (refreshRoutesBtn) {
        refreshRoutesBtn.addEventListener('click', refreshRouteOptions);
    }
    
    const clearHistoryBtn = document.getElementById('clearHistory');
    if (clearHistoryBtn) {
        clearHistoryBtn.addEventListener('click', clearRouteHistory);
    }
    
    // Map control buttons
    const mapControlBtns = document.querySelectorAll('.map-control-btn');
    mapControlBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            switchMapView(this.dataset.view);
        });
    });
    
    // Route option selection
    document.addEventListener('click', function(e) {
        if (e.target.matches('.route-actions .btn-primary')) {
            selectRoute(e.target.closest('.route-option'));
        }
        
        if (e.target.matches('.recent-route-card .btn-primary')) {
            useRecentRoute(e.target.closest('.recent-route-card'));
        }
    });
    
    // Close suggestions when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.form-group')) {
            document.querySelectorAll('.location-suggestions').forEach(suggestions => {
                suggestions.classList.remove('active');
            });
        }
    });
}

// ===== ROUTE SEARCH =====
function handleRouteSearch(event) {
    event.preventDefault();
    
    const origin = document.getElementById('originInput').value.trim();
    const destination = document.getElementById('destinationInput').value.trim();
    const departureTime = document.getElementById('departureTime').value;
    const preference = document.getElementById('routePreference').value;
    
    if (!origin || !destination) {
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Please enter both origin and destination', 'error');
        }
        return;
    }
    
    // Show loading state
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<div class="spinner spinner-sm"></div> Finding Routes...';
    submitBtn.disabled = true;
    
    // Simulate route search
    setTimeout(() => {
        const routes = generateRouteOptions(origin, destination, preference);
        displayRouteOptions(routes);
        updateRouteMap(origin, destination);
        
        // Add to history
        addToRouteHistory(origin, destination, routes[0]);
        
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Routes found successfully!', 'success');
        }
    }, 2000);
}

// ===== LOCATION SUGGESTIONS =====
function showLocationSuggestions(input, suggestionsId) {
    const query = input.value.trim();
    const suggestionsContainer = document.getElementById(suggestionsId);
    
    if (query.length < 2) {
        suggestionsContainer.classList.remove('active');
        return;
    }
    
    // Mock location suggestions
    const locations = [
        'Downtown Office Building',
        'Tech Campus - Building A',
        'Shopping Mall - Main Entrance',
        'Airport Terminal 1',
        'University Campus',
        'City Hall',
        'Central Park',
        'Business District',
        'Residential Area - Oak Street',
        'Industrial Zone - Warehouse District'
    ];
    
    const filteredLocations = locations.filter(location => 
        location.toLowerCase().includes(query.toLowerCase())
    );
    
    if (filteredLocations.length > 0) {
        suggestionsContainer.innerHTML = filteredLocations
            .slice(0, 5)
            .map(location => `
                <div class="suggestion-item" onclick="selectSuggestion('${input.id}', '${location}', '${suggestionsId}')">
                    ${location}
                </div>
            `).join('');
        suggestionsContainer.classList.add('active');
    } else {
        suggestionsContainer.classList.remove('active');
    }
}

function selectSuggestion(inputId, location, suggestionsId) {
    document.getElementById(inputId).value = location;
    document.getElementById(suggestionsId).classList.remove('active');
}

// ===== ROUTE GENERATION =====
function generateRouteOptions(origin, destination, preference) {
    const baseTime = 15 + Math.random() * 20; // 15-35 minutes
    const baseDistance = 8 + Math.random() * 10; // 8-18 km
    
    const routes = [
        {
            type: 'recommended',
            badge: 'Recommended',
            icon: 'fas fa-star',
            time: Math.round(baseTime),
            distance: baseDistance.toFixed(1),
            route: 'Via Main Street',
            condition: 'light',
            conditionText: 'Light traffic',
            conditionTime: 'Current conditions',
            fuelSavings: null
        },
        {
            type: 'alternative',
            badge: 'Alternative',
            icon: 'fas fa-route',
            time: Math.round(baseTime * 1.2),
            distance: (baseDistance * 0.9).toFixed(1),
            route: 'Via Highway 101',
            condition: 'moderate',
            conditionText: 'Moderate traffic',
            conditionTime: 'Current conditions',
            fuelSavings: null
        },
        {
            type: 'eco',
            badge: 'Eco-Friendly',
            icon: 'fas fa-leaf',
            time: Math.round(baseTime * 1.4),
            distance: (baseDistance * 1.1).toFixed(1),
            route: 'Via Park Avenue',
            condition: 'light',
            conditionText: 'Light traffic',
            conditionTime: '15% less fuel',
            fuelSavings: '15%'
        }
    ];
    
    // Adjust based on preference
    if (preference === 'fastest') {
        routes[0].time = Math.round(baseTime * 0.8);
    } else if (preference === 'shortest') {
        routes[0].distance = (baseDistance * 0.8).toFixed(1);
    } else if (preference === 'eco') {
        routes[2].time = Math.round(baseTime * 1.1);
    }
    
    return routes;
}

function displayRouteOptions(routes) {
    const routeOptionsContainer = document.getElementById('routeOptions');
    if (!routeOptionsContainer) return;
    
    routeOptionsContainer.innerHTML = routes.map(route => `
        <div class="route-option ${route.type}">
            <div class="route-option-header">
                <div class="route-badge ${route.type}">
                    <i class="${route.icon}"></i>
                    ${route.badge}
                </div>
                <div class="route-time">${route.time} min</div>
            </div>
            <div class="route-details">
                <div class="route-info">
                    <span class="route-distance">${route.distance} km</span>
                    <span class="route-type">${route.route}</span>
                </div>
                <div class="route-conditions">
                    <span class="condition ${route.condition}">${route.conditionText}</span>
                    <span class="condition-time">${route.conditionTime}</span>
                </div>
            </div>
            <div class="route-actions">
                <button class="btn btn-sm btn-primary">Select Route</button>
                <button class="btn btn-sm btn-secondary">View Details</button>
            </div>
        </div>
    `).join('');
}

function updateRouteMap(origin, destination) {
    // Update the visual representation of the route
    const originPoint = document.querySelector('.route-point.origin span');
    const destinationPoint = document.querySelector('.route-point.destination span');
    
    if (originPoint) {
        originPoint.textContent = origin.length > 15 ? origin.substring(0, 15) + '...' : origin;
    }
    
    if (destinationPoint) {
        destinationPoint.textContent = destination.length > 15 ? destination.substring(0, 15) + '...' : destination;
    }
    
    // Animate the route path
    const routeSegments = document.querySelectorAll('.route-segment');
    routeSegments.forEach((segment, index) => {
        segment.style.animationDelay = `${index * 0.5}s`;
    });
}

// ===== ROUTE SELECTION =====
function selectRoute(routeOption) {
    // Remove previous selection
    document.querySelectorAll('.route-option').forEach(option => {
        option.classList.remove('selected');
    });
    
    // Mark as selected
    routeOption.classList.add('selected');
    
    const routeType = routeOption.querySelector('.route-badge').textContent;
    const routeTime = routeOption.querySelector('.route-time').textContent;
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(`${routeType} route selected (${routeTime})`, 'success');
    }
    
    // Store current route
    currentRoute = {
        type: routeType,
        time: routeTime,
        selectedAt: new Date()
    };
}

// ===== ROUTE HISTORY =====
function addToRouteHistory(origin, destination, route) {
    const historyItem = {
        origin,
        destination,
        time: route.time,
        distance: route.distance,
        fuel: (parseFloat(route.distance) * 0.1).toFixed(1), // Estimate fuel consumption
        timestamp: new Date()
    };
    
    routeHistory.unshift(historyItem);
    
    // Keep only last 10 routes
    if (routeHistory.length > 10) {
        routeHistory = routeHistory.slice(0, 10);
    }
    
    // Save to localStorage
    localStorage.setItem('routeHistory', JSON.stringify(routeHistory));
    
    updateRouteHistoryDisplay();
}

function loadRouteHistory() {
    const saved = localStorage.getItem('routeHistory');
    if (saved) {
        routeHistory = JSON.parse(saved);
        updateRouteHistoryDisplay();
    }
}

function updateRouteHistoryDisplay() {
    const historyContainer = document.querySelector('.recent-routes-grid');
    if (!historyContainer) return;
    
    if (routeHistory.length === 0) {
        historyContainer.innerHTML = '<p class="text-center">No recent routes</p>';
        return;
    }
    
    historyContainer.innerHTML = routeHistory.slice(0, 3).map(route => {
        const timeAgo = getTimeAgo(new Date(route.timestamp));
        
        return `
            <div class="recent-route-card">
                <div class="route-card-header">
                    <div class="route-endpoints">
                        <span class="origin">${route.origin}</span>
                        <i class="fas fa-arrow-right"></i>
                        <span class="destination">${route.destination}</span>
                    </div>
                    <div class="route-time">${timeAgo}</div>
                </div>
                <div class="route-card-details">
                    <div class="route-stats">
                        <span class="stat">
                            <i class="fas fa-clock"></i>
                            ${route.time} min
                        </span>
                        <span class="stat">
                            <i class="fas fa-road"></i>
                            ${route.distance} km
                        </span>
                        <span class="stat">
                            <i class="fas fa-gas-pump"></i>
                            ${route.fuel}L
                        </span>
                    </div>
                    <button class="btn btn-sm btn-primary">Use Again</button>
                </div>
            </div>
        `;
    }).join('');
}

function useRecentRoute(routeCard) {
    const endpoints = routeCard.querySelector('.route-endpoints');
    const origin = endpoints.querySelector('.origin').textContent;
    const destination = endpoints.querySelector('.destination').textContent;
    
    // Fill form with recent route data
    document.getElementById('originInput').value = origin;
    document.getElementById('destinationInput').value = destination;
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Route loaded from history', 'success');
    }
}

function clearRouteHistory() {
    routeHistory = [];
    localStorage.removeItem('routeHistory');
    updateRouteHistoryDisplay();
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Route history cleared', 'success');
    }
}

// ===== UTILITY FUNCTIONS =====
function clearAllRoutes() {
    document.getElementById('routeForm').reset();
    document.getElementById('routeOptions').innerHTML = '<p class="text-center">Enter origin and destination to find routes</p>';
    
    // Reset map
    document.querySelector('.route-point.origin span').textContent = 'Origin';
    document.querySelector('.route-point.destination span').textContent = 'Destination';
    
    currentRoute = null;
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('All routes cleared', 'success');
    }
}

function optimizeAllRoutes() {
    const btn = document.getElementById('optimizeRoutes');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<div class="spinner spinner-sm"></div> Optimizing...';
    btn.disabled = true;
    
    setTimeout(() => {
        // Simulate optimization
        refreshRouteOptions();
        
        btn.innerHTML = originalText;
        btn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Routes optimized based on current traffic!', 'success');
        }
    }, 3000);
}

function refreshRouteOptions() {
    const origin = document.getElementById('originInput').value.trim();
    const destination = document.getElementById('destinationInput').value.trim();
    
    if (origin && destination) {
        const preference = document.getElementById('routePreference').value;
        const routes = generateRouteOptions(origin, destination, preference);
        displayRouteOptions(routes);
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Route options refreshed', 'success');
        }
    }
}

function addWaypoint() {
    // This would add a waypoint input field
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Waypoint functionality coming soon!', 'info');
    }
}

function switchMapView(view) {
    // Update active map control
    document.querySelectorAll('.map-control-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-view="${view}"]`).classList.add('active');
    
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(`Switched to ${view} view`, 'info');
    }
}

function getTimeAgo(date) {
    const now = new Date();
    const diffMs = now - date;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffDays > 0) {
        return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    } else if (diffHours > 0) {
        return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    } else {
        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        return `${diffMinutes} minute${diffMinutes > 1 ? 's' : ''} ago`;
    }
}

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

// ===== EXPORT ROUTES FUNCTIONS =====
window.TrafficRoutes = {
    clearAllRoutes,
    optimizeAllRoutes,
    refreshRouteOptions,
    addWaypoint,
    clearRouteHistory
};
