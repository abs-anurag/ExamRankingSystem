package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for parsing and serializing JSON payloads using Gson.
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static Gson getGson() {
        return gson;
    }

    /**
     * Reads the entire HTTP request body as a JSON string and parses into target class.
     */
    public static <T> T fromJson(BufferedReader reader, Class<T> classOfT) {
        return gson.fromJson(reader, classOfT);
    }

    /**
     * Serializes any Java object to a JSON string.
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Writes an object as JSON to the HttpServletResponse.
     */
    public static void writeJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(data));
        response.getWriter().flush();
    }

    /**
     * Writes a standard success JSON response.
     */
    public static void sendSuccess(HttpServletResponse response, String message, Object payload) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        if (payload != null) {
            map.put("data", payload);
        }
        writeJsonResponse(response, HttpServletResponse.SC_OK, map);
    }

    /**
     * Writes a standard error JSON response.
     */
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("error", message);
        writeJsonResponse(response, statusCode, map);
    }
}

