package dsa;

import dao.ExaminationDAO;
import dao.ScoreDAO;
import model.Examination;
import model.RankedCandidate;
import model.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * RankingSystem coordinates examination scores with the custom Max Heap
 * to generate ranked merit lists and top scorer reports.
 *
 * Academic Assessment (ADSA):
 * - Demonstrates practical integration of Max Heap with relational database (JDBC).
 * - Implements Standard Competition Ranking (1224 ranking) for equal marks.
 */
public class RankingSystem {

    private final ScoreDAO scoreDAO;
    private final ExaminationDAO examDAO;

    public RankingSystem() {
        this.scoreDAO = new ScoreDAO();
        this.examDAO = new ExaminationDAO();
    }

    /**
     * Generates a ranked merit list using the Custom Max Heap.
     * Returned as a List of RankedCandidate objects for JSON Web API.
     *
     * @param examId examination ID
     * @return List of RankedCandidate objects
     */
    public List<RankedCandidate> getRankedMeritList(int examId) {
        List<RankedCandidate> rankedList = new ArrayList<>();
        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null) {
            return rankedList;
        }

        List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
        if (records.isEmpty()) {
            return rankedList;
        }

        // 1. Build Custom Max Heap using Floyd's algorithm
        MaxHeap maxHeap = new MaxHeap(records);

        // 2. Extract elements in descending order
        List<ScoreRecord> sortedRecords = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            sortedRecords.add(maxHeap.extractMax());
        }

        // 3. Assign Competition Ranks
        int currentRank = 1;
        for (int i = 0; i < sortedRecords.size(); i++) {
            ScoreRecord curr = sortedRecords.get(i);
            boolean isTie = false;

            if (i > 0) {
                ScoreRecord prev = sortedRecords.get(i - 1);
                if (Double.compare(curr.getMarks(), prev.getMarks()) == 0) {
                    isTie = true;
                } else {
                    currentRank = i + 1;
                }
            } else {
                currentRank = 1;
            }

            // Also check if tied with next candidate
            if (i + 1 < sortedRecords.size()) {
                ScoreRecord next = sortedRecords.get(i + 1);
                if (Double.compare(curr.getMarks(), next.getMarks()) == 0) {
                    isTie = true;
                }
            }

            double percentage = (exam.getMaxMarks() > 0) ? (curr.getMarks() / exam.getMaxMarks()) * 100.0 : 0.0;

            RankedCandidate rc = new RankedCandidate(
                    currentRank,
                    curr.getCandidateId(),
                    curr.getRollNumber(),
                    curr.getCandidateName(),
                    curr.getDepartment(),
                    curr.getExamId(),
                    curr.getExamName(),
                    curr.getMarks(),
                    curr.getMaxMarks(),
                    percentage,
                    isTie
            );
            rankedList.add(rc);
        }
        return rankedList;
    }

    /**
     * Retrieves top K scorers for an examination using the Custom Max Heap.
     *
     * @param examId examination ID
     * @param k limit count (e.g. 5, 10, 20)
     * @return List of RankedCandidate objects
     */
    public List<RankedCandidate> getTopKScorers(int examId, int k) {
        List<RankedCandidate> topKList = new ArrayList<>();
        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null || k <= 0) {
            return topKList;
        }

        List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
        if (records.isEmpty()) {
            return topKList;
        }

        // Build Custom Max Heap
        MaxHeap maxHeap = new MaxHeap(records);

        // Retrieve top K from MaxHeap
        List<ScoreRecord> topK = maxHeap.getTopK(k);

        int currentRank = 1;
        for (int i = 0; i < topK.size(); i++) {
            ScoreRecord curr = topK.get(i);
            boolean isTie = false;

            if (i > 0) {
                ScoreRecord prev = topK.get(i - 1);
                if (Double.compare(curr.getMarks(), prev.getMarks()) == 0) {
                    isTie = true;
                } else {
                    currentRank = i + 1;
                }
            } else {
                currentRank = 1;
            }

            if (i + 1 < topK.size()) {
                ScoreRecord next = topK.get(i + 1);
                if (Double.compare(curr.getMarks(), next.getMarks()) == 0) {
                    isTie = true;
                }
            }

            double percentage = (exam.getMaxMarks() > 0) ? (curr.getMarks() / exam.getMaxMarks()) * 100.0 : 0.0;

            RankedCandidate rc = new RankedCandidate(
                    currentRank,
                    curr.getCandidateId(),
                    curr.getRollNumber(),
                    curr.getCandidateName(),
                    curr.getDepartment(),
                    curr.getExamId(),
                    curr.getExamName(),
                    curr.getMarks(),
                    curr.getMaxMarks(),
                    percentage,
                    isTie
            );
            topKList.add(rc);
        }
        return topKList;
    }

    /**
     * Generates and prints the complete merit list for a given examination.
     * Extracts all elements from the Max Heap in descending order and assigns ranks.
     * Equal marks receive equal ranks.
     *
     * @param examId examination ID
     */
    public void generateCompleteMeritList(int examId) {
        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null) {
            System.out.println("Error: Examination with ID " + examId + " does not exist.");
            return;
        }

        List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
        if (records.isEmpty()) {
            System.out.println("No score records found for examination: " + exam.getExamName());
            return;
        }

        // 1. Build Custom Max Heap using Floyd's O(N) buildHeap algorithm
        MaxHeap maxHeap = new MaxHeap(records);

        // 2. Extract records in descending order
        List<ScoreRecord> sortedRecords = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            sortedRecords.add(maxHeap.extractMax());
        }

        // 3. Print Merit List with Competition Ranking
        System.out.println("\n========== EXAMINATION MERIT LIST ==========");
        System.out.println("Examination : " + exam.getExamName() + " (ID: " + exam.getExamId() + ")");
        System.out.println("Max Marks   : " + formatMarks(exam.getMaxMarks()));
        System.out.println("Candidates  : " + sortedRecords.size());
        System.out.println("--------------------------------------------");
        System.out.printf("%-6s %-13s %-22s %s\n", "Rank", "Roll Number", "Name", "Marks");
        System.out.println("--------------------------------------------");

        int currentRank = 1;
        for (int i = 0; i < sortedRecords.size(); i++) {
            ScoreRecord curr = sortedRecords.get(i);

            if (i > 0) {
                ScoreRecord prev = sortedRecords.get(i - 1);
                if (Double.compare(curr.getMarks(), prev.getMarks()) == 0) {
                    // Equal marks: retain the same rank as previous candidate
                } else {
                    // Score dropped: rank becomes 1-based index
                    currentRank = i + 1;
                }
            } else {
                currentRank = 1;
            }

            System.out.printf("%-6d %-13s %-22s %s\n",
                    currentRank,
                    curr.getRollNumber(),
                    curr.getCandidateName(),
                    formatMarks(curr.getMarks()));
        }

        System.out.println("============================================\n");
    }

    /**
     * Displays the Top K scorers for a specific examination using the Max Heap.
     *
     * @param examId examination ID
     * @param k number of top positions to display (e.g. 5)
     */
    public void displayTopKScorers(int examId, int k) {
        Examination exam = examDAO.getExaminationById(examId);
        if (exam == null) {
            System.out.println("Error: Examination with ID " + examId + " does not exist.");
            return;
        }

        List<ScoreRecord> records = scoreDAO.getScoresByExamId(examId);
        if (records.isEmpty()) {
            System.out.println("No scores recorded yet for: " + exam.getExamName());
            return;
        }

        // Build Max Heap
        MaxHeap maxHeap = new MaxHeap(records);

        // Retrieve Top K elements using custom MaxHeap method
        List<ScoreRecord> topK = maxHeap.getTopK(k);

        System.out.println("\n============== TOP " + topK.size() + " SCORERS ==============");
        System.out.println("Examination: " + exam.getExamName());
        System.out.println("Max Marks  : " + formatMarks(exam.getMaxMarks()));
        System.out.println("--------------------------------------------");
        System.out.printf("%-6s %-13s %-22s %s\n", "Rank", "Roll Number", "Name", "Marks");
        System.out.println("--------------------------------------------");

        int currentRank = 1;
        for (int i = 0; i < topK.size(); i++) {
            ScoreRecord curr = topK.get(i);
            if (i > 0) {
                ScoreRecord prev = topK.get(i - 1);
                if (Double.compare(curr.getMarks(), prev.getMarks()) == 0) {
                    // Tie: same rank
                } else {
                    currentRank = i + 1;
                }
            } else {
                currentRank = 1;
            }

            System.out.printf("%-6d %-13s %-22s %s\n",
                    currentRank,
                    curr.getRollNumber(),
                    curr.getCandidateName(),
                    formatMarks(curr.getMarks()));
        }

        System.out.println("============================================\n");
    }

    /**
     * Helper to display marks cleanly (omits trailing .0 for whole numbers).
     */
    private String formatMarks(double marks) {
        if (marks == Math.floor(marks)) {
            return String.format("%d", (long) marks);
        } else {
            return String.format("%.2f", marks);
        }
    }
}

