# 🤝 BorrowBuddy – Campus Borrow & Exchange Management Portal

BorrowBuddy is a full-stack, three-tier academic DBMS project built with **Java 17 Swing**, **MySQL 8.0**, **JDBC**, and **Maven**. It provides a centralized, secure platform for students and faculty to borrow, temporarily exchange, and permanently exchange academic and campus resources.

---

## 🌟 Key Features

- **Three Core Transaction Types:**
  - **Temporary Borrowing:** Single item request with start/due dates, owner approval, and return confirmation.
  - **Temporary Exchange:** Simultaneous bilateral exchange of two items with independent return tracking.
  - **Permanent Exchange:** Mutual swap of item ownership executed atomically within a single MySQL transaction.
- **Relational Integrity & Normalization:** Fully normalized 9-table schema in 3NF/BCNF with zero insertion/update/deletion anomalies.
- **Security:** 12-round BCrypt salted password hashing, prepared statements for all SQL operations, and role-based permissions.
- **Modern Java Swing GUI:** Polished blue/white desktop interface, responsive layout, real-time KPI cards, and background database querying with `SwingWorker`.
- **Administrative Portal:** User management, role promotions, category administration, item moderation, and transaction CSV export.

---

## 📋 Technology Stack

- **Frontend / GUI:** Java Swing (Desktop client)
- **Backend:** Java 17
- **Database:** MySQL 8.0 (InnoDB engine, utf8mb4)
- **Database Connectivity:** JDBC with MySQL Connector/J 8.0
- **Build Tool:** Apache Maven 3.9+
- **Testing:** JUnit 5 (`junit-jupiter:5.10.0`)
- **Password Security:** BCrypt (`org.mindrot:jbcrypt:0.4`)

---

## 🚀 Quick Start Guide

### Prerequisites
1. **Java Development Kit (JDK):** Version 17 or higher
2. **Apache Maven:** Version 3.8+
3. **MySQL Server:** Version 8.0+ running on port 3306

### Step 1: Database Setup
Launch your MySQL command-line client or MySQL Workbench and run:

```bash
# Navigate to the database directory
cd database

# Execute the schema and seed data scripts in MySQL:
mysql -u root -p < schema.sql
mysql -u root -p < sample_data.sql
```

This creates the database `borrowbuddy_db` with all 9 tables, indexes, constraints, and initial test data.

### Step 2: Configure Database Credentials
Ensure `src/main/resources/db.properties` reflects your local MySQL credentials:

```properties
db.host=localhost
db.port=3306
db.name=borrowbuddy_db
db.user=root
db.password=your_mysql_password
```

### Step 3: Build the Project with Maven
Compile, run tests, and package the executable JAR:

```bash
# Run unit test suite
mvn test

# Package executable fat JAR with all dependencies
mvn package -DskipTests
```

### Step 4: Launch the Application
Run the executable standalone JAR:

```bash
java -jar target/borrowbuddy-1.0.0-jar-with-dependencies.jar
```

Or on Windows, double-click:
```cmd
run.bat
```

---

## 🔐 Default Demo Accounts

All seed passwords are authenticated securely in the database:

| Role | Email | Password | User Name | Department |
|---|---|---|---|---|
| **Administrator** | `admin@campus.edu` | `Admin@123` | Admin User | Administration |
| **Student** | `aarav@campus.edu` | `Password@123` | Aarav Sharma | Computer Science |
| **Student** | `priya@campus.edu` | `Password@123` | Priya Patel | Electronics |
| **Faculty** | `rahul@campus.edu` | `Password@123` | Rahul Mehta | Computer Science |
| **Faculty** | `neha@campus.edu` | `Password@123` | Neha Singh | Mechanical |

*(The login screen also features convenient **Quick Demo** shortcut buttons to auto-populate credentials for Aarav, Rahul, and Admin).*

---

## 📂 Project Architecture

```text
borrowbuddy/
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── run.bat
├── database/
│   ├── schema.sql           # Complete 9-table MySQL 8 schema DDL
│   └── sample_data.sql      # Realistic seed users, categories, items, txns
├── docs/
│   ├── ACADEMIC_REPORT.md   # 20-section comprehensive DBMS report
│   ├── er_diagram.md        # Mermaid ER diagram & 19 relationship narratives
│   └── relational_schema.md # Relational notation, FDs & 3NF proofs
├── src/
│   ├── main/
│   │   ├── java/com/borrowbuddy/
│   │   │   ├── Main.java                        # Entrypoint & L&F setup
│   │   │   ├── config/DatabaseConnection.java   # JDBC Connection singleton
│   │   │   ├── model/                           # 9 Entity domain models
│   │   │   ├── dao/                             # 9 Data Access Objects
│   │   │   ├── service/                         # Business logic & atomic txns
│   │   │   ├── ui/                              # 11 Java Swing GUI panels
│   │   │   └── util/                            # Theme, dates & BCrypt helpers
│   │   └── resources/
│   │       └── db.properties                    # Database connection config
│   └── test/
│       └── java/com/borrowbuddy/                # 15 automated JUnit 5 tests
```

---

## 🧪 Running Automated Tests

Run the full JUnit 5 test suite via Maven:

```bash
mvn test
```

All 15 automated test cases verify:
- BCrypt salted hashing and verification
- User registration validation and duplicate email rejection
- Session logout state cleanup
- Item creation, category classification, and search filtering
- Self-borrowing prevention
- Full temporary borrow lifecycle (Request $\rightarrow$ Approval $\rightarrow$ Return $\rightarrow$ Confirmation)
- Atomic permanent ownership transfer and audit trail preservation

---

## 📄 License
Academic project developed for Educational & Institutional Database Management Systems evaluation.
