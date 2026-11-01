package servlet;

import dao.CandidateDAO;
import dao.ExaminationDAO;
import dao.ScoreDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Candidate;
import model.Examination;
import model.Score;
import model.ScoreRecord;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;

/**
 * ScoreServlet manages examination marks recording, viewing, and updates.
 *
 * Endpoints:
 * - GET    /api/scores (optional ?examId=... or ?rollNumber=...)
 * - POST   /api/scores
 * - PUT    /api/scores/{id} or /api/scores
 * - DELETE /api/scores/{id} or /api/scores?id=...
 */
@WebServlet(name = "ScoreServlet", urlPatterns = {"/api/scores", "/api/scores/*"})
public class ScoreServlet extends HttpServlet {

    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String examIdParam = req.getParameter("examId");
        String rollNumberParam = req.getParameter("rollNumber");

        if (rollNumberParam != null && !rollNumberParam.trim().isEmpty()) {
            List<ScoreRecord> records = scoreDAO.getScoresByCandidateRoll(rollNumberParam.trim().toUpperCase());
            JsonUtil.sendSuccess(resp, "Candidate score records retrieved", records);
            return;
        }

        if (examIdParam != null && !examIdParam.trim().isEmpty()) {
            try {
                int examId = Integer.parseInt(examIdParam.trim());
                List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
                JsonUtil.sendSuccess(resp, "Exam score records retrieved", records);
                return;
            } catch (NumberFormatException e) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid exam ID format.");
                return;
            }
        }

        // Return all scores
        List<ScoreRecord> all = scoreDAO.getAllScores();
        JsonUtil.sendSuccess(resp, "All scores retrieved", all);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ScoreInput input = JsonUtil.fromJson(req.getReader(), ScoreInput.class);

        if (input == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        // Determine candidate
        Candidate candidate = null;
        if (input.candidateId > 0) {
            candidate = candidateDAO.getCandidateById(input.candidateId);
        } else if (input.rollNumber != null && !input.rollNumber.trim().isEmpty()) {
            candidate = candidateDAO.getCandidateByRollNumber(input.rollNumber.trim().toUpperCase());
        }

        if (candidate == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Specified candidate does not exist.");
            return;
        }

        // Determine examination
        Examination exam = examDAO.getExaminationById(input.examId);
        if (exam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Specified examination does not exist.");
            return;
        }

        // Validate Marks range
        if (input.marks < 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Marks cannot be negative.");
            return;
        }

        if (input.marks > exam.getMaxMarks()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Marks (%.2f) cannot exceed examination maximum marks (%.1f).", input.marks, exam.getMaxMarks()));
            return;
        }

        // Check for duplicate score
        if (scoreDAO.scoreExists(candidate.getCandidateId(), exam.getExamId())) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT,
                    "Score already recorded for this candidate in this examination. Use update instead.");
            return;
        }

        Score score = new Score(candidate.getCandidateId(), exam.getExamId(), input.marks);
        boolean success = scoreDAO.addScore(score);
        if (success) {
            ScoreRecord record = scoreDAO.getScoreRecordById(score.getScoreId());
            JsonUtil.sendSuccess(resp, "Score recorded successfully", record);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to record score in database.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ScoreInput input = JsonUtil.fromJson(req.getReader(), ScoreInput.class);

        int scoreId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                scoreId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (scoreId <= 0 && input != null) {
            scoreId = input.scoreId;
        }

        if (scoreId <= 0 && input != null && input.candidateId > 0 && input.examId > 0) {
            // Update by candidateId and examId
            Examination exam = examDAO.getExaminationById(input.examId);
            if (exam == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found.");
                return;
            }
            if (input.marks < 0 || input.marks > exam.getMaxMarks()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Marks out of range [0, " + exam.getMaxMarks() + "].");
                return;
            }
            boolean success = scoreDAO.updateScore(input.candidateId, input.examId, input.marks);
            if (success) {
                JsonUtil.sendSuccess(resp, "Score updated successfully", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update score.");
            }
            return;
        }

        if (scoreId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid score ID is required.");
            return;
        }

        ScoreRecord existing = scoreDAO.getScoreRecordById(scoreId);
        if (existing == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Score record not found with ID: " + scoreId);
            return;
        }

        if (input.marks < 0 || input.marks > existing.getMaxMarks()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Marks (%.2f) must be between 0 and maximum marks (%.1f).", input.marks, existing.getMaxMarks()));
            return;
        }

        boolean success = scoreDAO.updateScoreById(scoreId, input.marks);
        if (success) {
            existing.setMarks(input.marks);
            JsonUtil.sendSuccess(resp, "Score updated successfully", existing);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update score.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int scoreId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                scoreId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (scoreId <= 0) {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                try {
                    scoreId = Integer.parseInt(idParam);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (scoreId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Score ID is required for deletion.");
            return;
        }

        boolean success = scoreDAO.deleteScore(scoreId);
        if (success) {
            JsonUtil.sendSuccess(resp, "Score record deleted successfully", null);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete score record.");
        }
    }

    private static class ScoreInput {
        int scoreId;
        int candidateId;
        String rollNumber;
        int examId;
        double marks;
    }
}

