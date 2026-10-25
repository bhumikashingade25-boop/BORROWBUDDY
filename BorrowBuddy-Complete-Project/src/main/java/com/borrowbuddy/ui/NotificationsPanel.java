package com.borrowbuddy.ui;

import com.borrowbuddy.model.Notification;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.NotificationService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * In-app notifications center showing system alerts, request statuses,
 * due dates, and return notifications.
 */
public class NotificationsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final NotificationService notificationService = new NotificationService();

    private final JLabel unreadBadge;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Notification> notifications;

    public NotificationsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Campus Notifications & Activity Alerts");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        left.add(title);

        unreadBadge = new JLabel("0 New");
        unreadBadge.setFont(UITheme.FONT_SMALL);
        unreadBadge.setForeground(UITheme.TEXT_WHITE);
        unreadBadge.setBackground(UITheme.DANGER);
        unreadBadge.setOpaque(true);
        unreadBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        left.add(unreadBadge);
        topBar.add(left, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton markAllBtn = UITheme.createSecondaryButton("Mark All as Read");
        markAllBtn.addActionListener(e -> markAllRead());
        rightActions.add(markAllBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Type", "Message", "Timestamp", "Read?"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel && notifications != null && row < notifications.size()) {
                    Notification n = notifications.get(row);
                    if (!n.isRead()) {
                        c.setFont(UITheme.FONT_BOLD);
                        c.setBackground(new Color(235, 243, 255));
                    } else {
                        c.setFont(UITheme.FONT_BODY);
                        c.setBackground(row % 2 == 0 ? UITheme.BG_WHITE : UITheme.ROW_ALTERNATE);
                    }
                }
                return c;
            }
        });

        add(UITheme.createScrollPane(table), BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomBar.setOpaque(false);

        JButton markSelected = UITheme.createPrimaryButton("Mark Selected as Read");
        markSelected.addActionListener(e -> markSelectedRead());
        bottomBar.add(markSelected);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<List<Notification>, Void>() {
            @Override
            protected List<Notification> doInBackground() {
                return notificationService.getNotifications(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    notifications = get();
                    tableModel.setRowCount(0);
                    int unread = 0;
                    for (Notification n : notifications) {
                        if (!n.isRead()) unread++;
                        String iconType = switch (n.getType()) {
                            case "REQUEST_SUBMITTED" -> "📨 New Request";
                            case "REQUEST_ACCEPTED" -> "✅ Accepted";
                            case "REQUEST_REJECTED" -> "❌ Declined";
                            case "ACTIVATED" -> "🔄 Active";
                            case "RETURN_RECORDED" -> "📦 Return Logged";
                            case "RETURN_CONFIRMED" -> "✔️ Confirmed";
                            case "OWNERSHIP_TRANSFERRED" -> "🔁 Ownership Transfer";
                            default -> "🔔 Notification";
                        };

                        tableModel.addRow(new Object[]{
                                iconType,
                                n.getMessage(),
                                DateUtil.formatTimestamp(n.getCreatedAt()),
                                n.isRead() ? "Read" : "UNREAD"
                        });
                    }
                    unreadBadge.setText(unread + " New");
                    unreadBadge.setVisible(unread > 0);
                    mainFrame.refreshBadges();
                } catch (Exception ex) {
                    System.err.println("NotificationsPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void markSelectedRead() {
        int row = table.getSelectedRow();
        if (row < 0 || notifications == null || row >= notifications.size()) {
            JOptionPane.showMessageDialog(this, "Please select a notification to mark as read.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Notification n = notifications.get(row);
        if (!n.isRead()) {
            notificationService.markAsRead(n.getNotificationId());
            loadData();
        }
    }

    private void markAllRead() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;
        notificationService.markAllAsRead(user.getUserId());
        loadData();
    }
}
