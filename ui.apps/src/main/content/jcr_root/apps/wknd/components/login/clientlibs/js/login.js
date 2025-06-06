(function() {
    "use strict";

    async function getCsrfToken() {
        const response = await fetch('/libs/granite/csrf/token.json');
        const json = await response.json();
        return json.token;
    }

    async function handleLogin(event) {
        event.preventDefault();
        
        const form = event.target;
        const errorMessage = form.querySelector('.cmp-login__error-message');
        const submitButton = form.querySelector('.cmp-login__submit-button');
        
        // Get form data
        const formData = new FormData(form);
        
        // Disable button and show loading state
        submitButton.disabled = true;
        submitButton.textContent = 'Logging in...';
        errorMessage.style.display = 'none';
        
        try {
            // Get CSRF token
            const csrfToken = await getCsrfToken();
            
            // Submit the form to our custom login endpoint
            const response = await fetch('/bin/wknd/login', {
                method: 'POST',
                body: formData,
                credentials: 'same-origin',
                headers: {
                    'CSRF-Token': csrfToken
                }
            });
            
            const result = await response.json();
            
            if (result.success) {
                window.location.reload();
            } else {
                errorMessage.textContent = result.message || 'Invalid username or password';
                errorMessage.style.display = 'block';
                submitButton.disabled = false;
                submitButton.textContent = 'Login';
            }
        } catch (error) {
            console.error('Login error:', error);
            errorMessage.textContent = 'An error occurred. Please try again.';
            errorMessage.style.display = 'block';
            submitButton.disabled = false;
            submitButton.textContent = 'Login';
        }
    }

    function handleLogout(event) {
        event.preventDefault();
        window.location.href = '/system/sling/logout.html';
    }

    // Initialize login and logout forms
    function init() {
        const loginForm = document.querySelector('#loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', handleLogin);
        }

        const logoutForm = document.querySelector('#logoutForm');
        if (logoutForm) {
            logoutForm.addEventListener('submit', handleLogout);
        }
    }

    // Wait for DOM content to be loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})(); 