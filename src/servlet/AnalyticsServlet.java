package servlet;

import dao.CandidateDAO;
import dao.ExaminationDAO;
import dao.ScoreDAO;
import database.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Examination;
import model.ScoreRecord;
import util.JsonUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * AnalyticsServlet computes performance metrics, summary statistics,
 * and chart datasets for the Admin Dashboard and Analytics view.
 *
 * Endpoints:
 * - GET /api/analytics/dashboard
 * - GET /api/analytics?examId=...
 */
@WebServlet(name = "AnalyticsServlet", urlPatterns = {"/api/analytics", "/api/analytics/*"})
public class AnalyticsServlet extends HttpServlet {

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String examIdParam = req.getParameter("examId");

        if (pathInfo != null && pathInfo.equals("/dashboard")) {
            handleDashboardSummary(resp);
            return;
        }

        if (examIdParam != null && !examIdParam.trim().isEmpty()) {
            try {
                int examId = Integer.parseInt(examIdParam.trim());
                handleExamAnalytics(examId, resp);
                return;
            } catch (NumberFormatException e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid exam ID format.");
                return;
            }
        }

        // If no examId provided, default to first available exam or overall
        List<Examination> allExams = examDAO.getAllExaminations();
        if (!allExams.isEmpty()) {
            handleExamAnalytics(allExams.get(0).getExamId(), resp);
        } else {
            handleDashboardSummary(resp);
        }
    }

    private void handleDashboardSummary(HttpServletResponse resp) throws IOException {
        Map<String, Object> summary = new HashMap<>();

        int totalCandidates = candidateDAO.getAllCandidates().size();
        List<Examination> exams = examDAO.getAllExaminations();
        int totalExams = exams.size();
        List<ScoreRecord> allScores = scoreDAO.getAllScores();
        int totalScores = allScores.size();

        double highestMarks = 0.0;
        String topScorerName = "N/A";
        String topScorerRoll = "N/A";
        String topScorerExam = "N/A";

        for (ScoreRecord s : allScores) {
            if (s.getMarks() > highestMarks) {
                highestMarks = s.getMarks();
                topScorerName = s.getCandidateName();
                topScorerRoll = s.getRollNumber();
                topScorerExam = s.getExamName();
            }
        }

        Map<String, Object> topScorer = new HashMap<>();
        topScorer.put("marks", highestMarks);
        topScorer.put("candidateName", topScorerName);
        topScorer.put("rollNumber", topScorerRoll);
        topScorer.put("examName", topScorerExam);

        summary.put("totalCandidates", totalCandidates);
        summary.put("totalExaminations", totalExams);
        summary.put("totalScores", totalScores);
        summary.put("highestScore", topScorer);
        summary.put("examinations", exams);

        JsonUtil.sendSuccess(resp, "Dashboard summary retrieved", summary);
    }

    private void handleExamAnalytics(int examId, HttpServletResponse resp) throws IOException {
        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found with ID: " + examId);
            return;
        }

        List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("examination", exam);
        analytics.put("totalCandidates", records.size());

        if (records.isEmpty()) {
            analytics.put("averageMarks", 0.0);
            analytics.put("highestMarks", 0.0);
            analytics.put("lowestMarks", 0.0);
            analytics.put("passPercentage", 0.0);
            analytics.put("failPercentage", 0.0);
            analytics.put("scoreDistribution", Collections.emptyMap());
            analytics.put("departmentPerformance", Collections.emptyMap());
            JsonUtil.sendSuccess(resp, "No score records available for analytics", analytics);
            return;
        }

        double sum = 0.0;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        int passCount = 0;
        double passingThreshold = exam.getMaxMarks() * 0.40; // 40% pass criteria

        // Score Distribution Ranges
        int range0to40 = 0;
        int range40to60 = 0;
        int range60to75 = 0;
        int range75to90 = 0;
        int range90to100 = 0;

        // Department-wise stats: Department -> List of marks
        Map<String, List<Double>> deptMarks = new LinkedHashMap<>();

        for (ScoreRecord r : records) {
            double marks = r.getMarks();
            sum += marks;
            if (marks > max) max = marks;
            if (marks < min) min = marks;
            if (marks >= passingThreshold) passCount++;

            double pct = (exam.getMaxMarks() > 0) ? (marks / exam.getMaxMarks()) * 100.0 : 0.0;
            if (pct < 40.0) range0to40++;
            else if (pct < 60.0) range40to60++;
            else if (pct < 75.0) range60to75++;
            else if (pct < 90.0) range75to90++;
            else range90to100++;

            deptMarks.computeIfAbsent(r.getDepartment(), k -> new ArrayList<>()).add(marks);
        }

        double avg = sum / records.size();
        double passPct = (double) passCount / records.size() * 100.0;
        double failPct = 100.0 - passPct;

        analytics.put("averageMarks", Math.round(avg * 100.0) / 100.0);
        analytics.put("highestMarks", max);
        analytics.put("lowestMarks", min);
        analytics.put("passPercentage", Math.round(passPct * 100.0) / 100.0);
        analytics.put("failPercentage", Math.round(failPct * 100.0) / 100.0);

        // Chart 1: Score distribution buckets
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0% - 39% (Fail)", range0to40);
        distribution.put("40% - 59% (Pass)", range40to60);
        distribution.put("60% - 74% (First Class)", range60to75);
        distribution.put("75% - 89% (Distinction)", range75to90);
        distribution.put("90% - 100% (Outstanding)", range90to100);
        analytics.put("scoreDistribution", distribution);

        // Chart 2: Department-wise averages
        Map<String, Double> deptAverages = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : deptMarks.entrySet()) {
            double dSum = 0.0;
            for (Double d : entry.getValue()) dSum += d;
            double dAvg = dSum / entry.getValue().size();
            deptAverages.put(entry.getKey(), Math.round(dAvg * 100.0) / 100.0);
        }
        analytics.put("departmentPerformance", deptAverages);

        JsonUtil.sendSuccess(resp, "Exam analytics computed", analytics);
    }
}

