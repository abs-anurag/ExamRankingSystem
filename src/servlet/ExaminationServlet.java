package servlet;

import dao.ExaminationDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Examination;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;

/**
 * ExaminationServlet handles examination CRUD endpoints.
 * Endpoints:
 * - GET    /api/examinations
 * - GET    /api/examinations/{id}
 * - POST   /api/examinations
 * - PUT    /api/examinations/{id} or /api/examinations
 * - DELETE /api/examinations/{id} or /api/examinations?id=
 */
@WebServlet(name = "ExaminationServlet", urlPatterns = {"/api/examinations", "/api/examinations/*"})
public class ExaminationServlet extends HttpServlet {

    private final ExaminationDAO examinationDAO = new ExaminationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int examId = Integer.parseInt(pathInfo.substring(1));
                Examination exam = examinationDAO.getExaminationById(examId);
                if (exam != null) {
                    JsonUtil.sendSuccess(resp, "Examination retrieved", exam);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found with ID: " + examId);
                }
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        List<Examination> exams = examinationDAO.getAllExaminations();
        JsonUtil.sendSuccess(resp, "All examinations retrieved", exams);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Examination input = JsonUtil.fromJson(req.getReader(), Examination.class);

        if (input == null || input.getExamName() == null || input.getExamName().trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Examination name cannot be empty.");
            return;
        }

        if (input.getMaxMarks() <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Maximum marks must be strictly greater than 0.");
            return;
        }

        Examination exam = new Examination(input.getExamName().trim(), input.getMaxMarks());
        boolean success = examinationDAO.addExamination(exam);
        if (success) {
            JsonUtil.sendSuccess(resp, "Examination created successfully", exam);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create examination.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Examination input = JsonUtil.fromJson(req.getReader(), Examination.class);

        int examId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                examId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (examId <= 0 && input != null) {
            examId = input.getExamId();
        }

        if (examId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid Examination ID is required for update.");
            return;
        }

        Examination existing = examinationDAO.getExaminationById(examId);
        if (existing == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found with ID: " + examId);
            return;
        }

        if (input != null) {
            if (input.getExamName() != null && !input.getExamName().trim().isEmpty()) {
                existing.setExamName(input.getExamName().trim());
            }
            if (input.getMaxMarks() > 0) {
                existing.setMaxMarks(input.getMaxMarks());
            }
        }

        boolean success = examinationDAO.updateExamination(existing);
        if (success) {
            JsonUtil.sendSuccess(resp, "Examination updated successfully", existing);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update examination.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int examId = 0;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                examId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (examId <= 0) {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                try {
                    examId = Integer.parseInt(idParam);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (examId <= 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid Examination ID is required for deletion.");
            return;
        }

        if (!examinationDAO.examExists(examId)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Examination not found with ID: " + examId);
            return;
        }

        boolean success = examinationDAO.deleteExamination(examId);
        if (success) {
            JsonUtil.sendSuccess(resp, "Examination and all associated scores deleted successfully", null);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete examination.");
        }
    }
}

