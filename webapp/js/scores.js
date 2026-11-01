/**
 * ExamRank — Score Management Controller
 */

let allScores = [];
let allExaminations = [];
let allCandidates = [];

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await initScoresPage();

    document.getElementById('score-exam-filter').addEventListener('change', handleExamFilterChange);
    document.getElementById('search-score-input').addEventListener('input', handleScoreSearch);
    document.getElementById('btn-record-score').addEventListener('click', () => openModal('record-score-modal'));
    document.getElementById('record-score-form').addEventListener('submit', handleRecordScore);
    document.getElementById('edit-score-form').addEventListener('submit', handleEditScore);

    // Dynamic max-marks validator when exam is selected in record modal
    document.getElementById('modal-select-exam').addEventListener('change', updateMaxMarksIndicator);
});

async function initScoresPage() {
    await Promise.all([loadExaminations(), loadCandidates()]);
    await loadScores();
}

async function loadExaminations() {
    try {
        const resp = await fetch('/api/examinations');
        const json = await resp.json();
        if (json.success && json.data) {
            allExaminations = json.data;

            // Populate exam filter dropdown
            const filterEl = document.getElementById('score-exam-filter');
            filterEl.innerHTML = `<option value="">All Examinations</option>` +
                allExaminations.map(e => `<option value="${e.examId}">${e.examName} (Max: ${e.maxMarks})</option>`).join('');

            // Populate modal exam dropdown
            const modalExamSelect = document.getElementById('modal-select-exam');
            modalExamSelect.innerHTML = `<option value="">-- Choose Examination --</option>` +
                allExaminations.map(e => `<option value="${e.examId}" data-max="${e.maxMarks}">${e.examName} (Max: ${e.maxMarks})</option>`).join('');
        }
    } catch (e) {
        showToast('Failed to load examinations list', 'error');
    }
}

async function loadCandidates() {
    try {
        const resp = await fetch('/api/candidates');
        const json = await resp.json();
        if (json.success && json.data) {
            allCandidates = json.data;

            // Populate modal candidate dropdown
            const modalCandSelect = document.getElementById('modal-select-candidate');
            modalCandSelect.innerHTML = `<option value="">-- Choose Candidate --</option>` +
                allCandidates.map(c => `<option value="${c.candidateId}">${c.rollNumber} - ${c.name} (${c.department})</option>`).join('');
        }
    } catch (e) {
        showToast('Failed to load candidates list', 'error');
    }
}

async function loadScores(examId = '') {
    try {
        let url = '/api/scores';
        if (examId) url += `?examId=${examId}`;

        const resp = await fetch(url);
        const json = await resp.json();

        if (json.success && json.data) {
            allScores = json.data;
            renderScoresTable(allScores);
        } else {
            showToast(json.error || 'Failed to load scores', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function renderScoresTable(scores) {
    const tbody = document.getElementById('scores-tbody');
    const countEl = document.getElementById('scores-count');
    if (countEl) countEl.textContent = `${scores.length} Records`;

    if (scores.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--gray-400);">No score records found.</td></tr>`;
        return;
    }

    tbody.innerHTML = scores.map(s => {
        const pct = (s.maxMarks > 0) ? ((s.marks / s.maxMarks) * 100).toFixed(1) : 0;
        const isPass = pct >= 40.0;
        return `
            <tr>
                <td style="font-weight: 700;">#${s.scoreId}</td>
                <td><code style="background: var(--navy-50); color: var(--navy-600); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 700;">${s.rollNumber}</code></td>
                <td style="font-weight: 600; color: var(--navy-900);">${s.candidateName}</td>
                <td style="font-size: 0.875rem;">${s.examName}</td>
                <td>
                    <strong style="color: var(--navy-900);">${s.marks}</strong>
                    <span style="color: var(--gray-400); font-size: 0.8125rem;">/ ${s.maxMarks}</span>
                </td>
                <td>
                    <span class="badge-custom ${isPass ? 'badge-pass' : 'badge-fail'}">
                        ${pct}% (${isPass ? 'Pass' : 'Fail'})
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 0.5rem;">
                        <button onclick="openEditScoreModal(${s.scoreId}, '${s.candidateName.replace(/'/g, "\\'")}', '${s.examName.replace(/'/g, "\\'")}', ${s.marks}, ${s.maxMarks})" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem;">
                            ✏️ Update
                        </button>
                        <button onclick="confirmDeleteScore(${s.scoreId}, '${s.candidateName.replace(/'/g, "\\'")}', '${s.examName.replace(/'/g, "\\'")}')" class="btn-outline-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem; color: var(--danger); border-color: #fecaca;">
                            🗑️ Delete
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function handleExamFilterChange() {
    const examId = document.getElementById('score-exam-filter').value;
    loadScores(examId);
}

function handleScoreSearch() {
    const query = document.getElementById('search-score-input').value.trim().toLowerCase();
    const filtered = allScores.filter(s =>
        s.candidateName.toLowerCase().includes(query) ||
        s.rollNumber.toLowerCase().includes(query) ||
        s.examName.toLowerCase().includes(query)
    );
    renderScoresTable(filtered);
}

function updateMaxMarksIndicator() {
    const select = document.getElementById('modal-select-exam');
    const selectedOpt = select.options[select.selectedIndex];
    const max = selectedOpt ? selectedOpt.getAttribute('data-max') : null;
    const indicator = document.getElementById('max-marks-indicator');
    if (max && indicator) {
        indicator.textContent = `(Max Allowable: ${max})`;
    } else if (indicator) {
        indicator.textContent = '';
    }
}

async function handleRecordScore(e) {
    e.preventDefault();
    const candidateId = parseInt(document.getElementById('modal-select-candidate').value);
    const examId = parseInt(document.getElementById('modal-select-exam').value);
    const marks = parseFloat(document.getElementById('modal-score-marks').value);

    if (isNaN(candidateId) || isNaN(examId) || isNaN(marks)) {
        showToast('Please fill all required score fields', 'warning');
        return;
    }

    try {
        const resp = await fetch('/api/scores', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ candidateId: candidateId, examId: examId, marks: marks })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Examination score recorded successfully', 'success');
            closeModal('record-score-modal');
            document.getElementById('record-score-form').reset();
            const currentFilter = document.getElementById('score-exam-filter').value;
            await loadScores(currentFilter);
        } else {
            showToast(json.error || 'Failed to record score', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function openEditScoreModal(scoreId, candidateName, examName, currentMarks, maxMarks) {
    document.getElementById('edit-score-id').value = scoreId;
    document.getElementById('edit-score-info').textContent = `${candidateName} — ${examName} (Max: ${maxMarks})`;
    document.getElementById('edit-score-marks').value = currentMarks;
    document.getElementById('edit-score-marks').setAttribute('max', maxMarks);
    openModal('edit-score-modal');
}

async function handleEditScore(e) {
    e.preventDefault();
    const scoreId = parseInt(document.getElementById('edit-score-id').value);
    const marks = parseFloat(document.getElementById('edit-score-marks').value);

    try {
        const resp = await fetch(`/api/scores/${scoreId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ scoreId: scoreId, marks: marks })
        });
        const json = await resp.json();

        if (json.success) {
            showToast('Score updated successfully', 'success');
            closeModal('edit-score-modal');
            const currentFilter = document.getElementById('score-exam-filter').value;
            await loadScores(currentFilter);
        } else {
            showToast(json.error || 'Failed to update score', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

async function confirmDeleteScore(scoreId, candidateName, examName) {
    if (!confirm(`Are you sure you want to delete the score record for ${candidateName} in ${examName}?`)) {
        return;
    }

    try {
        const resp = await fetch(`/api/scores/${scoreId}`, { method: 'DELETE' });
        const json = await resp.json();

        if (json.success) {
            showToast('Score record deleted', 'success');
            const currentFilter = document.getElementById('score-exam-filter').value;
            await loadScores(currentFilter);
        } else {
            showToast(json.error || 'Failed to delete score', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

