package model;

/**
 * Represents an Examination conducted in the ranking system.
 */
public class Examination {
    private int examId;
    private String examName;
    private double maxMarks;

    // Default Constructor
    public Examination() {
    }

    // Constructor without examId (for new examinations)
    public Examination(String examName, double maxMarks) {
        this.examName = examName;
        this.maxMarks = maxMarks;
    }

    // Constructor with examId (when fetched from database)
    public Examination(int examId, String examName, double maxMarks) {
        this.examId = examId;
        this.examName = examName;
        this.maxMarks = maxMarks;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return String.format("Examination [ID: %d | Name: %-30s | Max Marks: %.1f]",
                examId, examName, maxMarks);
    }
}

