package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Web Integration Test Suite for ExamRank.
 * Executes live HTTP requests against the local Apache Tomcat server (http://localhost:8080).
 */
public class WebIntegrationTestSuite {

    private static final String BASE_URL = "http://localhost:8080";
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static String sessionCookie = "";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   EXAMRANK WEB API INTEGRATION TEST SUITE       ");
        System.out.println("=================================================");

        testStaticPages();
        testAuthentication();
        testCandidateEndpoints();
        testHashTableSearch();
        testExaminationEndpoints();
        testRankingMaxHeapEndpoints();
        testAnalyticsEndpoints();
        testHashTableDemoEndpoint();

        System.out.println("\n=================================================");
        System.out.printf("   TEST SUMMARY: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testStaticPages() {
        System.out.println("\n--- Testing Static Web Pages ---");
        int loginStatus = sendGet("/login.html").statusCode;
        assertTrue(loginStatus == 200, "GET /login.html returns HTTP 200");

        int dashStatus = sendGet("/pages/dashboard.html").statusCode;
        assertTrue(dashStatus == 200, "GET /pages/dashboard.html returns HTTP 200");

        int meritStatus = sendGet("/pages/merit-list.html").statusCode;
        assertTrue(meritStatus == 200, "GET /pages/merit-list.html returns HTTP 200");
    }

    private static void testAuthentication() {
        System.out.println("\n--- Testing Authentication Endpoints ---");

        // 1. Invalid Login
        HttpResponse resBad = sendPost("/api/login", "{\"username\":\"admin\",\"password\":\"wrongpassword\"}");
        assertTrue(resBad.statusCode == 401, "POST /api/login with invalid password returns 401 Unauthorized");

        // 2. Valid Login
        HttpResponse resOk = sendPost("/api/login", "{\"username\":\"admin\",\"password\":\"admin123\"}");
        assertTrue(resOk.statusCode == 200, "POST /api/login with valid admin credentials returns 200 OK");
        assertTrue(resOk.body.contains("\"role\":\"ADMIN\""), "Response payload contains ADMIN role");

        if (resOk.cookie != null && !resOk.cookie.isEmpty()) {
            sessionCookie = resOk.cookie.split(";")[0];
        }

        // 3. Auth Me check
        HttpResponse resMe = sendGetWithCookie("/api/auth/me", sessionCookie);
        assertTrue(resMe.statusCode == 200 && resMe.body.contains("System Administrator"), "GET /api/auth/me returns current session user");
    }

    private static void testCandidateEndpoints() {
        System.out.println("\n--- Testing Candidate Endpoints ---");
        HttpResponse res = sendGet("/api/candidates");
        assertTrue(res.statusCode == 200, "GET /api/candidates returns HTTP 200");
        assertTrue(res.body.contains("25BCS001"), "Response contains candidate 25BCS001 (Rahul Sharma)");
    }

    private static void testHashTableSearch() {
        System.out.println("\n--- Testing Custom Hash Table Roll Lookup ---");
        HttpResponse res = sendGet("/api/candidates/search?rollNumber=25BCS001");
        assertTrue(res.statusCode == 200, "GET /api/candidates/search returns HTTP 200");
        assertTrue(res.body.contains("Custom Hash Table with Separate Chaining"), "Uses Custom Hash Table data structure");
        assertTrue(res.body.contains("Rahul Sharma"), "Candidate name correctly resolved");
    }

    private static void testExaminationEndpoints() {
        System.out.println("\n--- Testing Examination Endpoints ---");
        HttpResponse res = sendGet("/api/examinations");
        assertTrue(res.statusCode == 200, "GET /api/examinations returns HTTP 200");
        assertTrue(res.body.contains("Algorithms and Data Structures"), "Contains ADSA examination");
    }

    private static void testRankingMaxHeapEndpoints() {
        System.out.println("\n--- Testing Ranking & Custom Max Heap Endpoints ---");

        // Merit list
        HttpResponse resMerit = sendGet("/api/rankings?examId=1");
        assertTrue(resMerit.statusCode == 200, "GET /api/rankings?examId=1 returns HTTP 200");
        assertTrue(resMerit.body.contains("Custom Max Heap"), "Rankings verified generated by Custom Max Heap");
        assertTrue(resMerit.body.contains("Standard Competition Ranking"), "Uses 1224 competition tie ranking");

        // Top-5 scorers
        HttpResponse resTop = sendGet("/api/rankings/top?examId=1&limit=5");
        assertTrue(resTop.statusCode == 200, "GET /api/rankings/top?examId=1&limit=5 returns HTTP 200");
        assertTrue(resTop.body.contains("\"totalCandidates\":5"), "Top 5 scorers limit enforced");
    }

    private static void testAnalyticsEndpoints() {
        System.out.println("\n--- Testing Analytics Endpoints ---");
        HttpResponse res = sendGet("/api/analytics?examId=1");
        assertTrue(res.statusCode == 200, "GET /api/analytics?examId=1 returns HTTP 200");
        assertTrue(res.body.contains("scoreDistribution"), "Analytics includes score distribution buckets for Chart.js");
        assertTrue(res.body.contains("departmentPerformance"), "Analytics includes department performance for Chart.js");

        HttpResponse resDash = sendGet("/api/analytics/dashboard");
        assertTrue(resDash.statusCode == 200, "GET /api/analytics/dashboard returns HTTP 200");
    }

    private static void testHashTableDemoEndpoint() {
        System.out.println("\n--- Testing Hash Table ADSA Demo Endpoint ---");
        HttpResponse res = sendGet("/api/dsa/hashtable");
        assertTrue(res.statusCode == 200, "GET /api/dsa/hashtable returns HTTP 200");
        assertTrue(res.body.contains("loadFactor"), "Hash Table snapshot contains load factor");
        assertTrue(res.body.contains("buckets"), "Hash Table snapshot contains bucket chains array");
    }

    // HTTP Helper Utilities
    private static class HttpResponse {
        int statusCode;
        String body;
        String cookie;
    }

    private static HttpResponse sendGet(String endpoint) {
        return sendGetWithCookie(endpoint, null);
    }

    private static HttpResponse sendGetWithCookie(String endpoint, String cookie) {
        HttpResponse response = new HttpResponse();
        try {
            URL url = URI.create(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            response.statusCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    response.statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();
            response.body = sb.toString();
        } catch (Exception e) {
            response.statusCode = 500;
            response.body = e.getMessage();
        }
        return response;
    }

    private static HttpResponse sendPost(String endpoint, String jsonBody) {
        HttpResponse response = new HttpResponse();
        try {
            URL url = URI.create(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            response.statusCode = conn.getResponseCode();
            response.cookie = conn.getHeaderField("Set-Cookie");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    response.statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();
            response.body = sb.toString();
        } catch (Exception e) {
            response.statusCode = 500;
            response.body = e.getMessage();
        }
        return response;
    }
}

