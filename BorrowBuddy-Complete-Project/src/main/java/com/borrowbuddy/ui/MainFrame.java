package com.borrowbuddy.ui;

import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.NotificationService;
import com.borrowbuddy.service.TransactionService;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window for BorrowBuddy.
 * Coordinates sidebar navigation, authenticated sessions, responsive layout,
 * and live notification badges.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Sidebar
    private final JPanel sidebar = new JPanel();
    private final JLabel lblCurrentUserName = new JLabel("Guest User");
    private final JLabel lblCurrentUserRole = new JLabel("Not Logged In");
    private final JButton btnAdmin;
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Panels
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final DashboardPanel dashboardPanel;
    private final BrowseItemsPanel browseItemsPanel;
    private final MyListingsPanel myListingsPanel;
    private final RequestsPanel requestsPanel;
    private final ActiveTransactionsPanel activeTransactionsPanel;
    private final TransactionHistoryPanel transactionHistoryPanel;
    private final NotificationsPanel notificationsPanel;
    private final ReviewsPanel reviewsPanel;
    private final AdminDashboardPanel adminDashboardPanel;

    private final NotificationService notificationService = new NotificationService();
    private final TransactionService transactionService = new TransactionService();

    public MainFrame() {
        super("BorrowBuddy – Campus Borrow & Exchange Management Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 780);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        // Instantiate panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        dashboardPanel = new DashboardPanel(this);
        browseItemsPanel = new BrowseItemsPanel(this);
        myListingsPanel = new MyListingsPanel(this);
        requestsPanel = new RequestsPanel(this);
        activeTransactionsPanel = new ActiveTransactionsPanel(this);
        transactionHistoryPanel = new TransactionHistoryPanel(this);
        notificationsPanel = new NotificationsPanel(this);
        reviewsPanel = new ReviewsPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);

        // Add to card layout
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");
        cardPanel.add(dashboardPanel, "DASHBOARD");
        cardPanel.add(browseItemsPanel, "BROWSE");
        cardPanel.add(myListingsPanel, "MY_LISTINGS");
        cardPanel.add(requestsPanel, "REQUESTS");
        cardPanel.add(activeTransactionsPanel, "ACTIVE_TXNS");
        cardPanel.add(transactionHistoryPanel, "HISTORY");
        cardPanel.add(notificationsPanel, "NOTIFICATIONS");
        cardPanel.add(reviewsPanel, "REVIEWS");
        cardPanel.add(adminDashboardPanel, "ADMIN");

        // Build Sidebar
        buildSidebar();
        btnAdmin = navButtons.get("ADMIN");

        // Main layout
        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        // Initially show Login and hide sidebar
        sidebar.setVisible(false);
        showPanel("LOGIN");
    }

    private void buildSidebar() {
        sidebar.setPreferredSize(new Dimension(240, 780));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setLayout(new BorderLayout());

        // Header
        JPanel topHeader = new JPanel();
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.setOpaque(false);
        topHeader.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));

        JLabel logo = new JLabel("🤝 BorrowBuddy");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(UITheme.TEXT_WHITE);
        topHeader.add(logo);

        JLabel tag = new JLabel("Campus Portal");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(new Color(180, 210, 245));
        topHeader.add(tag);
        topHeader.add(Box.createVerticalStrut(14));

        // User badge card
        JPanel userCard = new JPanel(new GridLayout(2, 1, 2, 2));
        userCard.setBackground(new Color(15, 48, 85));
        userCard.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lblCurrentUserName.setFont(UITheme.FONT_BOLD);
        lblCurrentUserName.setForeground(UITheme.TEXT_WHITE);
        lblCurrentUserRole.setFont(UITheme.FONT_SMALL);
        lblCurrentUserRole.setForeground(new Color(255, 215, 0));

        userCard.add(lblCurrentUserName);
        userCard.add(lblCurrentUserRole);
        topHeader.add(userCard);

        sidebar.add(topHeader, BorderLayout.NORTH);

        // Nav Links Container
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addNavButton(navList, "📊 Dashboard", "DASHBOARD");
        addNavButton(navList, "🔍 Browse Items", "BROWSE");
        addNavButton(navList, "📦 My Listings", "MY_LISTINGS");
        addNavButton(navList, "📥 Requests", "REQUESTS");
        addNavButton(navList, "🔄 Active Items", "ACTIVE_TXNS");
        addNavButton(navList, "📜 Past History", "HISTORY");
        addNavButton(navList, "🔔 Notifications", "NOTIFICATIONS");
        addNavButton(navList, "⭐ Reviews", "REVIEWS");
        addNavButton(navList, "🛡️ Admin Panel", "ADMIN");

        sidebar.add(navList, BorderLayout.CENTER);

        // Bottom Sign Out
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 16, 12));

        JButton logoutBtn = new JButton("🚪 Sign Out");
        logoutBtn.setFont(UITheme.FONT_BOLD);
        logoutBtn.setForeground(UITheme.TEXT_WHITE);
        logoutBtn.setBackground(new Color(198, 40, 40));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(210, 38));
        logoutBtn.addActionListener(e -> performLogout());

        bottomPanel.add(logoutBtn, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addNavButton(JPanel container, String text, String panelKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_WHITE);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 36));
        btn.setPreferredSize(new Dimension(220, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(30, 85, 140));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UITheme.BG_SIDEBAR);
            }
        });

        btn.addActionListener(e -> showPanel(panelKey));
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
        navButtons.put(panelKey, btn);
    }

    public void showPanel(String name) {
        cardLayout.show(cardPanel, name);

        // Trigger data load on target panel
        switch (name) {
            case "DASHBOARD" -> dashboardPanel.loadData();
            case "BROWSE" -> browseItemsPanel.loadData();
            case "MY_LISTINGS" -> myListingsPanel.loadData();
            case "REQUESTS" -> requestsPanel.loadData();
            case "ACTIVE_TXNS" -> activeTransactionsPanel.loadData();
            case "HISTORY" -> transactionHistoryPanel.loadData();
            case "NOTIFICATIONS" -> notificationsPanel.loadData();
            case "REVIEWS" -> reviewsPanel.loadData();
            case "ADMIN" -> adminDashboardPanel.loadData();
        }
    }

    public void onLoginSuccess(User user) {
        sidebar.setVisible(true);
        lblCurrentUserName.setText(user.getName());
        lblCurrentUserRole.setText(user.getRole() + (user.getDepartment() != null ? " • " + user.getDepartment() : ""));

        // Only show Admin nav button for ADMIN role
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (btnAdmin != null) {
            btnAdmin.setVisible(isAdmin);
        }

        refreshBadges();
        showPanel("DASHBOARD");
    }

    public void refreshBadges() {
        User u = AuthService.getInstance().getCurrentUser();
        if (u == null) return;

        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int unreadNotifs = notificationService.getUnreadCount(u.getUserId());
                int pendingRequests = transactionService.getIncomingRequests(u.getUserId()).size();
                return new int[]{unreadNotifs, pendingRequests};
            }

            @Override
            protected void done() {
                try {
                    int[] counts = get();
                    int unread = counts[0];
                    int pending = counts[1];

                    JButton notifBtn = navButtons.get("NOTIFICATIONS");
                    if (notifBtn != null) {
                        notifBtn.setText(unread > 0 ? "🔔 Notifications (" + unread + ")" : "🔔 Notifications");
                    }

                    JButton reqBtn = navButtons.get("REQUESTS");
                    if (reqBtn != null) {
                        reqBtn.setText(pending > 0 ? "📥 Requests (" + pending + ")" : "📥 Requests");
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void performLogout() {
        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            sidebar.setVisible(false);
            loginPanel.clearFields();
            showPanel("LOGIN");
        }
    }
}
