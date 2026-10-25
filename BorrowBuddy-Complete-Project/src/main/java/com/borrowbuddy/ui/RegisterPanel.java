package com.borrowbuddy.ui;

import com.borrowbuddy.model.User;
import com.borrowbuddy.service.AuthService;
import com.borrowbuddy.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Registration panel allowing Students and Faculty to sign up for BorrowBuddy.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private final JComboBox<String> roleCombo;
    private final JTextField deptField;
    private final JTextField phoneField;
    private final JLabel errorLabel;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        int row = 0;

        JLabel titleLbl = new JLabel("Join BorrowBuddy", SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY);
        gbc.gridy = row++;
        card.add(titleLbl, gbc);

        JLabel subtitle = new JLabel("Create a campus account to borrow and exchange items", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = row++;
        card.add(subtitle, gbc);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER);
        gbc.gridy = row++;
        card.add(errorLabel, gbc);

        // Fields
        nameField = UITheme.createTextField("e.g. John Doe");
        emailField = UITheme.createTextField("e.g. john@campus.edu");
        passwordField = UITheme.createPasswordField();
        confirmPasswordField = UITheme.createPasswordField();
        roleCombo = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});
        roleCombo.setFont(UITheme.FONT_BODY);
        roleCombo.setBackground(UITheme.BG_WHITE);
        deptField = UITheme.createTextField("e.g. Computer Science");
        phoneField = UITheme.createTextField("e.g. 555-0199");

        addField(card, gbc, row++, "Full Name *", nameField);
        addField(card, gbc, row++, "Campus Email *", emailField);
        addField(card, gbc, row++, "Account Role *", roleCombo);
        addField(card, gbc, row++, "Department", deptField);
        addField(card, gbc, row++, "Phone Number", phoneField);
        addField(card, gbc, row++, "Password (min 8 chars, 1 digit, 1 special) *", passwordField);
        addField(card, gbc, row++, "Confirm Password *", confirmPasswordField);

        // Register Button
        JButton registerBtn = UITheme.createPrimaryButton("Create Account");
        registerBtn.setPreferredSize(new Dimension(340, 40));
        registerBtn.addActionListener(e -> performRegister());
        gbc.gridy = row + 7;
        gbc.insets = new Insets(14, 6, 6, 6);
        card.add(registerBtn, gbc);

        // Back to login
        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginRow.setBackground(UITheme.BG_WHITE);
        JLabel haveAccount = new JLabel("Already registered?");
        haveAccount.setFont(UITheme.FONT_BODY);
        haveAccount.setForeground(UITheme.TEXT_SECONDARY);

        JButton backBtn = new JButton("Sign In");
        backBtn.setFont(UITheme.FONT_BOLD);
        backBtn.setForeground(UITheme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> mainFrame.showPanel("LOGIN"));

        loginRow.add(haveAccount);
        loginRow.add(backBtn);
        gbc.gridy = row + 8;
        gbc.insets = new Insets(4, 6, 4, 6);
        card.add(loginRow, gbc);

        add(card);
    }

    private void addField(JPanel card, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBackground(UITheme.BG_WHITE);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        comp.setPreferredSize(new Dimension(340, 36));
        card.add(p, gbc);
    }

    private void performRegister() {
        errorLabel.setText(" ");
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        String dept = deptField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all required fields (*).");
            return;
        }

        if (!pass.equals(confirmPass)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setRole(role);
        u.setDepartment(dept.isEmpty() ? null : dept);
        u.setPhone(phone.isEmpty() ? null : phone);

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return AuthService.getInstance().register(u, pass);
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(RegisterPanel.this,
                            "Registration successful! You can now log in with your credentials.",
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    mainFrame.showPanel("LOGIN");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText(cause.getMessage());
                }
            }
        }.execute();
    }

    public void clearFields() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        deptField.setText("");
        phoneField.setText("");
        errorLabel.setText(" ");
    }
}
