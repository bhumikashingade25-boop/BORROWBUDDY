package com.borrowbuddy.ui;

import com.borrowbuddy.model.Review;
import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.service.ReviewService;
import com.borrowbuddy.util.DateUtil;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Reviews panel displaying post-transaction ratings and user feedback.
 */
public class ReviewsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ReviewService reviewService = new ReviewService();

    private final DefaultTableModel receivedModel;
    private final JTable receivedTable;

    private final DefaultTableModel authoredModel;
    private final JTable authoredTable;

    public ReviewsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Community Reviews & Trust Ratings");
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

        // Tab 1: Reviews Received on My Items
        JPanel tabReceived = new JPanel(new BorderLayout(8, 8));
        tabReceived.setOpaque(false);

        String[] recCols = {"Item", "Reviewer", "Rating", "Comments", "Date"};
        receivedModel = new DefaultTableModel(recCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        receivedTable = new JTable(receivedModel);
        UITheme.styleTable(receivedTable);
        tabReceived.add(UITheme.createScrollPane(receivedTable), BorderLayout.CENTER);
        tabbedPane.addTab("⭐ Feedback on My Items", tabReceived);

        // Tab 2: Reviews I've Given
        JPanel tabAuthored = new JPanel(new BorderLayout(8, 8));
        tabAuthored.setOpaque(false);

        String[] authCols = {"Item", "Rating", "Comments", "Date"};
        authoredModel = new DefaultTableModel(authCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        authoredTable = new JTable(authoredModel);
        UITheme.styleTable(authoredTable);
        tabAuthored.add(UITheme.createScrollPane(authoredTable), BorderLayout.CENTER);
        tabbedPane.addTab("✍️ Reviews Written by Me", tabAuthored);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private String formatStars(int rating) {
        return "★".repeat(Math.max(0, rating)) + "☆".repeat(Math.max(0, 5 - rating)) + " (" + rating + "/5)";
    }

    public void loadData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        new SwingWorker<Void, Void>() {
            private List<Review> received;
            private List<Review> authored;

            @Override
            protected Void doInBackground() {
                received = reviewService.getReviewsForOwner(user.getUserId());
                authored = reviewService.getReviewsByUser(user.getUserId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    receivedModel.setRowCount(0);
                    for (Review r : received) {
                        receivedModel.addRow(new Object[]{
                                r.getItemName(),
                                r.getUserName(),
                                formatStars(r.getRating()),
                                r.getComments(),
                                DateUtil.formatTimestamp(r.getReviewDate())
                        });
                    }

                    authoredModel.setRowCount(0);
                    for (Review r : authored) {
                        authoredModel.addRow(new Object[]{
                                r.getItemName(),
                                formatStars(r.getRating()),
                                r.getComments(),
                                DateUtil.formatTimestamp(r.getReviewDate())
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("ReviewsPanel load error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
