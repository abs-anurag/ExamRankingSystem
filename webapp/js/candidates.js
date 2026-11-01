/**
 * ExamRank — Candidate Management Controller
 * Demonstrates integration with Custom Hash Table for O(1) roll number lookups.
 */

let allCandidates = [];

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await loadCandidates();

    document.getElementById('search-input').addEventListener('input', handleFilter);
    document.getElementById('dept-filter').addEventListener('change', handleFilter);
    document.getElementById('btn-add-candidate').addEventListener('click', () => openModal('add-candidate-modal'));
    document.getElementById('add-candidate-form').addEventListener('submit', handleAddCandidate);
    document.getElementById('edit-candidate-form').addEventListener('submit', handleEditCandidate);
    document.getElementById('btn-hash-search').addEventListener('click', handleHashSearch);
});

async function loadCandidates() {
    try {
        const resp = await fetch('/api/candidates');
        const json = await resp.json();

        if (json.success && json.data) {
            allCandidates = json.data;
            populateDepartmentFilter(allCandidates);
            renderCandidatesTable(allCandidates);
        } else {
            showToast(json.error || 'Failed to load candidates', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function populateDepartmentFilter(candidates) {
    const filter = document.getElementById('dept-filter');
    const departments = [...new Set(candidates.map(c => c.department))].sort();
    filter.innerHTML = `<option value="">All Departments</option>` +
        departments.map(d => `<option value="${d}">${d}</option>`).join('');
}

function renderCandidatesTable(candidates) {
    const tbody = document.getElementById('candidates-tbody');
    const countEl = document.getElementById('candidate-count');
    if (countEl) countEl.textContent = `${candidates.length} Registered`;

    if (candidates.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2.5rem; color: var(--gray-400);">No candidates found.</td></tr>`;
        return;
    }

    tbody.innerHTML = candidates.map(c => `
        <tr>
            <td style="font-weight: 700; color: var(--navy-900);">#${c.candidateId}</td>
            <td><code style="background: var(--navy-50); color: var(--navy-600); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 700;">${c.rollNumber}</code></td>
            <td style="font-weight: 600; color: var(--navy-900);">${c.name}</td>
            <td><span class="badge-custom badge-dept">${c.department}</span></td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <button onclick="openEditModal(${c.candidateId}, '${c.rollNumber}', '${c.name.replace(/'/g, "\\'")}', '${c.department.replace(/'/g, "\\'")}')" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem;">
                        ✏️ Edit
                    </button>
                    <button onclick="confirmDeleteCandidate(${c.candidateId}, '${c.rollNumber}', '${c.name.replace(/'/g, "\\'")}')" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem; color: var(--danger); border-color: #fecaca;">
                        🗑️ Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function handleFilter() {
    const searchVal = document.getElementById('search-input').value.trim().toLowerCase();
    const deptVal = document.getElementById('dept-filter').value;

    const filtered = allCandidates.filter(c => {
        const matchesSearch = c.name.toLowerCase().includes(searchVal) || c.rollNumber.toLowerCase().includes(searchVal);
        const matchesDept = !deptVal || c.department === deptVal;
        return matchesSearch && matchesDept;
    });

    renderCandidatesTable(filtered);
}

// Custom Hash Table Search
async function handleHashSearch() {
    const roll = document.getElementById('hash-search-input').value.trim().toUpperCase();
    const banner = document.getElementById('adsa-search-banner');

    if (!roll) {
        showToast('Please enter a roll number to search in Hash Table', 'warning');
        return;
    }

    try {
        const resp = await fetch(`/api/candidates/search?rollNumber=${encodeURIComponent(roll)}`);
        const json = await resp.json();

        if (json.success && json.data) {
            const data = json.data;
            const c = data.candidate;
            banner.style.display = 'block';
            banner.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <strong style="color: var(--navy-900);">🎯 ADSA Hash Table Result:</strong>
                        <span>Found <strong>${c.name}</strong> (${c.rollNumber}) in department <em>${c.department}</em>.</span>
                    </div>
                    <div>
                        <span class="badge-custom badge-rank-1">⚡ ${data.lookupTimeMicros} µs (${data.timeComplexity})</span>
                    </div>
                </div>
            `;
            // Filter table to this candidate
            renderCandidatesTable([c]);
            showToast(`Found candidate via Custom Hash Table in ${data.lookupTimeMicros} µs`, 'success');
        } else {
            banner.style.display = 'block';
            banner.innerHTML = `<span style="color: var(--danger);">❌ ${json.error || 'Candidate not found in Custom Hash Table.'}</span>`;
            showToast(json.error || 'Candidate not found in Hash Table', 'error');
        }
    } catch (err) {
        showToast('Failed to execute Hash Table search', 'error');
    }
}

// Add Candidate Form
async function handleAddCandidate(e) {
    e.preventDefault();
    const roll = document.getElementById('add-roll').value.trim().toUpperCase();
    const name = document.getElementById('add-name').value.trim();
    const dept = document.getElementById('add-dept').value.trim();

    if (!roll || !name || !dept) {
        showToast('All fields are required', 'warning');
        return;
    }

    try {
        const resp = await fetch('/api/candidates', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rollNumber: roll, name: name, department: dept })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Candidate registered and indexed in Hash Table', 'success');
            closeModal('add-candidate-modal');
            document.getElementById('add-candidate-form').reset();
            await loadCandidates();
        } else {
            showToast(json.error || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function openEditModal(id, roll, name, dept) {
    document.getElementById('edit-id').value = id;
    document.getElementById('edit-roll').value = roll;
    document.getElementById('edit-name').value = name;
    document.getElementById('edit-dept').value = dept;
    openModal('edit-candidate-modal');
}

async function handleEditCandidate(e) {
    e.preventDefault();
    const id = document.getElementById('edit-id').value;
    const name = document.getElementById('edit-name').value.trim();
    const dept = document.getElementById('edit-dept').value.trim();

    try {
        const resp = await fetch(`/api/candidates/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ candidateId: parseInt(id), name: name, department: dept })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Candidate details updated and re-indexed', 'success');
            closeModal('edit-candidate-modal');
            await loadCandidates();
        } else {
            showToast(json.error || 'Update failed', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

async function confirmDeleteCandidate(id, roll, name) {
    if (!confirm(`Are you sure you want to delete ${name} (${roll}) and all associated examination scores?`)) {
        return;
    }

    try {
        const resp = await fetch(`/api/candidates/${id}`, { method: 'DELETE' });
        const json = await resp.json();

        if (json.success) {
            showToast(`Candidate ${roll} removed from database and Hash Table`, 'success');
            await loadCandidates();
        } else {
            showToast(json.error || 'Deletion failed', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

