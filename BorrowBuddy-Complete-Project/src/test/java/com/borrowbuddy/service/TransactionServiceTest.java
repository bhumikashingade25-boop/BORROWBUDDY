package com.borrowbuddy.service;

import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Service Workflows & Atomicity Tests")
public class TransactionServiceTest {

    private TransactionService transactionService;
    private ItemService itemService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
        itemService = new ItemService();
        authService = AuthService.getInstance();
    }

    @Test
    @DisplayName("Should prevent user from requesting to borrow their own item")
    void testSelfBorrowPrevention() throws Exception {
        User aarav = authService.login("aarav@campus.edu", "Password@123");
        List<Item> aaravItems = itemService.getItemsByOwner(aarav.getUserId());
        assertFalse(aaravItems.isEmpty());

        Item myItem = aaravItems.get(0);
        Date start = Date.valueOf(LocalDate.now());
        Date due = Date.valueOf(LocalDate.now().plusDays(7));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.submitBorrowRequest(aarav.getUserId(), myItem.getItemId(), start, due, "Self borrow")
        );
    }

    @Test
    @DisplayName("Should create, approve, return, and complete a Borrow transaction")
    void testBorrowLifecycle() throws Exception {
        User priya = authService.login("priya@campus.edu", "Password@123");
        User rahul = authService.login("rahul@campus.edu", "Password@123");

        // 1. Create a test item owned by Rahul
        Item item = new Item();
        item.setItemName("Lab Multimeter " + System.currentTimeMillis());
        item.setCategoryId(3);
        item.setItemCondition("GOOD");
        item.setAllowBorrowing(true);
        item.setAllowTemporaryExchange(false);
        item.setAllowPermanentExchange(false);
        Item savedItem = itemService.createItem(item, rahul);

        // 2. Priya requests to borrow Rahul's multimeter
        Date start = Date.valueOf(LocalDate.now());
        Date due = Date.valueOf(LocalDate.now().plusDays(5));
        Transaction t = transactionService.submitBorrowRequest(priya.getUserId(), savedItem.getItemId(), start, due, "Need for physics experiment");

        assertNotNull(t);
        assertEquals("PENDING", t.getStatus());

        // 3. Rahul approves request
        transactionService.approveTransaction(t.getTransactionId(), rahul.getUserId());

        Transaction activeTxn = transactionService.getTransactionById(t.getTransactionId()).orElseThrow();
        assertEquals("ACTIVE", activeTxn.getStatus());
        Item reservedItem = itemService.getItemById(savedItem.getItemId()).orElseThrow();
        assertEquals("BORROWED", reservedItem.getAvailabilityStatus());

        // 4. Priya records return
        transactionService.recordReturn(t.getTransactionId(), savedItem.getItemId(), priya.getUserId());

        // 5. Rahul confirms return receipt
        var returns = transactionService.getReturnsForTransaction(t.getTransactionId());
        assertEquals(1, returns.size());
        transactionService.confirmReturn(returns.get(0).getReturnId(), rahul.getUserId());

        // 6. Verify completion and restored availability
        Transaction completedTxn = transactionService.getTransactionById(t.getTransactionId()).orElseThrow();
        assertEquals("COMPLETED", completedTxn.getStatus());
        assertNotNull(completedTxn.getCompletedAt());

        Item restoredItem = itemService.getItemById(savedItem.getItemId()).orElseThrow();
        assertEquals("AVAILABLE", restoredItem.getAvailabilityStatus());
    }

    @Test
    @DisplayName("Should atomically execute Permanent Exchange and swap ownership in MySQL")
    void testPermanentExchangeAtomicTransfer() throws Exception {
        User priya = authService.login("priya@campus.edu", "Password@123");
        User rahul = authService.login("rahul@campus.edu", "Password@123");

        // Priya creates Item A
        Item itemA = new Item();
        itemA.setItemName("Priya Board Game " + System.currentTimeMillis());
        itemA.setCategoryId(4);
        itemA.setItemCondition("GOOD");
        itemA.setAllowPermanentExchange(true);
        Item savedA = itemService.createItem(itemA, priya);

        // Rahul creates Item B
        Item itemB = new Item();
        itemB.setItemName("Rahul Drawing Tablet " + System.currentTimeMillis());
        itemB.setCategoryId(2);
        itemB.setItemCondition("GOOD");
        itemB.setAllowPermanentExchange(true);
        Item savedB = itemService.createItem(itemB, rahul);

        // Priya proposes permanent exchange: Priya wants Item B (Rahul's), offers Item A (Priya's)
        Transaction t = transactionService.submitPermanentExchangeRequest(priya.getUserId(), savedB.getItemId(), savedA.getItemId(), "Permanent swap");
        assertEquals("PENDING", t.getStatus());
        assertNull(t.getStartDate());
        assertNull(t.getDueDate());

        // Rahul accepts proposal
        transactionService.approveTransaction(t.getTransactionId(), rahul.getUserId());

        // Verify transaction is marked COMPLETED
        Transaction doneTxn = transactionService.getTransactionById(t.getTransactionId()).orElseThrow();
        assertEquals("COMPLETED", doneTxn.getStatus());

        // Verify ownership was atomically swapped in MySQL!
        Item updatedA = itemService.getItemById(savedA.getItemId()).orElseThrow();
        Item updatedB = itemService.getItemById(savedB.getItemId()).orElseThrow();

        assertEquals(rahul.getUserId(), updatedA.getOwnerId(), "Item A should now belong to Rahul");
        assertEquals(priya.getUserId(), updatedB.getOwnerId(), "Item B should now belong to Priya");
    }
}
