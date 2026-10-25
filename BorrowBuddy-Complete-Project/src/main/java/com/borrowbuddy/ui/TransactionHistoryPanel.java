package com.borrowbuddy.ui;

import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.ReviewService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Historical record of completed, rejected, and cancelled transactions,
 * including permanent ownership transfers and post-completion reviews.
 */
public class TransactionHistoryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final TransactionService transactionService = new TransactionService();
    private final ReviewService reviewService = new ReviewService();

    private final JComboBox<String> statusFilter;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Transaction> historyTxns;

    public TransactionHistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Transaction History & Past Exchanges");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        statusFilter = new JComboBox<>(new String[]{"All Finalized", "COMPLETED", "REJECTED", "CANCELLED"});
        statusFilter.setFont(UITheme.FONT_BODY);
        statusFilter.addActionListener(e -> filterData());
        rightActions.add(new JLabel("Filter Status:"));
        rightActions.add(statusFilter);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Type", "Requested Item", "Offered Item", "Counterparty", "Status", "Requested", "Completed", "Review Status"};
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

        JButton reviewBtn = UITheme.createPrimaryButton("⭐ Write Review");
        reviewBtn.addActionListener(e -> openReviewDialog());
        bottomBar.add(reviewBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() {
                return transactionService.getTransactionHistory(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    historyTxns = get();
                    filterData();
                } catch (Exception ex) {
                    System.err.println("TransactionHistoryPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void filterData() {
        if (historyTxns == null) return;
        String filter = (String) statusFilter.getSelectedItem();
        User user = AuthService.getInstance().getCurrentUser();

        tableModel.setRowCount(0);
        for (Transaction t : historyTxns) {
            if (filter != null && !filter.equals("All Finalized") && !t.getStatus().equalsIgnoreCase(filter)) {
                continue;
            }

            String partner = (t.getRequesterId() == user.getUserId()) ? t.getResponderName() : t.getRequesterName();
            boolean hasRev = reviewService.hasReview(t.getTransactionId());
            String revStatus = "COMPLETED".equalsIgnoreCase(t.getStatus()) ?
                    (hasRev ? "⭐ Reviewed" : "✍️ Needs Review") : "N/A";

            tableModel.addRow(new Object[]{
                    "#" + t.getTransactionId(),
                    t.getTransactionType().replace("_", " "),
                    t.getRequestedItemName(),
                    t.getOfferedItemName() != null ? t.getOfferedItemName() : "N/A",
                    partner,
                    t.getStatus(),
                    DateUtil.formatTimestamp(t.getRequestedAt()),
                    t.getCompletedAt() != null ? DateUtil.formatTimestamp(t.getCompletedAt()) : "N/A",
                    revStatus
            });
        }
    }

    private Transaction getSelectedTransaction() {
        int row = table.getSelectedRow();
        if (row < 0 || historyTxns == null || row >= historyTxns.size()) {
            JOptionPane.showMessageDialog(this, "Please select a completed transaction from the history table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return historyTxns.get(row);
    }

    private void openReviewDialog() {
        Transaction t = getSelectedTransaction();
        if (t == null) return;

        if (!"COMPLETED".equalsIgnoreCase(t.getStatus())) {
            JOptionPane.showMessageDialog(this, "Reviews can only be written for COMPLETED transactions.", "Cannot Review", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reviewService.hasReview(t.getTransactionId())) {
            JOptionPane.showMessageDialog(this, "A review has already been submitted for this transaction.", "Already Reviewed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User user = AuthService.getInstance().getCurrentUser();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Write Review: " + t.getRequestedItemName(), true);
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(UITheme.BG_WHITE);

        JPanel form = new JPanel(new GridLayout(3, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        form.setBackground(UITheme.BG_WHITE);

        JLabel itemLbl = new JLabel("Reviewing Item: " + t.getRequestedItemName());
        itemLbl.setFont(UITheme.FONT_SUBTITLE);
        form.add(itemLbl);

        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratePanel.setBackground(UITheme.BG_WHITE);
        ratePanel.add(new JLabel("Rating (1 to 5 Stars): "));
        JSpinner starSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        starSpinner.setPreferredSize(new Dimension(60, 30));
        ratePanel.add(starSpinner);
        form.add(ratePanel);

        JTextArea comments = new JTextArea(4, 20);
        comments.setLineWrap(true);
        form.add(new JScrollPane(comments));

        dlg.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton cancel = UITheme.createSecondaryButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton submit = UITheme.createPrimaryButton("Submit Review");
        submit.addActionListener(e -> {
            try {
                int rating = (Integer) starSpinner.getValue();
                String comm = comments.getText().trim();
                reviewService.submitReview(user.getUserId(), t.getTransactionId(), rating, comm);
                JOptionPane.showMessageDialog(dlg, "Thank you! Your review has been saved.", "Review Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error Saving Review", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(cancel);
        actions.add(submit);
        dlg.add(actions, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
