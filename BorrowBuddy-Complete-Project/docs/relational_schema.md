# BorrowBuddy – Relational Schema & Normalization Analysis

## 1. Relational Schema Specification

The relational database schema for `borrowbuddy_db` is defined using formal relational database notation:
- Primary keys are designated with **PK**.
- Foreign keys are designated with **FK -> TargetTable(column)**.
- Candidate keys / Unique attributes are designated with **UNIQUE**.
- Nullable attributes are designated with **NULL**.

```text
USERS (
    user_id INT PK,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    registration_date TIMESTAMP NOT NULL
)

CATEGORIES (
    category_id INT PK,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NULL
)

ITEMS (
    item_id INT PK,
    owner_id INT NOT NULL FK -> USERS(user_id),
    category_id INT NOT NULL FK -> CATEGORIES(category_id),
    item_name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    item_condition VARCHAR(20) NOT NULL,
    image_url VARCHAR(500) NULL,
    availability_status VARCHAR(20) NOT NULL,
    allow_borrowing BOOLEAN NOT NULL,
    allow_temporary_exchange BOOLEAN NOT NULL,
    allow_permanent_exchange BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
)

TRANSACTIONS (
    transaction_id INT PK,
    transaction_type VARCHAR(30) NOT NULL,
    requester_id INT NOT NULL FK -> USERS(user_id),
    responder_id INT NOT NULL FK -> USERS(user_id),
    requested_item_id INT NOT NULL FK -> ITEMS(item_id),
    offered_item_id INT NULL FK -> ITEMS(item_id),
    start_date DATE NULL,
    due_date DATE NULL,
    requested_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_message TEXT NULL,
    completed_at TIMESTAMP NULL
)

TRANSACTION_APPROVALS (
    approval_id INT PK,
    transaction_id INT NOT NULL FK -> TRANSACTIONS(transaction_id),
    user_id INT NOT NULL FK -> USERS(user_id),
    decision VARCHAR(20) NOT NULL,
    decision_date TIMESTAMP NULL,
    UNIQUE (transaction_id, user_id)
)

TRANSACTION_RETURNS (
    return_id INT PK,
    transaction_id INT NOT NULL FK -> TRANSACTIONS(transaction_id),
    item_id INT NOT NULL FK -> ITEMS(item_id),
    returned_by INT NOT NULL FK -> USERS(user_id),
    confirmed_by INT NULL FK -> USERS(user_id),
    returned_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    UNIQUE (transaction_id, item_id)
)

ITEM_OWNERSHIP_HISTORY (
    history_id INT PK,
    item_id INT NOT NULL FK -> ITEMS(item_id),
    previous_owner_id INT NOT NULL FK -> USERS(user_id),
    new_owner_id INT NOT NULL FK -> USERS(user_id),
    transaction_id INT NOT NULL FK -> TRANSACTIONS(transaction_id),
    transfer_date TIMESTAMP NOT NULL
)

NOTIFICATIONS (
    notification_id INT PK,
    user_id INT NOT NULL FK -> USERS(user_id),
    transaction_id INT NULL FK -> TRANSACTIONS(transaction_id),
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
)

REVIEWS (
    review_id INT PK,
    user_id INT NOT NULL FK -> USERS(user_id),
    item_id INT NOT NULL FK -> ITEMS(item_id),
    transaction_id INT UNIQUE NOT NULL FK -> TRANSACTIONS(transaction_id),
    rating INT NOT NULL,
    comments TEXT NULL,
    review_date TIMESTAMP NOT NULL
)
```

---

## 2. Functional Dependencies (FDs) Analysis

Let $X \rightarrow Y$ denote that attribute set $X$ functionally determines attribute set $Y$.

### Relation: `USERS`
- Primary Key: `user_id`
- Candidate Key: `email`
- Functional Dependencies:
  - $FD_{U1}: \text{user\_id} \rightarrow \{\text{name}, \text{email}, \text{password\_hash}, \text{role}, \text{department}, \text{phone}, \text{registration\_date}\}$
  - $FD_{U2}: \text{email} \rightarrow \{\text{user\_id}, \text{name}, \text{password\_hash}, \text{role}, \text{department}, \text{phone}, \text{registration\_date}\}$

### Relation: `CATEGORIES`
- Primary Key: `category_id`
- Candidate Key: `category_name`
- Functional Dependencies:
  - $FD_{C1}: \text{category\_id} \rightarrow \{\text{category\_name}, \text{description}\}$
  - $FD_{C2}: \text{category\_name} \rightarrow \{\text{category\_id}, \text{description}\}$

### Relation: `ITEMS`
- Primary Key: `item_id`
- Functional Dependencies:
  - $FD_{I1}: \text{item\_id} \rightarrow \{\text{owner\_id}, \text{category\_id}, \text{item\_name}, \text{description}, \text{item\_condition}, \text{image\_url}, \text{availability\_status}, \text{allow\_borrowing}, \text{allow\_temporary\_exchange}, \text{allow\_permanent\_exchange}, \text{created\_at}, \text{updated\_at}\}$

### Relation: `TRANSACTIONS`
- Primary Key: `transaction_id`
- Functional Dependencies:
  - $FD_{T1}: \text{transaction\_id} \rightarrow \{\text{transaction\_type}, \text{requester\_id}, \text{responder\_id}, \text{requested\_item\_id}, \text{offered\_item\_id}, \text{start\_date}, \text{due\_date}, \text{requested\_at}, \text{status}, \text{request\_message}, \text{completed\_at}\}$

### Relation: `TRANSACTION_APPROVALS`
- Primary Key: `approval_id`
- Candidate Key: `(transaction_id, user_id)`
- Functional Dependencies:
  - $FD_{A1}: \text{approval\_id} \rightarrow \{\text{transaction\_id}, \text{user\_id}, \text{decision}, \text{decision\_date}\}$
  - $FD_{A2}: \{\text{transaction\_id}, \text{user\_id}\} \rightarrow \{\text{approval\_id}, \text{decision}, \text{decision\_date}\}$

### Relation: `TRANSACTION_RETURNS`
- Primary Key: `return_id`
- Candidate Key: `(transaction_id, item_id)`
- Functional Dependencies:
  - $FD_{R1}: \text{return\_id} \rightarrow \{\text{transaction\_id}, \text{item\_id}, \text{returned\_by}, \text{confirmed\_by}, \text{returned\_at}, \text{status}\}$
  - $FD_{R2}: \{\text{transaction\_id}, \text{item\_id}\} \rightarrow \{\text{return\_id}, \text{returned\_by}, \text{confirmed\_by}, \text{returned\_at}, \text{status}\}$

### Relation: `ITEM_OWNERSHIP_HISTORY`
- Primary Key: `history_id`
- Functional Dependencies:
  - $FD_{H1}: \text{history\_id} \rightarrow \{\text{item\_id}, \text{previous\_owner\_id}, \text{new\_owner\_id}, \text{transaction\_id}, \text{transfer\_date}\}$

### Relation: `NOTIFICATIONS`
- Primary Key: `notification_id`
- Functional Dependencies:
  - $FD_{N1}: \text{notification\_id} \rightarrow \{\text{user\_id}, \text{transaction\_id}, \text{message}, \text{type}, \text{is\_read}, \text{created\_at}\}$

### Relation: `REVIEWS`
- Primary Key: `review_id`
- Candidate Key: `transaction_id`
- Functional Dependencies:
  - $FD_{V1}: \text{review\_id} \rightarrow \{\text{user\_id}, \text{item\_id}, \text{transaction\_id}, \text{rating}, \text{comments}, \text{review\_date}\}$
  - $FD_{V2}: \text{transaction\_id} \rightarrow \{\text{review\_id}, \text{user\_id}, \text{item\_id}, \text{rating}, \text{comments}, \text{review\_date}\}$

---

## 3. Normalization Verification (1NF, 2NF, 3NF, BCNF)

### First Normal Form (1NF)
A relation $R$ is in 1NF if and only if:
1. Every attribute domain contains only atomic (indivisible) values.
2. There are no repeating groups or multivalued attributes.
3. Each record is uniquely identified by a primary key.

**Verification for BorrowBuddy:**
- Each attribute holds a single scalar value (e.g. `phone`, `email`, `role`, dates are atomic).
- Array/list attributes such as item transaction history, multiple approvals, multiple returns, or multiple reviews are decomposed into dedicated relations (`ITEM_OWNERSHIP_HISTORY`, `TRANSACTION_APPROVALS`, `TRANSACTION_RETURNS`, `REVIEWS`) rather than stored as comma-separated or JSON lists in `TRANSACTIONS`.
- Therefore, all 9 relations satisfy **1NF**.

### Second Normal Form (2NF)
A relation $R$ is in 2NF if and only if:
1. $R$ is in 1NF.
2. No non-prime attribute is partially dependent on any candidate key of $R$ (i.e. full functional dependency holds).

**Verification for BorrowBuddy:**
- All 9 relations use single-column surrogate primary keys (`user_id`, `category_id`, `item_id`, `transaction_id`, etc.).
- For relations with composite alternate keys (`TRANSACTION_APPROVALS` with `(transaction_id, user_id)` and `TRANSACTION_RETURNS` with `(transaction_id, item_id)`):
  - In `TRANSACTION_APPROVALS`, `decision` and `decision_date` depend on the specific *pair* of transaction and user (neither the transaction alone nor the user alone determines the decision).
  - In `TRANSACTION_RETURNS`, `status`, `returned_by`, and `confirmed_by` depend on the specific item returned *within* that specific transaction.
- Since no non-prime attribute is dependent on a strict subset of any candidate key, all relations satisfy **2NF**.

### Third Normal Form (3NF)
A relation $R$ is in 3NF if and only if:
1. $R$ is in 2NF.
2. For every non-trivial functional dependency $X \rightarrow A$, either:
   - $X$ is a superkey of $R$, or
   - $A$ is a prime attribute (part of a candidate key).

**Verification for BorrowBuddy:**
- In `USERS`: Candidate keys are `user_id` and `email`. Both are superkeys. No non-key attribute determines another non-key attribute.
- In `CATEGORIES`: Candidate keys are `category_id` and `category_name`. Both are superkeys.
- In `ITEMS`: The left-hand side of $FD_{I1}$ is `item_id`, which is a superkey. There are no transitive dependencies: category attributes (`category_name`, `description`) are maintained in `CATEGORIES`, and user attributes (`name`, `department`) are maintained in `USERS`.
- In `TRANSACTIONS`: The determinant is `transaction_id` (a superkey). Item titles or user names are NOT stored redundantly in `TRANSACTIONS`.
- In `TRANSACTION_APPROVALS`, `TRANSACTION_RETURNS`, `ITEM_OWNERSHIP_HISTORY`, `NOTIFICATIONS`, `REVIEWS`: All determinants are superkeys.
- Therefore, all 9 relations satisfy **3NF**.

### Boyce-Codd Normal Form (BCNF)
A relation $R$ is in BCNF if for every non-trivial functional dependency $X \rightarrow A$, $X$ is a superkey.
- In every relation in `borrowbuddy_db`, every determinant is a candidate key.
- Thus, the schema is in **BCNF**.

---

## 4. Anomaly Mitigation Rationale

| Operation | Without Normalization | With BorrowBuddy 3NF Schema |
|---|---|---|
| **Insertion** | Adding an item category requires creating a dummy item or user. | `CATEGORIES` table allows creating new categories independently of items. |
| **Deletion** | Deleting an item might erase user profile or historical transaction audits. | Ownership history, approvals, returns, and reviews reference IDs with foreign key protection. |
| **Modification** | Updating a user's phone number or name requires updating dozens of past transaction records. | User profile data resides exclusively in `USERS`. Only `user_id` is referenced elsewhere. |
| **Concurrency** | Concurrent return of two items in an exchange creates row-level locking race conditions. | `TRANSACTION_RETURNS` allows independent logging of each item's physical return without locking the parent transaction row prematurely. |
