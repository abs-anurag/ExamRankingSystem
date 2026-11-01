/**
 * ExamRank — Analytics Dashboard Controller with Chart.js Integration
 */

let passFailChartInstance = null;
let scoreDistChartInstance = null;
let deptPerfChartInstance = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth(true);
    await initAnalyticsPage();

    document.getElementById('analytics-exam-select').addEventListener('change', handleExamChange);
});

async function initAnalyticsPage() {
    try {
        const resp = await fetch('/api/examinations');
        const json = await resp.json();

        if (json.success && json.data) {
            const exams = json.data;
            const select = document.getElementById('analytics-exam-select');

            select.innerHTML = exams.map(e => `
                <option value="${e.examId}">${e.examName} (Max: ${e.maxMarks})</option>
            `).join('');

            if (exams.length > 0) {
                await loadAnalytics(exams[0].examId);
            }
        }
    } catch (e) {
        showToast('Failed to load examinations', 'error');
    }
}

async function handleExamChange() {
    const examId = parseInt(document.getElementById('analytics-exam-select').value);
    await loadAnalytics(examId);
}

async function loadAnalytics(examId) {
    try {
        const resp = await fetch(`/api/analytics?examId=${examId}`);
        const json = await resp.json();

        if (json.success && json.data) {
            const data = json.data;

            // Summary metrics
            document.getElementById('m-candidates').textContent = data.totalCandidates ?? 0;
            document.getElementById('m-avg').textContent = data.averageMarks ? `${data.averageMarks} pts` : 'N/A';
            document.getElementById('m-highest').textContent = data.highestMarks ? `${data.highestMarks} pts` : 'N/A';
            document.getElementById('m-lowest').textContent = data.lowestMarks !== undefined && data.lowestMarks !== 1.7976931348623157e+308 ? `${data.lowestMarks} pts` : 'N/A';
            document.getElementById('m-pass').textContent = `${data.passPercentage ?? 0}%`;
            document.getElementById('m-fail').textContent = `${data.failPercentage ?? 0}%`;

            renderCharts(data);
        } else {
            showToast(json.error || 'Failed to compute analytics', 'error');
        }
    } catch (err) {
        showToast('Server communication error', 'error');
    }
}

function renderCharts(data) {
    // 1. Pass vs Fail Chart (Doughnut)
    const passFailCtx = document.getElementById('passFailChart').getContext('2d');
    if (passFailChartInstance) passFailChartInstance.destroy();

    passFailChartInstance = new Chart(passFailCtx, {
        type: 'doughnut',
        data: {
            labels: ['Passed (≥ 40%)', 'Failed (< 40%)'],
            datasets: [{
                data: [data.passPercentage || 0, data.failPercentage || 0],
                backgroundColor: ['#10b981', '#ef4444'],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });

    // 2. Score Distribution (Bar Chart)
    const scoreDistCtx = document.getElementById('scoreDistChart').getContext('2d');
    if (scoreDistChartInstance) scoreDistChartInstance.destroy();

    const distLabels = Object.keys(data.scoreDistribution || {});
    const distValues = Object.values(data.scoreDistribution || {});

    scoreDistChartInstance = new Chart(scoreDistCtx, {
        type: 'bar',
        data: {
            labels: distLabels,
            datasets: [{
                label: 'Candidate Count',
                data: distValues,
                backgroundColor: '#f97316',
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });

    // 3. Department Performance (Horizontal Bar Chart)
    const deptPerfCtx = document.getElementById('deptPerfChart').getContext('2d');
    if (deptPerfChartInstance) deptPerfChartInstance.destroy();

    const deptLabels = Object.keys(data.departmentPerformance || {});
    const deptValues = Object.values(data.departmentPerformance || {});

    deptPerfChartInstance = new Chart(deptPerfCtx, {
        type: 'bar',
        data: {
            labels: deptLabels,
            datasets: [{
                label: 'Average Score',
                data: deptValues,
                backgroundColor: '#1e3a8a',
                borderRadius: 6
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    beginAtZero: true
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

