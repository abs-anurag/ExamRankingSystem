import database.DBConnection;
import dao.ExaminationDAO;
import dao.ScoreDAO;
import dsa.RankingSystem;
import model.Candidate;
import model.Examination;
import model.Score;
import model.ScoreRecord;
import service.CandidateService;

import java.util.List;
import java.util.Scanner;

/**
 * Main Application Class for Online Examination Ranking System.
 *
 * Course: 2nd Year B.Tech CSE - ADSA (Algorithms and Data Structures)
 * Features:
 * - Pure Core Java (JDK 21)
 * - JDBC connectivity with MySQL
 * - Custom Max Heap (Ranking and Top-K)
 * - Custom Hash Table with Separate Chaining (Candidate Roll Number Lookups)
 * - Full CRUD Operations with Input Validation
 */
public class Main {

    private static CandidateService candidateService;
    private static ExaminationDAO examinationDAO;
    private static ScoreDAO scoreDAO;
    private static RankingSystem rankingSystem;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("       ONLINE EXAMINATION RANKING SYSTEM (ADSA PROJECT)           ");
        System.out.println("==================================================================");
        System.out.println("Initializing Database Connection...");

        // Test database connectivity
        if (!DBConnection.testConnection()) {
            System.err.println("\n[ERROR] Could not connect to MySQL at localhost:3306/exam_ranking_system.");
            System.err.println("Please check:");
            System.err.println("1. MySQL service is running.");
            System.err.println("2. Database 'exam_ranking_system' exists (run database/schema.sql).");
            System.err.println("3. MySQL credentials in database.DBConnection (root / 0000).");
            System.exit(1);
        }
        System.out.println("[SUCCESS] Connected to MySQL successfully.");

        // Initialize Services, DAOs, and Data Structures
        candidateService = new CandidateService();
        examinationDAO = new ExaminationDAO();
        scoreDAO = new ScoreDAO();
        rankingSystem = new RankingSystem();

        System.out.println("[SUCCESS] In-memory Custom Hash Table synchronized with database records.");
        System.out.println("==================================================================\n");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = readInt(scanner, "Enter your choice (1-14): ");

            switch (choice) {
                case 1 -> handleAddCandidate(scanner);
                case 2 -> handleViewAllCandidates();
                case 3 -> handleSearchCandidate(scanner);
                case 4 -> handleUpdateCandidate(scanner);
                case 5 -> handleDeleteCandidate(scanner);
                case 6 -> handleAddExamination(scanner);
                case 7 -> handleViewAllExaminations();
                case 8 -> handleRecordScore(scanner);
                case 9 -> handleUpdateScore(scanner);
                case 10 -> handleViewCandidateResults(scanner);
                case 11 -> handleDisplayTopScorers(scanner);
                case 12 -> handleGenerateMeritList(scanner);
                case 13 -> {
                    System.out.println("\nThank you for using Online Examination Ranking System. Goodbye!");
                    running = false;
                }
                case 14 -> handleDisplayHashTable();
                default -> System.out.println("[WARNING] Invalid choice! Please select an option between 1 and 14.");
            }

            if (running) {
                pressEnterToContinue(scanner);
            }
        }

        scanner.close();
    }

    /**
     * Displays the primary menu options.
     */
    private static void displayMenu() {
        System.out.println("\n=======================================================");
        System.out.println("                   MAIN MENU                           ");
        System.out.println("=======================================================");
        System.out.println("  1.  Add Candidate");
        System.out.println("  2.  View All Candidates");
        System.out.println("  3.  Search Candidate by Roll Number (Custom Hash Table)");
        System.out.println("  4.  Update Candidate Details");
        System.out.println("  5.  Delete Candidate");
        System.out.println("  6.  Add Examination");
        System.out.println("  7.  View All Examinations");
        System.out.println("  8.  Record Examination Score");
        System.out.println("  9.  Update Examination Score");
        System.out.println(" 10.  View Candidate Results");
        System.out.println(" 11.  Display Top 5 Scorers (Custom Max Heap)");
        System.out.println(" 12.  Generate Complete Merit List (Custom Max Heap)");
        System.out.println(" 13.  Exit");
        System.out.println("-------------------------------------------------------");
        System.out.println(" 14.  [ADSA Demo] View Custom Hash Table Internal Structure");
        System.out.println("=======================================================");
    }

    // -------------------------------------------------------------------------
    // 1. Add Candidate
    // -------------------------------------------------------------------------
    private static void handleAddCandidate(Scanner scanner) {
        System.out.println("\n--- 1. Add New Candidate ---");
        String rollNumber = readNonEmptyString(scanner, "Enter Roll Number (e.g. 25BCS015): ").toUpperCase();
        String name = readNonEmptyString(scanner, "Enter Candidate Name: ");
        String dept = readNonEmptyString(scanner, "Enter Department: ");

        candidateService.addCandidate(rollNumber, name, dept);
    }

    // -------------------------------------------------------------------------
    // 2. View All Candidates
    // -------------------------------------------------------------------------
    private static void handleViewAllCandidates() {
        System.out.println("\n--- 2. View All Candidates ---");
        List<Candidate> list = candidateService.getAllCandidates();
        if (list.isEmpty()) {
            System.out.println("No candidates found in database.");
            return;
        }

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-12s | %-25s | %-35s\n", "ID", "Roll Number", "Candidate Name", "Department");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Candidate c : list) {
            System.out.printf("%-6d | %-12s | %-25s | %-35s\n",
                    c.getCandidateId(), c.getRollNumber(), c.getName(), c.getDepartment());
        }
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("Total Registered Candidates: " + list.size());
    }

    // -------------------------------------------------------------------------
    // 3. Search Candidate by Roll Number (Custom Hash Table)
    // -------------------------------------------------------------------------
    private static void handleSearchCandidate(Scanner scanner) {
        System.out.println("\n--- 3. Search Candidate by Roll Number (Hash Table) ---");
        String rollNumber = readNonEmptyString(scanner, "Enter Roll Number to Search: ").toUpperCase();

        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate != null) {
            System.out.println("\nCandidate Found Details:");
            System.out.println("----------------------------------------");
            System.out.println("Candidate ID : " + candidate.getCandidateId());
            System.out.println("Roll Number  : " + candidate.getRollNumber());
            System.out.println("Name         : " + candidate.getName());
            System.out.println("Department   : " + candidate.getDepartment());
            System.out.println("----------------------------------------");
        } else {
            System.out.println("[NOT FOUND] No candidate found with roll number: " + rollNumber);
        }
    }

    // -------------------------------------------------------------------------
    // 4. Update Candidate Details
    // -------------------------------------------------------------------------
    private static void handleUpdateCandidate(Scanner scanner) {
        System.out.println("\n--- 4. Update Candidate Details ---");
        String rollNumber = readNonEmptyString(scanner, "Enter Roll Number of candidate to update: ").toUpperCase();

        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate == null) {
            System.out.println("[ERROR] Candidate not found with roll number: " + rollNumber);
            return;
        }

        System.out.println("Current Name      : " + candidate.getName());
        System.out.print("Enter New Name (press Enter to keep unchanged): ");
        String newName = scanner.nextLine().trim();

        System.out.println("Current Department: " + candidate.getDepartment());
        System.out.print("Enter New Department (press Enter to keep unchanged): ");
        String newDept = scanner.nextLine().trim();

        candidateService.updateCandidate(rollNumber, newName, newDept);
    }

    // -------------------------------------------------------------------------
    // 5. Delete Candidate
    // -------------------------------------------------------------------------
    private static void handleDeleteCandidate(Scanner scanner) {
        System.out.println("\n--- 5. Delete Candidate ---");
        String rollNumber = readNonEmptyString(scanner, "Enter Roll Number of candidate to delete: ").toUpperCase();

        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate == null) {
            System.out.println("[ERROR] Candidate not found with roll number: " + rollNumber);
            return;
        }

        System.out.println("Candidate Found: " + candidate.getName() + " (" + candidate.getRollNumber() + ")");
        System.out.print("Are you sure you want to delete this candidate and all associated scores? (y/N): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            candidateService.deleteCandidate(rollNumber);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // -------------------------------------------------------------------------
    // 6. Add Examination
    // -------------------------------------------------------------------------
    private static void handleAddExamination(Scanner scanner) {
        System.out.println("\n--- 6. Add New Examination ---");
        String examName = readNonEmptyString(scanner, "Enter Examination Name: ");
        double maxMarks = readPositiveDouble(scanner, "Enter Maximum Marks (e.g. 100): ");

        Examination exam = new Examination(examName, maxMarks);
        boolean success = examinationDAO.addExamination(exam);
        if (success) {
            System.out.println("Success: Examination '" + examName + "' added with ID " + exam.getExamId());
        } else {
            System.out.println("Error: Failed to add examination.");
        }
    }

    // -------------------------------------------------------------------------
    // 7. View All Examinations
    // -------------------------------------------------------------------------
    private static void handleViewAllExaminations() {
        System.out.println("\n--- 7. View All Examinations ---");
        List<Examination> exams = examinationDAO.getAllExaminations();
        if (exams.isEmpty()) {
            System.out.println("No examinations found in database.");
            return;
        }

        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-8s | %-42s | %-12s\n", "Exam ID", "Examination Name", "Max Marks");
        System.out.println("------------------------------------------------------------------");
        for (Examination e : exams) {
            System.out.printf("%-8d | %-42s | %-12.1f\n", e.getExamId(), e.getExamName(), e.getMaxMarks());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("Total Examinations: " + exams.size());
    }

    // -------------------------------------------------------------------------
    // 8. Record Examination Score
    // -------------------------------------------------------------------------
    private static void handleRecordScore(Scanner scanner) {
        System.out.println("\n--- 8. Record Examination Score ---");

        // Display available exams for user convenience
        List<Examination> exams = examinationDAO.getAllExaminations();
        if (exams.isEmpty()) {
            System.out.println("No examinations available. Please create an examination first.");
            return;
        }
        System.out.println("Available Examinations:");
        for (Examination e : exams) {
            System.out.printf("  [ID: %d] %s (Max: %.1f)\n", e.getExamId(), e.getExamName(), e.getMaxMarks());
        }

        int examId = readInt(scanner, "Enter Examination ID: ");
        Examination exam = examinationDAO.getExaminationById(examId);
        if (exam == null) {
            System.out.println("[ERROR] Invalid Examination ID: " + examId);
            return;
        }

        String rollNumber = readNonEmptyString(scanner, "Enter Candidate Roll Number: ").toUpperCase();
        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate == null) {
            System.out.println("[ERROR] Candidate with roll number '" + rollNumber + "' does not exist.");
            return;
        }

        // Check for duplicate score
        if (scoreDAO.scoreExists(candidate.getCandidateId(), examId)) {
            System.out.println("[ERROR] Score already recorded for " + candidate.getName() + " in " + exam.getExamName() + ".");
            System.out.println("Use Menu Option 9 to update the score if needed.");
            return;
        }

        // Validate Marks range: [0, maxMarks]
        double marks = readDoubleInRange(scanner, "Enter Marks Obtained: ", 0.0, exam.getMaxMarks());

        Score score = new Score(candidate.getCandidateId(), examId, marks);
        boolean success = scoreDAO.addScore(score);
        if (success) {
            System.out.printf("Success: Recorded %.2f marks for %s in '%s'.\n",
                    marks, candidate.getName(), exam.getExamName());
        } else {
            System.out.println("Error: Failed to record score in database.");
        }
    }

    // -------------------------------------------------------------------------
    // 9. Update Examination Score
    // -------------------------------------------------------------------------
    private static void handleUpdateScore(Scanner scanner) {
        System.out.println("\n--- 9. Update Examination Score ---");
        int examId = readInt(scanner, "Enter Examination ID: ");
        Examination exam = examinationDAO.getExaminationById(examId);
        if (exam == null) {
            System.out.println("[ERROR] Invalid Examination ID: " + examId);
            return;
        }

        String rollNumber = readNonEmptyString(scanner, "Enter Candidate Roll Number: ").toUpperCase();
        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate == null) {
            System.out.println("[ERROR] Candidate with roll number '" + rollNumber + "' does not exist.");
            return;
        }

        Score currentScore = scoreDAO.getScore(candidate.getCandidateId(), examId);
        if (currentScore == null) {
            System.out.println("[ERROR] No score record found for candidate in this examination.");
            System.out.println("Use Menu Option 8 to record an initial score.");
            return;
        }

        System.out.printf("Current Marks for %s in '%s': %.2f (Max: %.1f)\n",
                candidate.getName(), exam.getExamName(), currentScore.getMarks(), exam.getMaxMarks());

        double newMarks = readDoubleInRange(scanner, "Enter New Marks: ", 0.0, exam.getMaxMarks());

        boolean success = scoreDAO.updateScore(candidate.getCandidateId(), examId, newMarks);
        if (success) {
            System.out.printf("Success: Marks updated to %.2f for %s.\n", newMarks, candidate.getName());
        } else {
            System.out.println("Error: Failed to update score in database.");
        }
    }

    // -------------------------------------------------------------------------
    // 10. View Candidate Results
    // -------------------------------------------------------------------------
    private static void handleViewCandidateResults(Scanner scanner) {
        System.out.println("\n--- 10. View Candidate Results ---");
        String rollNumber = readNonEmptyString(scanner, "Enter Candidate Roll Number: ").toUpperCase();

        Candidate candidate = candidateService.searchByRollNumber(rollNumber);
        if (candidate == null) {
            System.out.println("[ERROR] Candidate not found with roll number: " + rollNumber);
            return;
        }

        List<ScoreRecord> results = scoreDAO.getScoresByCandidateRoll(rollNumber);

        System.out.println("\n==========================================================================");
        System.out.println("                     CANDIDATE EXAMINATION REPORT CARD                    ");
        System.out.println("==========================================================================");
        System.out.println("Roll Number : " + candidate.getRollNumber());
        System.out.println("Name        : " + candidate.getName());
        System.out.println("Department  : " + candidate.getDepartment());
        System.out.println("--------------------------------------------------------------------------");

        if (results.isEmpty()) {
            System.out.println("No examination scores recorded yet for this candidate.");
        } else {
            System.out.printf("%-8s | %-35s | %-10s | %-10s | %-10s\n",
                    "Exam ID", "Examination Name", "Marks", "Max Marks", "Percentage");
            System.out.println("--------------------------------------------------------------------------");

            double totalMarksObtained = 0.0;
            double totalMaxMarks = 0.0;

            for (ScoreRecord r : results) {
                double pct = (r.getMarks() / r.getMaxMarks()) * 100.0;
                totalMarksObtained += r.getMarks();
                totalMaxMarks += r.getMaxMarks();

                System.out.printf("%-8d | %-35s | %10.2f | %10.1f | %9.2f%%\n",
                        r.getExamId(), r.getExamName(), r.getMarks(), r.getMaxMarks(), pct);
            }

            System.out.println("--------------------------------------------------------------------------");
            double aggregatePct = (totalMaxMarks > 0) ? (totalMarksObtained / totalMaxMarks) * 100.0 : 0.0;
            System.out.printf("AGGREGATE TOTAL: %.2f / %.1f  (Overall: %.2f%%)\n",
                    totalMarksObtained, totalMaxMarks, aggregatePct);
        }
        System.out.println("==========================================================================\n");
    }

    // -------------------------------------------------------------------------
    // 11. Display Top 5 Scorers (Custom Max Heap)
    // -------------------------------------------------------------------------
    private static void handleDisplayTopScorers(Scanner scanner) {
        System.out.println("\n--- 11. Display Top 5 Scorers (Custom Max Heap) ---");
        handleViewAllExaminations();

        int examId = readInt(scanner, "Enter Examination ID: ");
        rankingSystem.displayTopKScorers(examId, 5);
    }

    // -------------------------------------------------------------------------
    // 12. Generate Complete Merit List (Custom Max Heap)
    // -------------------------------------------------------------------------
    private static void handleGenerateMeritList(Scanner scanner) {
        System.out.println("\n--- 12. Generate Complete Merit List (Custom Max Heap) ---");
        handleViewAllExaminations();

        int examId = readInt(scanner, "Enter Examination ID: ");
        rankingSystem.generateCompleteMeritList(examId);
    }

    // -------------------------------------------------------------------------
    // 14. Display Custom Hash Table Internal Buckets (ADSA Demo)
    // -------------------------------------------------------------------------
    private static void handleDisplayHashTable() {
        System.out.println("\n--- Custom Hash Table Visualization ---");
        candidateService.displayHashTableStructure();
    }

    // =========================================================================
    // Robust Scanner Helper Utilities (Prevents Crashes & Infinite Loops)
    // =========================================================================

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[Invalid Input] Please enter a valid integer number.");
            }
        }
    }

    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val > 0) {
                    return val;
                }
                System.out.println("[Invalid Input] Value must be strictly greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("[Invalid Input] Please enter a valid decimal or integer number.");
            }
        }
    }

    private static double readDoubleInRange(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("[Invalid Range] Marks must be between %.1f and %.1f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[Invalid Input] Please enter a valid decimal or integer number.");
            }
        }
    }

    private static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("[Invalid Input] This field cannot be empty. Please enter a value.");
        }
    }

    private static void pressEnterToContinue(Scanner scanner) {
        System.out.print("\nPress [Enter] to return to Main Menu...");
        scanner.nextLine();
    }
}

