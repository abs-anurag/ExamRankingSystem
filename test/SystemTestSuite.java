package test;

import dao.CandidateDAO;
import dao.ExaminationDAO;
import dao.ScoreDAO;
import database.DBConnection;
import dsa.HashTable;
import dsa.MaxHeap;
import dsa.RankingSystem;
import model.Candidate;
import model.Examination;
import model.Score;
import model.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated Test Suite for Online Examination Ranking System.
 * Thoroughly verifies custom MaxHeap, custom HashTable, DAOs, and Ranking
 * logic.
 */
public class SystemTestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   ONLINE EXAMINATION RANKING SYSTEM TEST SUITE  ");
        System.out.println("=================================================");

        testDatabaseConnection();
        testCustomHashTable();
        testCustomMaxHeap();
        testTieBreakingAndRanking();
        testDAOs();

        System.out.println("\n=================================================");
        System.out.printf("   TEST SUMMARY: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: <" + expected + ">, Got: <" + actual + ">");
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: TRUE, Got: FALSE");
            testsFailed++;
        }
    }

    // -------------------------------------------------------------------------
    // 1. Database Connection Test
    // -------------------------------------------------------------------------
    private static void testDatabaseConnection() {
        System.out.println("\n--- Testing Database Connectivity ---");
        boolean connected = DBConnection.testConnection();
        assertTrue(connected, "MySQL Database Connection established successfully");
    }

    // -------------------------------------------------------------------------
    // 2. Custom HashTable Test
    // -------------------------------------------------------------------------
    private static void testCustomHashTable() {
        System.out.println("\n--- Testing Custom HashTable (Separate Chaining) ---");
        HashTable table = new HashTable(4); // Small initial capacity to force collisions and rehash

        Candidate c1 = new Candidate(1, "25BCS001", "Rahul", "CSE");
        Candidate c2 = new Candidate(2, "25BCS002", "Priya", "CSE");
        Candidate c3 = new Candidate(3, "25BCS003", "Arjun", "IT");
        Candidate c4 = new Candidate(4, "25BCS004", "Karan", "CSE");
        Candidate c5 = new Candidate(5, "25BCS005", "Ananya", "AIDS");

        // Insert operations
        table.insert(c1.getRollNumber(), c1);
        table.insert(c2.getRollNumber(), c2);
        table.insert(c3.getRollNumber(), c3);
        table.insert(c4.getRollNumber(), c4);
        table.insert(c5.getRollNumber(), c5);

        assertEquals(5, table.size(), "HashTable contains 5 elements after insertions");

        // Search operations
        Candidate found = table.search("25BCS001");
        assertTrue(found != null && found.getName().equals("Rahul"), "HashTable.search existing key 25BCS001");

        Candidate foundCaseInsensitive = table.search("25bcs001");
        assertTrue(foundCaseInsensitive != null && foundCaseInsensitive.getName().equals("Rahul"),
                "HashTable.search case-insensitive key");

        Candidate notFound = table.search("NON_EXISTENT_KEY");
        assertEquals(null, notFound, "HashTable.search non-existent key returns null");

        // Update existing key
        Candidate updatedC1 = new Candidate(1, "25BCS001", "Rahul Sharma", "CSE");
        table.insert("25BCS001", updatedC1);
        assertEquals(5, table.size(), "HashTable.insert with existing key updates without increasing size");
        assertEquals("Rahul Sharma", table.search("25BCS001").getName(), "HashTable value updated correctly");

        // Delete operation
        boolean deleted = table.delete("25BCS003");
        assertTrue(deleted, "HashTable.delete existing key returns true");
        assertEquals(4, table.size(), "HashTable size decremented to 4");
        assertEquals(null, table.search("25BCS003"), "Deleted key can no longer be found");

        boolean deleteMissing = table.delete("25BCS999");
        assertTrue(!deleteMissing, "HashTable.delete non-existent key returns false");
    }

    // -------------------------------------------------------------------------
    // 3. Custom MaxHeap Test
    // -------------------------------------------------------------------------
    private static void testCustomMaxHeap() {
        System.out.println("\n--- Testing Custom MaxHeap ---");
        MaxHeap heap = new MaxHeap();

        ScoreRecord s1 = new ScoreRecord(1, 1, "25BCS001", "Rahul", "CSE", 1, "ADSA", 100, 75.0);
        ScoreRecord s2 = new ScoreRecord(2, 2, "25BCS002", "Priya", "CSE", 1, "ADSA", 100, 95.0);
        ScoreRecord s3 = new ScoreRecord(3, 3, "25BCS003", "Arjun", "IT", 1, "ADSA", 100, 85.0);
        ScoreRecord s4 = new ScoreRecord(4, 4, "25BCS004", "Karan", "CSE", 1, "ADSA", 100, 60.0);
        ScoreRecord s5 = new ScoreRecord(5, 5, "25BCS005", "Ananya", "AIDS", 1, "ADSA", 100, 99.0);

        // Test insert and heapifyUp
        heap.insert(s1);
        heap.insert(s2);
        heap.insert(s3);
        heap.insert(s4);
        heap.insert(s5);

        assertEquals(5, heap.size(), "MaxHeap size is 5 after insertions");
        assertEquals("Ananya", heap.getMax().getCandidateName(), "Max element is Ananya with 99.0 marks");

        // Test extractMax
        ScoreRecord max1 = heap.extractMax();
        assertEquals("Ananya", max1.getCandidateName(), "First extractMax returns 99.0 (Ananya)");
        assertEquals("Priya", heap.getMax().getCandidateName(), "Next root is Priya with 95.0 marks");

        ScoreRecord max2 = heap.extractMax();
        assertEquals("Priya", max2.getCandidateName(), "Second extractMax returns 95.0 (Priya)");

        // Test buildHeap (Floyd's algorithm)
        List<ScoreRecord> list = new ArrayList<>();
        list.add(s1); // 75
        list.add(s2); // 95
        list.add(s3); // 85
        list.add(s4); // 60
        list.add(s5); // 99

        MaxHeap floydHeap = new MaxHeap(list);
        assertEquals(5, floydHeap.size(), "buildHeap created heap of size 5");
        assertEquals(99.0, floydHeap.getMax().getMarks(), "buildHeap correctly placed maximum at root");

        // Test getTopK without mutating heap
        List<ScoreRecord> top3 = floydHeap.getTopK(3);
        assertEquals(3, top3.size(), "getTopK(3) returns 3 records");
        assertEquals(99.0, top3.get(0).getMarks(), "Top 1 is 99.0");
        assertEquals(95.0, top3.get(1).getMarks(), "Top 2 is 95.0");
        assertEquals(85.0, top3.get(2).getMarks(), "Top 3 is 85.0");
        assertEquals(5, floydHeap.size(), "Original heap size remains intact after getTopK");
    }

    // -------------------------------------------------------------------------
    // 4. Tie Breaking and Competition Ranking Test
    // -------------------------------------------------------------------------
    private static void testTieBreakingAndRanking() {
        System.out.println("\n--- Testing Tie-Breaking & Competition Ranking ---");

        // Matching the exact example in prompt:
        // Rahul: 98, Karan: 95, Aman: 95, Priya: 90, Arjun: 85
        ScoreRecord r1 = new ScoreRecord(1, 1, "25BCS001", "Rahul Sharma", "CSE", 1, "ADSA", 100, 98.0);
        ScoreRecord r2 = new ScoreRecord(2, 4, "25BCS004", "Karan Singh", "CSE", 1, "ADSA", 100, 95.0);
        ScoreRecord r3 = new ScoreRecord(3, 6, "25BCS006", "Aman Verma", "IT", 1, "ADSA", 100, 95.0);
        ScoreRecord r4 = new ScoreRecord(4, 2, "25BCS002", "Priya Sharma", "CSE", 1, "ADSA", 100, 90.0);
        ScoreRecord r5 = new ScoreRecord(5, 3, "25BCS003", "Arjun Kumar", "IT", 1, "ADSA", 100, 85.0);

        List<ScoreRecord> list = new ArrayList<>();
        list.add(r4);
        list.add(r2);
        list.add(r5);
        list.add(r1);
        list.add(r3);

        MaxHeap heap = new MaxHeap(list);
        List<ScoreRecord> extracted = new ArrayList<>();
        while (!heap.isEmpty()) {
            extracted.add(heap.extractMax());
        }

        assertEquals(5, extracted.size(), "All 5 records extracted from heap");
        assertEquals(98.0, extracted.get(0).getMarks(), "1st is Rahul (98.0)");
        assertEquals(95.0, extracted.get(1).getMarks(), "2nd is 95.0");
        assertEquals(95.0, extracted.get(2).getMarks(), "3rd is 95.0 (tie)");
        assertEquals(90.0, extracted.get(3).getMarks(), "4th is Priya (90.0)");
        assertEquals(85.0, extracted.get(4).getMarks(), "5th is Arjun (85.0)");

        // Verify ranks
        int[] expectedRanks = { 1, 2, 2, 4, 5 };
        int currentRank = 1;
        for (int i = 0; i < extracted.size(); i++) {
            ScoreRecord curr = extracted.get(i);
            if (i > 0) {
                ScoreRecord prev = extracted.get(i - 1);
                if (Double.compare(curr.getMarks(), prev.getMarks()) != 0) {
                    currentRank = i + 1;
                }
            }
            assertEquals(expectedRanks[i], currentRank,
                    "Record " + i + " (" + curr.getCandidateName() + ") rank calculation");
        }
    }

    // -------------------------------------------------------------------------
    // 5. DAO Tests with Real MySQL Database
    // -------------------------------------------------------------------------
    private static void testDAOs() {
        System.out.println("\n--- Testing DAO Database Operations ---");
        CandidateDAO candidateDAO = new CandidateDAO();
        ExaminationDAO examDAO = new ExaminationDAO();
        ScoreDAO scoreDAO = new ScoreDAO();

        // Verify candidate retrieval
        Candidate c = candidateDAO.getCandidateByRollNumber("25BCS001");
        assertTrue(c != null, "CandidateDAO.getCandidateByRollNumber(25BCS001) returns record");
        assertEquals("Rahul Sharma", c.getName(), "Candidate 25BCS001 name is Rahul Sharma");

        // Verify examination retrieval
        Examination exam = examDAO.getExaminationById(1);
        assertTrue(exam != null, "ExaminationDAO.getExaminationById(1) returns record");
        assertEquals(100.0, exam.getMaxMarks(), "Exam 1 max marks is 100.0");

        // Verify scores retrieval
        List<ScoreRecord> examScores = scoreDAO.getScoresByExamId(1);
        assertTrue(!examScores.isEmpty(), "ScoreDAO.getScoresByExamId(1) returns score list");
        assertTrue(examScores.size() >= 10, "Exam 1 has at least 10 recorded candidate scores");

        // Verify ranking system execution
        RankingSystem rankingSystem = new RankingSystem();
        System.out.println("\nVerifying RankingSystem merit list output:");
        rankingSystem.generateCompleteMeritList(1);
    }
}
