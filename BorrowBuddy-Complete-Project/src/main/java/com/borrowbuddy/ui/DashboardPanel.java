package com.borrowbuddy.ui;

import com.borrowbuddy.dao.TransactionDAO;
import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.ItemService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Main dashboard displaying live MySQL statistics and recent campus activity.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ItemService itemService = new ItemService();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final JLabel welcomeLabel;
    private final JLabel roleLabel;
    private final JLabel lblPendingTitle = new JLabel("⏳ Pending Requests");
    private final JLabel lblCompletedTitle = new JLabel("✔️ Completed Total");
    private final JLabel activityTitle = new JLabel("Recent Activity & Transactions");

    // Stat cards value labels
    private final JLabel lblTotalItems = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblAvailableItems = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblPendingRequests = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblActiveTransactions = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblTempExchanges = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblPermExchanges = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblOverdue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblCompleted = new JLabel("0", SwingConstants.CENTER);

    private final DefaultTableModel recentTableModel;
    private final JTable recentTable;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Welcome Banner
        JPanel topBanner = new JPanel(new BorderLayout(12, 4));
        topBanner.setOpaque(false);

        JPanel userTitles = new JPanel(new GridLayout(2, 1, 2, 2));
        userTitles.setOpaque(false);
        welcomeLabel = new JLabel("Welcome back to BorrowBuddy!");
        welcomeLabel.setFont(UITheme.FONT_APP_TITLE);
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);

        roleLabel = new JLabel("Campus Resource Management Portal");
        roleLabel.setFont(UITheme.FONT_SUBTITLE);
        roleLabel.setForeground(UITheme.TEXT_SECONDARY);

        userTitles.add(welcomeLabel);
        userTitles.add(roleLabel);
        topBanner.add(userTitles, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Stats");
        refreshBtn.addActionListener(e -> loadData());
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightActions.setOpaque(false);
        rightActions.add(refreshBtn);
        topBanner.add(rightActions, BorderLayout.EAST);

        add(topBanner, BorderLayout.NORTH);

        // Center Content: Stats Cards + Recent Activity Table
        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.setOpaque(false);

        // 2. Stats Grid (2 rows x 4 cols)
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(800, 190));

        statsGrid.add(createStatCard("📚 Total Catalog Items", lblTotalItems, UITheme.PRIMARY));
        statsGrid.add(createStatCard("✅ Available Items", lblAvailableItems, UITheme.SUCCESS));
        statsGrid.add(createStatCard(lblPendingTitle, lblPendingRequests, UITheme.WARNING));
        statsGrid.add(createStatCard("🔄 Active Transactions", lblActiveTransactions, UITheme.ACCENT));
        statsGrid.add(createStatCard("📦 Temp Exchanges", lblTempExchanges, new Color(142, 68, 173)));
        statsGrid.add(createStatCard("🔁 Permanent Exchanges", lblPermExchanges, new Color(41, 128, 185)));
        statsGrid.add(createStatCard("⚠️ Overdue Items", lblOverdue, UITheme.DANGER));
        statsGrid.add(createStatCard(lblCompletedTitle, lblCompleted, new Color(39, 174, 96)));

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // 3. Recent Activity Section
        JPanel activityPanel = new JPanel(new BorderLayout(8, 8));
        activityPanel.setBackground(UITheme.BG_WHITE);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // activityTitle is a field – set initial text here
        activityTitle.setText("Recent Campus Activity & Transactions");
        activityTitle.setFont(UITheme.FONT_TITLE);
        activityTitle.setForeground(UITheme.TEXT_PRIMARY);
        activityPanel.add(activityTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Type", "Requested Item", "Requester", "Owner / Partner", "Status", "Date"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        recentTable = new JTable(recentTableModel);
        UITheme.styleTable(recentTable);
        JScrollPane scrollPane = UITheme.createScrollPane(recentTable);
        activityPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(activityPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JLabel titleLbl = new JLabel(title);
        return buildStatCard(titleLbl, valueLabel, accentColor);
    }

    private JPanel createStatCard(JLabel titleLbl, JLabel valueLabel, Color accentColor) {
        return buildStatCard(titleLbl, valueLabel, accentColor);
    }

    private JPanel buildStatCard(JLabel titleLbl, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(UITheme.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 229, 235), 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        ));

        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshUserInfo() {
        User u = AuthService.getInstance().getCurrentUser();
        if (u != null) {
            welcomeLabel.setText("Welcome back, " + u.getName() + "!");
            String dept = u.getDepartment() != null ? u.getDepartment() : "General Campus";
            roleLabel.setText(u.getRole() + " • " + dept + " • " + u.getEmail());
        }
    }

    public void loadData() {
        refreshUserInfo();

        User currentUser = AuthService.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        // Update card titles based on role
        if (!isAdmin && currentUser != null) {
            lblCompletedTitle.setText("✔️ Completed by Me");
            lblPendingTitle.setText("⏳ My Pending");
            activityTitle.setText("My Recent Activity & Transactions");
        } else {
            lblCompletedTitle.setText("✔️ Completed Total");
            lblPendingTitle.setText("⏳ Pending Requests");
            activityTitle.setText("Recent Campus Activity & Transactions");
        }

        new SwingWorker<Map<String, Integer>, Void>() {
            private List<Transaction> recentTxns;

            @Override
            protected Map<String, Integer> doInBackground() {
                if (isAdmin) {
                    recentTxns = transactionDAO.findRecentActivity(15);
                    return itemService.getDashboardStats();
                } else {
                    recentTxns = transactionDAO.findByUser(currentUser.getUserId());
                    // Limit to 15 most recent
                    if (recentTxns.size() > 15) recentTxns = recentTxns.subList(0, 15);
                    return itemService.getDashboardStats(currentUser);
                }
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> stats = get();
                    lblTotalItems.setText(String.valueOf(stats.getOrDefault("totalItems", 0)));
                    lblAvailableItems.setText(String.valueOf(stats.getOrDefault("availableItems", 0)));
                    lblPendingRequests.setText(String.valueOf(stats.getOrDefault("pendingRequests", 0)));
                    lblActiveTransactions.setText(String.valueOf(stats.getOrDefault("activeTransactions", 0)));
                    lblTempExchanges.setText(String.valueOf(stats.getOrDefault("tempExchanges", 0)));
                    lblPermExchanges.setText(String.valueOf(stats.getOrDefault("permExchanges", 0)));
                    lblOverdue.setText(String.valueOf(stats.getOrDefault("overdueTransactions", 0)));
                    lblCompleted.setText(String.valueOf(stats.getOrDefault("completedTransactions", 0)));

                    // Populate table
                    recentTableModel.setRowCount(0);
                    for (Transaction t : recentTxns) {
                        recentTableModel.addRow(new Object[]{
                                "#" + t.getTransactionId(),
                                t.getTransactionType().replace("_", " "),
                                t.getRequestedItemName(),
                                t.getRequesterName(),
                                t.getResponderName(),
                                t.getStatus(),
                                DateUtil.formatTimestamp(t.getRequestedAt())
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("DashboardPanel.loadData error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
