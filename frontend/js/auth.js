// ===== AUTHENTICATION JAVASCRIPT =====

// API Configuration
const API_BASE_URL = 'http://localhost:8081/api/v1';

// DOM Elements
let loginForm, registerForm, usernameInput, passwordInput, passwordToggle, loginButton, registerButton, loadingOverlay;
let firstNameInput, lastNameInput, emailInput, organizationInput, roleInput, confirmPasswordInput, confirmPasswordToggle;
let passwordStrengthBar, passwordStrengthText, agreeTermsCheckbox;

// Initialize authentication functionality
document.addEventListener('DOMContentLoaded', function() {
    initializeAuthElements();
    initializeEventListeners();
    initializeDemoCredentials();
    checkExistingAuth();
});

// ===== INITIALIZATION =====
function initializeAuthElements() {
    // Login elements
    loginForm = document.getElementById('loginForm');
    usernameInput = document.getElementById('username');
    passwordInput = document.getElementById('password');
    passwordToggle = document.getElementById('passwordToggle');
    loginButton = document.getElementById('loginButton');
    loadingOverlay = document.getElementById('loadingOverlay');

    // Registration elements
    registerForm = document.getElementById('registerForm');
    firstNameInput = document.getElementById('firstName');
    lastNameInput = document.getElementById('lastName');
    emailInput = document.getElementById('email');
    organizationInput = document.getElementById('organization');
    roleInput = document.getElementById('role');
    confirmPasswordInput = document.getElementById('confirmPassword');
    confirmPasswordToggle = document.getElementById('confirmPasswordToggle');
    registerButton = document.getElementById('registerButton');
    passwordStrengthBar = document.querySelector('.strength-fill');
    passwordStrengthText = document.querySelector('.strength-text');
    agreeTermsCheckbox = document.getElementById('agreeTerms');
}

function initializeEventListeners() {
    // Login form submission
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Registration form submission
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    // Password toggle for login (only if it exists)
    if (passwordToggle) {
        passwordToggle.addEventListener('click', () => togglePasswordVisibility('password'));
    }

    // Password toggle for registration (only if it exists)
    if (confirmPasswordToggle) {
        confirmPasswordToggle.addEventListener('click', () => togglePasswordVisibility('confirmPassword'));
    }

    // Login form validation
    if (usernameInput) {
        usernameInput.addEventListener('input', validateLoginForm);
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', () => {
            if (loginForm) {
                validateLoginForm();
            }
            if (registerForm && passwordStrengthBar) {
                checkPasswordStrength();
            }
        });
        passwordInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && loginForm) {
                handleLogin(e);
            }
        });
    }

    // Registration form validation
    if (registerForm) {
        const inputs = [firstNameInput, lastNameInput, emailInput, usernameInput, passwordInput, confirmPasswordInput];
        inputs.forEach(input => {
            if (input) {
                input.addEventListener('input', validateRegistrationForm);
            }
        });

        if (agreeTermsCheckbox) {
            agreeTermsCheckbox.addEventListener('change', validateRegistrationForm);
        }

        if (confirmPasswordInput) {
            confirmPasswordInput.addEventListener('input', checkPasswordMatch);
        }
    }
}

function initializeDemoCredentials() {
    const demoButtons = document.querySelectorAll('.use-demo');
    
    demoButtons.forEach(button => {
        button.addEventListener('click', function() {
            const demoUser = this.closest('.demo-user');
            const username = demoUser.getAttribute('data-username');
            const password = demoUser.getAttribute('data-password');
            
            usernameInput.value = username;
            passwordInput.value = password;
            
            // Add visual feedback
            this.innerHTML = '<i class="fas fa-check"></i> Applied';
            this.classList.add('btn-success');
            
            setTimeout(() => {
                this.innerHTML = 'Use';
                this.classList.remove('btn-success');
            }, 2000);
            
            validateForm();
        });
    });
}

// ===== AUTHENTICATION LOGIC =====
async function handleLogin(event) {
    event.preventDefault();
    
    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    
    if (!username || !password) {
        showError('Please enter both username and password');
        return;
    }
    
    setLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Store authentication data
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('userInfo', JSON.stringify({
                username: data.username,
                role: data.role,
                loginTime: new Date().toISOString()
            }));
            
            // Show success message
            showSuccess('Login successful! Redirecting to dashboard...');
            
            // Redirect to dashboard after short delay
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1500);
            
        } else {
            throw new Error(data.message || 'Login failed');
        }
        
    } catch (error) {
        console.error('Login error:', error);
        showError(error.message || 'Login failed. Please check your credentials and try again.');
    } finally {
        setLoading(false);
    }
}

// ===== REGISTRATION LOGIC =====
async function handleRegistration(event) {
    event.preventDefault();

    if (!validateRegistrationForm()) {
        showError('Please fill in all required fields correctly');
        return;
    }

    const formData = {
        firstName: firstNameInput.value.trim(),
        lastName: lastNameInput.value.trim(),
        email: emailInput.value.trim(),
        username: usernameInput.value.trim(),
        organization: organizationInput.value.trim(),
        role: roleInput.value,
        password: passwordInput.value
    };

    setRegistrationLoading(true);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok) {
            // Store authentication data (user is automatically logged in)
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('userInfo', JSON.stringify({
                username: data.username,
                role: data.role,
                loginTime: new Date().toISOString()
            }));

            // Show success message
            showSuccess('Account created successfully! Redirecting to dashboard...');

            // Clear form
            registerForm.reset();

            // Redirect to dashboard after short delay
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 2000);

        } else {
            throw new Error(data.message || 'Registration failed');
        }

    } catch (error) {
        console.error('Registration error:', error);
        showError(error.message || 'Registration failed. Please try again.');
    } finally {
        setRegistrationLoading(false);
    }
}

function checkExistingAuth() {
    // Only check on login page, not on dashboard
    if (!window.location.pathname.includes('login.html')) {
        return;
    }

    const token = localStorage.getItem('authToken');
    const userInfo = localStorage.getItem('userInfo');

    if (token && userInfo) {
        // Check if token is still valid (basic check)
        try {
            const user = JSON.parse(userInfo);
            const loginTime = new Date(user.loginTime);
            const now = new Date();
            const hoursSinceLogin = (now - loginTime) / (1000 * 60 * 60);

            // If logged in within last 24 hours, redirect to dashboard
            if (hoursSinceLogin < 24) {
                // Only show message once
                if (!sessionStorage.getItem('redirectMessageShown')) {
                    sessionStorage.setItem('redirectMessageShown', 'true');
                    showInfo('You are already logged in. Redirecting to dashboard...');
                    setTimeout(() => {
                        window.location.href = 'dashboard.html';
                    }, 2000);
                } else {
                    // Silent redirect
                    window.location.href = 'dashboard.html';
                }
                return;
            }
        } catch (error) {
            // Clear invalid data
            localStorage.removeItem('authToken');
            localStorage.removeItem('userInfo');
        }
    }
}

// ===== UI HELPERS =====
function togglePasswordVisibility(inputType) {
    const input = inputType === 'confirmPassword' ? confirmPasswordInput : passwordInput;
    const toggle = inputType === 'confirmPassword' ? confirmPasswordToggle : passwordToggle;

    if (!input || !toggle) return;

    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';

    const icon = toggle.querySelector('i');
    icon.className = isPassword ? 'fas fa-eye-slash' : 'fas fa-eye';
}

function validateLoginForm() {
    if (!usernameInput || !passwordInput || !loginButton) return false;

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    const isValid = username.length >= 3 && password.length >= 6;

    loginButton.disabled = !isValid;
    loginButton.style.opacity = isValid ? '1' : '0.6';

    return isValid;
}

function validateRegistrationForm() {
    if (!registerForm) return false;

    const firstName = firstNameInput?.value.trim() || '';
    const lastName = lastNameInput?.value.trim() || '';
    const email = emailInput?.value.trim() || '';
    const username = usernameInput?.value.trim() || '';
    const password = passwordInput?.value || '';
    const confirmPassword = confirmPasswordInput?.value || '';
    const role = roleInput?.value || '';
    const agreeTerms = agreeTermsCheckbox?.checked || false;

    // Validation rules
    const isValidName = firstName.length >= 2 && lastName.length >= 2;
    const isValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    const isValidUsername = username.length >= 3 && username.length <= 20;
    const isValidPassword = password.length >= 8;
    const isPasswordMatch = password === confirmPassword;
    const isValidRole = role !== '';

    const isValid = isValidName && isValidEmail && isValidUsername &&
                   isValidPassword && isPasswordMatch && isValidRole && agreeTerms;

    if (registerButton) {
        registerButton.disabled = !isValid;
        registerButton.style.opacity = isValid ? '1' : '0.6';
    }

    return isValid;
}

function checkPasswordStrength() {
    if (!passwordInput || !passwordStrengthBar || !passwordStrengthText) {
        // If strength indicators don't exist, just validate the form
        if (registerForm) {
            validateRegistrationForm();
        }
        return;
    }

    const password = passwordInput.value;
    let strength = 0;
    let strengthText = 'Very Weak';

    // Length check
    if (password.length >= 8) strength += 1;
    if (password.length >= 12) strength += 1;

    // Character variety checks
    if (/[a-z]/.test(password)) strength += 1;
    if (/[A-Z]/.test(password)) strength += 1;
    if (/[0-9]/.test(password)) strength += 1;
    if (/[^A-Za-z0-9]/.test(password)) strength += 1;

    // Set strength class and text
    passwordStrengthBar.className = 'strength-fill';

    if (strength <= 2) {
        passwordStrengthBar.classList.add('weak');
        strengthText = 'Weak';
    } else if (strength <= 3) {
        passwordStrengthBar.classList.add('fair');
        strengthText = 'Fair';
    } else if (strength <= 4) {
        passwordStrengthBar.classList.add('good');
        strengthText = 'Good';
    } else {
        passwordStrengthBar.classList.add('strong');
        strengthText = 'Strong';
    }

    passwordStrengthText.textContent = strengthText;
}

function checkPasswordMatch() {
    if (!passwordInput || !confirmPasswordInput) return;

    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (confirmPassword.length > 0) {
        const isMatch = password === confirmPassword;
        confirmPasswordInput.style.borderColor = isMatch ? 'var(--success-color)' : 'var(--error-color)';

        // Add/remove match indicator
        let indicator = confirmPasswordInput.parentElement.querySelector('.password-match-indicator');
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.className = 'password-match-indicator';
            indicator.style.cssText = `
                position: absolute;
                right: 3rem;
                top: 50%;
                transform: translateY(-50%);
                font-size: 0.9rem;
            `;
            confirmPasswordInput.parentElement.style.position = 'relative';
            confirmPasswordInput.parentElement.appendChild(indicator);
        }

        indicator.innerHTML = isMatch ?
            '<i class="fas fa-check" style="color: var(--success-color);"></i>' :
            '<i class="fas fa-times" style="color: var(--error-color);"></i>';
    }

    validateRegistrationForm();
}

function setLoading(loading) {
    if (loading) {
        loadingOverlay.classList.add('active');
        if (loginButton) {
            loginButton.disabled = true;
            loginButton.innerHTML = '<div class="spinner spinner-sm"></div> Signing In...';
        }
    } else {
        loadingOverlay.classList.remove('active');
        if (loginButton) {
            loginButton.disabled = false;
            loginButton.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
        }
    }
}

function setRegistrationLoading(loading) {
    if (loading) {
        loadingOverlay.classList.add('active');
        if (registerButton) {
            registerButton.disabled = true;
            registerButton.innerHTML = '<div class="spinner spinner-sm"></div> Creating Account...';
        }
    } else {
        loadingOverlay.classList.remove('active');
        if (registerButton) {
            registerButton.disabled = false;
            registerButton.innerHTML = '<i class="fas fa-user-plus"></i> Create Account';
        }
    }
}

// ===== NOTIFICATION HELPERS =====
function showSuccess(message) {
    showNotification(message, 'success');
}

function showError(message) {
    showNotification(message, 'error');
}

function showInfo(message) {
    showNotification(message, 'info');
}

function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.auth-notification');
    existingNotifications.forEach(notification => notification.remove());
    
    const notification = document.createElement('div');
    notification.className = `auth-notification alert alert-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 300px;
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
    `;
    
    notification.innerHTML = `
        <i class="fas fa-${getNotificationIcon(type)}"></i>
        <span>${message}</span>
        <button onclick="this.parentElement.remove()" style="margin-left: auto; background: none; border: none; color: inherit; cursor: pointer;">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'check-circle';
        case 'error': return 'exclamation-circle';
        case 'warning': return 'exclamation-triangle';
        default: return 'info-circle';
    }
}

// ===== LOGOUT FUNCTIONALITY =====
function logout() {
    // Clear stored authentication data
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    
    // Show logout message
    showInfo('You have been logged out successfully.');
    
    // Redirect to login page
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 2000);
}

// ===== AUTH VALIDATION =====
function isAuthenticated() {
    const token = localStorage.getItem('authToken');
    const userInfo = localStorage.getItem('userInfo');
    
    if (!token || !userInfo) {
        return false;
    }
    
    try {
        const user = JSON.parse(userInfo);
        const loginTime = new Date(user.loginTime);
        const now = new Date();
        const hoursSinceLogin = (now - loginTime) / (1000 * 60 * 60);
        
        // Token expires after 24 hours
        return hoursSinceLogin < 24;
    } catch (error) {
        return false;
    }
}

function requireAuth() {
    if (!isAuthenticated()) {
        console.log('Authentication required - redirecting to login');

        // Determine correct login path
        const currentPath = window.location.pathname;
        let loginPath = 'login.html';

        if (currentPath.includes('/pages/')) {
            loginPath = 'login.html';
        } else if (currentPath === '/' || currentPath.endsWith('index.html')) {
            loginPath = 'pages/login.html';
        }

        // Only show message once per session
        if (!sessionStorage.getItem('authErrorShown')) {
            sessionStorage.setItem('authErrorShown', 'true');
            showError('Please log in to access this page.');
            setTimeout(() => {
                window.location.href = loginPath;
            }, 2000);
        } else {
            // Silent redirect
            window.location.href = loginPath;
        }
        return false;
    }

    console.log('Authentication check passed');
    return true;
}

function getUserInfo() {
    try {
        const userInfo = localStorage.getItem('userInfo');
        return userInfo ? JSON.parse(userInfo) : null;
    } catch (error) {
        return null;
    }
}

function getAuthToken() {
    return localStorage.getItem('authToken');
}

// ===== API HELPER WITH AUTH =====
async function authenticatedApiCall(url, options = {}) {
    const token = getAuthToken();
    
    if (!token) {
        throw new Error('No authentication token found');
    }
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(url, mergedOptions);
        
        if (response.status === 401) {
            // Token expired or invalid
            logout();
            throw new Error('Session expired. Please log in again.');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// ===== EXPORT AUTH FUNCTIONS =====
window.TrafficAuth = {
    login: handleLogin,
    register: handleRegistration,
    logout,
    isAuthenticated,
    requireAuth,
    getUserInfo,
    getAuthToken,
    authenticatedApiCall,
    validateLoginForm,
    validateRegistrationForm,
    checkPasswordStrength,
    API_BASE_URL
};
