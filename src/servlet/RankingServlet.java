package servlet;

import dao.ExaminationDAO;
import dsa.RankingSystem;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Examination;
import model.RankedCandidate;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RankingServlet generates merit lists and top-K leaderboards using the custom Max Heap.
 *
 * Endpoints:
 * - GET /api/rankings?examId=...
 * - GET /api/rankings/top?examId=...&limit=...
 */
@WebServlet(name = "RankingServlet", urlPatterns = {"/api/rankings", "/api/rankings/*"})
public class RankingServlet extends HttpServlet {

    private final RankingSystem rankingSystem = new RankingSystem();
    private final ExaminationDAO examDAO = new ExaminationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String examIdParam = req.getParameter("examId");
        String pathInfo = req.getPathInfo();
        String limitParam = req.getParameter("limit");

        if (examIdParam == null || examIdParam.trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter 'examId' is required.");
            return;
        }

        int examId;
        try {
            examId = Integer.parseInt(examIdParam.trim());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid exam ID format.");
            return;
        }

        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found with ID: " + examId);
            return;
        }

        boolean isTopK = (pathInfo != null && pathInfo.equals("/top")) || limitParam != null;

        List<RankedCandidate> rankings;
        int limit = 5; // default top 5

        if (isTopK) {
            if (limitParam != null && !limitParam.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            rankings = rankingSystem.getTopKScorers(examId, limit);
        } else {
            rankings = rankingSystem.getRankedMeritList(examId);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("examination", exam);
        responseData.put("rankings", rankings);
        responseData.put("totalCandidates", rankings.size());
        responseData.put("dataStructure", "Custom Max Heap (O(N) Floyd's buildHeap + extractMax)");
        responseData.put("rankingScheme", "Standard Competition Ranking (1224) - Equal marks receive equal ranks");

        JsonUtil.sendSuccess(resp, isTopK ? "Top " + limit + " scorers retrieved" : "Merit list generated", responseData);
    }
}

