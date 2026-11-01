/**
 * ExamRank — Admin Dashboard Controller
 */

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await loadDashboardStats();
});

async function loadDashboardStats() {
    try {
        const resp = await fetch('/api/analytics/dashboard');
        const json = await resp.json();

        if (json.success && json.data) {
            const data = json.data;

            // Update Statistics Cards
            document.getElementById('stat-candidates').textContent = data.totalCandidates ?? 0;
            document.getElementById('stat-exams').textContent = data.totalExaminations ?? 0;
            document.getElementById('stat-scores').textContent = data.totalScores ?? 0;

            const topScorer = data.highestScore;
            if (topScorer && topScorer.marks > 0) {
                document.getElementById('stat-highest').textContent = `${topScorer.marks} pts`;
                document.getElementById('stat-highest-sub').textContent = `${topScorer.candidateName} (${topScorer.rollNumber})`;
            } else {
                document.getElementById('stat-highest').textContent = 'N/A';
                document.getElementById('stat-highest-sub').textContent = 'No scores recorded';
            }

            // Populate examinations table
            renderExaminationsTable(data.examinations || []);
        } else {
            showToast(json.error || 'Failed to load dashboard data', 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend server', 'error');
    }
}

function renderExaminationsTable(exams) {
    const tbody = document.getElementById('dashboard-exams-tbody');
    if (!tbody) return;

    if (exams.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center" style="padding: 2rem; color: var(--gray-400);">No examinations scheduled yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = exams.map(e => `
        <tr>
            <td style="font-weight: 600;">#${e.examId}</td>
            <td style="font-weight: 600; color: var(--navy-900);">${e.examName}</td>
            <td><span class="badge-custom badge-dept">${e.maxMarks} Marks</span></td>
            <td>
                <a href="/pages/merit-list.html?examId=${e.examId}" class="btn-primary-custom" style="padding: 0.35rem 0.75rem; font-size: 0.8125rem;">
                    🏆 View Merit List
                </a>
            </td>
        </tr>
    `).join('');
}

