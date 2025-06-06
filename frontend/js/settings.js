// ===== SETTINGS JAVASCRIPT =====

// Global variables
let currentSettings = {};
let hasUnsavedChanges = false;

// Initialize settings when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!TrafficAuth.requireAuth()) {
        return;
    }
    
    initializeSettings();
    initializeEventListeners();
    loadSettings();
    loadUserInfo();
});

// ===== INITIALIZATION =====
function initializeSettings() {
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
    // Settings navigation
    const settingsNavItems = document.querySelectorAll('.settings-nav-item');
    settingsNavItems.forEach(item => {
        item.addEventListener('click', function() {
            switchSettingsPanel(this.dataset.section);
        });
    });
    
    // Save and reset buttons
    const saveSettingsBtn = document.getElementById('saveSettings');
    if (saveSettingsBtn) {
        saveSettingsBtn.addEventListener('click', saveSettings);
    }
    
    const resetSettingsBtn = document.getElementById('resetSettings');
    if (resetSettingsBtn) {
        resetSettingsBtn.addEventListener('click', resetSettings);
    }
    
    // Theme selector
    const themeOptions = document.querySelectorAll('.theme-option');
    console.log('Found theme options:', themeOptions.length); // Debug log
    themeOptions.forEach(option => {
        console.log('Adding click listener to theme option:', option.dataset.theme); // Debug log
        option.addEventListener('click', function() {
            console.log('Theme option clicked:', this.dataset.theme); // Debug log
            selectTheme(this.dataset.theme);
        });
    });
    
    // Form inputs change tracking
    const formInputs = document.querySelectorAll('select, input[type="checkbox"], input[type="radio"], input[type="range"]');
    formInputs.forEach(input => {
        input.addEventListener('change', function() {
            markAsChanged();
            handleSettingChange(this);
        });
    });
    
    // Special handlers
    const optimizationSensitivity = document.getElementById('optimizationSensitivity');
    if (optimizationSensitivity) {
        optimizationSensitivity.addEventListener('input', updateSensitivityDisplay);
    }
    
    // Action buttons
    document.addEventListener('click', function(e) {
        if (e.target.matches('.btn') && e.target.closest('.settings-panel')) {
            handleActionButton(e.target);
        }
    });
    
    // Warn about unsaved changes
    window.addEventListener('beforeunload', function(e) {
        if (hasUnsavedChanges) {
            e.preventDefault();
            e.returnValue = '';
        }
    });
}

// ===== SETTINGS NAVIGATION =====
function switchSettingsPanel(section) {
    // Update navigation
    document.querySelectorAll('.settings-nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-section="${section}"]`).classList.add('active');
    
    // Update panels
    document.querySelectorAll('.settings-panel').forEach(panel => {
        panel.classList.remove('active');
    });
    document.getElementById(`${section}-panel`).classList.add('active');
}

// ===== SETTINGS MANAGEMENT =====
function loadSettings() {
    // Load settings from localStorage or use defaults
    const savedSettings = localStorage.getItem('trafficSettings');
    
    if (savedSettings) {
        currentSettings = JSON.parse(savedSettings);
    } else {
        currentSettings = getDefaultSettings();
    }
    
    applySettingsToUI();
}

function getDefaultSettings() {
    return {
        // General
        language: 'en',
        timezone: 'UTC-8',
        dateFormat: 'MM/DD/YYYY',
        distanceUnit: 'metric',
        speedUnit: 'kmh',
        
        // Appearance
        theme: 'light',
        sidebarCollapsed: false,
        enableAnimations: true,
        
        // Notifications
        trafficAlerts: true,
        signalAlerts: true,
        systemAlerts: true,
        emailNotifications: false,
        browserNotifications: true,
        
        // Traffic
        updateFrequency: 30,
        dataRetention: 30,
        autoOptimization: true,
        optimizationSensitivity: 7,
        
        // Security
        twoFactorAuth: false,
        sessionTimeout: 480
    };
}

function applySettingsToUI() {
    // Apply each setting to the corresponding UI element
    Object.keys(currentSettings).forEach(key => {
        const element = document.querySelector(`[name="${key}"], #${key}`);

        if (element) {
            if (element.type === 'checkbox') {
                element.checked = currentSettings[key];
            } else if (element.type === 'radio') {
                const radioGroup = document.querySelectorAll(`[name="${key}"]`);
                radioGroup.forEach(radio => {
                    radio.checked = radio.value === currentSettings[key];
                });
            } else if (element.type === 'range') {
                element.value = currentSettings[key];
                if (key === 'optimizationSensitivity') {
                    updateSensitivityDisplay();
                }
            } else {
                element.value = currentSettings[key];
            }
        }
    });

    // Apply theme immediately and update UI
    if (currentSettings.theme) {
        // Update theme selector UI
        document.querySelectorAll('.theme-option').forEach(option => {
            option.classList.remove('active');
        });
        const selectedOption = document.querySelector(`[data-theme="${currentSettings.theme}"]`);
        if (selectedOption) {
            selectedOption.classList.add('active');
        }

        // Apply theme to document
        document.body.setAttribute('data-theme', currentSettings.theme);
        document.documentElement.setAttribute('data-theme', currentSettings.theme);
    }

    hasUnsavedChanges = false;
    updateSaveButton();
}

function saveSettings() {
    const saveBtn = document.getElementById('saveSettings');
    const originalText = saveBtn.innerHTML;
    saveBtn.innerHTML = '<div class="spinner spinner-sm"></div> Saving...';
    saveBtn.disabled = true;
    
    // Collect current settings from UI
    const newSettings = { ...currentSettings };
    
    // General settings
    newSettings.language = document.querySelector('[name="language"]')?.value || 'en';
    newSettings.timezone = document.querySelector('[name="timezone"]')?.value || 'UTC-8';
    newSettings.dateFormat = document.querySelector('[name="dateFormat"]')?.value || 'MM/DD/YYYY';
    newSettings.distanceUnit = document.querySelector('[name="distance"]:checked')?.value || 'metric';
    newSettings.speedUnit = document.querySelector('[name="speed"]:checked')?.value || 'kmh';
    
    // Appearance settings
    newSettings.sidebarCollapsed = document.getElementById('sidebarCollapsed')?.checked || false;
    newSettings.enableAnimations = document.getElementById('enableAnimations')?.checked || true;
    
    // Notification settings
    newSettings.trafficAlerts = document.getElementById('trafficAlerts')?.checked || false;
    newSettings.signalAlerts = document.getElementById('signalAlerts')?.checked || false;
    newSettings.systemAlerts = document.getElementById('systemAlerts')?.checked || false;
    newSettings.emailNotifications = document.getElementById('emailNotifications')?.checked || false;
    newSettings.browserNotifications = document.getElementById('browserNotifications')?.checked || false;
    
    // Traffic settings
    newSettings.updateFrequency = parseInt(document.querySelector('[name="updateFrequency"]')?.value) || 30;
    newSettings.dataRetention = parseInt(document.querySelector('[name="dataRetention"]')?.value) || 30;
    newSettings.autoOptimization = document.getElementById('autoOptimization')?.checked || false;
    newSettings.optimizationSensitivity = parseInt(document.getElementById('optimizationSensitivity')?.value) || 7;
    
    // Security settings
    newSettings.twoFactorAuth = document.getElementById('twoFactorAuth')?.checked || false;
    newSettings.sessionTimeout = parseInt(document.querySelector('[name="sessionTimeout"]')?.value) || 480;
    
    // Simulate save operation
    setTimeout(() => {
        currentSettings = newSettings;
        localStorage.setItem('trafficSettings', JSON.stringify(currentSettings));
        
        hasUnsavedChanges = false;
        updateSaveButton();
        
        saveBtn.innerHTML = originalText;
        saveBtn.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Settings saved successfully!', 'success');
        }
        
        // Apply certain settings immediately
        applyImmediateSettings();
    }, 2000);
}

function resetSettings() {
    if (confirm('Are you sure you want to reset all settings to default? This action cannot be undone.')) {
        currentSettings = getDefaultSettings();
        applySettingsToUI();
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Settings reset to default values', 'success');
        }
    }
}

function applyImmediateSettings() {
    // Apply theme
    if (currentSettings.theme) {
        document.body.setAttribute('data-theme', currentSettings.theme);
    }
    
    // Apply sidebar state
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        if (currentSettings.sidebarCollapsed) {
            sidebar.classList.add('collapsed');
        } else {
            sidebar.classList.remove('collapsed');
        }
    }
    
    // Apply animations
    if (!currentSettings.enableAnimations) {
        document.body.style.setProperty('--transition-fast', '0s');
        document.body.style.setProperty('--transition-normal', '0s');
    } else {
        document.body.style.removeProperty('--transition-fast');
        document.body.style.removeProperty('--transition-normal');
    }
}

// ===== THEME MANAGEMENT =====
function selectTheme(theme) {
    console.log('Selecting theme:', theme); // Debug log

    // Update theme options
    document.querySelectorAll('.theme-option').forEach(option => {
        option.classList.remove('active');
    });

    const selectedOption = document.querySelector(`[data-theme="${theme}"]`);
    if (selectedOption) {
        selectedOption.classList.add('active');
    }

    // Apply theme immediately
    document.body.setAttribute('data-theme', theme);
    document.documentElement.setAttribute('data-theme', theme);

    // Update current settings
    currentSettings.theme = theme;

    // Save to localStorage immediately for theme changes
    localStorage.setItem('trafficSettings', JSON.stringify(currentSettings));

    // Also update the main theme system
    if (window.TrafficApp && window.TrafficApp.setTheme) {
        window.TrafficApp.setTheme(theme);
    }

    markAsChanged();

    // Show feedback
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification(`Theme changed to ${theme}`, 'success');
    }
}

// ===== CHANGE TRACKING =====
function markAsChanged() {
    hasUnsavedChanges = true;
    updateSaveButton();
}

function updateSaveButton() {
    const saveBtn = document.getElementById('saveSettings');
    if (saveBtn) {
        if (hasUnsavedChanges) {
            saveBtn.classList.add('btn-warning');
            saveBtn.innerHTML = '<i class="fas fa-save"></i> Save Changes *';
        } else {
            saveBtn.classList.remove('btn-warning');
            saveBtn.innerHTML = '<i class="fas fa-save"></i> Save Changes';
        }
    }
}

function handleSettingChange(element) {
    // Handle specific setting changes that need immediate feedback
    const settingName = element.name || element.id;
    
    switch (settingName) {
        case 'theme':
            selectTheme(element.value);
            break;
        case 'enableAnimations':
            if (!element.checked) {
                document.body.style.setProperty('--transition-fast', '0s');
                document.body.style.setProperty('--transition-normal', '0s');
            } else {
                document.body.style.removeProperty('--transition-fast');
                document.body.style.removeProperty('--transition-normal');
            }
            break;
        case 'sidebarCollapsed':
            const sidebar = document.getElementById('sidebar');
            if (sidebar) {
                if (element.checked) {
                    sidebar.classList.add('collapsed');
                } else {
                    sidebar.classList.remove('collapsed');
                }
            }
            break;
    }
}

// ===== SPECIAL HANDLERS =====
function updateSensitivityDisplay() {
    const slider = document.getElementById('optimizationSensitivity');
    const value = slider.value;
    
    // Update any display elements if needed
    // This could show the current value or description
}

function handleActionButton(button) {
    const buttonText = button.textContent.trim();
    
    switch (buttonText) {
        case 'Change Password':
            showChangePasswordModal();
            break;
        case 'View Active Sessions':
            showActiveSessionsModal();
            break;
        case 'Clear Cache':
            clearApplicationCache();
            break;
        case 'Export All Data':
            exportApplicationData();
            break;
        default:
            // Handle other action buttons
            break;
    }
}

function showChangePasswordModal() {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Password change functionality coming soon!', 'info');
    }
}

function showActiveSessionsModal() {
    if (window.TrafficApp && window.TrafficApp.showNotification) {
        window.TrafficApp.showNotification('Active sessions viewer coming soon!', 'info');
    }
}

function clearApplicationCache() {
    const button = event.target;
    const originalText = button.innerHTML;
    button.innerHTML = '<div class="spinner spinner-sm"></div> Clearing...';
    button.disabled = true;
    
    setTimeout(() => {
        // Clear various caches
        if ('caches' in window) {
            caches.keys().then(names => {
                names.forEach(name => {
                    caches.delete(name);
                });
            });
        }
        
        // Clear localStorage except for essential data
        const essentialKeys = ['authToken', 'userInfo', 'trafficSettings'];
        const keysToRemove = [];
        
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (!essentialKeys.includes(key)) {
                keysToRemove.push(key);
            }
        }
        
        keysToRemove.forEach(key => localStorage.removeItem(key));
        
        button.innerHTML = originalText;
        button.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Application cache cleared successfully!', 'success');
        }
    }, 2000);
}

function exportApplicationData() {
    const button = event.target;
    const originalText = button.innerHTML;
    button.innerHTML = '<div class="spinner spinner-sm"></div> Exporting...';
    button.disabled = true;
    
    setTimeout(() => {
        const exportData = {
            settings: currentSettings,
            userInfo: TrafficAuth.getUserInfo(),
            routeHistory: JSON.parse(localStorage.getItem('routeHistory') || '[]'),
            exportedAt: new Date().toISOString(),
            version: '1.0.0'
        };
        
        const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `traffic_system_data_export_${new Date().toISOString().split('T')[0]}.json`;
        a.click();
        window.URL.revokeObjectURL(url);
        
        button.innerHTML = originalText;
        button.disabled = false;
        
        if (window.TrafficApp && window.TrafficApp.showNotification) {
            window.TrafficApp.showNotification('Data exported successfully!', 'success');
        }
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

// ===== EXPORT SETTINGS FUNCTIONS =====
window.TrafficSettings = {
    saveSettings,
    resetSettings,
    loadSettings,
    exportApplicationData,
    clearApplicationCache
};
