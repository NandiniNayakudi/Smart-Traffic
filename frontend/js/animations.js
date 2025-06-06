// ===== ANIMATIONS JAVASCRIPT FILE =====

// Initialize animations when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeTrafficLightAnimation();
    initializeCounterAnimations();
    initializeParallaxEffects();
    initializeHoverEffects();
});

// ===== TRAFFIC LIGHT ANIMATION =====
function initializeTrafficLightAnimation() {
    const lights = document.querySelectorAll('.traffic-light .light');
    if (lights.length === 0) return;
    
    let currentLight = 2; // Start with green (index 2)
    
    function cycleLights() {
        // Remove active class from all lights
        lights.forEach(light => light.classList.remove('active'));
        
        // Add active class to current light
        lights[currentLight].classList.add('active');
        
        // Move to next light (cycle: green -> yellow -> red -> green)
        currentLight = (currentLight + 1) % 3;
        if (currentLight === 0) currentLight = 2; // Skip to green after red
        if (currentLight === 2 && Math.random() > 0.7) currentLight = 1; // Sometimes go to yellow
    }
    
    // Initial cycle
    cycleLights();
    
    // Continue cycling every 3 seconds
    setInterval(cycleLights, 3000);
}

// ===== COUNTER ANIMATIONS =====
function initializeCounterAnimations() {
    const counters = document.querySelectorAll('.stat-number');
    
    const observerOptions = {
        threshold: 0.5,
        rootMargin: '0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                observer.unobserve(entry.target); // Only animate once
            }
        });
    }, observerOptions);
    
    counters.forEach(counter => {
        observer.observe(counter);
    });
}

function animateCounter(element) {
    const target = element.textContent;
    const isPercentage = target.includes('%');
    const targetNumber = parseInt(target.replace(/[^\d]/g, ''));
    
    if (isNaN(targetNumber)) return;
    
    let current = 0;
    const increment = targetNumber / 60; // 60 frames for smooth animation
    const duration = 2000; // 2 seconds
    const frameTime = duration / 60;
    
    function updateCounter() {
        current += increment;
        
        if (current >= targetNumber) {
            element.textContent = target; // Set final value
            return;
        }
        
        if (isPercentage) {
            element.textContent = Math.floor(current) + '%';
        } else if (target.includes('/')) {
            element.textContent = '24/7'; // Special case for 24/7
        } else {
            element.textContent = Math.floor(current);
        }
        
        setTimeout(updateCounter, frameTime);
    }
    
    updateCounter();
}

// ===== PARALLAX EFFECTS =====
function initializeParallaxEffects() {
    const parallaxElements = document.querySelectorAll('.hero-visual, .about-image');
    
    if (parallaxElements.length === 0) return;
    
    function updateParallax() {
        const scrolled = window.pageYOffset;
        const rate = scrolled * -0.5;
        
        parallaxElements.forEach(element => {
            element.style.transform = `translateY(${rate}px)`;
        });
    }
    
    // Use requestAnimationFrame for smooth animation
    let ticking = false;
    
    function requestTick() {
        if (!ticking) {
            requestAnimationFrame(updateParallax);
            ticking = true;
        }
    }
    
    function handleScroll() {
        requestTick();
        ticking = false;
    }
    
    window.addEventListener('scroll', handleScroll);
}

// ===== HOVER EFFECTS =====
function initializeHoverEffects() {
    // Feature cards hover effect
    const featureCards = document.querySelectorAll('.feature-card');
    
    featureCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px) scale(1.02)';
            this.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.1)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        });
    });
    
    // Button hover effects
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            if (this.classList.contains('btn-primary')) {
                this.style.transform = 'translateY(-2px)';
                this.style.boxShadow = '0 8px 25px rgba(59, 130, 246, 0.3)';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '';
        });
    });
}

// ===== VEHICLE ANIMATION =====
function initializeVehicleAnimation() {
    const vehicles = document.querySelectorAll('.vehicle');
    
    vehicles.forEach((vehicle, index) => {
        // Random delay for each vehicle
        vehicle.style.animationDelay = `${index * 1.5}s`;
        
        // Random colors for vehicles
        const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];
        vehicle.style.backgroundColor = colors[index % colors.length];
        
        // Add random movement patterns
        vehicle.addEventListener('animationiteration', function() {
            // Change color on each iteration
            this.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
        });
    });
}

// ===== LOADING ANIMATIONS =====
function createLoadingAnimation(container) {
    const loadingHTML = `
        <div class="loading-container" style="
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            min-height: 200px;
        ">
            <div class="spinner" style="margin-bottom: 1rem;"></div>
            <p style="color: var(--text-secondary); margin: 0;">Loading...</p>
        </div>
    `;
    
    if (container) {
        container.innerHTML = loadingHTML;
    }
    
    return loadingHTML;
}

// ===== FADE IN ANIMATION =====
function fadeInElement(element, delay = 0) {
    element.style.opacity = '0';
    element.style.transform = 'translateY(30px)';
    element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    
    setTimeout(() => {
        element.style.opacity = '1';
        element.style.transform = 'translateY(0)';
    }, delay);
}

// ===== SLIDE IN ANIMATION =====
function slideInElement(element, direction = 'left', delay = 0) {
    const translateValue = direction === 'left' ? '-100px' : '100px';
    
    element.style.opacity = '0';
    element.style.transform = `translateX(${translateValue})`;
    element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    
    setTimeout(() => {
        element.style.opacity = '1';
        element.style.transform = 'translateX(0)';
    }, delay);
}

// ===== SCALE ANIMATION =====
function scaleInElement(element, delay = 0) {
    element.style.opacity = '0';
    element.style.transform = 'scale(0.8)';
    element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    
    setTimeout(() => {
        element.style.opacity = '1';
        element.style.transform = 'scale(1)';
    }, delay);
}

// ===== TYPEWRITER EFFECT =====
function typewriterEffect(element, text, speed = 50) {
    element.textContent = '';
    let i = 0;
    
    function typeChar() {
        if (i < text.length) {
            element.textContent += text.charAt(i);
            i++;
            setTimeout(typeChar, speed);
        }
    }
    
    typeChar();
}

// ===== PULSE ANIMATION =====
function pulseElement(element, duration = 1000) {
    element.style.animation = `pulse ${duration}ms ease-in-out infinite`;
}

// Add pulse keyframes to CSS if not already present
function addPulseKeyframes() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.05); }
            100% { transform: scale(1); }
        }
    `;
    document.head.appendChild(style);
}

// ===== INTERSECTION OBSERVER UTILITIES =====
function createIntersectionObserver(callback, options = {}) {
    const defaultOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    return new IntersectionObserver(callback, { ...defaultOptions, ...options });
}

// ===== ANIMATION UTILITIES =====
function animateOnScroll(elements, animationType = 'fadeIn') {
    const observer = createIntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                const delay = index * 100; // Stagger animations
                
                switch (animationType) {
                    case 'fadeIn':
                        fadeInElement(entry.target, delay);
                        break;
                    case 'slideLeft':
                        slideInElement(entry.target, 'left', delay);
                        break;
                    case 'slideRight':
                        slideInElement(entry.target, 'right', delay);
                        break;
                    case 'scaleIn':
                        scaleInElement(entry.target, delay);
                        break;
                }
                
                observer.unobserve(entry.target);
            }
        });
    });
    
    elements.forEach(element => observer.observe(element));
}

// ===== EXPORT ANIMATION FUNCTIONS =====
window.TrafficAnimations = {
    fadeInElement,
    slideInElement,
    scaleInElement,
    typewriterEffect,
    pulseElement,
    animateOnScroll,
    createLoadingAnimation,
    createIntersectionObserver
};

// Initialize vehicle animation when page loads
document.addEventListener('DOMContentLoaded', initializeVehicleAnimation);

// Add pulse keyframes
addPulseKeyframes();
