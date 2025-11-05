// Admin Product Management JavaScript Functions

// Initialize page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeAdminPage();
});

function initializeAdminPage() {
    console.log('Admin Product Management Page Initialized');
    
    // Add loading states to forms
    addLoadingStates();
    
    // Enhance form validation
    enhanceFormValidation();
    
    // Add image preview functionality
    addImagePreview();
    
    // Add table enhancements
    enhanceDataTable();
    
    // Add confirmation dialogs
    enhanceConfirmations();
}

// Add loading states to buttons and forms
function addLoadingStates() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const buttons = form.querySelectorAll('input[type="submit"], button');
            buttons.forEach(button => {
                button.disabled = true;
                button.classList.add('loading');
                const originalText = button.value || button.textContent;
                button.value = 'Đang xử lý...';
                button.textContent = 'Đang xử lý...';
            });
        });
    });
}

// Enhance form validation
function enhanceFormValidation() {
    const requiredFields = document.querySelectorAll('input[required], textarea[required], select[required]');
    
    requiredFields.forEach(field => {
        field.addEventListener('blur', function() {
            validateField(this);
        });
        
        field.addEventListener('input', function() {
            if (this.classList.contains('error')) {
                validateField(this);
            }
        });
    });
}

function validateField(field) {
    const value = field.value.trim();
    const isValid = value.length > 0;
    
    if (isValid) {
        field.classList.remove('error');
        field.classList.add('valid');
        removeErrorMessage(field);
    } else {
        field.classList.remove('valid');
        field.classList.add('error');
        showErrorMessage(field, 'Trường này là bắt buộc');
    }
}

function showErrorMessage(field, message) {
    removeErrorMessage(field);
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    errorDiv.style.color = '#dc3545';
    errorDiv.style.fontSize = '12px';
    errorDiv.style.marginTop = '5px';
    
    field.parentNode.appendChild(errorDiv);
}

function removeErrorMessage(field) {
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }
}

// Add image preview functionality
function addImagePreview() {
    const fileInputs = document.querySelectorAll('input[type="file"]');
    
    fileInputs.forEach(input => {
        input.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                previewImage(file, this);
            }
        });
    });
}

function previewImage(file, input) {
    const reader = new FileReader();
    
    reader.onload = function(e) {
        // Remove existing preview
        const existingPreview = input.parentNode.querySelector('.image-preview');
        if (existingPreview) {
            existingPreview.remove();
        }
        
        // Create new preview
        const preview = document.createElement('div');
        preview.className = 'image-preview';
        preview.style.marginTop = '10px';
        
        const img = document.createElement('img');
        img.src = e.target.result;
        img.style.maxWidth = '200px';
        img.style.maxHeight = '200px';
        img.style.borderRadius = '5px';
        img.style.border = '2px solid #ddd';
        
        preview.appendChild(img);
        input.parentNode.appendChild(preview);
    };
    
    reader.readAsDataURL(file);
}

// Enhance data table functionality
function enhanceDataTable() {
    const tables = document.querySelectorAll('table');
    
    tables.forEach(table => {
        // Add hover effects
        const rows = table.querySelectorAll('tr');
        rows.forEach(row => {
            row.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f8f9fa';
            });
            
            row.addEventListener('mouseleave', function() {
                this.style.backgroundColor = '';
            });
        });
        
        // Add sorting functionality to headers
        const headers = table.querySelectorAll('th');
        headers.forEach(header => {
            header.style.cursor = 'pointer';
            header.addEventListener('click', function() {
                sortTable(table, this.cellIndex);
            });
        });
    });
}

function sortTable(table, columnIndex) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    
    rows.sort((a, b) => {
        const aText = a.cells[columnIndex].textContent.trim();
        const bText = b.cells[columnIndex].textContent.trim();
        
        // Try to parse as numbers first
        const aNum = parseFloat(aText);
        const bNum = parseFloat(bText);
        
        if (!isNaN(aNum) && !isNaN(bNum)) {
            return aNum - bNum;
        }
        
        // Otherwise sort as strings
        return aText.localeCompare(bText);
    });
    
    // Clear tbody and append sorted rows
    tbody.innerHTML = '';
    rows.forEach(row => tbody.appendChild(row));
}

// Enhance confirmation dialogs
function enhanceConfirmations() {
    const deleteButtons = document.querySelectorAll('input[value="Xóa"], button[value="Xóa"]');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này? Hành động này không thể hoàn tác.')) {
                e.preventDefault();
                return false;
            }
        });
    });
}

// Utility functions
function showMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    // Insert at top of page
    const body = document.body;
    body.insertBefore(messageDiv, body.firstChild);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

// AJAX helper functions
function makeAjaxRequest(url, method = 'GET', data = null) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url, true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const response = JSON.parse(xhr.responseText);
                        resolve(response);
                    } catch (e) {
                        resolve(xhr.responseText);
                    }
                } else {
                    reject(new Error(`Request failed with status: ${xhr.status}`));
                }
            }
        };
        
        xhr.onerror = function() {
            reject(new Error('Network error'));
        };
        
        if (data) {
            xhr.send(JSON.stringify(data));
        } else {
            xhr.send();
        }
    });
}

// Export functions for global use
window.AdminProductManager = {
    showMessage,
    formatCurrency,
    formatDate,
    makeAjaxRequest,
    validateField,
    previewImage
};
