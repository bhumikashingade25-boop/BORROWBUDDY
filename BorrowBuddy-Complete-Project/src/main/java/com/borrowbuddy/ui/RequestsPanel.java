package com.borrowbuddy.ui;

import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Manages Incoming requests (waiting for user's approval) and Outgoing requests
 * (submitted by user, with cancellation capability).
 */
public class RequestsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final TransactionService transactionService = new TransactionService();

    private final DefaultTableModel incomingModel;
    private final JTable incomingTable;
    private List<Transaction> incomingTxns;

    private final DefaultTableModel outgoingModel;
    private final JTable outgoingTable;
    private List<Transaction> outgoingTxns;

    public RequestsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Borrow & Exchange Requests");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        topBar.add(refreshBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        // Tab 1: Incoming Requests
        JPanel incomingPanel = new JPanel(new BorderLayout(8, 8));
        incomingPanel.setOpaque(false);

        String[] inCols = {"ID", "Type", "Requested Item", "Offered Item", "Requester", "Start Date", "Due Date", "Status", "Date"};
        incomingModel = new DefaultTableModel(inCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        incomingTable = new JTable(incomingModel);
        UITheme.styleTable(incomingTable);
        incomingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incomingPanel.add(UITheme.createScrollPane(incomingTable), BorderLayout.CENTER);

        JPanel inActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        inActions.setOpaque(false);

        JButton approveBtn = UITheme.createSuccessButton("✅ Approve Request");
        approveBtn.addActionListener(e -> approveSelected());
        inActions.add(approveBtn);

        JButton rejectBtn = UITheme.createDangerButton("❌ Decline Request");
        rejectBtn.addActionListener(e -> rejectSelected());
        inActions.add(rejectBtn);

        incomingPanel.add(inActions, BorderLayout.SOUTH);
        tabbedPane.addTab("📥 Incoming Requests", incomingPanel);

        // Tab 2: Outgoing Requests
        JPanel outgoingPanel = new JPanel(new BorderLayout(8, 8));
        outgoingPanel.setOpaque(false);

        String[] outCols = {"ID", "Type", "Requested Item", "Offered Item", "Owner", "Start Date", "Due Date", "Status", "Date"};
        outgoingModel = new DefaultTableModel(outCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        outgoingTable = new JTable(outgoingModel);
        UITheme.styleTable(outgoingTable);
        outgoingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outgoingPanel.add(UITheme.createScrollPane(outgoingTable), BorderLayout.CENTER);

        JPanel outActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        outActions.setOpaque(false);

        JButton cancelBtn = UITheme.createDangerButton("🚫 Cancel Request");
        cancelBtn.addActionListener(e -> cancelSelected());
        outActions.add(cancelBtn);

        outgoingPanel.add(outActions, BorderLayout.SOUTH);
        tabbedPane.addTab("📤 Outgoing Requests", outgoingPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                incomingTxns = transactionService.getIncomingRequests(user.getUserId());
                outgoingTxns = transactionService.getOutgoingRequests(user.getUserId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Populate incoming
                    incomingModel.setRowCount(0);
                    for (Transaction t : incomingTxns) {
                        incomingModel.addRow(new Object[]{
                                "#" + t.getTransactionId(),
                                t.getTransactionType().replace("_", " "),
                                t.getRequestedItemName(),
                                t.getOfferedItemName() != null ? t.getOfferedItemName() : "N/A",
                                t.getRequesterName(),
                                t.getStartDate() != null ? DateUtil.formatDate(t.getStartDate()) : "N/A",
                                t.getDueDate() != null ? DateUtil.formatDate(t.getDueDate()) : "N/A",
                                t.getStatus(),
                                DateUtil.formatTimestamp(t.getRequestedAt())
                        });
                    }

                    // Populate outgoing
                    outgoingModel.setRowCount(0);
                    for (Transaction t : outgoingTxns) {
                        outgoingModel.addRow(new Object[]{
                                "#" + t.getTransactionId(),
                                t.getTransactionType().replace("_", " "),
                                t.getRequestedItemName(),
                                t.getOfferedItemName() != null ? t.getOfferedItemName() : "N/A",
                                t.getResponderName(),
                                t.getStartDate() != null ? DateUtil.formatDate(t.getStartDate()) : "N/A",
                                t.getDueDate() != null ? DateUtil.formatDate(t.getDueDate()) : "N/A",
                                t.getStatus(),
                                DateUtil.formatTimestamp(t.getRequestedAt())
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("RequestsPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void approveSelected() {
        int row = incomingTable.getSelectedRow();
        if (row < 0 || incomingTxns == null || row >= incomingTxns.size()) {
            JOptionPane.showMessageDialog(this, "Please select an incoming request to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction t = incomingTxns.get(row);
        User user = AuthService.getInstance().getCurrentUser();

        String confirmPrompt = "Approve " + t.getTransactionType().replace("_", " ") + " request for '" + t.getRequestedItemName() + "'?";
        if ("PERMANENT_EXCHANGE".equalsIgnoreCase(t.getTransactionType())) {
            confirmPrompt = "PERMANENT OWNERSHIP EXCHANGE:\nApproving this will immediately transfer ownership of '" +
                    t.getRequestedItemName() + "' to " + t.getRequesterName() +
                    ", and '" + t.getOfferedItemName() + "' to you.\nProceed?";
        }

        int conf = JOptionPane.showConfirmDialog(this, confirmPrompt, "Confirm Approval", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            transactionService.approveTransaction(t.getTransactionId(), user.getUserId());
            JOptionPane.showMessageDialog(this, "Request approved successfully! Transaction is now active.", "Request Approved", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            mainFrame.refreshBadges();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Approval Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelected() {
        int row = incomingTable.getSelectedRow();
        if (row < 0 || incomingTxns == null || row >= incomingTxns.size()) {
            JOptionPane.showMessageDialog(this, "Please select an incoming request to decline.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction t = incomingTxns.get(row);
        User user = AuthService.getInstance().getCurrentUser();

        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to decline this request?", "Confirm Decline", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            transactionService.rejectTransaction(t.getTransactionId(), user.getUserId());
            JOptionPane.showMessageDialog(this, "Request declined.", "Request Declined", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            mainFrame.refreshBadges();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Declining Request", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        int row = outgoingTable.getSelectedRow();
        if (row < 0 || outgoingTxns == null || row >= outgoingTxns.size()) {
            JOptionPane.showMessageDialog(this, "Please select an outgoing request to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction t = outgoingTxns.get(row);
        User user = AuthService.getInstance().getCurrentUser();

        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this request?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            transactionService.cancelTransaction(t.getTransactionId(), user.getUserId());
            JOptionPane.showMessageDialog(this, "Request cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            mainFrame.refreshBadges();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
