/**
 * ExamRank — Leaderboard Controller
 * Generates Top-K Rankings using custom Java MaxHeap.getTopK()!
 */

let currentTopRankings = [];
let currentLimit = 5;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await initLeaderboardPage();

    document.getElementById('leaderboard-exam-select').addEventListener('change', handleExamChange);

    // Range buttons: Top 5, Top 10, Top 20
    const filterButtons = document.querySelectorAll('.limit-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            filterButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentLimit = parseInt(btn.getAttribute('data-limit'));
            const examId = parseInt(document.getElementById('leaderboard-exam-select').value);
            loadTopK(examId, currentLimit);
        });
    });
});

async function initLeaderboardPage() {
    try {
        const resp = await fetch('/api/examinations');
        const json = await resp.json();

        if (json.success && json.data) {
            const exams = json.data;
            const select = document.getElementById('leaderboard-exam-select');

            select.innerHTML = exams.map(e => `
                <option value="${e.examId}">${e.examName} (Max: ${e.maxMarks})</option>
            `).join('');

            if (exams.length > 0) {
                await loadTopK(exams[0].examId, currentLimit);
            }
        }
    } catch (e) {
        showToast('Failed to load examinations', 'error');
    }
}

async function handleExamChange() {
    const examId = parseInt(document.getElementById('leaderboard-exam-select').value);
    await loadTopK(examId, currentLimit);
}

async function loadTopK(examId, limit) {
    try {
        const resp = await fetch(`/api/rankings/top?examId=${examId}&limit=${limit}`);
        const json = await resp.json();

        if (json.success && json.data) {
            currentTopRankings = json.data.rankings;
            renderLeaderboardPodium(currentTopRankings);
            renderLeaderboardTable(currentTopRankings);
        } else {
            showToast(json.error || 'Failed to load leaderboard', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function renderLeaderboardPodium(rankings) {
    const container = document.getElementById('leaderboard-podium');
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
        ${second ? `
            <div class="podium-card rank-2">
                <div class="podium-badge">🥈</div>
                <div class="podium-name">${second.candidateName}</div>
                <div class="podium-roll">${second.rollNumber}</div>
                <div class="podium-marks">${second.marks} pts</div>
                <div class="podium-pct">${second.percentage.toFixed(1)}%</div>
            </div>
        ` : `<div></div>`}

        ${first ? `
            <div class="podium-card rank-1">
                <div class="podium-badge">👑</div>
                <div class="podium-name" style="font-size: 1.3rem;">${first.candidateName}</div>
                <div class="podium-roll">${first.rollNumber}</div>
                <div class="podium-marks" style="font-size: 2.2rem; color: #b45309;">${first.marks} pts</div>
                <div class="podium-pct">${first.percentage.toFixed(1)}% • Top 1</div>
            </div>
        ` : `<div></div>`}

        ${third ? `
            <div class="podium-card rank-3">
                <div class="podium-badge">🥉</div>
                <div class="podium-name">${third.candidateName}</div>
                <div class="podium-roll">${third.rollNumber}</div>
                <div class="podium-marks">${third.marks} pts</div>
                <div class="podium-pct">${third.percentage.toFixed(1)}%</div>
            </div>
        ` : `<div></div>`}
    `;
}

function renderLeaderboardTable(rankings) {
    const tbody = document.getElementById('leaderboard-tbody');
    const headerEl = document.getElementById('leaderboard-title');
    if (headerEl) headerEl.textContent = `Top ${currentLimit} Achievers (Custom Max Heap)`;

    if (rankings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2.5rem; color: var(--gray-400);">No scores available.</td></tr>`;
        return;
    }

    tbody.innerHTML = rankings.map(r => {
        let rankBadge = '';
        if (r.rank === 1) rankBadge = 'badge-rank-1';
        else if (r.rank === 2) rankBadge = 'badge-rank-2';
        else if (r.rank === 3) rankBadge = 'badge-rank-3';

        return `
            <tr ${r.rank <= 3 ? 'style="background: rgba(254, 243, 199, 0.15);"' : ''}>
                <td>
                    <span class="badge-custom ${rankBadge}">
                        ${r.rank === 1 ? '👑' : r.rank === 2 ? '🥈' : r.rank === 3 ? '🥉' : ''} Rank ${r.rank}
                    </span>
                    ${r.isTie ? '<span class="badge-custom badge-tie">Tied</span>' : ''}
                </td>
                <td><code style="background: var(--navy-50); color: var(--navy-600); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 700;">${r.rollNumber}</code></td>
                <td style="font-weight: 700; color: var(--navy-900);">${r.candidateName}</td>
                <td><span class="badge-custom badge-dept">${r.department}</span></td>
                <td>
                    <strong style="color: var(--navy-900); font-size: 1.05rem;">${r.marks}</strong>
                    <span style="color: var(--gray-400); font-size: 0.8125rem;">/ ${r.maxMarks} (${r.percentage.toFixed(1)}%)</span>
                </td>
            </tr>
        `;
    }).join('');
}

