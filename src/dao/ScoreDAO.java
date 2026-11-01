package dao;

import database.DBConnection;
import model.Score;
import model.ScoreRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ScoreDAO handles database operations for student examination scores.
 */
public class ScoreDAO {

    /**
     * Records a new score for a candidate in an examination.
     *
     * @param score Score object
     * @return true if recorded, false otherwise
     */
    public boolean addScore(Score score) {
        String sql = "INSERT INTO scores (candidate_id, exam_id, marks) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, score.getCandidateId());
            stmt.setInt(2, score.getExamId());
            stmt.setDouble(3, score.getMarks());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        score.setScoreId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error recording score: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing examination score for a candidate.
     *
     * @param candidateId candidate ID
     * @param examId exam ID
     * @param newMarks updated marks
     * @return true if updated, false otherwise
     */
    public boolean updateScore(int candidateId, int examId, double newMarks) {
        String sql = "UPDATE scores SET marks = ? WHERE candidate_id = ? AND exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newMarks);
            stmt.setInt(2, candidateId);
            stmt.setInt(3, examId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating score: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a score record already exists for candidate and examination.
     *
     * @param candidateId candidate ID
     * @param examId exam ID
     * @return true if score exists, false otherwise
     */
    public boolean scoreExists(int candidateId, int examId) {
        String sql = "SELECT 1 FROM scores WHERE candidate_id = ? AND exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, candidateId);
            stmt.setInt(2, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking score existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a single Score record by candidateId and examId.
     *
     * @param candidateId candidate ID
     * @param examId exam ID
     * @return Score object if found, null otherwise
     */
    public Score getScore(int candidateId, int examId) {
        String sql = "SELECT score_id, candidate_id, exam_id, marks FROM scores WHERE candidate_id = ? AND exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, candidateId);
            stmt.setInt(2, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Score(
                            rs.getInt("score_id"),
                            rs.getInt("candidate_id"),
                            rs.getInt("exam_id"),
                            rs.getDouble("marks")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching score: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all detailed scores recorded for a specific examination.
     * Used by the Max Heap and Ranking System to construct the merit list.
     *
     * @param examId examination ID
     * @return List of ScoreRecord objects
     */
    public List<ScoreRecord> getScoresByExamId(int examId) {
        List<ScoreRecord> records = new ArrayList<>();
        String sql = "SELECT s.score_id, s.candidate_id, c.roll_number, c.name AS candidate_name, " +
                     "c.department, s.exam_id, e.exam_name, e.max_marks, s.marks " +
                     "FROM scores s " +
                     "JOIN candidates c ON s.candidate_id = c.candidate_id " +
                     "JOIN examinations e ON s.exam_id = e.exam_id " +
                     "WHERE s.exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScoreRecord record = new ScoreRecord(
                            rs.getInt("score_id"),
                            rs.getInt("candidate_id"),
                            rs.getString("roll_number"),
                            rs.getString("candidate_name"),
                            rs.getString("department"),
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getDouble("max_marks"),
                            rs.getDouble("marks")
                    );
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching scores for exam ID " + examId + ": " + e.getMessage());
        }
        return records;
    }

    /**
     * Retrieves all scores for a particular candidate by their roll number.
     * Used to display complete student results.
     *
     * @param rollNumber candidate roll number
     * @return List of ScoreRecord objects
     */
    public List<ScoreRecord> getScoresByCandidateRoll(String rollNumber) {
        List<ScoreRecord> records = new ArrayList<>();
        String sql = "SELECT s.score_id, s.candidate_id, c.roll_number, c.name AS candidate_name, " +
                     "c.department, s.exam_id, e.exam_name, e.max_marks, s.marks " +
                     "FROM scores s " +
                     "JOIN candidates c ON s.candidate_id = c.candidate_id " +
                     "JOIN examinations e ON s.exam_id = e.exam_id " +
                     "WHERE c.roll_number = ? ORDER BY e.exam_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScoreRecord record = new ScoreRecord(
                            rs.getInt("score_id"),
                            rs.getInt("candidate_id"),
                            rs.getString("roll_number"),
                            rs.getString("candidate_name"),
                            rs.getString("department"),
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getDouble("max_marks"),
                            rs.getDouble("marks")
                    );
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching scores for candidate: " + e.getMessage());
        }
        return records;
    }

    /**
     * Retrieves all recorded scores across all examinations.
     *
     * @return List of ScoreRecord objects
     */
    public List<ScoreRecord> getAllScores() {
        List<ScoreRecord> records = new ArrayList<>();
        String sql = "SELECT s.score_id, s.candidate_id, c.roll_number, c.name AS candidate_name, " +
                     "c.department, s.exam_id, e.exam_name, e.max_marks, s.marks " +
                     "FROM scores s " +
                     "JOIN candidates c ON s.candidate_id = c.candidate_id " +
                     "JOIN examinations e ON s.exam_id = e.exam_id " +
                     "ORDER BY s.score_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ScoreRecord record = new ScoreRecord(
                        rs.getInt("score_id"),
                        rs.getInt("candidate_id"),
                        rs.getString("roll_number"),
                        rs.getString("candidate_name"),
                        rs.getString("department"),
                        rs.getInt("exam_id"),
                        rs.getString("exam_name"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("marks")
                );
                records.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all scores: " + e.getMessage());
        }
        return records;
    }

    /**
     * Deletes a score record by its primary key score_id.
     *
     * @param scoreId score ID
     * @return true if deleted, false otherwise
     */
    public boolean deleteScore(int scoreId) {
        String sql = "DELETE FROM scores WHERE score_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scoreId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting score: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing score by score_id.
     */
    public boolean updateScoreById(int scoreId, double newMarks) {
        String sql = "UPDATE scores SET marks = ? WHERE score_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newMarks);
            stmt.setInt(2, scoreId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating score by ID: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a ScoreRecord by score_id.
     */
    public ScoreRecord getScoreRecordById(int scoreId) {
        String sql = "SELECT s.score_id, s.candidate_id, c.roll_number, c.name AS candidate_name, " +
                     "c.department, s.exam_id, e.exam_name, e.max_marks, s.marks " +
                     "FROM scores s " +
                     "JOIN candidates c ON s.candidate_id = c.candidate_id " +
                     "JOIN examinations e ON s.exam_id = e.exam_id " +
                     "WHERE s.score_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scoreId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ScoreRecord(
                            rs.getInt("score_id"),
                            rs.getInt("candidate_id"),
                            rs.getString("roll_number"),
                            rs.getString("candidate_name"),
                            rs.getString("department"),
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getDouble("max_marks"),
                            rs.getDouble("marks")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching score record by ID: " + e.getMessage());
        }
        return null;
    }
}

