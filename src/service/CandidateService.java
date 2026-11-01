package service;

import dao.CandidateDAO;
import dsa.HashTable;
import model.Candidate;

import java.util.List;

/**
 * CandidateService coordinates business logic between the CandidateDAO (MySQL)
 * and the custom in-memory HashTable data structure.
 *
 * Academic Assessment (ADSA):
 * - Demonstrates in-memory caching and fast O(1) lookups using custom HashTable.
 * - Synchronizes state between MySQL persistent storage and in-memory hash buckets.
 */
public class CandidateService {

    private final CandidateDAO candidateDAO;
    private final HashTable candidateHashTable;

    public CandidateService() {
        this.candidateDAO = new CandidateDAO();
        this.candidateHashTable = new HashTable(16);
        syncFromDatabase();
    }

    /**
     * Loads all existing candidates from MySQL into the in-memory custom HashTable.
     */
    public void syncFromDatabase() {
        candidateHashTable.clear();
        List<Candidate> allCandidates = candidateDAO.getAllCandidates();
        for (Candidate c : allCandidates) {
            candidateHashTable.insert(c.getRollNumber(), c);
        }
    }

    /**
     * Registers a new candidate.
     * Prevents duplicate roll numbers, writes to MySQL, and caches in HashTable.
     *
     * @param rollNumber unique roll number
     * @param name student name
     * @param department student department
     * @return true if successful, false otherwise
     */
    public boolean addCandidate(String rollNumber, String name, String department) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            System.out.println("Error: Roll number cannot be empty.");
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty.");
            return false;
        }
        if (department == null || department.trim().isEmpty()) {
            System.out.println("Error: Department cannot be empty.");
            return false;
        }

        rollNumber = rollNumber.trim().toUpperCase();

        // 1. Check duplicate roll number in Hash Table (O(1))
        if (candidateHashTable.search(rollNumber) != null || candidateDAO.rollNumberExists(rollNumber)) {
            System.out.println("Error: Candidate with Roll Number '" + rollNumber + "' already exists!");
            return false;
        }

        Candidate newCandidate = new Candidate(rollNumber, name.trim(), department.trim());

        // 2. Persist to MySQL via JDBC
        boolean dbSuccess = candidateDAO.addCandidate(newCandidate);
        if (dbSuccess) {
            // 3. Cache into custom Hash Table
            candidateHashTable.insert(newCandidate.getRollNumber(), newCandidate);
            System.out.println("Success: Candidate added with ID " + newCandidate.getCandidateId());
            return true;
        } else {
            System.out.println("Error: Failed to insert candidate into database.");
            return false;
        }
    }

    /**
     * Searches for a candidate by roll number using the custom Hash Table.
     * Demonstrates O(1) average search efficiency.
     *
     * @param rollNumber roll number to look up
     * @return Candidate object or null
     */
    public Candidate searchByRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return null;
        }
        rollNumber = rollNumber.trim().toUpperCase();

        // Search directly in custom HashTable
        long startTime = System.nanoTime();
        Candidate found = candidateHashTable.search(rollNumber);
        long endTime = System.nanoTime();

        if (found != null) {
            System.out.printf("[ADSA] Candidate located in custom Hash Table in %.3f microseconds (O(1) lookup).\n",
                    (endTime - startTime) / 1000.0);
            return found;
        }

        // Fallback check in DB in case of cache desync
        Candidate fromDb = candidateDAO.getCandidateByRollNumber(rollNumber);
        if (fromDb != null) {
            candidateHashTable.insert(fromDb.getRollNumber(), fromDb);
            return fromDb;
        }

        return null;
    }

    /**
     * Updates an existing candidate's name and department.
     */
    public boolean updateCandidate(String rollNumber, String newName, String newDept) {
        Candidate existing = searchByRollNumber(rollNumber);
        if (existing == null) {
            System.out.println("Error: Candidate with Roll Number '" + rollNumber + "' not found.");
            return false;
        }

        if (newName != null && !newName.trim().isEmpty()) {
            existing.setName(newName.trim());
        }
        if (newDept != null && !newDept.trim().isEmpty()) {
            existing.setDepartment(newDept.trim());
        }

        boolean dbSuccess = candidateDAO.updateCandidate(existing);
        if (dbSuccess) {
            candidateHashTable.insert(existing.getRollNumber(), existing);
            System.out.println("Success: Candidate details updated successfully.");
            return true;
        } else {
            System.out.println("Error: Failed to update candidate in database.");
            return false;
        }
    }

    /**
     * Deletes a candidate by their roll number.
     */
    public boolean deleteCandidate(String rollNumber) {
        Candidate existing = searchByRollNumber(rollNumber);
        if (existing == null) {
            System.out.println("Error: Candidate with Roll Number '" + rollNumber + "' not found.");
            return false;
        }

        boolean dbSuccess = candidateDAO.deleteCandidate(existing.getCandidateId());
        if (dbSuccess) {
            candidateHashTable.delete(existing.getRollNumber());
            System.out.println("Success: Candidate " + existing.getRollNumber() + " (" + existing.getName() + ") deleted.");
            return true;
        } else {
            System.out.println("Error: Failed to delete candidate from database.");
            return false;
        }
    }

    /**
     * Returns all candidates from MySQL.
     */
    public List<Candidate> getAllCandidates() {
        return candidateDAO.getAllCandidates();
    }

    /**
     * Displays internal Hash Table structure for ADSA assessment demo.
     */
    public void displayHashTableStructure() {
        candidateHashTable.display();
    }

    /**
     * Returns internal Hash Table structure snapshot for ADSA Web API visualization.
     */
    public java.util.Map<String, Object> getHashTableDebugSnapshot() {
        return candidateHashTable.getDebugSnapshot();
    }

    public CandidateDAO getCandidateDAO() {
        return candidateDAO;
    }
}

