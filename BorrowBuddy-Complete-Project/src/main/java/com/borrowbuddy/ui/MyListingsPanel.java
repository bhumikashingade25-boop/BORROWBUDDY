package com.borrowbuddy.ui;

import com.borrowbuddy.dao.CategoryDAO;
import com.borrowbuddy.model.Category;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.ItemService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Management panel for items owned by the current logged-in user.
 * Supports adding, editing, toggling availability, and deleting items safely.
 */
public class MyListingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ItemService itemService = new ItemService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Item> myItems;

    public MyListingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("My Listed Items");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JButton addBtn = UITheme.createPrimaryButton("➕ List New Item");
        addBtn.addActionListener(e -> openItemDialog(null));
        topBar.add(addBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Item Name", "Category", "Condition", "Status", "Borrow", "Temp Exc", "Perm Exc", "Listed Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(UITheme.createScrollPane(table), BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomBar.setOpaque(false);

        JButton editBtn = UITheme.createSecondaryButton("Edit Listing");
        editBtn.addActionListener(e -> {
            Item item = getSelectedItem();
            if (item != null) openItemDialog(item);
        });
        bottomBar.add(editBtn);

        JButton toggleBtn = UITheme.createSecondaryButton("Toggle Availability");
        toggleBtn.addActionListener(e -> toggleAvailability());
        bottomBar.add(toggleBtn);

        JButton deleteBtn = UITheme.createDangerButton("Delete Item");
        deleteBtn.addActionListener(e -> deleteSelectedItem());
        bottomBar.add(deleteBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() {
                return itemService.getItemsByOwner(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    myItems = get();
                    tableModel.setRowCount(0);
                    for (Item item : myItems) {
                        tableModel.addRow(new Object[]{
                                item.getItemId(),
                                item.getItemName(),
                                item.getCategoryName(),
                                item.getItemCondition(),
                                item.getAvailabilityStatus(),
                                item.isAllowBorrowing() ? "Yes" : "No",
                                item.isAllowTemporaryExchange() ? "Yes" : "No",
                                item.isAllowPermanentExchange() ? "Yes" : "No",
                                DateUtil.formatTimestamp(item.getCreatedAt())
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("MyListingsPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Item getSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || myItems == null || row >= myItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from your listings table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return myItems.get(row);
    }

    private void toggleAvailability() {
        Item item = getSelectedItem();
        if (item == null) return;

        User user = AuthService.getInstance().getCurrentUser();
        String nextStatus = "AVAILABLE".equalsIgnoreCase(item.getAvailabilityStatus()) ? "UNAVAILABLE" : "AVAILABLE";

        item.setAvailabilityStatus(nextStatus);
        try {
            itemService.updateItem(item, user);
            JOptionPane.showMessageDialog(this, "Item status updated to " + nextStatus + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Status Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        Item item = getSelectedItem();
        if (item == null) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + item.getItemName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        User user = AuthService.getInstance().getCurrentUser();
        try {
            itemService.deleteItem(item.getItemId(), user);
            JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Item Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Item", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openItemDialog(Item existing) {
        ItemDialog dlg = new ItemDialog((Frame) SwingUtilities.getWindowAncestor(this), existing);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    // Modal dialog for Add / Edit Item
    private class ItemDialog extends JDialog {
        private boolean saved = false;
        private final JTextField nameField = UITheme.createTextField("Item title");
        private final JComboBox<Category> categoryCombo = new JComboBox<>();
        private final JComboBox<String> conditionCombo = new JComboBox<>(new String[]{"NEW", "GOOD", "FAIR", "POOR"});
        private final JTextArea descArea = new JTextArea(4, 20);
        private final JTextField imageField = UITheme.createTextField("Optional image URL or filename");
        private final JCheckBox chkBorrow = new JCheckBox("Allow Temporary Borrowing", true);
        private final JCheckBox chkTempEx = new JCheckBox("Allow Temporary Exchange", true);
        private final JCheckBox chkPermEx = new JCheckBox("Allow Permanent Exchange", false);

        ItemDialog(Frame owner, Item item) {
            super(owner, item == null ? "Add New Listing" : "Edit Listing", true);
            setSize(480, 520);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(12, 12));
            getContentPane().setBackground(UITheme.BG_WHITE);

            // Populate categories
            for (Category c : categoryDAO.findAll()) {
                categoryCombo.addItem(c);
            }

            JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            form.setBackground(UITheme.BG_WHITE);

            form.add(new JLabel("Item Name: *"));
            form.add(nameField);

            form.add(new JLabel("Category: *"));
            form.add(categoryCombo);

            form.add(new JLabel("Condition: *"));
            form.add(conditionCombo);

            form.add(new JLabel("Description:"));
            descArea.setLineWrap(true);
            form.add(new JScrollPane(descArea));

            form.add(new JLabel("Image URL (optional):"));
            form.add(imageField);

            form.add(new JLabel("Sharing Preferences:"));
            JPanel chkPanel = new JPanel(new GridLayout(3, 1));
            chkPanel.setBackground(UITheme.BG_WHITE);
            chkPanel.add(chkBorrow);
            chkPanel.add(chkTempEx);
            chkPanel.add(chkPermEx);
            form.add(chkPanel);

            // Pre-fill if editing
            if (item != null) {
                nameField.setText(item.getItemName());
                conditionCombo.setSelectedItem(item.getItemCondition());
                if (item.getDescription() != null) descArea.setText(item.getDescription());
                if (item.getImageUrl() != null) imageField.setText(item.getImageUrl());
                chkBorrow.setSelected(item.isAllowBorrowing());
                chkTempEx.setSelected(item.isAllowTemporaryExchange());
                chkPermEx.setSelected(item.isAllowPermanentExchange());

                for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                    if (categoryCombo.getItemAt(i).getCategoryId() == item.getCategoryId()) {
                        categoryCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }

            add(form, BorderLayout.CENTER);

            // Bottom Buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton cancel = UITheme.createSecondaryButton("Cancel");
            cancel.addActionListener(e -> dispose());

            JButton save = UITheme.createPrimaryButton("Save Listing");
            save.addActionListener(e -> saveItem(item));

            actions.add(cancel);
            actions.add(save);
            add(actions, BorderLayout.SOUTH);
        }

        private void saveItem(Item existing) {
            String name = nameField.getText().trim();
            Category cat = (Category) categoryCombo.getSelectedItem();
            String cond = (String) conditionCombo.getSelectedItem();
            String desc = descArea.getText().trim();
            String img = imageField.getText().trim();
            boolean allowB = chkBorrow.isSelected();
            boolean allowT = chkTempEx.isSelected();
            boolean allowP = chkPermEx.isSelected();

            if (name.isEmpty() || cat == null) {
                JOptionPane.showMessageDialog(this, "Please enter an item name and category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!allowB && !allowT && !allowP) {
                JOptionPane.showMessageDialog(this, "Please enable at least one transaction type (Borrow, Temp Exchange, or Perm Exchange).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = AuthService.getInstance().getCurrentUser();
            try {
                if (existing == null) {
                    Item newItem = new Item();
                    newItem.setItemName(name);
                    newItem.setCategoryId(cat.getCategoryId());
                    newItem.setItemCondition(cond);
                    newItem.setDescription(desc.isEmpty() ? null : desc);
                    newItem.setImageUrl(img.isEmpty() ? null : img);
                    newItem.setAllowBorrowing(allowB);
                    newItem.setAllowTemporaryExchange(allowT);
                    newItem.setAllowPermanentExchange(allowP);
                    itemService.createItem(newItem, user);
                } else {
                    existing.setItemName(name);
                    existing.setCategoryId(cat.getCategoryId());
                    existing.setItemCondition(cond);
                    existing.setDescription(desc.isEmpty() ? null : desc);
                    existing.setImageUrl(img.isEmpty() ? null : img);
                    existing.setAllowBorrowing(allowB);
                    existing.setAllowTemporaryExchange(allowT);
                    existing.setAllowPermanentExchange(allowP);
                    itemService.updateItem(existing, user);
                }
                saved = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        boolean isSaved() { return saved; }
    }
}
