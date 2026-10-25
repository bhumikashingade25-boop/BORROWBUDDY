package com.borrowbuddy;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.ui.MainFrame;

import javax.swing.*;
import java.sql.Connection;

/**
 * Main application entry point for BorrowBuddy.
 * Tests database connectivity, sets system look-and-feel, and launches the UI.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  BorrowBuddy – Campus Borrow & Exchange Management Portal");
        System.out.println("==========================================================");

        // 1. Verify MySQL database connectivity
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Database connection verified: " + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion());
        } catch (Exception ex) {
            System.err.println("⚠️ Warning: Could not connect to MySQL database.");
            System.err.println("   Details: " + ex.getMessage());
            System.err.println("   Please check your db.properties configuration.");
        }

        // 2. Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look and Feel
        }

        // 3. Launch GUI on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("🚀 BorrowBuddy graphical user interface initialized successfully.");
            } catch (Exception ex) {
                System.err.println("❌ Failed to launch UI: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
