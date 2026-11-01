package model;

/**
 * Represents a Score record mapping a Candidate to an Examination with marks obtained.
 */
public class Score {
    private int scoreId;
    private int candidateId;
    private int examId;
    private double marks;

    // Default Constructor
    public Score() {
    }

    // Constructor without scoreId (for new score records)
    public Score(int candidateId, int examId, double marks) {
        this.candidateId = candidateId;
        this.examId = examId;
        this.marks = marks;
    }

    // Constructor with scoreId (when retrieved from database)
    public Score(int scoreId, int candidateId, int examId, double marks) {
        this.scoreId = scoreId;
        this.candidateId = candidateId;
        this.examId = examId;
        this.marks = marks;
    }

    // Getters and Setters
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

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return String.format("Score [ID: %d | Candidate ID: %d | Exam ID: %d | Marks: %.2f]",
                scoreId, candidateId, examId, marks);
    }
}

