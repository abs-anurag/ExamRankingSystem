package dao;

import database.DBConnection;
import model.Candidate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CandidateDAO handles all CRUD database interactions for Candidate entities.
 * Implements JDBC best practices using PreparedStatement and try-with-resources.
 */
public class CandidateDAO {

    /**
     * Inserts a new candidate into the database.
     *
     * @param candidate Candidate to add
     * @return true if inserted successfully, false otherwise
     */
    public boolean addCandidate(Candidate candidate) {
        String sql = "INSERT INTO candidates (roll_number, name, department) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, candidate.getRollNumber().trim());
            stmt.setString(2, candidate.getName().trim());
            stmt.setString(3, candidate.getDepartment().trim());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        candidate.setCandidateId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding candidate: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all candidates from the database, ordered by roll number.
     *
     * @return List of Candidate objects
     */
    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT candidate_id, roll_number, name, department FROM candidates ORDER BY roll_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Candidate c = new Candidate(
                        rs.getInt("candidate_id"),
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("department")
                );
                candidates.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving candidates: " + e.getMessage());
        }
        return candidates;
    }

    /**
     * Finds a candidate by their unique roll number.
     *
     * @param rollNumber student roll number
     * @return Candidate object if found, null otherwise
     */
    public Candidate getCandidateByRollNumber(String rollNumber) {
        String sql = "SELECT candidate_id, roll_number, name, department FROM candidates WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getString("roll_number"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching candidate by roll number: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a candidate by candidate_id primary key.
     *
     * @param candidateId candidate primary key
     * @return Candidate object if found, null otherwise
     */
    public Candidate getCandidateById(int candidateId) {
        String sql = "SELECT candidate_id, roll_number, name, department FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, candidateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getString("roll_number"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding candidate by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a candidate with the given roll number already exists.
     *
     * @param rollNumber roll number to verify
     * @return true if roll number exists, false otherwise
     */
    public boolean rollNumberExists(String rollNumber) {
        String sql = "SELECT 1 FROM candidates WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking roll number existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing candidate's name and department.
     *
     * @param candidate Candidate with updated details
     * @return true if updated, false otherwise
     */
    public boolean updateCandidate(Candidate candidate) {
        String sql = "UPDATE candidates SET name = ?, department = ? WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, candidate.getName().trim());
            stmt.setString(2, candidate.getDepartment().trim());
            stmt.setInt(3, candidate.getCandidateId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating candidate: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a candidate by their candidate_id.
     * Associated scores are automatically cascaded by the foreign key constraint.
     *
     * @param candidateId candidate primary key
     * @return true if deleted, false otherwise
     */
    public boolean deleteCandidate(int candidateId) {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, candidateId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting candidate: " + e.getMessage());
        }
        return false;
    }
}

