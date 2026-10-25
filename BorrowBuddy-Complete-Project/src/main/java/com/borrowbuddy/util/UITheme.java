package com.borrowbuddy.util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralised UI theme constants and factory methods for the BorrowBuddy Swing interface.
 * Implements a modern blue/white/light-grey palette with rounded components.
 */
public class UITheme {

    // ── Colour Palette ──────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(25, 118, 210);
    public static final Color PRIMARY_DARK   = new Color(13, 71, 161);
    public static final Color PRIMARY_LIGHT  = new Color(100, 181, 246);
    public static final Color ACCENT         = new Color(0, 172, 193);
    public static final Color SUCCESS        = new Color(46, 125, 50);
    public static final Color WARNING        = new Color(230, 81, 0);
    public static final Color DANGER         = new Color(198, 40, 40);
    public static final Color BG_WHITE       = Color.WHITE;
    public static final Color BG_LIGHT       = new Color(245, 248, 255);
    public static final Color BG_GRAY        = new Color(236, 239, 241);
    public static final Color BG_SIDEBAR     = new Color(21, 67, 115);
    public static final Color TEXT_PRIMARY   = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY = new Color(97, 97, 97);
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color BORDER         = new Color(189, 189, 189);
    public static final Color ROW_ALTERNATE  = new Color(232, 240, 254);
    public static final Color OVERDUE_ROW    = new Color(255, 235, 238);

    // ── Typography ──────────────────────────────────────────────────────────
    public static final Font FONT_APP_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO      = new Font("Consolas", Font.PLAIN, 12);

    // ── Spacing ─────────────────────────────────────────────────────────────
    public static final int PADDING       = 16;
    public static final int PADDING_SMALL = 8;
    public static final int BORDER_RADIUS = 8;

    private UITheme() {}

    // ────────────────────────────────────────────────────────────────────────
    // Button Factories
    // ────────────────────────────────────────────────────────────────────────

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BG_WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(227, 242, 253)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(BG_WHITE); }
        });
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(183, 28, 28)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(DANGER); }
        });
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(27, 94, 32)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(SUCCESS); }
        });
        return btn;
    }

    public static JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(2, 8, 2, 8));
        return btn;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Label Factories
    // ────────────────────────────────────────────────────────────────────────

    public static JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel createBodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Card / Panel Factories
    // ────────────────────────────────────────────────────────────────────────

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 240), 1),
            new EmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        return card;
    }

    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, PADDING_SMALL));
        card.setBackground(BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 240), 1),
            new EmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Input Factories
    // ────────────────────────────────────────────────────────────────────────

    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(5, 8, 5, 8)
        ));
        if (placeholder != null && !placeholder.isEmpty()) {
            tf.putClientProperty("placeholder", placeholder);
        }
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return pf;
    }

    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_WHITE);
        return cb;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Table Styling
    // ────────────────────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(187, 222, 251));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Alternating row colours via custom renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_WHITE : ROW_ALTERNATE);
                }
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // Header styling – ALWAYS visible, solid blue bar across all Look & Feels
        JTableHeader header = table.getTableHeader();
        // Use BasicTableHeaderUI so OS Look-and-Feel (e.g. Windows) doesn't hide text behind unhovered skin
        header.setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        header.setFont(FONT_BOLD);
        header.setBackground(PRIMARY);
        header.setForeground(TEXT_WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        header.setOpaque(true);

        TableCellRenderer headerRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "");
                lbl.setFont(FONT_BOLD);
                lbl.setForeground(TEXT_WHITE);
                lbl.setBackground(PRIMARY);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, PRIMARY_DARK),
                        new EmptyBorder(0, 10, 0, 10)
                ));
                return lbl;
            }
        };

        header.setDefaultRenderer(headerRenderer);
        javax.swing.table.TableColumnModel colModel = table.getColumnModel();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            colModel.getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    public static JScrollPane createScrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        // Ensure the column header (table header) is always shown
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            sp.setColumnHeaderView(table.getTableHeader());
        }
        return sp;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Status Badge Labels
    // ────────────────────────────────────────────────────────────────────────

    public static JLabel createStatusLabel(String status) {
        JLabel lbl = new JLabel(" " + status + " ");
        lbl.setFont(FONT_SMALL);
        lbl.setOpaque(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        switch (status) {
            case "PENDING"   -> { lbl.setBackground(new Color(255, 243, 224)); lbl.setForeground(new Color(230, 81, 0)); }
            case "ACCEPTED"  -> { lbl.setBackground(new Color(232, 245, 233)); lbl.setForeground(SUCCESS); }
            case "ACTIVE"    -> { lbl.setBackground(new Color(227, 242, 253)); lbl.setForeground(PRIMARY); }
            case "COMPLETED" -> { lbl.setBackground(new Color(232, 245, 233)); lbl.setForeground(new Color(27, 94, 32)); }
            case "REJECTED"  -> { lbl.setBackground(new Color(255, 235, 238)); lbl.setForeground(DANGER); }
            case "CANCELLED" -> { lbl.setBackground(BG_GRAY); lbl.setForeground(TEXT_SECONDARY); }
            default          -> { lbl.setBackground(BG_GRAY); lbl.setForeground(TEXT_PRIMARY); }
        }
        return lbl;
    }

    /** Renders a star rating string, e.g. "★★★★☆" for rating=4. */
    public static String starsFor(int rating) {
        return "★".repeat(Math.max(0, Math.min(5, rating))) +
               "☆".repeat(Math.max(0, 5 - Math.min(5, rating)));
    }

    /** Applies the global Swing look-and-feel defaults for a clean appearance. */
    public static void applyGlobalDefaults() {
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("OptionPane.background", BG_LIGHT);
        UIManager.put("Panel.background", BG_LIGHT);
    }
}
