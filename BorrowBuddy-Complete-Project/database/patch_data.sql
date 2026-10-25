-- ============================================================
-- BorrowBuddy – Data Patch Part 2: Transactions, Approvals, Returns,
--               Notifications, Reviews for Aarav Sharma
-- Item IDs: 1=DBMS Textbook(Aarav), 2=Calculator(Priya), 3=Multimeter(Rahul),
--           4=Football(Neha), 5=Drawing Kit(Admin)
--           16=DSA Book(Aarav), 17=USB-C Hub(Aarav), 18=Geometry Set(Aarav)
--           19=Python Book(Priya), 20=BT Speaker(Priya), 21=Ukulele(Priya)
--           22=Oscilloscope(Rahul), 23=EngMaths(Rahul)
--           24=WhiteboardMarkers(Neha), 25=Screwdriver(Neha)
-- ============================================================
USE borrowbuddy_db;

-- ── 1. TRANSACTIONS ──────────────────────────────────────────

-- [T6] PENDING incoming – Priya wants Aarav's DSA Book
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('BORROW', 2, 1, 16, NULL, '2026-10-27', '2026-11-10',
    'PENDING', 'Hi Aarav! Could I borrow your DSA book for exam prep?',
    '2026-10-24 10:15:00');

-- [T7] PENDING incoming – Rahul wants Aarav's Geometry Set, offers Engineering Maths
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('TEMPORARY_EXCHANGE', 3, 1, 18, 23, '2026-10-28', '2026-11-15',
    'PENDING', 'Hi Aarav! I need your geometry set for my drafting project. I can offer Engineering Maths in exchange.',
    '2026-10-24 14:30:00');

-- [T8] PENDING outgoing – Aarav wants Priya's Python Book
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('BORROW', 1, 2, 19, NULL, '2026-10-28', '2026-11-05',
    'PENDING', 'Hi Priya! Can I borrow your Python crash course? I want to practise for the hackathon.',
    '2026-10-25 09:00:00');

-- [T9] PENDING outgoing – Aarav offers USB-C Hub for Priya Ukulele (permanent)
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('PERMANENT_EXCHANGE', 1, 2, 21, 17, NULL, NULL,
    'PENDING', 'Hey Priya, I would love your ukulele! Happy to give my USB-C hub in exchange permanently.',
    '2026-10-25 11:00:00');

-- [T10] ACTIVE – Aarav borrowing Priya's Bluetooth Speaker
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('BORROW', 1, 2, 20, NULL, '2026-10-20', '2026-11-01',
    'ACTIVE', 'Need speaker for college fest preparations.',
    '2026-10-19 16:00:00');

-- [T11] ACTIVE – Aarav temp exchange with Rahul (Aarav DBMS Textbook <-> Rahul Oscilloscope)
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('TEMPORARY_EXCHANGE', 1, 3, 22, 1, '2026-10-15', '2026-10-31',
    'ACTIVE', 'Swapping textbook for oscilloscope for the lab project week.',
    '2026-10-14 12:00:00');

-- [T12] COMPLETED – Aarav borrowed Neha's Football (returned)
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at, completed_at)
VALUES ('BORROW', 1, 4, 4, NULL, '2026-09-10', '2026-09-17',
    'COMPLETED', 'Need the football for inter-department match.',
    '2026-09-09 10:00:00', '2026-09-17 18:00:00');

-- [T13] COMPLETED – Priya borrowed Aarav's DBMS Textbook, returned
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at, completed_at)
VALUES ('BORROW', 2, 1, 1, NULL, '2026-09-20', '2026-09-30',
    'COMPLETED', 'Need DBMS textbook for database assignment.',
    '2026-09-19 09:00:00', '2026-09-30 17:00:00');

-- [T14] COMPLETED – Aarav temp exchange with Neha (USB-C Hub <-> Screwdriver)
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at, completed_at)
VALUES ('TEMPORARY_EXCHANGE', 1, 4, 25, 17, '2026-10-01', '2026-10-10',
    'COMPLETED', 'Swapping for the project presentation week.',
    '2026-09-30 11:00:00', '2026-10-10 15:00:00');

-- [T15] REJECTED – Rahul rejected Aarav's borrow request for Oscilloscope
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('BORROW', 1, 3, 22, NULL, '2026-10-05', '2026-10-12',
    'REJECTED', 'Can I borrow your pocket oscilloscope for my circuit lab?',
    '2026-10-04 14:00:00');

-- [T16] CANCELLED – Aarav cancelled his borrow for Engineering Maths
INSERT INTO transactions (transaction_type, requester_id, responder_id,
    requested_item_id, offered_item_id, start_date, due_date,
    status, request_message, requested_at)
VALUES ('BORROW', 1, 3, 23, NULL, '2026-10-08', '2026-10-18',
    'CANCELLED', 'Borrow Kreyszig for exam week.',
    '2026-10-06 09:00:00');

-- ── 2. TRANSACTION_APPROVALS ──────────────────────────────────
-- Re-check transaction IDs first (they may be 6-16 or different depending on sequence)
-- We use the known IDs from fresh inserts in order: 6,7,8,9,10,11,12,13,14,15,16

INSERT INTO transaction_approvals (transaction_id, user_id, decision)
    SELECT t.transaction_id, 1, 'PENDING'
    FROM transactions t WHERE t.request_message = 'Hi Aarav! Could I borrow your DSA book for exam prep?' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision)
    SELECT t.transaction_id, 1, 'PENDING'
    FROM transactions t WHERE t.request_message LIKE 'Hi Aarav! I need your geometry set%' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision)
    SELECT t.transaction_id, 2, 'PENDING'
    FROM transactions t WHERE t.request_message LIKE 'Hi Priya! Can I borrow your Python%' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision)
    SELECT t.transaction_id, 2, 'PENDING'
    FROM transactions t WHERE t.request_message LIKE 'Hey Priya, I would love your ukulele%' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision, decision_date)
    SELECT t.transaction_id, 2, 'ACCEPTED', '2026-10-19 17:30:00'
    FROM transactions t WHERE t.request_message = 'Need speaker for college fest preparations.' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision, decision_date)
    SELECT t.transaction_id, 1, 'ACCEPTED', '2026-10-14 13:00:00'
    FROM transactions t WHERE t.request_message = 'Swapping textbook for oscilloscope for the lab project week.' LIMIT 1;

INSERT INTO transaction_approvals (transaction_id, user_id, decision, decision_date)
    SELECT t.transaction_id, 3, 'ACCEPTED', '2026-10-14 13:30:00'
    FROM transactions t WHERE t.request_message = 'Swapping textbook for oscilloscope for the lab project week.' LIMIT 1;

-- ── 3. TRANSACTION_RETURNS ────────────────────────────────────
INSERT INTO transaction_returns (transaction_id, item_id, returned_by, confirmed_by, returned_at, status)
    SELECT t.transaction_id, 4, 1, 4, '2026-09-17 18:00:00', 'CONFIRMED'
    FROM transactions t WHERE t.request_message = 'Need the football for inter-department match.' LIMIT 1;

INSERT INTO transaction_returns (transaction_id, item_id, returned_by, confirmed_by, returned_at, status)
    SELECT t.transaction_id, 1, 2, 1, '2026-09-30 17:00:00', 'CONFIRMED'
    FROM transactions t WHERE t.request_message = 'Need DBMS textbook for database assignment.' LIMIT 1;

INSERT INTO transaction_returns (transaction_id, item_id, returned_by, confirmed_by, returned_at, status)
    SELECT t.transaction_id, 25, 1, 4, '2026-10-10 14:00:00', 'CONFIRMED'
    FROM transactions t WHERE t.request_message = 'Swapping for the project presentation week.' LIMIT 1;

INSERT INTO transaction_returns (transaction_id, item_id, returned_by, confirmed_by, returned_at, status)
    SELECT t.transaction_id, 17, 4, 1, '2026-10-10 15:00:00', 'CONFIRMED'
    FROM transactions t WHERE t.request_message = 'Swapping for the project presentation week.' LIMIT 1;

-- ── 4. NOTIFICATIONS ──────────────────────────────────────────
TRUNCATE TABLE notifications;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '📨 New borrow request from Priya Patel for your DSA Book.',
    'REQUEST_SUBMITTED', 0, '2026-10-24 10:16:00'
FROM transactions t WHERE t.request_message = 'Hi Aarav! Could I borrow your DSA book for exam prep?' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '📨 Rahul Mehta wants to temporarily exchange your Geometry Set.',
    'REQUEST_SUBMITTED', 0, '2026-10-24 14:31:00'
FROM transactions t WHERE t.request_message LIKE 'Hi Aarav! I need your geometry set%' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '✅ Priya Patel accepted your borrow request for Bluetooth Speaker.',
    'REQUEST_ACCEPTED', 1, '2026-10-19 17:31:00'
FROM transactions t WHERE t.request_message = 'Need speaker for college fest preparations.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '🔄 Your borrow of Bluetooth Speaker is now ACTIVE. Due: 01 Nov 2026.',
    'ACTIVATED', 1, '2026-10-19 17:32:00'
FROM transactions t WHERE t.request_message = 'Need speaker for college fest preparations.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '🔄 Temporary exchange with Rahul Mehta is now ACTIVE. Due: 31 Oct 2026.',
    'ACTIVATED', 1, '2026-10-14 13:31:00'
FROM transactions t WHERE t.request_message = 'Swapping textbook for oscilloscope for the lab project week.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '✔️ Return of Football confirmed by Neha Singh. Transaction complete!',
    'RETURN_CONFIRMED', 1, '2026-09-17 18:01:00'
FROM transactions t WHERE t.request_message = 'Need the football for inter-department match.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '✔️ Priya Patel returned your DBMS Textbook. Transaction complete!',
    'RETURN_CONFIRMED', 1, '2026-09-30 17:01:00'
FROM transactions t WHERE t.request_message = 'Need DBMS textbook for database assignment.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '✔️ Temporary exchange with Neha Singh completed successfully!',
    'RETURN_CONFIRMED', 1, '2026-10-10 15:01:00'
FROM transactions t WHERE t.request_message = 'Swapping for the project presentation week.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '❌ Rahul Mehta rejected your borrow request for Oscilloscope.',
    'REQUEST_REJECTED', 1, '2026-10-04 16:00:00'
FROM transactions t WHERE t.request_message = 'Can I borrow your pocket oscilloscope for my circuit lab?' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '⏳ Your borrow request for Python Book sent to Priya. Awaiting approval.',
    'REQUEST_SUBMITTED', 0, '2026-10-25 09:01:00'
FROM transactions t WHERE t.request_message LIKE 'Hi Priya! Can I borrow your Python%' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 1, t.transaction_id,
    '⏳ Your permanent exchange offer for Ukulele has been sent to Priya.',
    'REQUEST_SUBMITTED', 0, '2026-10-25 11:01:00'
FROM transactions t WHERE t.request_message LIKE 'Hey Priya, I would love your ukulele%' LIMIT 1;

-- Priya notifications
INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 2, t.transaction_id,
    '📨 Aarav Sharma wants to borrow your Python Programming book.',
    'REQUEST_SUBMITTED', 0, '2026-10-25 09:01:00'
FROM transactions t WHERE t.request_message LIKE 'Hi Priya! Can I borrow your Python%' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 2, t.transaction_id,
    '📨 Aarav Sharma offers USB-C Hub for your Ukulele (permanent exchange).',
    'REQUEST_SUBMITTED', 0, '2026-10-25 11:01:00'
FROM transactions t WHERE t.request_message LIKE 'Hey Priya, I would love your ukulele%' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 2, t.transaction_id,
    '✔️ DBMS Textbook returned to Aarav Sharma. Transaction complete!',
    'RETURN_CONFIRMED', 1, '2026-09-30 17:02:00'
FROM transactions t WHERE t.request_message = 'Need DBMS textbook for database assignment.' LIMIT 1;

-- Rahul notifications
INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 3, t.transaction_id,
    '⏳ Your temp exchange request for Aarav Sharma Geometry Set is pending.',
    'REQUEST_SUBMITTED', 0, '2026-10-24 14:32:00'
FROM transactions t WHERE t.request_message LIKE 'Hi Aarav! I need your geometry set%' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 3, t.transaction_id,
    '✅ Aarav Sharma accepted the temporary exchange. Now ACTIVE.',
    'REQUEST_ACCEPTED', 1, '2026-10-14 13:01:00'
FROM transactions t WHERE t.request_message = 'Swapping textbook for oscilloscope for the lab project week.' LIMIT 1;

-- Neha notifications
INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 4, t.transaction_id,
    '📨 Aarav Sharma is borrowing your Football. Return due 17 Sep 2026.',
    'ACTIVATED', 1, '2026-09-10 11:00:00'
FROM transactions t WHERE t.request_message = 'Need the football for inter-department match.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 4, t.transaction_id,
    '✔️ Football returned by Aarav Sharma. Transaction complete!',
    'RETURN_CONFIRMED', 1, '2026-09-17 18:02:00'
FROM transactions t WHERE t.request_message = 'Need the football for inter-department match.' LIMIT 1;

INSERT INTO notifications (user_id, transaction_id, message, type, is_read, created_at)
SELECT 4, t.transaction_id,
    '✔️ Temporary exchange with Aarav Sharma completed successfully!',
    'RETURN_CONFIRMED', 1, '2026-10-10 15:02:00'
FROM transactions t WHERE t.request_message = 'Swapping for the project presentation week.' LIMIT 1;

-- ── 5. REVIEWS ────────────────────────────────────────────────
TRUNCATE TABLE reviews;

INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments, review_date)
SELECT 1, 4, t.transaction_id,
    5, 'Great football, perfect for the inter-department match! Neha was very cooperative.', '2026-09-18 10:00:00'
FROM transactions t WHERE t.request_message = 'Need the football for inter-department match.' LIMIT 1;

INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments, review_date)
SELECT 2, 1, t.transaction_id,
    4, 'DBMS textbook was very helpful for the database assignment. Returned in perfect condition.', '2026-10-01 09:00:00'
FROM transactions t WHERE t.request_message = 'Need DBMS textbook for database assignment.' LIMIT 1;

INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments, review_date)
SELECT 1, 25, t.transaction_id,
    5, 'Cordless screwdriver saved so much time during the project! Smooth exchange with Neha.', '2026-10-11 11:00:00'
FROM transactions t WHERE t.request_message = 'Swapping for the project presentation week.' LIMIT 1;

INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments, review_date)
    VALUES (1, 2, 1, 4, 'Calculator was very handy for the semester tests. Returned in same condition.', '2026-09-08 12:00:00');

INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments, review_date)
    VALUES (2, 3, 2, 5, 'Excellent multimeter! Rahul was very helpful and prompt. Would exchange again.', '2026-09-08 14:00:00');

-- ── 6. Mark items in ACTIVE transactions as BORROWED ─────────
UPDATE items SET availability_status = 'BORROWED'
WHERE item_id IN (
    SELECT requested_item_id FROM transactions WHERE status='ACTIVE'
    UNION
    SELECT offered_item_id FROM transactions WHERE status='ACTIVE' AND offered_item_id IS NOT NULL
);

-- ── 7. Verify ────────────────────────────────────────────────
SELECT 'items'         AS tbl, COUNT(*) AS cnt FROM items
UNION ALL SELECT 'transactions',  COUNT(*) FROM transactions
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'reviews',       COUNT(*) FROM reviews
UNION ALL SELECT 'approvals',     COUNT(*) FROM transaction_approvals
UNION ALL SELECT 'returns',       COUNT(*) FROM transaction_returns;
