package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Candidate;
import service.CandidateService;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CandidateServlet handles candidate management via RESTful endpoints.
 * Integrates directly with the Custom in-memory HashTable for O(1) roll number lookups.
 *
 * Endpoints:
 * - GET    /api/candidates
 * - GET    /api/candidates/search?rollNumber=
 * - POST   /api/candidates
 * - PUT    /api/candidates/{id} or /api/candidates
 * - DELETE /api/candidates/{id} or /api/candidates?id=
 */
@WebServlet(name = "CandidateServlet", urlPatterns = {"/api/candidates", "/api/candidates/*"})
public class CandidateServlet extends HttpServlet {

    private final CandidateService candidateService = new CandidateService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String rollNumberParam = req.getParameter("rollNumber");

        // Search by Roll Number via Custom HashTable
        if (rollNumberParam != null || (pathInfo != null && pathInfo.equals("/search"))) {
            if (rollNumberParam == null || rollNumberParam.trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Roll number parameter is required.");
                return;
            }

            long startTime = System.nanoTime();
            Candidate candidate = candidateService.searchByRollNumber(rollNumberParam.trim().toUpperCase());
            long endTime = System.nanoTime();
            double elapsedMicros = (endTime - startTime) / 1000.0;

            if (candidate != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("candidate", candidate);
                result.put("lookupTimeMicros", elapsedMicros);
                result.put("dataStructure", "Custom Hash Table with Separate Chaining");
                result.put("timeComplexity", "O(1) average case");
                JsonUtil.sendSuccess(resp, "Candidate found in custom Hash Table", result);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No candidate found with roll number: " + rollNumberParam);
            }
            return;
        }

        // Get single candidate by ID if path is /api/candidates/{id}
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Candidate c = candidateService.getCandidateDAO().getCandidateById(id);
                if (c != null) {
                    JsonUtil.sendSuccess(resp, "Candidate details retrieved", c);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Candidate not found with ID: " + id);
                }
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        // Default: Get all candidates
        List<Candidate> allCandidates = candidateService.getAllCandidates();
        JsonUtil.sendSuccess(resp, "All candidates retrieved", allCandidates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Candidate input = JsonUtil.fromJson(req.getReader(), Candidate.class);

        if (input == null || input.getRollNumber() == null || input.getName() == null || input.getDepartment() == null ||
                input.getRollNumber().trim().isEmpty() || input.getName().trim().isEmpty() || input.getDepartment().trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Roll number, name, and department are required.");
            return;
        }

        String roll = input.getRollNumber().trim().toUpperCase();

        // Check duplicate roll number
        if (candidateService.searchByRollNumber(roll) != null || candidateService.getCandidateDAO().rollNumberExists(roll)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "A candidate with roll number '" + roll + "' already exists.");
            return;
        }

        boolean success = candidateService.addCandidate(roll, input.getName().trim(), input.getDepartment().trim());
        if (success) {
            Candidate created = candidateService.searchByRollNumber(roll);
            JsonUtil.sendSuccess(resp, "Candidate added successfully", created);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create candidate in database.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Candidate input = JsonUtil.fromJson(req.getReader(), Candidate.class);

        int candidateId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                candidateId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (candidateId <= 0 && input != null) {
            candidateId = input.getCandidateId();
        }

        if (candidateId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Candidate ID is required for update.");
            return;
        }

        Candidate existing = candidateService.getCandidateDAO().getCandidateById(candidateId);
        if (existing == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Candidate with ID " + candidateId + " not found.");
            return;
        }

        if (input.getName() != null && !input.getName().trim().isEmpty()) {
            existing.setName(input.getName().trim());
        }
        if (input.getDepartment() != null && !input.getDepartment().trim().isEmpty()) {
            existing.setDepartment(input.getDepartment().trim());
        }

        boolean success = candidateService.getCandidateDAO().updateCandidate(existing);
        if (success) {
            // Synchronize with Hash Table
            candidateService.syncFromDatabase();
            JsonUtil.sendSuccess(resp, "Candidate updated successfully", existing);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update candidate.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int candidateId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                candidateId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (candidateId <= 0) {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                try {
                    candidateId = Integer.parseInt(idParam);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (candidateId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid candidate ID is required for deletion.");
            return;
        }

        Candidate existing = candidateService.getCandidateDAO().getCandidateById(candidateId);
        if (existing == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Candidate not found.");
            return;
        }

        boolean success = candidateService.deleteCandidate(existing.getRollNumber());
        if (success) {
            JsonUtil.sendSuccess(resp, "Candidate and associated scores deleted successfully", null);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete candidate.");
        }
    }
}

