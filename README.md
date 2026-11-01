# ExamRank — Online Examination Ranking System

A full-stack, enterprise-grade academic platform built with **Java (JDK 21+)**, **Java Servlets**, **JDBC**, **MySQL**, and an **Apache Tomcat** web server, featuring a modern **HTML5 / CSS3 / JavaScript** frontend styled in a professional **Navy Blue, White, and Orange** aesthetic.

This project was developed for the **2nd-year B.Tech Computer Science & Engineering ADSA (Algorithms and Data Structures)** coursework. It connects frontend web workflows directly to two custom, in-memory algorithmic data structures:
1. **Custom Max Heap**: Powers automated competition ranking and top-$K$ retrieval ($O(N)$ buildHeap + $O(N + K \log N)$ top-$K$).
2. **Custom Hash Table with Separate Chaining**: Powers instant student roll number lookups ($O(1)$ average time).

---

## 🚀 Key Highlights & Architectural Overview

- **Full-Stack Java Architecture**: No Spring Boot, No Hibernate, No React, No Node.js. Built using pure **Jakarta Java Servlets (10.x)** and **JDBC**.
- **Modern Academic Dashboard**: Responsive UI with a Navy Blue (`#0a192f`, `#1e3a8a`), Orange (`#f97316`), and White theme.
- **Top 3 Visual Podium**: Gold 👑, Silver 🥈, and Bronze 🥉 visual podium cards for top achievers.
- **Fair Competition Ranking**: Handles ties using Standard Competition Ranking (`1224`), calculated on the Java backend.
- **In-Memory Hash Table Sync**: High-speed search bar in the web UI querying the custom Hash Table in microseconds.
- **Interactive Analytics**: Integrated **Chart.js** visualizations for score distribution, pass/fail ratios, and departmental performance.
- **Role-Based Authentication**: Secure login with SHA-256 password hashing and `HttpSession` management.
- **Dual Execution Modes**: Web application (via Apache Tomcat at `http://localhost:8080/`) and Console CLI (via `./run.sh`).

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | HTML5, CSS3, Modern JavaScript (ES6+), Chart.js, Responsive Flexbox/Grid |
| **Web Server** | Apache Tomcat 10.1 (Jakarta Servlet 6.0) |
| **Backend** | Java (JDK 21+), Java Servlets, RESTful JSON APIs, Google Gson |
| **Database** | MySQL Server (localhost:3306), JDBC (PreparedStatement, Connection Pooling) |
| **Data Structures** | Custom Max Heap, Custom Hash Table with Separate Chaining (Zero built-in collections) |

---

## 📂 Project Folder Structure

```
Online_Examination_Ranking_System_JDBC/
│
├── webapp/                              # Modern Web Application Root
│   ├── index.html                       # Entry redirector
│   ├── login.html                       # Professional login interface
│   ├── pages/
│   │   ├── dashboard.html               # Admin Dashboard (Stats, active exams, ADSA inspector)
│   │   ├── candidates.html              # Candidate management (CRUD & Hash Table lookup)
│   │   ├── examinations.html            # Examination management (CRUD & max marks)
│   │   ├── scores.html                  # Score recording & evaluation
│   │   ├── merit-list.html              # Official Merit List (Max Heap, Podium, CSV export)
│   │   ├── leaderboard.html             # Top 5 / 10 / 20 Leaderboard (MaxHeap.getTopK)
│   │   └── analytics.html               # Performance analytics & Chart.js visualizations
│   ├── css/
│   │   ├── style.css                    # Design system, variables, modals, toast engine
│   │   ├── dashboard.css                # Sidebar, cards, podium styles, tables
│   │   └── responsive.css               # Breakpoints for mobile & tablet
│   ├── js/
│   │   ├── auth.js                      # Session verification, login/logout, toasts
│   │   ├── dashboard.js                 # Dashboard stats & table loader
│   │   ├── candidates.js                # Candidate CRUD & Hash Table microsecond search
│   │   ├── examinations.js              # Examination CRUD controller
│   │   ├── scores.js                    # Score recorder & range validation
│   │   ├── merit-list.js                # Merit list renderer & CSV generator
│   │   ├── leaderboard.js               # Leaderboard Top-K controller
│   │   └── analytics.js                 # Chart.js analytics engine
│   └── WEB-INF/
│       ├── web.xml                      # Servlet mappings & session configuration
│       ├── lib/                         # Runtime JARs (MySQL Connector, Gson)
│       └── classes/                     # Compiled Servlet bytecode
│
├── src/                                 # Java Backend Source Code
│   ├── Main.java                        # Legacy interactive Console Application
│   ├── database/
│   │   └── DBConnection.java            # JDBC Connection manager
│   ├── model/
│   │   ├── Candidate.java               # Candidate entity
│   │   ├── Examination.java             # Examination entity
│   │   ├── Score.java                   # Score entity
│   │   ├── ScoreRecord.java             # Joined score record
│   │   ├── User.java                    # User auth model
│   │   └── RankedCandidate.java         # Ranked candidate DTO for Web API
│   ├── dao/
│   │   ├── CandidateDAO.java            # JDBC CRUD for candidates
│   │   ├── ExaminationDAO.java          # JDBC CRUD for examinations
│   │   ├── ScoreDAO.java                # JDBC CRUD for scores
│   │   └── UserDAO.java                 # User authentication & credentials
│   ├── dsa/
│   │   ├── MaxHeap.java                 # Custom Max Heap implementation
│   │   ├── HashTable.java               # Custom Hash Table with Separate Chaining
│   │   └── RankingSystem.java           # Merit list generator & competition ranking
│   ├── service/
│   │   └── CandidateService.java        # Coordinates DB caching with HashTable
│   ├── servlet/
│   │   ├── AuthServlet.java             # /api/login, /api/logout, /api/auth/me
│   │   ├── CandidateServlet.java        # /api/candidates, /api/candidates/search
│   │   ├── ExaminationServlet.java      # /api/examinations
│   │   ├── ScoreServlet.java            # /api/scores
│   │   ├── RankingServlet.java          # /api/rankings, /api/rankings/top
│   │   ├── AnalyticsServlet.java        # /api/analytics, /api/analytics/dashboard
│   │   └── HashTableDemoServlet.java    # /api/dsa/hashtable (bucket inspector)
│   └── util/
│       ├── JsonUtil.java                # Gson JSON serialization helper
│       └── SecurityUtil.java            # SHA-256 password hashing utility
│
├── test/
│   ├── SystemTestSuite.java             # 40-test ADSA and JDBC Unit Suite
│   └── WebIntegrationTestSuite.java     # 26-test HTTP & Servlet Integration Suite
│
├── database/
│   └── schema.sql                       # Complete MySQL schema & initial seed data
├── server/
│   └── apache-tomcat-10.1.25/           # Self-contained Apache Tomcat Server
├── lib/                                 # Project dependencies
├── bin/                                 # Compiled Java bytecode
├── start-tomcat.sh                      # One-click Tomcat start & web deploy
├── stop-tomcat.sh                       # One-click Tomcat stop
├── compile.sh                           # Full build script
├── run.sh                               # Console application runner
├── test.sh                              # Complete automated test runner
└── README.md
```

---

## ⚡ Quick Start: Running the Web Application

### Step 1: Ensure MySQL is Running
```bash
mysql -u root -p0000 -e "USE exam_ranking_system; SELECT COUNT(*) FROM candidates;"
```
*(If database is not initialized, run `mysql -u root -p0000 < database/schema.sql`)*

### Step 2: Start Apache Tomcat & Launch Web App
In your terminal at `/Users/ishaanbalana/Documents/Online_Examination_Ranking_System_JDBC`:
```bash
./start-tomcat.sh
```

### Step 3: Open in Your Browser
Navigate to:
👉 **[http://localhost:8080/](http://localhost:8080/)**

#### Default Login Credentials:
- **System Administrator**: Username `admin` | Password `admin123` (Full CRUD + System Control)
- **Exam Coordinator**: Username `faculty` | Password `faculty123` (Faculty Role)

### Step 4: Stop Tomcat
When done:
```bash
./stop-tomcat.sh
```

---

## 🧪 Comprehensive Automated Testing (66 Tests)

Run the unified test runner:
```bash
./test.sh
```

This automatically runs:
1. **Core ADSA & JDBC Tests** ([SystemTestSuite.java](file:///Users/ishaanbalana/Documents/Online_Examination_Ranking_System_JDBC/test/SystemTestSuite.java)): 40 Unit tests verifying `MaxHeap`, `HashTable`, Floyd's `buildHeap`, separate chaining collisions, rehashing, and DAOs.
2. **Web API & Servlet Integration Tests** ([WebIntegrationTestSuite.java](file:///Users/ishaanbalana/Documents/Online_Examination_Ranking_System_JDBC/test/WebIntegrationTestSuite.java)): 26 Live HTTP integration tests verifying `/api/login`, sessions, `/api/candidates/search`, `/api/rankings`, `/api/analytics`, and `/api/dsa/hashtable`.

```text
=================================================
   TEST SUMMARY: 66 PASSED, 0 FAILED (100%)
=================================================
```

---

## 🧠 Algorithms and Data Structures (ADSA) Integration

### 1. Custom Max Heap ([MaxHeap.java](file:///Users/ishaanbalana/Documents/Online_Examination_Ranking_System_JDBC/src/dsa/MaxHeap.java))
- **No Java PriorityQueue**: Implemented manually using array arithmetic.
- **Floyd's Bottom-Up Heapify**: $O(N)$ linear build-time starting from index $\lfloor N/2 \rfloor - 1$ down to $0$.
- **Merit List Ranking**: Extracts candidates in $O(N \log N)$ and applies **Standard Competition Ranking (`1224`)**:
  - Rank 1: Marks 98.0
  - Rank 2: Marks 95.0
  - Rank 2: Marks 95.0 (Tied)
  - Rank 4: Marks 90.0 (Rank 3 is skipped)
- **Top-$K$ Retrieval (`MaxHeap.getTopK(k)`)**: Clones the heap and extracts top-$K$ candidates in $O(N + K \log N)$ time, significantly faster than full sorting ($O(N \log N)$).

### 2. Custom Hash Table ([HashTable.java](file:///Users/ishaanbalana/Documents/Online_Examination_Ranking_System_JDBC/src/dsa/HashTable.java))
- **No Java HashMap**: Implemented manually with an array of buckets and singly linked lists.
- **Polynomial Rolling Hash**: Maps string roll numbers uniformly using prime base $31$:
  $$\text{hash}(S) = \left( \sum_{i=0}^{L-1} S[i] \cdot 31^i \right) \pmod{\text{capacity}}$$
- **Separate Chaining Collision Handling**: Colliding keys form a linked list chain at the bucket index.
- **Dynamic Rehashing**: Doubles capacity when load factor $\lambda = N/B > 0.75$, maintaining $O(1)$ average search time.
- **Live UI Inspector**: The dashboard includes an interactive inspector showing live bucket chains and collision counts via `/api/dsa/hashtable`.

---

## 🌐 Web Application Features Walkthrough

1. **Login Page (`/login.html`)**: Role-based authentication, SHA-256 validation, credential cards for quick evaluation.
2. **Admin Dashboard (`/pages/dashboard.html`)**: Real-time stats cards (Candidates, Exams, Scores, Highest Score), scheduled exams overview, and live ADSA Hash Table inspector modal.
3. **Candidate Directory (`/pages/candidates.html`)**: Full CRUD operations, department filters, and a dedicated **Custom Hash Table Search Bar** displaying execution time in microseconds.
4. **Examination Management (`/pages/examinations.html`)**: Create, edit, and delete examinations with positive maximum mark constraints.
5. **Score Management (`/pages/scores.html`)**: Record marks with instant client- and server-side validation against examination maximum marks and duplicate score prevention.
6. **Official Merit List (`/pages/merit-list.html`)**:
   - Visual Top 3 Podium (Gold, Silver, Bronze cards).
   - Full rankings table with Competition Ranks (`1224`) and tie badges.
   - Live department filtering and candidate search.
   - **One-click "Download Merit List as CSV"** button.
7. **Leaderboard (`/pages/leaderboard.html`)**: Top 5, Top 10, and Top 20 buttons powered by `MaxHeap.getTopK()`.
8. **Performance Analytics (`/pages/analytics.html`)**:
   - Total candidates, average marks, highest, lowest, pass %, fail %.
   - **Chart.js** Doughnut Chart: Pass vs. Fail.
   - **Chart.js** Bar Chart: Score Distribution across grade tiers.
   - **Chart.js** Horizontal Bar Chart: Department-wise performance comparison.

---

## 💻 Console Application (Still Available)

The original console application remains fully functional:
```bash
./run.sh
```
All 13 interactive console menu choices continue to operate seamlessly alongside the new web frontend.
