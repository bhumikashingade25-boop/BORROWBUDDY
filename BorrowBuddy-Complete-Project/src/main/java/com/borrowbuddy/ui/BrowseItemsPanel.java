package com.borrowbuddy.ui;

import com.borrowbuddy.dao.CategoryDAO;
import com.borrowbuddy.model.Category;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.ItemService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

/**
 * Browse items catalog with search, category/condition filters,
 * detailed item view, and request submission dialog.
 */
public class BrowseItemsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ItemService itemService = new ItemService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionService transactionService = new TransactionService();

    private final JTextField searchField;
    private final JComboBox<CategoryItem> categoryCombo;
    private final JComboBox<String> conditionCombo;
    private final JComboBox<String> availabilityCombo;

    private final DefaultTableModel tableModel;
    private final JTable itemsTable;
    private List<Item> currentItems;

    public BrowseItemsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Top Search and Filter Bar
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        JLabel pageTitle = new JLabel("Browse Campus Catalog");
        pageTitle.setFont(UITheme.FONT_TITLE);
        pageTitle.setForeground(UITheme.TEXT_PRIMARY);
        topPanel.add(pageTitle, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new GridLayout(2, 1, 0, 8));
        filterBar.setBackground(UITheme.BG_WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Line 1: Keyword search + Search (Blue) button + Reset (Grey) button
        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        line1.setOpaque(false);
        JLabel kwLabel = new JLabel("Keyword:");
        kwLabel.setFont(UITheme.FONT_BOLD);
        searchField = UITheme.createTextField("Search items or descriptions...");
        searchField.setPreferredSize(new Dimension(280, 36));

        JButton searchBtn = UITheme.createPrimaryButton("🔍 Search");
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = UITheme.createSecondaryButton("↺ Reset");
        resetBtn.addActionListener(e -> resetFilter());

        line1.add(kwLabel);
        line1.add(searchField);
        line1.add(searchBtn);
        line1.add(resetBtn);

        // Line 2: Category, Condition, and Status dropdowns
        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        line2.setOpaque(false);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(UITheme.FONT_BOLD);
        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(170, 34));
        categoryCombo.setFont(UITheme.FONT_BODY);

        JLabel condLabel = new JLabel("Condition:");
        condLabel.setFont(UITheme.FONT_BOLD);
        conditionCombo = new JComboBox<>(new String[]{"All", "NEW", "GOOD", "FAIR", "POOR"});
        conditionCombo.setPreferredSize(new Dimension(110, 34));
        conditionCombo.setFont(UITheme.FONT_BODY);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(UITheme.FONT_BOLD);
        availabilityCombo = new JComboBox<>(new String[]{"All", "AVAILABLE", "BORROWED", "UNAVAILABLE"});
        availabilityCombo.setPreferredSize(new Dimension(130, 34));
        availabilityCombo.setFont(UITheme.FONT_BODY);

        line2.add(catLabel);
        line2.add(categoryCombo);
        line2.add(condLabel);
        line2.add(conditionCombo);
        line2.add(statusLabel);
        line2.add(availabilityCombo);

        filterBar.add(line1);
        filterBar.add(line2);

        topPanel.add(filterBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // 2. Table
        String[] columns = {"ID", "Item Name", "Category", "Condition", "Owner", "Availability", "Borrow", "Temp Exc", "Perm Exc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        itemsTable = new JTable(tableModel);
        UITheme.styleTable(itemsTable);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedItemDetails();
                }
            }
        });

        JScrollPane scrollPane = UITheme.createScrollPane(itemsTable);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setOpaque(true);
        bottomBar.setBackground(UITheme.BG_WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton detailsBtn = UITheme.createSecondaryButton("View Details");
        detailsBtn.addActionListener(e -> showSelectedItemDetails());
        bottomBar.add(detailsBtn);

        JButton requestBtn = UITheme.createPrimaryButton("Request Selected Item");
        requestBtn.addActionListener(e -> openRequestDialog());
        bottomBar.add(requestBtn);

        add(bottomBar, BorderLayout.SOUTH);

        // Enter key in search field triggers search
        searchField.addActionListener(e -> applyFilter());
    }

    public void loadCategories() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(new CategoryItem(0, "All Categories"));
        for (Category c : categoryDAO.findAll()) {
            categoryCombo.addItem(new CategoryItem(c.getCategoryId(), c.getCategoryName()));
        }
    }

    public void loadData() {
        loadCategories();
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText().trim();
        CategoryItem selCat = (CategoryItem) categoryCombo.getSelectedItem();
        Integer catId = (selCat != null && selCat.id > 0) ? selCat.id : null;
        String condition = (String) conditionCombo.getSelectedItem();
        String availability = (String) availabilityCombo.getSelectedItem();

        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() {
                return itemService.searchItems(keyword, catId, condition, availability);
            }

            @Override
            protected void done() {
                try {
                    currentItems = get();
                    tableModel.setRowCount(0);
                    for (Item item : currentItems) {
                        tableModel.addRow(new Object[]{
                                item.getItemId(),
                                item.getItemName(),
                                item.getCategoryName(),
                                item.getItemCondition(),
                                item.getOwnerName(),
                                item.getAvailabilityStatus(),
                                item.isAllowBorrowing() ? "Yes" : "No",
                                item.isAllowTemporaryExchange() ? "Yes" : "No",
                                item.isAllowPermanentExchange() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("BrowseItemsPanel search error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void resetFilter() {
        searchField.setText("");
        categoryCombo.setSelectedIndex(0);
        conditionCombo.setSelectedIndex(0);
        availabilityCombo.setSelectedIndex(0);
        applyFilter();
    }

    private Item getSelectedItem() {
        int row = itemsTable.getSelectedRow();
        if (row < 0 || currentItems == null || row >= currentItems.size()) {
            return null;
        }
        return currentItems.get(row);
    }

    private void showSelectedItemDetails() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Item Details: " + item.getItemName(), true);
        dlg.setSize(460, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(9, 1, 6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        p.setBackground(UITheme.BG_WHITE);

        p.add(new JLabel("Name: " + item.getItemName()));
        p.add(new JLabel("Category: " + item.getCategoryName()));
        p.add(new JLabel("Owner: " + item.getOwnerName()));
        p.add(new JLabel("Condition: " + item.getItemCondition()));
        p.add(new JLabel("Status: " + item.getAvailabilityStatus()));
        p.add(new JLabel("Borrowing Allowed: " + (item.isAllowBorrowing() ? "Yes" : "No")));
        p.add(new JLabel("Temp Exchange Allowed: " + (item.isAllowTemporaryExchange() ? "Yes" : "No")));
        p.add(new JLabel("Perm Exchange Allowed: " + (item.isAllowPermanentExchange() ? "Yes" : "No")));
        p.add(new JLabel("Description: " + (item.getDescription() != null ? item.getDescription() : "None")));

        dlg.getContentPane().add(p, BorderLayout.CENTER);
        JButton close = UITheme.createPrimaryButton("Close");
        close.addActionListener(e -> dlg.dispose());
        JPanel bp = new JPanel();
        bp.add(close);
        dlg.getContentPane().add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void openRequestDialog() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to request.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Not Logged In", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (item.getOwnerId() == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot request your own item.", "Invalid Request", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!item.isAvailable()) {
            JOptionPane.showMessageDialog(this, "This item is currently " + item.getAvailabilityStatus() + " and cannot be requested.",
                    "Item Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Open Dialog
        RequestDialog dlg = new RequestDialog((Frame) SwingUtilities.getWindowAncestor(this), item, currentUser);
        dlg.setVisible(true);
        if (dlg.isSuccess()) {
            applyFilter();
            mainFrame.refreshBadges();
        }
    }

    private static class CategoryItem {
        final int id;
        final String name;
        CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    // Modal dialog for request creation
    private class RequestDialog extends JDialog {
        private boolean success = false;
        private final JRadioButton rbBorrow = new JRadioButton("Borrow (Temporary)");
        private final JRadioButton rbTempEx = new JRadioButton("Temporary Exchange");
        private final JRadioButton rbPermEx = new JRadioButton("Permanent Exchange");
        private final JSpinner startSpinner;
        private final JSpinner dueSpinner;
        private final JComboBox<Item> offeredCombo = new JComboBox<>();
        private final JTextArea messageArea = new JTextArea(3, 20);

        RequestDialog(Frame owner, Item item, User user) {
            super(owner, "Request Item: " + item.getItemName(), true);
            setSize(480, 520);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(12, 12));
            getContentPane().setBackground(UITheme.BG_WHITE);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            content.setBackground(UITheme.BG_WHITE);

            JLabel title = new JLabel("Request: " + item.getItemName());
            title.setFont(UITheme.FONT_TITLE);
            title.setForeground(UITheme.PRIMARY);
            content.add(title);

            JLabel sub = new JLabel("Owner: " + item.getOwnerName() + " | Condition: " + item.getItemCondition());
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(UITheme.TEXT_SECONDARY);
            content.add(sub);
            content.add(Box.createVerticalStrut(14));

            // Type Selection
            ButtonGroup group = new ButtonGroup();
            rbBorrow.setEnabled(item.isAllowBorrowing());
            rbTempEx.setEnabled(item.isAllowTemporaryExchange());
            rbPermEx.setEnabled(item.isAllowPermanentExchange());
            group.add(rbBorrow);
            group.add(rbTempEx);
            group.add(rbPermEx);

            if (item.isAllowBorrowing()) rbBorrow.setSelected(true);
            else if (item.isAllowTemporaryExchange()) rbTempEx.setSelected(true);
            else if (item.isAllowPermanentExchange()) rbPermEx.setSelected(true);

            JPanel typePanel = new JPanel(new GridLayout(3, 1, 4, 4));
            typePanel.setBorder(BorderFactory.createTitledBorder("Transaction Type"));
            typePanel.setBackground(UITheme.BG_WHITE);
            typePanel.add(rbBorrow);
            typePanel.add(rbTempEx);
            typePanel.add(rbPermEx);
            content.add(typePanel);
            content.add(Box.createVerticalStrut(10));

            // Dates
            Calendar cal = Calendar.getInstance();
            java.util.Date today = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            java.util.Date nextWeek = cal.getTime();

            startSpinner = new JSpinner(new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH));
            dueSpinner = new JSpinner(new SpinnerDateModel(nextWeek, null, null, Calendar.DAY_OF_MONTH));
            startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd"));
            dueSpinner.setEditor(new JSpinner.DateEditor(dueSpinner, "yyyy-MM-dd"));

            JPanel datePanel = new JPanel(new GridLayout(2, 2, 8, 8));
            datePanel.setBorder(BorderFactory.createTitledBorder("Borrow / Exchange Period"));
            datePanel.setBackground(UITheme.BG_WHITE);
            datePanel.add(new JLabel("Start Date:"));
            datePanel.add(startSpinner);
            datePanel.add(new JLabel("Expected Due Date:"));
            datePanel.add(dueSpinner);
            content.add(datePanel);
            content.add(Box.createVerticalStrut(10));

            // Offered Item for exchanges
            JPanel offPanel = new JPanel(new BorderLayout(4, 4));
            offPanel.setBorder(BorderFactory.createTitledBorder("Offered Item (For Exchanges Only)"));
            offPanel.setBackground(UITheme.BG_WHITE);

            List<Item> myItems = itemService.getItemsByOwner(user.getUserId());
            for (Item my : myItems) {
                if (my.isAvailable()) offeredCombo.addItem(my);
            }
            offPanel.add(offeredCombo, BorderLayout.CENTER);
            content.add(offPanel);
            content.add(Box.createVerticalStrut(10));

            // Message
            JPanel msgPanel = new JPanel(new BorderLayout(4, 4));
            msgPanel.setBorder(BorderFactory.createTitledBorder("Message to Owner (Optional)"));
            msgPanel.setBackground(UITheme.BG_WHITE);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            msgPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
            content.add(msgPanel);

            // Listeners to enable/disable fields depending on type
            java.awt.event.ActionListener radioListener = e -> {
                boolean isPerm = rbPermEx.isSelected();
                boolean isExchange = rbTempEx.isSelected() || rbPermEx.isSelected();
                startSpinner.setEnabled(!isPerm);
                dueSpinner.setEnabled(!isPerm);
                offeredCombo.setEnabled(isExchange);
            };
            rbBorrow.addActionListener(radioListener);
            rbTempEx.addActionListener(radioListener);
            rbPermEx.addActionListener(radioListener);
            radioListener.actionPerformed(null);

            add(new JScrollPane(content), BorderLayout.CENTER);

            // Action Buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton cancel = UITheme.createSecondaryButton("Cancel");
            cancel.addActionListener(e -> dispose());
            JButton submit = UITheme.createPrimaryButton("Submit Request");
            submit.addActionListener(e -> submitRequest(item, user));

            actions.add(cancel);
            actions.add(submit);
            add(actions, BorderLayout.SOUTH);
        }

        private void submitRequest(Item requestedItem, User user) {
            String msg = messageArea.getText().trim();
            java.util.Date sDateUtil = (java.util.Date) startSpinner.getValue();
            java.util.Date dDateUtil = (java.util.Date) dueSpinner.getValue();
            Date startDate = new Date(sDateUtil.getTime());
            Date dueDate = new Date(dDateUtil.getTime());

            try {
                if (rbBorrow.isSelected()) {
                    transactionService.submitBorrowRequest(user.getUserId(), requestedItem.getItemId(), startDate, dueDate, msg);
                } else if (rbTempEx.isSelected()) {
                    Item offered = (Item) offeredCombo.getSelectedItem();
                    if (offered == null) {
                        JOptionPane.showMessageDialog(this, "Please select an item you own to offer in exchange.", "Offered Item Required", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    transactionService.submitTemporaryExchangeRequest(user.getUserId(), requestedItem.getItemId(), offered.getItemId(), startDate, dueDate, msg);
                } else if (rbPermEx.isSelected()) {
                    Item offered = (Item) offeredCombo.getSelectedItem();
                    if (offered == null) {
                        JOptionPane.showMessageDialog(this, "Please select an item you own to offer in exchange.", "Offered Item Required", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int conf = JOptionPane.showConfirmDialog(this,
                            "PERMANENT EXCHANGE WARNING:\nOwnership of both items will be PERMANENTLY swapped upon approval.\nProceed?",
                            "Confirm Permanent Exchange Proposal", JOptionPane.YES_NO_OPTION);
                    if (conf != JOptionPane.YES_OPTION) return;
                    transactionService.submitPermanentExchangeRequest(user.getUserId(), requestedItem.getItemId(), offered.getItemId(), msg);
                }

                JOptionPane.showMessageDialog(this, "Request submitted successfully! The owner has been notified.", "Request Sent", JOptionPane.INFORMATION_MESSAGE);
                success = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Request Failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        boolean isSuccess() { return success; }
    }
}
