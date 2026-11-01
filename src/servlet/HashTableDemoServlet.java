package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CandidateService;
import util.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * HashTableDemoServlet exposes internal bucket chains, collision details,
 * and load factor of the custom Hash Table for the web UI.
 *
 * Endpoint:
 * - GET /api/dsa/hashtable
 */
@WebServlet(name = "HashTableDemoServlet", urlPatterns = {"/api/dsa/hashtable"})
public class HashTableDemoServlet extends HttpServlet {

    private final CandidateService candidateService = new CandidateService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> snapshot = candidateService.getHashTableDebugSnapshot();
        JsonUtil.sendSuccess(resp, "Hash Table snapshot retrieved", snapshot);
    }
}

