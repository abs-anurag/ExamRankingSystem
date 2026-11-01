package model;

/**
 * Represents a Candidate (Student) enrolled in the examination ranking system.
 */
public class Candidate {
    private int candidateId;
    private String rollNumber;
    private String name;
    private String department;

    // Default Constructor
    public Candidate() {
    }

    // Constructor without candidateId (used when inserting a new candidate)
    public Candidate(String rollNumber, String name, String department) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
    }

    // Constructor with candidateId (used when retrieving from database)
    public Candidate(int candidateId, String rollNumber, String name, String department) {
        this.candidateId = candidateId;
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
    }

    // Getters and Setters
    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("Candidate [ID: %d | Roll: %-10s | Name: %-20s | Dept: %s]",
                candidateId, rollNumber, name, department);
    }
}

