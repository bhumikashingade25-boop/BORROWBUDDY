package com.borrowbuddy.ui;

import com.borrowbuddy.dao.ItemDAO;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.TransactionReturn;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Manages in-progress active borrowings and temporary exchanges.
 * Features overdue indicators and independent return/confirmation flows.
 */
public class ActiveTransactionsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final TransactionService transactionService = new TransactionService();
    private final ItemDAO itemDAO = new ItemDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Transaction> activeTxns;

    public ActiveTransactionsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Active Transactions & Return Tracking");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        topBar.add(refreshBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Type", "Requested Item", "Offered Item", "Counterparty", "Start Date", "Due Date", "Overdue Status", "Returns Recorded"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for overdue warning
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel && row < activeTxns.size()) {
                    Transaction txn = activeTxns.get(row);
                    if (txn.getDueDate() != null && DateUtil.isOverdue(txn.getDueDate())) {
                        c.setBackground(UITheme.OVERDUE_ROW);
                        c.setForeground(UITheme.DANGER);
                    } else {
                        c.setBackground(row % 2 == 0 ? UITheme.BG_WHITE : UITheme.ROW_ALTERNATE);
                        c.setForeground(UITheme.TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        add(UITheme.createScrollPane(table), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomBar.setOpaque(false);

        JButton viewReturnsBtn = UITheme.createSecondaryButton("View Return Status");
        viewReturnsBtn.addActionListener(e -> viewReturnStatus());
        bottomBar.add(viewReturnsBtn);

        JButton returnItemBtn = UITheme.createPrimaryButton("Mark Item Returned");
        returnItemBtn.addActionListener(e -> recordItemReturn());
        bottomBar.add(returnItemBtn);

        JButton confirmBtn = UITheme.createSuccessButton("Confirm Item Receipt");
        confirmBtn.addActionListener(e -> confirmItemReceipt());
        bottomBar.add(confirmBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() {
                return transactionService.getActiveTransactions(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    activeTxns = get();
                    tableModel.setRowCount(0);
                    for (Transaction t : activeTxns) {
                        boolean isOverdue = t.getDueDate() != null && DateUtil.isOverdue(t.getDueDate());
                        String overdueStr = isOverdue ? "⚠️ OVERDUE" : "On Schedule";

                        List<TransactionReturn> returns = transactionService.getReturnsForTransaction(t.getTransactionId());
                        long confirmedCount = returns.stream().filter(TransactionReturn::isConfirmed).count();
                        String returnProgress = returns.size() + " returned (" + confirmedCount + " confirmed)";

                        String partner = (t.getRequesterId() == user.getUserId()) ? t.getResponderName() : t.getRequesterName();

                        tableModel.addRow(new Object[]{
                                "#" + t.getTransactionId(),
                                t.getTransactionType().replace("_", " "),
                                t.getRequestedItemName(),
                                t.getOfferedItemName() != null ? t.getOfferedItemName() : "N/A",
                                partner,
                                t.getStartDate() != null ? DateUtil.formatDate(t.getStartDate()) : "N/A",
                                t.getDueDate() != null ? DateUtil.formatDate(t.getDueDate()) : "N/A",
                                overdueStr,
                                returnProgress
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("ActiveTransactionsPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Transaction getSelectedTransaction() {
        int row = table.getSelectedRow();
        if (row < 0 || activeTxns == null || row >= activeTxns.size()) {
            JOptionPane.showMessageDialog(this, "Please select an active transaction.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return activeTxns.get(row);
    }

    private void recordItemReturn() {
        Transaction t = getSelectedTransaction();
        if (t == null) return;

        User user = AuthService.getInstance().getCurrentUser();

        // Determine which item the current user needs to return
        int itemIdToReturn;
        String itemName;
        if ("BORROW".equalsIgnoreCase(t.getTransactionType())) {
            if (t.getRequesterId() != user.getUserId()) {
                JOptionPane.showMessageDialog(this, "Only the borrower can mark the borrowed item as returned.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
                return;
            }
            itemIdToReturn = t.getRequestedItemId();
            itemName = t.getRequestedItemName();
        } else {
            // Temporary exchange:
            // Requester borrowed requestedItem, Responder borrowed offeredItem
            if (t.getRequesterId() == user.getUserId()) {
                itemIdToReturn = t.getRequestedItemId();
                itemName = t.getRequestedItemName();
            } else {
                itemIdToReturn = t.getOfferedItemId();
                itemName = t.getOfferedItemName();
            }
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Mark '" + itemName + "' as returned back to its owner?\nThe owner will be asked to confirm receipt.",
                "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            transactionService.recordReturn(t.getTransactionId(), itemIdToReturn, user.getUserId());
            JOptionPane.showMessageDialog(this, "Return recorded! The owner has been notified to confirm receipt.", "Return Recorded", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Recording Return", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmItemReceipt() {
        Transaction t = getSelectedTransaction();
        if (t == null) return;

        User user = AuthService.getInstance().getCurrentUser();
        List<TransactionReturn> returns = transactionService.getReturnsForTransaction(t.getTransactionId());

        // Find return record where current user is the owner of that returned item
        TransactionReturn returnToConfirm = null;
        for (TransactionReturn ret : returns) {
            if ("PENDING".equalsIgnoreCase(ret.getStatus())) {
                Item item = itemDAO.findById(ret.getItemId()).orElse(null);
                if (item != null && item.getOwnerId() == user.getUserId()) {
                    returnToConfirm = ret;
                    break;
                }
            }
        }

        if (returnToConfirm == null) {
            JOptionPane.showMessageDialog(this, "No pending return confirmation found for your items in this transaction.",
                    "Nothing to Confirm", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Confirm that you have received your item back in acceptable condition?\nThis will mark the item AVAILABLE again.",
                "Confirm Receipt", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            transactionService.confirmReturn(returnToConfirm.getReturnId(), user.getUserId());
            JOptionPane.showMessageDialog(this, "Receipt confirmed! Item availability has been restored.", "Return Confirmed", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            mainFrame.refreshBadges();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Confirming Receipt", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewReturnStatus() {
        Transaction t = getSelectedTransaction();
        if (t == null) return;

        List<TransactionReturn> returns = transactionService.getReturnsForTransaction(t.getTransactionId());

        StringBuilder sb = new StringBuilder();
        sb.append("Transaction #").append(t.getTransactionId()).append(" (").append(t.getTransactionType()).append(")\n\n");
        if (returns.isEmpty()) {
            sb.append("No return records logged yet.\nBoth participants are still holding the items.");
        } else {
            for (TransactionReturn r : returns) {
                sb.append("• Item: ").append(r.getItemName() != null ? r.getItemName() : "Item #" + r.getItemId())
                        .append(" | Status: ").append(r.getStatus())
                        .append(" | Logged: ").append(DateUtil.formatTimestamp(r.getReturnedAt()))
                        .append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Return Status Overview", JOptionPane.INFORMATION_MESSAGE);
    }
}
