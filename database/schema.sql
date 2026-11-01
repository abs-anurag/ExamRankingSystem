-- ==============================================================
-- Online Examination Ranking System - Database Schema & Sample Data
-- Academic Project for 2nd Year B.Tech CSE (ADSA)
-- ==============================================================

CREATE DATABASE IF NOT EXISTS exam_ranking_system;
USE exam_ranking_system;

-- Drop tables if they already exist (in reverse order of foreign keys)
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS examinations;
DROP TABLE IF EXISTS candidates;
DROP TABLE IF EXISTS users;

-- -------------------------------------------------------------
-- Table 0: users (Authentication & Role-Based Access)
-- -------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------
-- Table 1: candidates
-- -------------------------------------------------------------
CREATE TABLE candidates (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- -------------------------------------------------------------
-- Table 2: examinations
-- -------------------------------------------------------------
CREATE TABLE examinations (
    exam_id INT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(100) NOT NULL,
    max_marks DOUBLE NOT NULL
);

-- -------------------------------------------------------------
-- Table 3: scores
-- -------------------------------------------------------------
CREATE TABLE scores (
    score_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    exam_id INT NOT NULL,
    marks DOUBLE NOT NULL,
    CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id) ON DELETE CASCADE,
    CONSTRAINT fk_exam FOREIGN KEY (exam_id) REFERENCES examinations(exam_id) ON DELETE CASCADE,
    CONSTRAINT uq_candidate_exam UNIQUE (candidate_id, exam_id)
);

-- ==============================================================
-- Sample Records
-- ==============================================================

-- 1. Insert 12 Sample Candidates
INSERT INTO candidates (roll_number, name, department) VALUES
('25BCS001', 'Rahul Sharma', 'Computer Science and Engineering'),
('25BCS002', 'Priya Sharma', 'Computer Science and Engineering'),
('25BCS003', 'Arjun Kumar', 'Information Technology'),
('25BCS004', 'Karan Singh', 'Computer Science and Engineering'),
('25BCS005', 'Ananya Gupta', 'Artificial Intelligence & DS'),
('25BCS006', 'Aman Verma', 'Information Technology'),
('25BCS007', 'Rohan Mehta', 'Computer Science and Engineering'),
('25BCS008', 'Sneha Patel', 'Electronics & Communication'),
('25BCS009', 'Vikram Aditya', 'Computer Science and Engineering'),
('25BCS010', 'Neha Reddy', 'Artificial Intelligence & DS'),
('25BCS011', 'Devansh Joshi', 'Information Technology'),
('25BCS012', 'Tanvi Chawla', 'Electronics & Communication');

-- 2. Insert 3 Sample Examinations
INSERT INTO examinations (exam_name, max_marks) VALUES
('Algorithms and Data Structures (ADSA)', 100.0),
('Database Management Systems (DBMS)', 100.0),
('Discrete Mathematical Structures (DMS)', 50.0);

-- 3. Insert Sample Scores for Exam 1 (ADSA - Max: 100)
-- Notice deliberate ties: Karan Singh (95) and Aman Verma (95); Arjun Kumar (85) and Sneha Patel (85)
INSERT INTO scores (candidate_id, exam_id, marks) VALUES
(1, 1, 98.0),   -- Rahul Sharma
(4, 1, 95.0),   -- Karan Singh (Tie for Rank 2)
(6, 1, 95.0),   -- Aman Verma (Tie for Rank 2)
(2, 1, 90.0),   -- Priya Sharma (Rank 4)
(3, 1, 85.0),   -- Arjun Kumar (Tie for Rank 5)
(8, 1, 85.0),   -- Sneha Patel (Tie for Rank 5)
(5, 1, 82.5),   -- Ananya Gupta
(7, 1, 79.0),   -- Rohan Mehta
(9, 1, 74.0),   -- Vikram Aditya
(10, 1, 68.0);  -- Neha Reddy

-- 4. Insert Sample Scores for Exam 2 (DBMS - Max: 100)
INSERT INTO scores (candidate_id, exam_id, marks) VALUES
(2, 2, 96.0),   -- Priya Sharma
(1, 2, 94.0),   -- Rahul Sharma
(5, 2, 94.0),   -- Ananya Gupta (Tie with Rahul)
(3, 2, 88.0),   -- Arjun Kumar
(4, 2, 86.0),   -- Karan Singh
(7, 2, 84.0),   -- Rohan Mehta
(11, 2, 80.0),  -- Devansh Joshi
(12, 2, 75.0);  -- Tanvi Chawla

-- 5. Insert Sample Scores for Exam 3 (DMS - Max: 50)
INSERT INTO scores (candidate_id, exam_id, marks) VALUES
(1, 3, 49.0),
(6, 3, 47.5),
(2, 3, 45.0),
(9, 3, 42.0),
(10, 3, 38.0);

-- 6. Insert Default Administrative & Faculty Users
-- Passwords: 'admin' -> 'admin123' | 'faculty' -> 'faculty123' (SHA-256 encrypted)
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN'),
('faculty', 'ea8c21a44e59df92a2a0eb7ebca1e654eb6a59966b42b93850785161476db467', 'Exam Coordinator', 'FACULTY');

