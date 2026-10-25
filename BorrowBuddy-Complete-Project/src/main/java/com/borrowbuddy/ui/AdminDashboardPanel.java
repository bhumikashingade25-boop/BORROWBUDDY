package com.borrowbuddy.ui;

import com.borrowbuddy.model.Category;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AdminService;
import com.borrowbuddy.service.ItemService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Platform Administration Panel.
 * Restricted to administrators for managing users, categories, moderation, and auditing.
 */
public class AdminDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AdminService adminService = new AdminService();
    private final ItemService itemService = new ItemService();
    private final TransactionService transactionService = new TransactionService();

    // Stats
    private final JLabel lblUsers = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblItems = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblAvailable = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblActive = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblOverdue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblCompleted = new JLabel("0", SwingConstants.CENTER);

    // Tables
    private final DefaultTableModel usersModel;
    private final JTable usersTable;
    private List<User> allUsers;

    private final DefaultTableModel categoriesModel;
    private final JTable categoriesTable;
    private List<Category> allCategories;

    private final DefaultTableModel itemsModel;
    private final JTable itemsTable;
    private List<Item> allItems;

    private final DefaultTableModel txnsModel;
    private final JTable txnsTable;
    private List<Transaction> allTxns;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("🛡️ Platform Administration & Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY_DARK);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Admin Data");
        refreshBtn.addActionListener(e -> loadData());
        topBar.add(refreshBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        // Tab 1: Overview
        JPanel pOverview = new JPanel(new BorderLayout(16, 16));
        pOverview.setOpaque(false);
        pOverview.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.add(createStatCard("👥 Registered Users", lblUsers, UITheme.PRIMARY));
        statsGrid.add(createStatCard("📚 Listed Items", lblItems, UITheme.ACCENT));
        statsGrid.add(createStatCard("✅ Available Items", lblAvailable, UITheme.SUCCESS));
        statsGrid.add(createStatCard("🔄 Active Transactions", lblActive, new Color(142, 68, 173)));
        statsGrid.add(createStatCard("⚠️ Overdue Transactions", lblOverdue, UITheme.DANGER));
        statsGrid.add(createStatCard("✔️ Completed Transactions", lblCompleted, new Color(39, 174, 96)));
        pOverview.add(statsGrid, BorderLayout.CENTER);
        tabs.addTab("📊 System Overview", pOverview);

        // Tab 2: Users Management
        JPanel pUsers = new JPanel(new BorderLayout(8, 8));
        pUsers.setOpaque(false);
        String[] userCols = {"ID", "Name", "Email", "Role", "Department", "Phone", "Registration Date"};
        usersModel = new DefaultTableModel(userCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        UITheme.styleTable(usersTable);
        pUsers.add(UITheme.createScrollPane(usersTable), BorderLayout.CENTER);

        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        userActions.setOpaque(false);
        JButton changeRoleBtn = UITheme.createSecondaryButton("Promote / Change Role");
        changeRoleBtn.addActionListener(e -> changeUserRole());
        userActions.add(changeRoleBtn);
        JButton delUserBtn = UITheme.createDangerButton("Delete User");
        delUserBtn.addActionListener(e -> deleteSelectedUser());
        userActions.add(delUserBtn);
        pUsers.add(userActions, BorderLayout.SOUTH);
        tabs.addTab("👥 User Management", pUsers);

        // Tab 3: Category Management
        JPanel pCategories = new JPanel(new BorderLayout(8, 8));
        pCategories.setOpaque(false);
        String[] catCols = {"ID", "Category Name", "Description"};
        categoriesModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        categoriesTable = new JTable(categoriesModel);
        UITheme.styleTable(categoriesTable);
        pCategories.add(UITheme.createScrollPane(categoriesTable), BorderLayout.CENTER);

        JPanel catActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        catActions.setOpaque(false);
        JButton addCatBtn = UITheme.createPrimaryButton("➕ Add Category");
        addCatBtn.addActionListener(e -> openCategoryDialog(null));
        catActions.add(addCatBtn);
        JButton editCatBtn = UITheme.createSecondaryButton("Edit Category");
        editCatBtn.addActionListener(e -> {
            int row = categoriesTable.getSelectedRow();
            if (row >= 0 && row < allCategories.size()) openCategoryDialog(allCategories.get(row));
        });
        catActions.add(editCatBtn);
        JButton delCatBtn = UITheme.createDangerButton("Delete Category");
        delCatBtn.addActionListener(e -> deleteSelectedCategory());
        catActions.add(delCatBtn);
        pCategories.add(catActions, BorderLayout.SOUTH);
        tabs.addTab("🗂️ Categories", pCategories);

        // Tab 4: Item Moderation
        JPanel pItems = new JPanel(new BorderLayout(8, 8));
        pItems.setOpaque(false);
        String[] itemCols = {"ID", "Item Name", "Owner", "Category", "Condition", "Status", "Created"};
        itemsModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        UITheme.styleTable(itemsTable);
        pItems.add(UITheme.createScrollPane(itemsTable), BorderLayout.CENTER);

        JPanel itemActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        itemActions.setOpaque(false);
        JButton unavailBtn = UITheme.createDangerButton("Mark UNAVAILABLE");
        unavailBtn.addActionListener(e -> setItemStatus("UNAVAILABLE"));
        itemActions.add(unavailBtn);
        JButton availBtn = UITheme.createSuccessButton("Restore to AVAILABLE");
        availBtn.addActionListener(e -> setItemStatus("AVAILABLE"));
        itemActions.add(availBtn);
        pItems.add(itemActions, BorderLayout.SOUTH);
        tabs.addTab("📦 Item Moderation", pItems);

        // Tab 5: All Transactions & Export
        JPanel pTxns = new JPanel(new BorderLayout(8, 8));
        pTxns.setOpaque(false);
        String[] txnCols = {"ID", "Type", "Requested Item", "Requester", "Responder", "Status", "Start Date", "Due Date", "Date"};
        txnsModel = new DefaultTableModel(txnCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        txnsTable = new JTable(txnsModel);
        UITheme.styleTable(txnsTable);
        pTxns.add(UITheme.createScrollPane(txnsTable), BorderLayout.CENTER);

        JPanel txnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        txnActions.setOpaque(false);
        JButton exportBtn = UITheme.createPrimaryButton("📥 Export Transactions to CSV");
        exportBtn.addActionListener(e -> exportTransactionsToCSV());
        txnActions.add(exportBtn);
        pTxns.add(txnActions, BorderLayout.SOUTH);
        tabs.addTab("📑 All Transactions & Audit", pTxns);

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel c = new JPanel(new BorderLayout(6, 6));
        c.setBackground(UITheme.BG_WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 229, 235), 1),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                )
        ));
        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        c.add(t, BorderLayout.NORTH);
        c.add(valueLabel, BorderLayout.CENTER);
        return c;
    }

    public void loadData() {
        new SwingWorker<Void, Void>() {
            private Map<String, Object> stats;

            @Override
            protected Void doInBackground() {
                stats = adminService.getSystemStats();
                allUsers = adminService.getAllUsers();
                allCategories = adminService.getAllCategories();
                allItems = itemService.getAllItems();
                allTxns = transactionService.getAllTransactions();
                return null;
            }

            @Override
            protected void done() {
                try {
                    lblUsers.setText(String.valueOf(stats.getOrDefault("totalUsers", 0)));
                    lblItems.setText(String.valueOf(stats.getOrDefault("totalItems", 0)));
                    lblAvailable.setText(String.valueOf(stats.getOrDefault("availableItems", 0)));
                    lblActive.setText(String.valueOf(stats.getOrDefault("activeTransactions", 0)));
                    lblOverdue.setText(String.valueOf(stats.getOrDefault("overdueTransactions", 0)));
                    lblCompleted.setText(String.valueOf(stats.getOrDefault("completedTransactions", 0)));

                    // Users Table
                    usersModel.setRowCount(0);
                    for (User u : allUsers) {
                        usersModel.addRow(new Object[]{
                                u.getUserId(), u.getName(), u.getEmail(), u.getRole(),
                                u.getDepartment() != null ? u.getDepartment() : "N/A",
                                u.getPhone() != null ? u.getPhone() : "N/A",
                                DateUtil.formatTimestamp(u.getRegistrationDate())
                        });
                    }

                    // Categories Table
                    categoriesModel.setRowCount(0);
                    for (Category c : allCategories) {
                        categoriesModel.addRow(new Object[]{
                                c.getCategoryId(), c.getCategoryName(), c.getDescription()
                        });
                    }

                    // Items Table
                    itemsModel.setRowCount(0);
                    for (Item i : allItems) {
                        itemsModel.addRow(new Object[]{
                                i.getItemId(), i.getItemName(), i.getOwnerName(), i.getCategoryName(),
                                i.getItemCondition(), i.getAvailabilityStatus(),
                                DateUtil.formatTimestamp(i.getCreatedAt())
                        });
                    }

                    // Transactions Table
                    txnsModel.setRowCount(0);
                    for (Transaction t : allTxns) {
                        txnsModel.addRow(new Object[]{
                                "#" + t.getTransactionId(),
                                t.getTransactionType().replace("_", " "),
                                t.getRequestedItemName(),
                                t.getRequesterName(),
                                t.getResponderName(),
                                t.getStatus(),
                                t.getStartDate() != null ? DateUtil.formatDate(t.getStartDate()) : "N/A",
                                t.getDueDate() != null ? DateUtil.formatDate(t.getDueDate()) : "N/A",
                                DateUtil.formatTimestamp(t.getRequestedAt())
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("AdminDashboard load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void changeUserRole() {
        int row = usersTable.getSelectedRow();
        if (row < 0 || row >= allUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User target = allUsers.get(row);
        String[] roles = {"STUDENT", "FACULTY", "ADMIN"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new role for " + target.getName() + ":",
                "Update User Role", JOptionPane.QUESTION_MESSAGE, null, roles, target.getRole());

        if (selected != null && !selected.equals(target.getRole())) {
            adminService.updateUserRole(target.getUserId(), selected);
            JOptionPane.showMessageDialog(this, "Role updated successfully.", "Role Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }

    private void deleteSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0 || row >= allUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User target = allUsers.get(row);
        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + target.getName() + "' (" + target.getEmail() + ")?",
                "Confirm User Deletion", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            adminService.deleteUser(target.getUserId());
            JOptionPane.showMessageDialog(this, "User deleted.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete User", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCategoryDialog(Category existing) {
        JTextField nameField = UITheme.createTextField("Category Name");
        JTextArea descArea = new JTextArea(3, 20);

        if (existing != null) {
            nameField.setText(existing.getCategoryName());
            if (existing.getDescription() != null) descArea.setText(existing.getDescription());
        }

        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("Name:"));
        p.add(nameField);
        p.add(new JLabel("Description:"));
        p.add(new JScrollPane(descArea));

        int res = JOptionPane.showConfirmDialog(this, p,
                existing == null ? "Add Category" : "Edit Category", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            try {
                if (existing == null) {
                    adminService.createCategory(name, desc);
                } else {
                    existing.setCategoryName(name);
                    existing.setDescription(desc);
                    adminService.updateCategory(existing);
                }
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Category Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCategory() {
        int row = categoriesTable.getSelectedRow();
        if (row < 0 || row >= allCategories.size()) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Category c = allCategories.get(row);
        int conf = JOptionPane.showConfirmDialog(this, "Delete category '" + c.getCategoryName() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            adminService.deleteCategory(c.getCategoryId());
            JOptionPane.showMessageDialog(this, "Category deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Category", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setItemStatus(String status) {
        int row = itemsTable.getSelectedRow();
        if (row < 0 || row >= allItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Item item = allItems.get(row);
        if ("UNAVAILABLE".equals(status)) {
            adminService.markItemUnavailable(item.getItemId());
        } else {
            adminService.restoreItem(item.getItemId());
        }
        loadData();
    }

    private void exportTransactionsToCSV() {
        if (allTxns == null || allTxns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions available to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File outFile = new File(System.getProperty("user.home"), "borrowbuddy_transactions.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
            pw.println("TransactionID,Type,RequestedItem,OfferedItem,Requester,Responder,StartDate,DueDate,Status,RequestedAt,CompletedAt");
            for (Transaction t : allTxns) {
                pw.printf("%d,%s,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%s%n",
                        t.getTransactionId(),
                        t.getTransactionType(),
                        t.getRequestedItemName() != null ? t.getRequestedItemName().replace("\"", "\"\"") : "",
                        t.getOfferedItemName() != null ? t.getOfferedItemName().replace("\"", "\"\"") : "",
                        t.getRequesterName() != null ? t.getRequesterName().replace("\"", "\"\"") : "",
                        t.getResponderName() != null ? t.getResponderName().replace("\"", "\"\"") : "",
                        t.getStartDate() != null ? t.getStartDate().toString() : "",
                        t.getDueDate() != null ? t.getDueDate().toString() : "",
                        t.getStatus(),
                        t.getRequestedAt() != null ? t.getRequestedAt().toString() : "",
                        t.getCompletedAt() != null ? t.getCompletedAt().toString() : ""
                );
            }
            JOptionPane.showMessageDialog(this, "Successfully exported " + allTxns.size() +
                    " transactions to:\n" + outFile.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
