-- BorrowBuddy Sample Data (STAGE 2 IMPLEMENTATION)
-- Campus Borrow & Exchange Management Portal

USE borrowbuddy_db;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reviews;
TRUNCATE TABLE notifications;
TRUNCATE TABLE item_ownership_history;
TRUNCATE TABLE transaction_returns;
TRUNCATE TABLE transaction_approvals;
TRUNCATE TABLE transactions;
TRUNCATE TABLE items;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS
-- Standard password for Aarav, Priya, Rahul, Neha: 'Password@123'
-- Admin password for Admin User: 'Admin@123'
INSERT INTO users (user_id, name, email, password_hash, role, department) VALUES
(1, 'Aarav Sharma', 'aarav@campus.edu', '$2a$12$REu6WGvAurLtvlF6XvNlO.DM0IrUALR2m9HbnSxjMQXNTBQHUWw0i', 'STUDENT', 'Computer Science'),
(2, 'Priya Patel', 'priya@campus.edu', '$2a$12$REu6WGvAurLtvlF6XvNlO.DM0IrUALR2m9HbnSxjMQXNTBQHUWw0i', 'STUDENT', 'Electronics'),
(3, 'Rahul Mehta', 'rahul@campus.edu', '$2a$12$REu6WGvAurLtvlF6XvNlO.DM0IrUALR2m9HbnSxjMQXNTBQHUWw0i', 'FACULTY', 'Computer Science'),
(4, 'Neha Singh', 'neha@campus.edu', '$2a$12$REu6WGvAurLtvlF6XvNlO.DM0IrUALR2m9HbnSxjMQXNTBQHUWw0i', 'FACULTY', 'Mechanical'),
(5, 'Admin User', 'admin@campus.edu', '$2a$12$JCiOCNTggIRGjgfl.icRfOFtoxwfG0/IsPTIaiKYrV8yBINwjxrhu', 'ADMIN', 'Administration');

-- 2. CATEGORIES
INSERT INTO categories (category_id, category_name, description) VALUES
(1, 'Books', 'Academic books and textbooks'),
(2, 'Electronics', 'Electronic devices and accessories'),
(3, 'Lab Equipment', 'Laboratory tools and measuring instruments'),
(4, 'Sports', 'Sports and fitness equipment'),
(5, 'Stationery', 'Stationery, drafting and art supplies'),
(6, 'Musical Instruments', 'Guitars, keyboards and musical gear'),
(7, 'Tools', 'Hand and power tools for projects');

-- 3. ITEMS (23 total items, rich campus catalog)
INSERT INTO items (item_id, owner_id, category_id, item_name, description, item_condition, availability_status, allow_borrowing, allow_temporary_exchange, allow_permanent_exchange) VALUES
(1, 1, 1, 'DBMS Textbook', 'Fundamentals of Database Systems by Navathe', 'GOOD', 'BORROWED', 1, 1, 1),
(2, 2, 2, 'Scientific Calculator', 'Casio fx-991EX ClassWiz engineering calculator', 'GOOD', 'AVAILABLE', 1, 1, 1),
(3, 3, 3, 'Digital Multimeter', 'Electrical measurement tool with test probes', 'GOOD', 'AVAILABLE', 1, 1, 1),
(4, 4, 4, 'Football', 'Standard size 5 match football', 'GOOD', 'AVAILABLE', 1, 1, 1),
(5, 5, 5, 'Drawing Kit', 'Engineering drawing kit with T-square and mini-drafter', 'NEW', 'AVAILABLE', 1, 1, 1),
(6, 1, 1, 'Data Structures & Algorithms Book', 'Classic DSA textbook by Cormen (CLRS), 3rd edition', 'GOOD', 'AVAILABLE', 1, 1, 0),
(7, 1, 2, 'USB-C Hub (7-in-1)', 'Anker 7-port USB-C hub with HDMI, SD card, and Ethernet', 'NEW', 'AVAILABLE', 1, 0, 1),
(8, 1, 5, 'Geometry Set', 'Complete geometry drafting box with compass and protractor', 'GOOD', 'AVAILABLE', 1, 1, 0),
(9, 2, 1, 'Python Programming Crash Course', 'No-starch-press Python 3 book, practical exercises', 'GOOD', 'AVAILABLE', 1, 1, 1),
(10, 2, 2, 'Bluetooth Speaker', 'JBL Clip 4 portable speaker with carabiner clip', 'GOOD', 'BORROWED', 1, 1, 0),
(11, 2, 6, 'Ukulele', 'Soprano ukulele with padded carry bag and tuner', 'GOOD', 'AVAILABLE', 1, 0, 1),
(12, 3, 3, 'Oscilloscope (Pocket)', 'Hantek DSO150 handheld oscilloscope for circuit debug', 'GOOD', 'BORROWED', 1, 1, 0),
(13, 3, 1, 'Engineering Mathematics (Vol 1 and 2)', 'Kreyszig Advanced Engineering Mathematics combined', 'FAIR', 'AVAILABLE', 1, 1, 0),
(14, 4, 5, 'Whiteboard Marker Set', 'Pack of 12 Expo dry-erase markers in assorted colours', 'NEW', 'AVAILABLE', 1, 0, 0),
(15, 4, 7, 'Cordless Screwdriver', 'Bosch GO mini 3.6V cordless screwdriver kit', 'GOOD', 'AVAILABLE', 1, 1, 0),
(16, 3, 1, 'Advanced Database Management Systems (Elmasri & Navathe)', 'Comprehensive 7th edition covering indexing and transactions', 'GOOD', 'AVAILABLE', 1, 1, 0),
(17, 2, 2, 'Arduino Mega 2560 Starter Kit', 'Includes board, breadboard, 40+ sensors and RFID module', 'NEW', 'AVAILABLE', 1, 1, 1),
(18, 1, 3, 'Digital Vernier Caliper (Stainless Steel)', 'High accuracy 0-150mm digital caliper with LCD screen', 'NEW', 'AVAILABLE', 1, 1, 0),
(19, 4, 4, 'Yonex Badminton Racket Set with Shuttlecocks', 'Pair of Nanoray rackets with bag and tube of shuttles', 'GOOD', 'AVAILABLE', 1, 0, 0),
(20, 1, 5, 'A3 Professional Sketchpad & Technical Pens', 'Heavyweight 200gsm pad with 5 Micron fineliners', 'NEW', 'AVAILABLE', 1, 0, 0),
(21, 2, 6, 'Acoustic Guitar with Padded Gig Bag', 'Yamaha F310 dreadnought acoustic guitar, fresh strings', 'GOOD', 'AVAILABLE', 1, 1, 1),
(22, 3, 7, 'Temperature Controlled Soldering Station 60W', 'Digital ESD-safe soldering station with brass cleaner', 'GOOD', 'AVAILABLE', 1, 1, 0),
(23, 4, 2, 'STM32 ARM Cortex-M4 Microcontroller Board', 'Discovery board with onboard debugger and sensors', 'NEW', 'AVAILABLE', 1, 1, 0);

-- 4. TRANSACTIONS (Rich set covering all statuses and user views)
INSERT INTO transactions (transaction_id, transaction_type, requester_id, responder_id, requested_item_id, offered_item_id, start_date, due_date, status, request_message, requested_at, completed_at) VALUES
-- Past completed transactions
(1, 'BORROW', 1, 2, 2, NULL, '2026-09-01', '2026-09-07', 'COMPLETED', 'Request to borrow calculator', '2026-09-01 09:00:00', '2026-09-07 17:00:00'),
(2, 'TEMPORARY_EXCHANGE', 2, 3, 3, 2, '2026-09-01', '2026-09-07', 'COMPLETED', 'Exchange calculator for multimeter temporarily', '2026-09-01 10:00:00', '2026-09-07 18:00:00'),
(3, 'PERMANENT_EXCHANGE', 3, 4, 4, 3, NULL, NULL, 'COMPLETED', 'Permanent exchange proposal', '2026-09-02 11:00:00', '2026-09-03 14:00:00'),
(4, 'BORROW', 4, 5, 5, NULL, '2026-09-02', '2026-09-08', 'COMPLETED', 'Request to borrow drawing kit', '2026-09-02 12:00:00', '2026-09-08 16:00:00'),
(5, 'PERMANENT_EXCHANGE', 1, 2, 2, 1, NULL, NULL, 'COMPLETED', 'Permanent calculator and textbook exchange', '2026-09-05 14:00:00', '2026-09-06 11:00:00'),
(6, 'BORROW', 1, 4, 4, NULL, '2026-09-10', '2026-09-17', 'COMPLETED', 'Need the football for inter-department match', '2026-09-09 10:00:00', '2026-09-17 18:00:00'),
(7, 'BORROW', 2, 1, 1, NULL, '2026-09-20', '2026-09-30', 'COMPLETED', 'Need DBMS textbook for database assignment', '2026-09-19 09:00:00', '2026-09-30 17:00:00'),
(8, 'TEMPORARY_EXCHANGE', 1, 4, 15, 7, '2026-10-01', '2026-10-10', 'COMPLETED', 'Swapping for the project presentation week', '2026-09-30 11:00:00', '2026-10-10 15:00:00'),
-- Incoming Pending requests for Aarav (responder_id=1)
(9, 'BORROW', 2, 1, 6, NULL, '2026-10-27', '2026-11-10', 'PENDING', 'Hi Aarav! Could I borrow your DSA book for exam prep?', '2026-10-24 10:15:00', NULL),
(10, 'TEMPORARY_EXCHANGE', 3, 1, 8, 13, '2026-10-28', '2026-11-15', 'PENDING', 'Hi Aarav! Need geometry set for drafting. Offering Engineering Maths in exchange.', '2026-10-24 14:30:00', NULL),
-- Outgoing Pending requests from Aarav (requester_id=1)
(11, 'BORROW', 1, 2, 9, NULL, '2026-10-28', '2026-11-05', 'PENDING', 'Hi Priya! Can I borrow your Python crash course for the hackathon?', '2026-10-25 09:00:00', NULL),
(12, 'PERMANENT_EXCHANGE', 1, 2, 11, 7, NULL, NULL, 'PENDING', 'Hey Priya, love your ukulele! Offering my USB-C hub in permanent exchange.', '2026-10-25 11:00:00', NULL),
-- Active transactions for Aarav
(13, 'BORROW', 1, 2, 10, NULL, '2026-10-20', '2026-11-01', 'ACTIVE', 'Need speaker for college fest preparations.', '2026-10-19 16:00:00', NULL),
(14, 'TEMPORARY_EXCHANGE', 1, 3, 12, 1, '2026-10-15', '2026-10-31', 'ACTIVE', 'Swapping textbook for oscilloscope for lab project week.', '2026-10-14 12:00:00', NULL),
-- Rejected and Cancelled for Aarav history
(15, 'BORROW', 1, 3, 12, NULL, '2026-10-05', '2026-10-12', 'REJECTED', 'Can I borrow your pocket oscilloscope for circuit lab?', '2026-10-04 14:00:00', NULL),
(16, 'BORROW', 1, 3, 13, NULL, '2026-10-08', '2026-10-18', 'CANCELLED', 'Borrow Kreyszig for exam week.', '2026-10-06 09:00:00', NULL);

-- 5. TRANSACTION_APPROVALS
INSERT INTO transaction_approvals (approval_id, transaction_id, user_id, decision, decision_date) VALUES
(1, 1, 2, 'ACCEPTED', '2026-09-01 10:00:00'),
(2, 2, 3, 'ACCEPTED', '2026-09-01 11:00:00'),
(3, 3, 4, 'ACCEPTED', '2026-09-02 12:00:00'),
(4, 4, 5, 'ACCEPTED', '2026-09-02 13:00:00'),
(5, 5, 2, 'ACCEPTED', '2026-09-05 15:00:00'),
(6, 6, 4, 'ACCEPTED', '2026-09-09 11:00:00'),
(7, 7, 1, 'ACCEPTED', '2026-09-19 10:00:00'),
(8, 8, 4, 'ACCEPTED', '2026-09-30 12:00:00'),
(9, 9, 1, 'PENDING', NULL),
(10, 10, 1, 'PENDING', NULL),
(11, 11, 2, 'PENDING', NULL),
(12, 12, 2, 'PENDING', NULL),
(13, 13, 2, 'ACCEPTED', '2026-10-19 17:30:00'),
(14, 14, 1, 'ACCEPTED', '2026-10-14 13:00:00'),
(15, 14, 3, 'ACCEPTED', '2026-10-14 13:30:00');

-- 6. TRANSACTION_RETURNS
INSERT INTO transaction_returns (return_id, transaction_id, item_id, returned_by, confirmed_by, returned_at, status) VALUES
(1, 1, 2, 1, 2, '2026-09-07 17:00:00', 'RETURNED'),
(2, 2, 3, 2, 3, '2026-09-07 18:00:00', 'RETURNED'),
(3, 4, 5, 4, 5, '2026-09-08 16:00:00', 'RETURNED'),
(4, 6, 4, 1, 4, '2026-09-17 18:00:00', 'RETURNED'),
(5, 7, 1, 2, 1, '2026-09-30 17:00:00', 'RETURNED'),
(6, 8, 15, 1, 4, '2026-10-10 14:00:00', 'RETURNED'),
(7, 8, 7, 4, 1, '2026-10-10 15:00:00', 'RETURNED');

-- 7. ITEM_OWNERSHIP_HISTORY
INSERT INTO item_ownership_history (history_id, item_id, previous_owner_id, new_owner_id, transaction_id, transfer_date) VALUES
(1, 3, 3, 4, 3, '2026-09-03 14:00:00'),
(2, 4, 4, 3, 3, '2026-09-03 14:00:00'),
(3, 2, 2, 1, 5, '2026-09-06 11:00:00'),
(4, 1, 1, 2, 5, '2026-09-06 11:00:00');

-- 8. NOTIFICATIONS
INSERT INTO notifications (notification_id, user_id, transaction_id, message, type, is_read, created_at) VALUES
(1, 1, 9,  '📨 New borrow request from Priya Patel for your DSA Book.', 'REQUEST_SUBMITTED', 0, '2026-10-24 10:16:00'),
(2, 1, 10, '📨 Rahul Mehta wants to temporarily exchange your Geometry Set.', 'REQUEST_SUBMITTED', 0, '2026-10-24 14:31:00'),
(3, 1, 13, '✅ Priya Patel accepted your borrow request for Bluetooth Speaker.', 'REQUEST_ACCEPTED', 1, '2026-10-19 17:31:00'),
(4, 1, 13, '🔄 Your borrow of Bluetooth Speaker is now ACTIVE. Due: 01 Nov 2026.', 'ACTIVATED', 1, '2026-10-19 17:32:00'),
(5, 1, 14, '🔄 Temporary exchange with Rahul Mehta is now ACTIVE. Due: 31 Oct 2026.', 'ACTIVATED', 1, '2026-10-14 13:31:00'),
(6, 1, 6,  '✔️ Return of Football confirmed by Neha Singh. Transaction complete!', 'RETURN_CONFIRMED', 1, '2026-09-17 18:01:00'),
(7, 1, 7,  '✔️ Priya Patel returned your DBMS Textbook. Transaction complete!', 'RETURN_CONFIRMED', 1, '2026-09-30 17:01:00'),
(8, 1, 8,  '✔️ Temporary exchange with Neha Singh completed successfully!', 'RETURN_CONFIRMED', 1, '2026-10-10 15:01:00'),
(9, 1, 15, '❌ Rahul Mehta rejected your borrow request for Oscilloscope.', 'REQUEST_REJECTED', 1, '2026-10-04 16:00:00'),
(10, 1, 11, '⏳ Your borrow request for Python Book sent to Priya. Awaiting approval.', 'REQUEST_SUBMITTED', 0, '2026-10-25 09:01:00'),
(11, 1, 12, '⏳ Your permanent exchange offer for Ukulele has been sent to Priya.', 'REQUEST_SUBMITTED', 0, '2026-10-25 11:01:00'),
(12, 2, 11, '📨 Aarav Sharma wants to borrow your Python Programming book.', 'REQUEST_SUBMITTED', 0, '2026-10-25 09:01:00'),
(13, 2, 12, '📨 Aarav Sharma offers USB-C Hub for your Ukulele (permanent exchange).', 'REQUEST_SUBMITTED', 0, '2026-10-25 11:01:00'),
(14, 2, 7,  '✔️ DBMS Textbook returned to Aarav Sharma. Transaction complete!', 'RETURN_CONFIRMED', 1, '2026-09-30 17:02:00'),
(15, 3, 10, '⏳ Your temp exchange request for Aarav Sharma Geometry Set is pending.', 'REQUEST_SUBMITTED', 0, '2026-10-24 14:32:00'),
(16, 3, 14, '✅ Aarav Sharma accepted the temporary exchange. Now ACTIVE.', 'REQUEST_ACCEPTED', 1, '2026-10-14 13:01:00'),
(17, 4, 6,  '📨 Aarav Sharma is borrowing your Football. Return due 17 Sep 2026.', 'ACTIVATED', 1, '2026-09-10 11:00:00'),
(18, 4, 6,  '✔️ Football returned by Aarav Sharma. Transaction complete!', 'RETURN_CONFIRMED', 1, '2026-09-17 18:02:00'),
(19, 4, 8,  '✔️ Temporary exchange with Aarav Sharma completed successfully!', 'RETURN_CONFIRMED', 1, '2026-10-10 15:02:00');

-- 9. REVIEWS
INSERT INTO reviews (review_id, user_id, item_id, transaction_id, rating, comments, review_date) VALUES
(1, 1, 2, 1, 4, 'Calculator was very handy for the semester tests. Returned in original condition.', '2026-09-08 12:00:00'),
(2, 2, 3, 2, 5, 'Excellent multimeter! Rahul was very helpful and prompt. Would exchange again.', '2026-09-08 14:00:00'),
(3, 1, 4, 6, 5, 'Great football, perfect for the inter-department match! Neha was very cooperative.', '2026-09-18 10:00:00'),
(4, 2, 1, 7, 4, 'DBMS textbook was very helpful for database assignment. Returned in perfect shape.', '2026-10-01 09:00:00'),
(5, 1, 15, 8, 5, 'Cordless screwdriver saved so much time during our robotics assembly! Great exchange.', '2026-10-11 11:00:00');
