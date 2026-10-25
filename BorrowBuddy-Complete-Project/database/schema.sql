-- BorrowBuddy Database Schema (STAGE 2 IMPLEMENTATION)
-- Campus Borrow & Exchange Management Portal
-- MySQL 8.0+

DROP DATABASE IF EXISTS borrowbuddy_db;
CREATE DATABASE borrowbuddy_db;
USE borrowbuddy_db;

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
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
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. ITEMS
CREATE TABLE IF NOT EXISTS items (
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
CREATE TABLE IF NOT EXISTS transactions (
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
CREATE TABLE IF NOT EXISTS transaction_approvals (
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
CREATE TABLE IF NOT EXISTS transaction_returns (
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
CREATE TABLE IF NOT EXISTS item_ownership_history (
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
CREATE TABLE IF NOT EXISTS notifications (
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
CREATE TABLE IF NOT EXISTS reviews (
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
