package model;

/**
 * ScoreRecord represents a comprehensive view of a student's score in an examination.
 * It joins Candidate, Examination, and Score data to facilitate Max Heap ranking
 * and merit list generation.
 */
public class ScoreRecord implements Comparable<ScoreRecord> {
    private int scoreId;
    private int candidateId;
    private String rollNumber;
    private String candidateName;
    private String department;
    private int examId;
    private String examName;
    private double maxMarks;
    private double marks;

    public ScoreRecord() {
    }

    public ScoreRecord(int scoreId, int candidateId, String rollNumber, String candidateName,
                       String department, int examId, String examName, double maxMarks, double marks) {
        this.scoreId = scoreId;
        this.candidateId = candidateId;
        this.rollNumber = rollNumber;
        this.candidateName = candidateName;
        this.department = department;
        this.examId = examId;
        this.examName = examName;
        this.maxMarks = maxMarks;
        this.marks = marks;
    }

    public int getScoreId() {
        return scoreId;
    }

    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
    }

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

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    /**
     * Natural ordering for Max Heap:
     * Higher marks have higher priority.
     * Tie-breaker: If marks are identical, alphabetical roll number order
     * provides deterministic heap ordering.
     */
    @Override
    public int compareTo(ScoreRecord other) {
        if (other == null) return 1;
        if (Double.compare(this.marks, other.marks) != 0) {
            return Double.compare(this.marks, other.marks);
        }
        // Consistent tie-breaker using roll number (reversed so smaller roll has priority or lexicographical)
        return other.rollNumber.compareTo(this.rollNumber);
    }

    @Override
    public String toString() {
        return String.format("%-12s %-20s %-25s %6.2f / %-5.1f",
                rollNumber, candidateName, examName, marks, maxMarks);
    }
}

