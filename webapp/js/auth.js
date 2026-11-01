/**
 * ExamRank — Authentication & Common UI Utilities
 */

const API_BASE = '/api';

// Toast Notification Engine
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast-item ${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    else if (type === 'error') icon = '❌';
    else if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `
        <span style="font-size: 1.2rem;">${icon}</span>
        <span style="flex-grow: 1;">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Session Checker
async function checkAuth(requireAuth = true) {
    try {
        const resp = await fetch(`${API_BASE}/auth/me`);
        const json = await resp.json();

        if (json.success && json.data) {
            updateUserInterface(json.data);
            if (!requireAuth && window.location.pathname.includes('login.html')) {
                window.location.href = '/pages/dashboard.html';
            }
            return json.data;
        } else {
            if (requireAuth) {
                window.location.href = '/login.html';
            }
            return null;
        }
    } catch (err) {
        if (requireAuth) {
            window.location.href = '/login.html';
        }
        return null;
    }
}

// Update User info in sidebar
function updateUserInterface(user) {
    const nameEl = document.getElementById('sidebar-user-name');
    const roleEl = document.getElementById('sidebar-user-role');
    const avatarEl = document.getElementById('sidebar-user-avatar');

    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (roleEl) roleEl.textContent = user.role || 'USER';
    if (avatarEl) {
        const initials = (user.fullName || user.username).substring(0, 2).toUpperCase();
        avatarEl.textContent = initials;
    }
}

// Logout handler
async function logout() {
    try {
        await fetch(`${API_BASE}/logout`, { method: 'POST' });
        showToast('Logged out successfully', 'success');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 500);
    } catch (e) {
        window.location.href = '/login.html';
    }
}

// Helper to open/close modal
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

