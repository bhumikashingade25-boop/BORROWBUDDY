package com.borrowbuddy.ui;

import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern login panel with authentication and quick demo-login shortcuts.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        // Card container
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(36, 40, 36, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        int row = 0;

        // Brand Header
        JLabel logoLabel = new JLabel("🤝 BorrowBuddy", SwingConstants.CENTER);
        logoLabel.setFont(UITheme.FONT_APP_TITLE);
        logoLabel.setForeground(UITheme.PRIMARY);
        gbc.gridy = row++;
        card.add(logoLabel, gbc);

        JLabel subtitle = new JLabel("Campus Borrow & Exchange Management Portal", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = row++;
        card.add(subtitle, gbc);

        // Spacing
        gbc.gridy = row++;
        card.add(Box.createVerticalStrut(16), gbc);

        // Error message
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER);
        gbc.gridy = row++;
        card.add(errorLabel, gbc);

        // Email field
        JLabel emailLbl = new JLabel("Campus Email Address");
        emailLbl.setFont(UITheme.FONT_BOLD);
        emailLbl.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = row++;
        card.add(emailLbl, gbc);

        emailField = UITheme.createTextField("student@campus.edu");
        emailField.setPreferredSize(new Dimension(320, 38));
        gbc.gridy = row++;
        card.add(emailField, gbc);

        // Password field
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.FONT_BOLD);
        passLbl.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = row++;
        card.add(passLbl, gbc);

        passwordField = UITheme.createPasswordField();
        passwordField.setPreferredSize(new Dimension(320, 38));
        gbc.gridy = row++;
        card.add(passwordField, gbc);

        // Login Button
        JButton loginBtn = UITheme.createPrimaryButton("Sign In to Portal");
        loginBtn.setPreferredSize(new Dimension(320, 42));
        loginBtn.addActionListener(e -> performLogin());
        gbc.gridy = row++;
        gbc.insets = new Insets(16, 6, 6, 6);
        card.add(loginBtn, gbc);

        // Register link
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerRow.setBackground(UITheme.BG_WHITE);
        JLabel noAccountLbl = new JLabel("Don't have an account?");
        noAccountLbl.setFont(UITheme.FONT_BODY);
        noAccountLbl.setForeground(UITheme.TEXT_SECONDARY);

        JButton registerBtn = new JButton("Register Here");
        registerBtn.setFont(UITheme.FONT_BOLD);
        registerBtn.setForeground(UITheme.PRIMARY);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> mainFrame.showPanel("REGISTER"));

        registerRow.add(noAccountLbl);
        registerRow.add(registerBtn);
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 6, 6, 6);
        card.add(registerRow, gbc);

        // Quick Demo Fill Bar for convenience
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        demoBar.setBackground(new Color(240, 244, 248));
        demoBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JLabel demoLbl = new JLabel("Quick Demo:");
        demoLbl.setFont(UITheme.FONT_SMALL);
        demoBar.add(demoLbl);

        JButton fillAdmin = new JButton("Admin");
        fillAdmin.setFont(UITheme.FONT_SMALL);
        fillAdmin.addActionListener(e -> {
            emailField.setText("admin@campus.edu");
            passwordField.setText("Admin@123");
        });
        demoBar.add(fillAdmin);

        JButton fillStudent = new JButton("Aarav (Student)");
        fillStudent.setFont(UITheme.FONT_SMALL);
        fillStudent.addActionListener(e -> {
            emailField.setText("aarav@campus.edu");
            passwordField.setText("Password@123");
        });
        demoBar.add(fillStudent);

        JButton fillFaculty = new JButton("Rahul (Faculty)");
        fillFaculty.setFont(UITheme.FONT_SMALL);
        fillFaculty.addActionListener(e -> {
            emailField.setText("rahul@campus.edu");
            passwordField.setText("Password@123");
        });
        demoBar.add(fillFaculty);

        gbc.gridy = row++;
        gbc.insets = new Insets(14, 6, 0, 6);
        card.add(demoBar, gbc);

        add(card);

        // Enter key triggers login
        passwordField.addActionListener(e -> performLogin());
        emailField.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        errorLabel.setText(" ");
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return AuthService.getInstance().login(email, pass);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    mainFrame.onLoginSuccess(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText(cause.getMessage());
                }
            }
        }.execute();
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
    }
}
