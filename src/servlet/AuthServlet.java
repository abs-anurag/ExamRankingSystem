package servlet;

import dao.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthServlet handles user login, logout, and session state verification.
 * Endpoints:
 * - POST /api/login
 * - POST /api/logout
 * - GET  /api/auth/me
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/login", "/api/logout", "/api/auth/me"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if ("/api/auth/me".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                JsonUtil.sendSuccess(resp, "Authenticated", user);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();

        if ("/api/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/api/logout".equals(path)) {
            handleLogout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest loginReq = JsonUtil.fromJson(req.getReader(), LoginRequest.class);

        if (loginReq == null || loginReq.username == null || loginReq.password == null ||
                loginReq.username.trim().isEmpty() || loginReq.password.trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        User user = userDAO.authenticate(loginReq.username, loginReq.password);
        if (user != null) {
            // Create session
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", user.getUserId());
            responseData.put("username", user.getUsername());
            responseData.put("fullName", user.getFullName());
            responseData.put("role", user.getRole());

            JsonUtil.sendSuccess(resp, "Login successful", responseData);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.sendSuccess(resp, "Logged out successfully", null);
    }

    private static class LoginRequest {
        String username;
        String password;
    }
}

