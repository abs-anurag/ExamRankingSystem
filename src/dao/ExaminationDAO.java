package dao;

import database.DBConnection;
import model.Examination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExaminationDAO handles database CRUD operations for examinations.
 */
public class ExaminationDAO {

    /**
     * Adds a new examination to the database.
     *
     * @param exam Examination to insert
     * @return true if successful, false otherwise
     */
    public boolean addExamination(Examination exam) {
        String sql = "INSERT INTO examinations (exam_name, max_marks) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, exam.getExamName().trim());
            stmt.setDouble(2, exam.getMaxMarks());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exam.setExamId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding examination: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all examinations from the database.
     *
     * @return List of Examination objects
     */
    public List<Examination> getAllExaminations() {
        List<Examination> examinations = new ArrayList<>();
        String sql = "SELECT exam_id, exam_name, max_marks FROM examinations ORDER BY exam_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Examination exam = new Examination(
                        rs.getInt("exam_id"),
                        rs.getString("exam_name"),
                        rs.getDouble("max_marks")
                );
                examinations.add(exam);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving examinations: " + e.getMessage());
        }
        return examinations;
    }

    /**
     * Retrieves an examination by its primary key ID.
     *
     * @param examId exam primary key
     * @return Examination object if found, null otherwise
     */
    public Examination getExaminationById(int examId) {
        String sql = "SELECT exam_id, exam_name, max_marks FROM examinations WHERE exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Examination(
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getDouble("max_marks")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching examination by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if an examination with the given ID exists.
     *
     * @param examId exam primary key
     * @return true if exists, false otherwise
     */
    public boolean examExists(int examId) {
        String sql = "SELECT 1 FROM examinations WHERE exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking exam existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing examination's name and max marks.
     *
     * @param exam Examination with updated details
     * @return true if updated, false otherwise
     */
    public boolean updateExamination(Examination exam) {
        String sql = "UPDATE examinations SET exam_name = ?, max_marks = ? WHERE exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, exam.getExamName().trim());
            stmt.setDouble(2, exam.getMaxMarks());
            stmt.setInt(3, exam.getExamId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating examination: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an examination by its primary key.
     * Associated score records are automatically cascaded.
     *
     * @param examId examination ID
     * @return true if deleted, false otherwise
     */
    public boolean deleteExamination(int examId) {
        String sql = "DELETE FROM examinations WHERE exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting examination: " + e.getMessage());
        }
        return false;
    }
}

