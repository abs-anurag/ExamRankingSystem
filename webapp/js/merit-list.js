/**
 * ExamRank — Merit List Controller
 * Rankings are generated server-side using the custom Java Max Heap!
 */

let currentRankings = [];
let currentExam = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await initMeritPage();

    document.getElementById('exam-select').addEventListener('change', handleExamChange);
    document.getElementById('search-rankings-input').addEventListener('input', handleFilterRankings);
    document.getElementById('dept-filter-merit').addEventListener('change', handleFilterRankings);
    document.getElementById('btn-download-csv').addEventListener('click', downloadCSV);
});

async function initMeritPage() {
    try {
        const resp = await fetch('/api/examinations');
        const json = await resp.json();

        if (json.success && json.data) {
            const exams = json.data;
            const select = document.getElementById('exam-select');

            // Check if URL has ?examId=...
            const urlParams = new URLSearchParams(window.location.search);
            const targetExamId = urlParams.get('examId');

            select.innerHTML = exams.map(e => `
                <option value="${e.examId}" ${targetExamId == e.examId ? 'selected' : ''}>
                    ${e.examName} (Max: ${e.maxMarks})
                </option>
            `).join('');

            if (exams.length > 0) {
                const initialExamId = targetExamId ? parseInt(targetExamId) : exams[0].examId;
                await loadMeritList(initialExamId);
            }
        }
    } catch (e) {
        showToast('Failed to load examinations', 'error');
    }
}

async function handleExamChange() {
    const examId = parseInt(document.getElementById('exam-select').value);
    await loadMeritList(examId);
}

async function loadMeritList(examId) {
    try {
        const resp = await fetch(`/api/rankings?examId=${examId}`);
        const json = await resp.json();

        if (json.success && json.data) {
            currentExam = json.data.examination;
            currentRankings = json.data.rankings;

            // Populate department filter dropdown
            populateMeritDeptFilter(currentRankings);

            // Render Podium (Top 3)
            renderPodium(currentRankings);

            // Render Rankings Table
            renderRankingsTable(currentRankings);
        } else {
            showToast(json.error || 'Failed to generate merit list', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function populateMeritDeptFilter(rankings) {
    const filter = document.getElementById('dept-filter-merit');
    const depts = [...new Set(rankings.map(r => r.department))].sort();
    filter.innerHTML = `<option value="">All Departments</option>` +
        depts.map(d => `<option value="${d}">${d}</option>`).join('');
}

function renderPodium(rankings) {
    const container = document.getElementById('podium-section');
    if (!container) return;

    if (rankings.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'grid';

    const first = rankings[0] || null;
    const second = rankings[1] || null;
    const third = rankings[2] || null;

    container.innerHTML = `
        <!-- Rank 2: Silver -->
        ${second ? `
            <div class="podium-card rank-2">
                <div class="podium-badge">🥈</div>
                <div class="podium-name">${second.candidateName}</div>
                <div class="podium-roll">${second.rollNumber} • ${second.department}</div>
                <div class="podium-marks">${second.marks} pts</div>
                <div class="podium-pct">${second.percentage.toFixed(1)}% Score</div>
            </div>
        ` : `<div></div>`}

        <!-- Rank 1: Gold -->
        ${first ? `
            <div class="podium-card rank-1">
                <div class="podium-badge">👑</div>
                <div class="podium-name" style="font-size: 1.3rem;">${first.candidateName}</div>
                <div class="podium-roll">${first.rollNumber} • ${first.department}</div>
                <div class="podium-marks" style="font-size: 2.2rem; color: #b45309;">${first.marks} pts</div>
                <div class="podium-pct">${first.percentage.toFixed(1)}% • Top Achiever</div>
            </div>
        ` : `<div></div>`}

        <!-- Rank 3: Bronze -->
        ${third ? `
            <div class="podium-card rank-3">
                <div class="podium-badge">🥉</div>
                <div class="podium-name">${third.candidateName}</div>
                <div class="podium-roll">${third.rollNumber} • ${third.department}</div>
                <div class="podium-marks">${third.marks} pts</div>
                <div class="podium-pct">${third.percentage.toFixed(1)}% Score</div>
            </div>
        ` : `<div></div>`}
    `;
}

function renderRankingsTable(rankings) {
    const tbody = document.getElementById('merit-tbody');
    const countEl = document.getElementById('merit-count');
    if (countEl) countEl.textContent = `${rankings.length} Ranked Candidates`;

    if (rankings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--gray-400);">No scores recorded for this examination yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = rankings.map(r => {
        let rankBadgeClass = '';
        let medal = '';
        if (r.rank === 1) {
            rankBadgeClass = 'badge-rank-1';
            medal = '👑';
        } else if (r.rank === 2) {
            rankBadgeClass = 'badge-rank-2';
            medal = '🥈';
        } else if (r.rank === 3) {
            rankBadgeClass = 'badge-rank-3';
            medal = '🥉';
        }

        return `
            <tr ${r.rank <= 3 ? 'style="background: rgba(254, 243, 199, 0.15);"' : ''}>
                <td>
                    <span class="badge-custom ${rankBadgeClass}" style="font-size: 0.85rem; padding: 0.35rem 0.65rem;">
                        ${medal} Rank ${r.rank}
                    </span>
                    ${r.isTie ? '<span class="badge-custom badge-tie">Tied</span>' : ''}
                </td>
                <td><code style="background: var(--navy-50); color: var(--navy-600); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 700;">${r.rollNumber}</code></td>
                <td style="font-weight: 700; color: var(--navy-900);">${r.candidateName}</td>
                <td><span class="badge-custom badge-dept">${r.department}</span></td>
                <td>
                    <strong style="font-size: 1.05rem; color: var(--navy-900);">${r.marks}</strong>
                    <span style="color: var(--gray-400); font-size: 0.8125rem;">/ ${r.maxMarks}</span>
                </td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <div style="flex-grow: 1; height: 8px; background: var(--gray-200); border-radius: 4px; overflow: hidden; width: 60px;">
                            <div style="width: ${r.percentage}%; height: 100%; background: ${r.percentage >= 75 ? 'var(--success)' : r.percentage >= 40 ? 'var(--orange-500)' : 'var(--danger)'};"></div>
                        </div>
                        <span style="font-weight: 600; font-size: 0.875rem;">${r.percentage.toFixed(1)}%</span>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function handleFilterRankings() {
    const searchVal = document.getElementById('search-rankings-input').value.trim().toLowerCase();
    const deptVal = document.getElementById('dept-filter-merit').value;

    const filtered = currentRankings.filter(r => {
        const matchesSearch = r.candidateName.toLowerCase().includes(searchVal) || r.rollNumber.toLowerCase().includes(searchVal);
        const matchesDept = !deptVal || r.department === deptVal;
        return matchesSearch && matchesDept;
    });

    renderRankingsTable(filtered);
}

// Download Merit List as CSV
function downloadCSV() {
    if (!currentRankings || currentRankings.length === 0) {
        showToast('No rankings to export', 'warning');
        return;
    }

    const examTitle = currentExam ? currentExam.examName.replace(/[^a-zA-Z0-9]/g, '_') : 'Exam';
    const filename = `Merit_List_${examTitle}.csv`;

    let csvContent = 'Rank,Roll Number,Candidate Name,Department,Marks Obtained,Max Marks,Percentage,Is Tied\n';

    currentRankings.forEach(r => {
        const row = [
            r.rank,
            `"${r.rollNumber}"`,
            `"${r.candidateName}"`,
            `"${r.department}"`,
            r.marks,
            r.maxMarks,
            `${r.percentage.toFixed(2)}%`,
            r.isTie ? 'YES' : 'NO'
        ].join(',');
        csvContent += row + '\n';
    });

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Merit list downloaded as CSV', 'success');
}

