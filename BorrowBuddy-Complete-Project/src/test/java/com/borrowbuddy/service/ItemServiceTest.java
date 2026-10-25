package com.borrowbuddy.service;

import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Item Service Tests")
public class ItemServiceTest {

    private ItemService itemService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();
        authService = AuthService.getInstance();
    }

    @Test
    @DisplayName("Should successfully list a new item for an authenticated user")
    void testCreateItem() throws Exception {
        User aarav = authService.login("aarav@campus.edu", "Password@123");

        Item item = new Item();
        item.setItemName("Arduino Starter Kit " + System.currentTimeMillis());
        item.setCategoryId(2); // Electronics
        item.setItemCondition("NEW");
        item.setDescription("Complete robotics kit with sensors");
        item.setAllowBorrowing(true);
        item.setAllowTemporaryExchange(true);
        item.setAllowPermanentExchange(false);

        Item saved = itemService.createItem(item, aarav);
        assertNotNull(saved);
        assertTrue(saved.getItemId() > 0);
        assertEquals("AVAILABLE", saved.getAvailabilityStatus());
        assertEquals(aarav.getUserId(), saved.getOwnerId());
    }

    @Test
    @DisplayName("Should fail item creation when no sharing type is enabled")
    void testCreateItemNoOptions() throws Exception {
        User aarav = authService.login("aarav@campus.edu", "Password@123");

        Item item = new Item();
        item.setItemName("Invalid Item");
        item.setCategoryId(1);
        item.setItemCondition("FAIR");
        item.setAllowBorrowing(false);
        item.setAllowTemporaryExchange(false);
        item.setAllowPermanentExchange(false);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(item, aarav));
    }

    @Test
    @DisplayName("Should filter items by keyword and category")
    void testSearchItems() {
        List<Item> searchResults = itemService.searchItems("DBMS", 1, null, null);
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().allMatch(i -> i.getItemName().contains("DBMS")));
    }

    @Test
    @DisplayName("Should fetch aggregated dashboard statistics correctly")
    void testDashboardStats() {
        Map<String, Integer> stats = itemService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalItems"));
        assertTrue(stats.containsKey("availableItems"));
        assertTrue(stats.get("totalItems") > 0);
    }
}
