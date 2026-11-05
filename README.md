# ExamRank — Online Examination Ranking System

A full-stack, enterprise-grade academic platform built with **Java (JDK 21+)**, **Java Servlets (Jakarta Servlet 10)**, **JDBC**, **MySQL**, and an **Apache Tomcat 10.1** web server, featuring a modern **HTML5 / CSS3 / JavaScript** frontend styled in a professional **Navy Blue, White, and Orange** aesthetic.

This project was developed for the **2nd-year B.Tech Computer Science & Engineering ADSA (Algorithms and Data Structures)** coursework. It connects frontend web workflows directly to two custom, in-memory algorithmic data structures:
1. **Custom Max Heap**: Powers automated competition ranking and top-$K$ retrieval ($O(N)$ buildHeap + $O(N + K \log N)$ top-$K$).
2. **Custom Hash Table with Separate Chaining**: Powers instant student roll number lookups ($O(1)$ average time).

---

## 🚀 Key Highlights & Architectural Overview

- **Full-Stack Java Architecture**: No Spring Boot, No Hibernate, No React, No Node.js. Built using pure **Jakarta Java Servlets (10.x)** and **JDBC**.
- **Modern Academic Dashboard**: Responsive UI with a Navy Blue (`#0a192f`, `#1e3a8a`), Orange (`#f97316`), and White theme.
- **Cloud & Local Ready**: Centralized `DBConnection` supports Railway MySQL environment variables (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`), generic variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), and local `.env` configuration.
- **Top 3 Visual Podium**: Gold 👑, Silver 🥈, and Bronze 🥉 visual podium cards for top achievers.
- **Fair Competition Ranking**: Handles ties using Standard Competition Ranking (`1224`), calculated on the Java backend.
- **In-Memory Hash Table Sync**: High-speed search bar in the web UI querying the custom Hash Table in microseconds.
- **Interactive Analytics**: Integrated **Chart.js** visualizations for score distribution, pass/fail ratios, and departmental performance.
- **Role-Based Authentication**: Secure login with SHA-256 password hashing and `HttpSession` management.
- **Dual Execution Modes**: Web application (via Apache Tomcat or Docker) and Console CLI (via `./run.sh`).

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | HTML5, CSS3, Modern JavaScript (ES6+), Chart.js, Responsive Flexbox/Grid |
| **Web Server** | Apache Tomcat 10.1 (Jakarta Servlet 6.0 / Servlet 10 runtime) |
| **Backend** | Java (JDK 21+), Java Servlets, RESTful JSON APIs, Google Gson |
| **Database** | MySQL Server (Local or Railway Cloud), JDBC (`com.mysql.cj.jdbc.Driver`), Env-based connection manager |
| **Data Structures** | Custom Max Heap, Custom Hash Table with Separate Chaining (Zero built-in collections) |
| **Containerization** | Dockerfile with Apache Tomcat 10.1 & JDK 21 (Temurin) for Railway / Cloud deployment |

---

## 🔌 Database Configuration (Local & Cloud / Railway)

Database connectivity is managed centrally by `src/database/DBConnection.java`. All hardcoded credentials have been removed. Configuration is read at runtime with the following precedence:
1. **OS Environment Variables** (`System.getenv`)
2. **JVM System Properties** (`System.getProperty`)
3. **Local `.env` file** in the project directory (optional, for local development)

### Supported Environment Variables

| Variable | Alternatives | Default | Description |
| :--- | :--- | :--- | :--- |
| `MYSQLHOST` | `DB_HOST` | *(Required)* | MySQL hostname (e.g., `localhost` locally, or Railway host) |
| `MYSQLPORT` | `DB_PORT` | `3306` | MySQL port number |
| `MYSQLDATABASE` | `DB_NAME`, `DB_DATABASE` | `exam_ranking_system` | Database name |
| `MYSQLUSER` | `DB_USERNAME`, `DB_USER` | *(Required)* | MySQL username (e.g., `root`) |
| `MYSQLPASSWORD` | `DB_PASSWORD` | *(Required)* | MySQL password (never exposed in logs/errors) |
| `DB_URL` | `MYSQL_URL`, `DATABASE_URL` | *(Optional)* | Full JDBC URL (e.g., `jdbc:mysql://host:port/db`) or Railway URI (`mysql://user:pass@host:port/db`) |
| `DB_PARAMS` | `MYSQL_JDBC_PARAMS` | `useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Custom JDBC connection parameters |

> [!NOTE]
> `allowPublicKeyRetrieval=true` and `useSSL=false` (or valid SSL) are required for modern MySQL 8.0+ authentication (`caching_sha2_password`), especially on Railway and cloud container environments.

---

### 1. Instructions for Configuring Local MySQL

#### Method A: Using a `.env` file (Recommended for Local Dev)
1. Copy the provided `.env.example` template to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and fill in your local MySQL credentials:
   ```env
   MYSQLHOST=localhost
   MYSQLPORT=3306
   MYSQLDATABASE=exam_ranking_system
   MYSQLUSER=root
   MYSQLPASSWORD=your_local_password
   ```
   *(The `.env` file is excluded in `.gitignore` so your real credentials will never be committed).*

#### Method B: Exporting in Shell / Terminal
**macOS / Linux / WSL:**
```bash
export MYSQLHOST=localhost
export MYSQLPORT=3306
export MYSQLDATABASE=exam_ranking_system
export MYSQLUSER=root
export MYSQLPASSWORD=your_local_password
```

**Windows PowerShell:**
```powershell
$env:MYSQLHOST="localhost"
$env:MYSQLPORT="3306"
$env:MYSQLDATABASE="exam_ranking_system"
$env:MYSQLUSER="root"
$env:MYSQLPASSWORD="your_local_password"
```

#### Method C: Using Generic JDBC URL
```bash
export DB_URL="jdbc:mysql://localhost:3306/exam_ranking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_local_password"
```

---

### 2. Instructions for Configuring Railway MySQL

When deploying to [Railway](https://railway.com/):

#### Step 1: Provision a MySQL Service in Railway
1. Open your Railway Project dashboard.
2. Click **+ New** &rarr; **Database** &rarr; **Add MySQL**.
3. Railway automatically creates the instance and provides the connection variables in the service dashboard.

#### Step 2: Link MySQL to the Web Application Service
1. In your web application service settings on Railway, navigate to the **Variables** tab.
2. If using Railway service linking: Reference the variables from the MySQL service (or Railway will inject them automatically if linked):
   - `MYSQLHOST` = `${{MySQL.MYSQLHOST}}`
   - `MYSQLPORT` = `${{MySQL.MYSQLPORT}}`
   - `MYSQLDATABASE` = `exam_ranking_system` *(or `${{MySQL.MYSQLDATABASE}}`, see note below)*
   - `MYSQLUSER` = `${{MySQL.MYSQLUSER}}`
   - `MYSQLPASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
   - *(Optional)* `MYSQL_URL` = `${{MySQL.MYSQL_URL}}`

> [!IMPORTANT]
> **Railway Database Name Note:**
> By default, Railway creates a database named `railway` (`MYSQLDATABASE=railway`).
> The schema file (`database/schema.sql`) specifies `CREATE DATABASE IF NOT EXISTS exam_ranking_system; USE exam_ranking_system;`.
> If you import `schema.sql` into Railway MySQL without modification, set `MYSQLDATABASE=exam_ranking_system` in your web service variables so the application connects to the created schema!

#### Step 3: Initialize the Database on Railway
Before running the application on Railway, run the schema initialization script:
1. Use the **Railway CLI** to execute the script:
   ```bash
   railway run mysql -h $MYSQLHOST -u $MYSQLUSER -p$MYSQLPASSWORD < database/schema.sql
   ```
2. **Or** connect using an external database client (such as MySQL Workbench, DBeaver, or TablePlus) using Railway's public MySQL connection details, open `database/schema.sql`, and execute it.

---

### 3. Deploying to Railway with Docker

The project includes a production-ready `Dockerfile` configured for Apache Tomcat 10.1:
1. Connect your GitHub repository to Railway.
2. Railway detects the `Dockerfile` in the root directory automatically.
3. The `Dockerfile`:
   - Uses `tomcat:10.1-jdk21-temurin` (Apache Tomcat 10.1 + Eclipse Temurin OpenJDK 21)
   - Compiles all Java servlets with JDK 21 and copies runtime dependencies (`mysql-connector-j-8.2.0.jar`, `gson-2.13.2.jar`)
   - Deploys the web application to `/usr/local/tomcat/webapps/ROOT/`
   - Binds Tomcat's HTTP connector to `0.0.0.0` and listens dynamically on Railway's `$PORT` environment variable
4. Deploy the service and verify logs in Railway dashboard.

---

## 📂 Project Folder Structure

```
ExamRankingSystem/
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
│   ├── Main.java                        # Interactive Console Application
│   ├── database/
│   │   └── DBConnection.java            # Cloud & Local JDBC Connection manager
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
│   ├── DBConnectionConfigTest.java      # 25-test Database Config & Cloud Env Suite
│   ├── SystemTestSuite.java             # 40-test ADSA and JDBC Unit Suite
│   └── WebIntegrationTestSuite.java     # 26-test HTTP & Servlet Integration Suite
│
├── database/
│   └── schema.sql                       # Complete MySQL schema & initial seed data
├── lib/                                 # Runtime JAR dependencies
├── Dockerfile                           # Production container deployment for Railway
├── .dockerignore                        # Docker build ignore rules
├── .env.example                         # Environment variable configuration template
├── .gitignore                           # Git exclusion rules (includes .env)
├── compile.sh                           # Full build script
├── start-tomcat.sh                      # One-click Tomcat start & web deploy
├── stop-tomcat.sh                       # One-click Tomcat stop
├── run.sh                               # Console application runner
├── test.sh                              # Complete automated test runner
└── README.md
```

---

## ⚡ Quick Start: Running the Web Application Locally

### Step 1: Configure Environment Variables
Copy `.env.example` to `.env` and set your MySQL credentials:
```bash
cp .env.example .env
```
Edit `.env` with your MySQL user and password:
```env
MYSQLHOST=localhost
MYSQLPORT=3306
MYSQLDATABASE=exam_ranking_system
MYSQLUSER=root
MYSQLPASSWORD=your_local_password
```

### Step 2: Ensure MySQL Database is Initialized
```bash
mysql -u root -p < database/schema.sql
```

### Step 3: Start Apache Tomcat & Launch Web App
In your project directory:
```bash
./start-tomcat.sh
```

### Step 4: Open in Your Browser
Navigate to:
👉 **[http://localhost:8080/](http://localhost:8080/)**

#### Default Login Credentials:
- **System Administrator**: Username `admin` | Password `admin123` (Full CRUD + System Control)
- **Exam Coordinator**: Username `faculty` | Password `faculty123` (Faculty Role)

### Step 5: Stop Tomcat
When done:
```bash
./stop-tomcat.sh
```

---

## 🧪 Comprehensive Automated Testing

Run the unified test runner:
```bash
./test.sh
```

This runs three automated test suites:
1. **Cloud & Database Connection Config Suite** ([DBConnectionConfigTest.java](file:///test/DBConnectionConfigTest.java)): 25 Unit tests verifying Railway environment variables, generic variables, `mysql://` URI parsing, default port/db fallback, clear missing variable error reporting, and password masking.
2. **Core ADSA & JDBC Tests** ([SystemTestSuite.java](file:///test/SystemTestSuite.java)): 40 Unit tests verifying `MaxHeap`, `HashTable`, Floyd's `buildHeap`, separate chaining collisions, rehashing, and DAOs.
3. **Web API & Servlet Integration Tests** ([WebIntegrationTestSuite.java](file:///test/WebIntegrationTestSuite.java)): 26 Live HTTP integration tests verifying `/api/login`, sessions, `/api/candidates/search`, `/api/rankings`, `/api/analytics`, and `/api/dsa/hashtable`.

---

## 🧠 Algorithms and Data Structures (ADSA) Integration

### 1. Custom Max Heap ([MaxHeap.java](file:///src/dsa/MaxHeap.java))
- **No Java PriorityQueue**: Implemented manually using array arithmetic.
- **Floyd's Bottom-Up Heapify**: $O(N)$ linear build-time starting from index $\lfloor N/2 \rfloor - 1$ down to $0$.
- **Merit List Ranking**: Extracts candidates in $O(N \log N)$ and applies **Standard Competition Ranking (`1224`)**:
  - Rank 1: Marks 98.0
  - Rank 2: Marks 95.0
  - Rank 2: Marks 95.0 (Tied)
  - Rank 4: Marks 90.0 (Rank 3 is skipped)
- **Top-$K$ Retrieval (`MaxHeap.getTopK(k)`)**: Clones the heap and extracts top-$K$ candidates in $O(N + K \log N)$ time, significantly faster than full sorting ($O(N \log N)$).

### 2. Custom Hash Table ([HashTable.java](file:///src/dsa/HashTable.java))
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
