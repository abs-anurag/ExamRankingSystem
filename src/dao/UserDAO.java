package dao;

import database.DBConnection;
import model.User;
import util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO handles authentication and user persistence in MySQL.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and password.
     * Verifies the password using SHA-256 hash comparison.
     *
     * @param username username string
     * @param plainPassword plaintext password
     * @return User object without password hash if authenticated, null otherwise
     */
    public User authenticate(String username, String plainPassword) {
        if (username == null || plainPassword == null) {
            return null;
        }

        String sql = "SELECT user_id, username, password_hash, full_name, role, created_at FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtil.verifyPassword(plainPassword, storedHash)) {
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user by their username.
     */
    public User getUserByUsername(String username) {
        if (username == null) return null;
        String sql = "SELECT user_id, username, full_name, role, created_at FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
}

