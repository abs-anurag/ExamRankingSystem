/**
 * ExamRank — Examination Management Controller
 */

let allExaminations = [];

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await loadExaminations();

    document.getElementById('btn-add-exam').addEventListener('click', () => openModal('add-exam-modal'));
    document.getElementById('add-exam-form').addEventListener('submit', handleAddExam);
    document.getElementById('edit-exam-form').addEventListener('submit', handleEditExam);
    document.getElementById('search-exam-input').addEventListener('input', handleExamSearch);
});

async function loadExaminations() {
    try {
        const resp = await fetch('/api/examinations');
        const json = await resp.json();

        if (json.success && json.data) {
            allExaminations = json.data;
            renderExaminationsTable(allExaminations);
        } else {
            showToast(json.error || 'Failed to load examinations', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function renderExaminationsTable(exams) {
    const tbody = document.getElementById('exams-tbody');
    const countEl = document.getElementById('exam-count');
    if (countEl) countEl.textContent = `${exams.length} Examinations`;

    if (exams.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; padding: 2.5rem; color: var(--gray-400);">No examinations found.</td></tr>`;
        return;
    }

    tbody.innerHTML = exams.map(e => `
        <tr>
            <td style="font-weight: 700; color: var(--navy-900);">#${e.examId}</td>
            <td style="font-weight: 600; color: var(--navy-900);">${e.examName}</td>
            <td><span class="badge-custom badge-dept" style="font-weight: 700;">${e.maxMarks} Points</span></td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <a href="/pages/merit-list.html?examId=${e.examId}" class="btn-primary-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem;">
                        🏆 Merit List
                    </a>
                    <button onclick="openEditExamModal(${e.examId}, '${e.examName.replace(/'/g, "\\'")}', ${e.maxMarks})" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem;">
                        ✏️ Edit
                    </button>
                    <button onclick="confirmDeleteExam(${e.examId}, '${e.examName.replace(/'/g, "\\'")}')" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem; color: var(--danger); border-color: #fecaca;">
                        🗑️ Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function handleExamSearch() {
    const query = document.getElementById('search-exam-input').value.trim().toLowerCase();
    const filtered = allExaminations.filter(e => e.examName.toLowerCase().includes(query) || e.examId.toString().includes(query));
    renderExaminationsTable(filtered);
}

async function handleAddExam(e) {
    e.preventDefault();
    const name = document.getElementById('add-exam-name').value.trim();
    const maxMarks = parseFloat(document.getElementById('add-exam-max').value);

    if (!name || isNaN(maxMarks) || maxMarks <= 0) {
        showToast('Please enter a valid examination name and positive max marks', 'warning');
        return;
    }

    try {
        const resp = await fetch('/api/examinations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ examName: name, maxMarks: maxMarks })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Examination created successfully', 'success');
            closeModal('add-exam-modal');
            document.getElementById('add-exam-form').reset();
            await loadExaminations();
        } else {
            showToast(json.error || 'Failed to create exam', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function openEditExamModal(id, name, maxMarks) {
    document.getElementById('edit-exam-id').value = id;
    document.getElementById('edit-exam-name').value = name;
    document.getElementById('edit-exam-max').value = maxMarks;
    openModal('edit-exam-modal');
}

async function handleEditExam(e) {
    e.preventDefault();
    const id = document.getElementById('edit-exam-id').value;
    const name = document.getElementById('edit-exam-name').value.trim();
    const maxMarks = parseFloat(document.getElementById('edit-exam-max').value);

    try {
        const resp = await fetch(`/api/examinations/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ examId: parseInt(id), examName: name, maxMarks: maxMarks })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Examination updated successfully', 'success');
            closeModal('edit-exam-modal');
            await loadExaminations();
        } else {
            showToast(json.error || 'Update failed', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

async function confirmDeleteExam(id, name) {
    if (!confirm(`Are you sure you want to delete examination "${name}" and all associated student scores?`)) {
        return;
    }

    try {
        const resp = await fetch(`/api/examinations/${id}`, { method: 'DELETE' });
        const json = await resp.json();

        if (json.success) {
            showToast('Examination deleted', 'success');
            await loadExaminations();
        } else {
            showToast(json.error || 'Failed to delete examination', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

