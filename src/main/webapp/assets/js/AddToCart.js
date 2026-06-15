/**
 * Add to Cart Handler with Login Check and Toast Notification
 */

// Show toast notification
function showToast(message, type = 'info') {
    // Remove existing toast if any
    const existingToast = document.querySelector('.toast-notification');
    if (existingToast) {
        existingToast.remove();
    }

    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <span class="toast-icon">${getToastIcon(type)}</span>
            <span class="toast-message">${message}</span>
        </div>
    `;

    // Add to body
    document.body.appendChild(toast);

    // Show toast with animation
    setTimeout(() => toast.classList.add('show'), 10);

    // Auto hide after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function getToastIcon(type) {
    switch(type) {
        case 'success': return '✓';
        case 'error': return '✕';
        case 'warning': return '⚠';
        default: return 'ℹ';
    }
}

// Add to cart with AJAX
function addToCartAjax(productId, quantity, contextPath) {
    fetch(`${contextPath}/cart/add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: `productId=${encodeURIComponent(productId)}&quantity=${encodeURIComponent(quantity)}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast(data.message, 'success');
            // Update cart count if element exists
            const cartCountElements = document.querySelectorAll('.cart-count, .cart-quantity');
            cartCountElements.forEach(el => {
                if (data.cartCount) {
                    el.textContent = data.cartCount;
                    el.style.display = 'inline-block';
                }
            });
        } else if (data.requireLogin) {
            showToast(data.message, 'warning');
            setTimeout(() => {
                window.location.href = `${contextPath}/login`;
            }, 1500);
        } else {
            showToast(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
    });
}

// Global function for inline onclick handlers
window.addToCart = function(productId, quantity, contextPath) {
    addToCartAjax(productId, quantity, contextPath);
};

// Handle form submissions
document.addEventListener('DOMContentLoaded', function() {
    // Handle all add-to-cart forms
    document.querySelectorAll('form[action*="cart/add"]').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(form);
            const productId = formData.get('productId');
            const quantity = formData.get('quantity') || 1;
            const contextPath = form.action.split('/cart/add')[0];
            
            addToCartAjax(productId, quantity, contextPath);
        });
    });

    // Handle all add-to-cart buttons with data attributes
    document.querySelectorAll('[data-add-to-cart]').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const productId = this.dataset.productId;
            const quantity = this.dataset.quantity || 1;
            const contextPath = this.dataset.contextPath || '';
            
            addToCartAjax(productId, quantity, contextPath);
        });
    });
});
