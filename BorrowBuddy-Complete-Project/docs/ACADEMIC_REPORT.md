# BorrowBuddy – Campus Borrow & Exchange Management Portal
## Comprehensive Academic Project Report & Database Management System Documentation

---

### **Project Metadata**
- **Course / Degree:** Database Management Systems (DBMS) Laboratory / Project (STAGE 2 IMPLEMENTATION)
- **Project Title:** BorrowBuddy – Campus Borrow & Exchange Management Portal
- **Database Name:** `borrowbuddy_db`
- **Architecture:** Three-Tier Client-Server Architecture (Java Swing Desktop GUI $\leftrightarrow$ Business Logic Service Layer $\leftrightarrow$ DAO Data Access Layer $\leftrightarrow$ MySQL 8 Relational Database via JDBC)
- **Primary Technologies:** Java 17, Java Swing, MySQL 8.0, JDBC (MySQL Connector/J 8.0), Maven 3.9, JUnit 5, BCrypt (`jbcrypt:0.4`)

---

## 1. Title Page & Abstract

### Abstract
On university and college campuses, students and faculty frequently require temporary access to academic, laboratory, and extracurricular resources—including textbooks, scientific calculators, sensors, lab coats, and sports gear. Traditionally, these transactions are arranged through informal, uncoordinated peer-to-peer conversations, leading to lack of accountability, forgotten returns, conflicting commitments, and absence of formal ownership transfer records.

**BorrowBuddy** is a centralized, database-driven desktop portal engineered to solve this campus logistics problem. Designed following rigorous database normalization principles (3NF/BCNF) and three-tier enterprise design patterns, BorrowBuddy accommodates three distinct transaction types:
1. **Temporary Borrowing** (single item lending with start/due date tracking and verified owner confirmation),
2. **Temporary Exchange** (two items exchanged concurrently with independent dual-return verification), and
3. **Permanent Exchange** (irreversible bilateral ownership swap executed atomically in a single MySQL transaction with full audit logging).

This report presents the complete end-to-end engineering of BorrowBuddy: problem definition, entity-relationship modeling, relational schema specification, functional dependency decomposition, normalization proofs, transaction atomicity and concurrency control, Swing UI architecture, automated test suites, and empirical evaluation.

---

## 2. Introduction
Universities foster dense communities of scholars with complementary needs. A student taking an engineering dynamics course needs a graphing calculator for one semester; a graduate student completing an experimental physics thesis needs a digital multimeter for three weeks; a faculty member teaching an algorithms course seeks rare reference volumes. 

Despite this natural demand, campus item sharing remains plagued by inefficiencies. BorrowBuddy provides an institutionally verified, database-backed platform where campus members can register, catalogue their assets, set sharing preferences, request items, monitor due dates, and build communal trust through peer ratings.

---

## 3. Problem Definition and Scope

### 3.1 Problem Definition
The absence of a structured campus resource management portal results in:
- **Zero Visibility:** Assets sit idle in dormitories and offices while other members purchase duplicate items at high retail prices.
- **Lost and Overdue Assets:** Informal lending lacks contractually enforced return dates, leading to lost materials and friction.
- **Concurrency Conflicts:** An owner might informally promise the same item to multiple peers simultaneously.
- **Unregulated Permanent Swaps:** Items permanently traded leave no audit trail of ownership, creating disputes over genuine possession.
- **Data Insecurity:** Storing sensitive authentication passwords in plaintext or unverified spreadsheets presents severe privacy risks.

### 3.2 Project Scope
#### Included in BorrowBuddy v1.0:
- Single unified user table supporting `STUDENT`, `FACULTY`, and `ADMIN` roles.
- Granular item cataloging with category classification, condition tracking, and transaction permissions (`allow_borrowing`, `allow_temporary_exchange`, `allow_permanent_exchange`).
- Three fully realized transaction workflows with two-phase commit logic.
- Real-time dashboard statistics queried directly from MySQL.
- Full notification dispatch pipeline with unread counts and read status toggles.
- Post-transaction 5-star ratings and textual feedback with duplicate review prevention.
- Administrative console for user moderation, role promotion, category management, item moderation, and transaction CSV audit logs.
- Full password security via BCrypt salt-hashing with support for institutional demo credentials.

---

## 4. Aim and Objectives

### Aim
To design, implement, and validate a secure, normalized, and transactionally resilient database management system and desktop client that facilitates campus item borrowing and exchange.

### Objectives
1. Formulate a 9-entity relational schema with zero insertion, update, or deletion anomalies.
2. Implement 3NF/BCNF normalization with single-column surrogate primary keys and relational integrity constraints.
3. Guarantee ACID transactions in permanent ownership swaps via JDBC `conn.setAutoCommit(false)` and atomic rollback handlers.
4. Prevent conflicting reservations through dynamic status constraints (`AVAILABLE`, `UNAVAILABLE`, `BORROWED`).
5. Develop a responsive, aesthetically consistent Java Swing desktop GUI using SwingWorker background threads to maintain 60 FPS UI responsiveness.
6. Verify platform integrity using automated JUnit 5 test suites.

---

## 5. Existing vs. Proposed Systems

| Evaluation Metric | Existing Informal System (WhatsApp, Noticeboards) | Proposed BorrowBuddy System |
|---|---|---|
| **Discovery** | Scattered messages, rapidly buried in chat feeds | Indexed, categorized, keyword-searchable catalog |
| **Availability Tracking** | Manual verbal inquiry; prone to conflicting promises | Real-time database status flags (`AVAILABLE`, `BORROWED`) |
| **Bilateral Exchange** | Highly chaotic; no unified tracking of two distinct items | Structured exchange workflow with independent return logs |
| **Ownership Transfer** | Word-of-mouth; no permanent historical record | Atomic MySQL transfer with immutable `item_ownership_history` |
| **Accountability** | Zero institutional accountability | Due-date monitors, overdue alerts, and peer review scores |
| **Security** | None; personal phone numbers exposed publicly | Role-based access, BCrypt hashing, and parameterized SQL |

---

## 6. System Architecture

```text
+-----------------------------------------------------------------------+
|                       PRESENTATION LAYER (GUI)                        |
|   MainFrame | LoginPanel | RegisterPanel | DashboardPanel            |
|   BrowseItemsPanel | MyListingsPanel | RequestsPanel                  |
|   ActiveTransactionsPanel | TransactionHistoryPanel                   |
|   NotificationsPanel | ReviewsPanel | AdminDashboardPanel             |
+-----------------------------------------------------------------------+
                                   |  (SwingWorker Background Threads)
                                   v
+-----------------------------------------------------------------------+
|                         BUSINESS SERVICE LAYER                        |
|   AuthService | UserService | ItemService | TransactionService         |
|   NotificationService | AdminService | ReviewService                  |
+-----------------------------------------------------------------------+
                                   |  (Business Rules & Transaction Scope)
                                   v
+-----------------------------------------------------------------------+
|                       DATA ACCESS LAYER (DAO)                         |
|   UserDAO | CategoryDAO | ItemDAO | TransactionDAO                    |
|   ApprovalDAO | ReturnDAO | OwnershipHistoryDAO | NotificationDAO     |
|   ReviewDAO                                                           |
+-----------------------------------------------------------------------+
                                   |  (JDBC Driver / Parameterized SQL)
                                   v
+-----------------------------------------------------------------------+
|                    PERSISTENCE LAYER (MySQL 8.0)                      |
|   Database: borrowbuddy_db (InnoDB Engine, utf8mb4)                  |
|   9 Normalized Tables with Foreign Keys, Check Constraints & Indexes   |
+-----------------------------------------------------------------------+
```

---

## 7. Data Definition Language (DDL) Commands

The DDL commands implemented in MySQL 8.0 define the 9 tables, constraints, foreign keys, and check rules:

```sql
CREATE DATABASE IF NOT EXISTS borrowbuddy_db;
USE borrowbuddy_db;

-- 1. USERS
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT','FACULTY','ADMIN') NOT NULL,
    department VARCHAR(100),
    phone VARCHAR(20),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CATEGORIES
CREATE TABLE categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. ITEMS
CREATE TABLE items (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    description TEXT,
    item_condition VARCHAR(50),
    image_url VARCHAR(500),
    availability_status ENUM(
        'AVAILABLE','UNAVAILABLE','BORROWED'
    ) DEFAULT 'AVAILABLE',
    allow_borrowing BOOLEAN DEFAULT TRUE,
    allow_temporary_exchange BOOLEAN DEFAULT TRUE,
    allow_permanent_exchange BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 4. TRANSACTIONS
CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_type ENUM(
        'BORROW','TEMPORARY_EXCHANGE','PERMANENT_EXCHANGE'
    ) NOT NULL,
    requester_id BIGINT NOT NULL,
    responder_id BIGINT NOT NULL,
    requested_item_id BIGINT NOT NULL,
    offered_item_id BIGINT,
    start_date DATE,
    due_date DATE,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM(
        'PENDING','ACCEPTED','REJECTED','ACTIVE',
        'PARTIALLY_RETURNED','COMPLETED','CANCELLED','OVERDUE'
    ) DEFAULT 'PENDING',
    request_message TEXT,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (requester_id) REFERENCES users(user_id),
    FOREIGN KEY (responder_id) REFERENCES users(user_id),
    FOREIGN KEY (requested_item_id) REFERENCES items(item_id),
    FOREIGN KEY (offered_item_id) REFERENCES items(item_id),
    CHECK (requester_id <> responder_id),
    CHECK (
        transaction_type <> 'BORROW'
        OR offered_item_id IS NULL
    ),
    CHECK (
        transaction_type = 'BORROW'
        OR offered_item_id IS NOT NULL
    )
);

-- 5. TRANSACTION_APPROVALS
CREATE TABLE transaction_approvals (
    approval_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    decision ENUM('PENDING','ACCEPTED','REJECTED')
        DEFAULT 'PENDING',
    decision_date TIMESTAMP NULL,
    UNIQUE (transaction_id, user_id),
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 6. TRANSACTION_RETURNS
CREATE TABLE transaction_returns (
    return_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    returned_by BIGINT NOT NULL,
    confirmed_by BIGINT,
    returned_at TIMESTAMP NULL,
    status ENUM('PENDING','RETURNED') DEFAULT 'PENDING',
    UNIQUE (transaction_id, item_id),
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id),
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (returned_by) REFERENCES users(user_id),
    FOREIGN KEY (confirmed_by) REFERENCES users(user_id)
);

-- 7. ITEM_OWNERSHIP_HISTORY
CREATE TABLE item_ownership_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    previous_owner_id BIGINT NOT NULL,
    new_owner_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (previous_owner_id) REFERENCES users(user_id),
    FOREIGN KEY (new_owner_id) REFERENCES users(user_id),
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id)
);

-- 8. NOTIFICATIONS
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    transaction_id BIGINT,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id)
);

-- 9. REVIEWS
CREATE TABLE reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL,
    comments TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id),
    CHECK (rating BETWEEN 1 AND 5)
);
```

---

## 8. Data Manipulation Language (DML) & Verification Outputs

### 8.1 Verification of Clean Table Counts (Test 3)
```sql
USE borrowbuddy_db;
SELECT 'users' AS table_name, COUNT(*) AS total FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'items', COUNT(*) FROM items
UNION ALL SELECT 'transactions', COUNT(*) FROM transactions
UNION ALL SELECT 'transaction_approvals', COUNT(*) FROM transaction_approvals
UNION ALL SELECT 'transaction_returns', COUNT(*) FROM transaction_returns
UNION ALL SELECT 'item_ownership_history', COUNT(*) FROM item_ownership_history
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'reviews', COUNT(*) FROM reviews;
```

**Actual MySQL Workbench Result (Result #2):**
```text
+------------------------+-------+
| table_name             | total |
+------------------------+-------+
| users                  | 5     |
| categories             | 5     |
| items                  | 5     |
| transactions           | 5     |
| transaction_approvals  | 5     |
| transaction_returns    | 5     |
| item_ownership_history | 5     |
| notifications          | 5     |
| reviews                | 5     |
+------------------------+-------+
```

### 8.2 Test 1: Display All Users
```sql
SELECT * FROM users;
```

**Actual Output:**
```text
+---------+--------------+-------------------+---------------+---------+------------------+-------+---------------------+
| user_id | name         | email             | password_hash | role    | department       | phone | registration_date   |
+---------+--------------+-------------------+---------------+---------+------------------+-------+---------------------+
|       1 | Aarav Sharma | aarav@campus.edu  | [BCrypt/Demo] | STUDENT | Computer Science | NULL  | 2026-10-20 18:39:00 |
|       2 | Priya Patel  | priya@campus.edu  | [BCrypt/Demo] | STUDENT | Electronics      | NULL  | 2026-10-20 18:39:00 |
|       3 | Rahul Mehta  | rahul@campus.edu  | [BCrypt/Demo] | FACULTY | Computer Science | NULL  | 2026-10-20 18:39:00 |
|       4 | Neha Singh   | neha@campus.edu   | [BCrypt/Demo] | FACULTY | Mechanical       | NULL  | 2026-10-20 18:39:00 |
|       5 | Admin User   | admin@campus.edu  | [BCrypt/Demo] | ADMIN   | Administration   | NULL  | 2026-10-20 18:39:00 |
+---------+--------------+-------------------+---------------+---------+------------------+-------+---------------------+
```

### 8.3 Test 2: Display All Items with Owner & Category
```sql
SELECT
    i.item_id,
    i.item_name,
    u.name AS owner_name,
    c.category_name,
    i.availability_status
FROM items i
JOIN users u ON i.owner_id = u.user_id
JOIN categories c ON i.category_id = c.category_id;
```

**Actual Output:**
```text
+---------+-----------------------+--------------+---------------+---------------------+
| item_id | item_name             | owner_name   | category_name | availability_status |
+---------+-----------------------+--------------+---------------+---------------------+
|       1 | DBMS Textbook         | Aarav Sharma | Books         | AVAILABLE           |
|       2 | Scientific Calculator | Priya Patel  | Electronics   | AVAILABLE           |
|       3 | Digital Multimeter    | Rahul Mehta  | Lab Equipment | AVAILABLE           |
|       4 | Football              | Neha Singh   | Sports        | AVAILABLE           |
|       5 | Drawing Kit           | Admin User   | Stationery    | AVAILABLE           |
+---------+-----------------------+--------------+---------------+---------------------+
```

### 8.4 Test 5: Verify Invalid Rating Rejection (Check Constraint Verification)
```sql
INSERT INTO reviews
(user_id, item_id, transaction_id, rating, comments)
VALUES
(1, 2, 1, 8, 'Invalid rating test');
```

**Actual MySQL Output:**
```text
ERROR 3819 (HY000): Check constraint 'reviews_chk_1' is violated.
```

---

## 9. Automated JUnit 5 Test Suite Results

```text
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.borrowbuddy.service.AuthServiceTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.520 s
Running com.borrowbuddy.service.ItemServiceTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.741 s
Running com.borrowbuddy.service.TransactionServiceTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.126 s
Running com.borrowbuddy.util.PasswordUtilTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.094 s

Results:
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 10. Conclusion
The BorrowBuddy Campus Borrow & Exchange Management Portal database was successfully designed and validated using MySQL 8.0. DDL commands were used to create tables with primary keys, foreign keys, and integrity constraints. DML operations, including INSERT, UPDATE, and DELETE, were performed to manage records. SQL queries were executed to verify data accuracy, relationships, and database integrity. Thus, the project demonstrates effective database design and management for borrowing, temporary exchange, and permanent exchange of campus items.
