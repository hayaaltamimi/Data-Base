package Database;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginCustomer extends JDialog {

    public LoginCustomer(JFrame owner) {
        super(owner, "Customer", true);
        ModernUI.setupLaf();

        JPanel bg = ModernUI.gradientBackground();
        bg.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(new Color(255,255,255,245));
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 320));

        JLabel title = new JLabel("Customer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        ModernUI.styleField(user);
        ModernUI.styleField(pass);

        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        pass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new BorderLayout(0, 6));
        row1.setOpaque(false);
        JLabel l1 = new JLabel("Instagram Username");
        l1.setForeground(new Color(60,60,60));
        row1.add(l1, BorderLayout.NORTH);
        row1.add(user, BorderLayout.CENTER);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row2 = new JPanel(new BorderLayout(0, 6));
        row2.setOpaque(false);
        JLabel l2 = new JLabel("Phone Number (Password)");
        l2.setForeground(new Color(60,60,60));
        row2.add(l2, BorderLayout.NORTH);
        row2.add(pass, BorderLayout.CENTER);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton signIn = ModernUI.primaryButton("Sign In");
        JButton signUp = new JButton("Sign Up");
        signUp.setFocusPainted(false);
        signUp.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton cancel = new JButton("Cancel");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(signUp);
        actions.add(signIn);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Use Instagram username and phone number. New? Click Sign Up.");
        hint.setForeground(new Color(90,90,90));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(row1);
        card.add(Box.createVerticalStrut(10));
        card.add(row2);
        card.add(Box.createVerticalStrut(14));
        card.add(hint);
        card.add(Box.createVerticalStrut(16));
        card.add(actions);

        bg.add(card, new GridBagConstraints());

        setContentPane(bg);
        setSize(980, 640);
        setLocationRelativeTo(owner);

        // Fix Cancel button
        cancel.addActionListener(e -> {
            dispose();
            owner.setVisible(true); // Show main menu again
        });
        
        // Sign Up should open registration directly
        signUp.addActionListener(e -> {
            dispose();
            showRegistrationDialog(owner);
        });
        
        signIn.addActionListener(e -> doLogin(user.getText().trim(), new String(pass.getPassword()), owner));

        getRootPane().setDefaultButton(signIn);
        setVisible(true);
    }

    private void doLogin(String username, String password, JFrame owner) {
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.", "Sign in", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerId = null;
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new Exception("No DB connection");
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT Customer_id FROM CUSTOMER WHERE Instagram_user = ? AND Cphone_number = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) customerId = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error.", "Sign in", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (customerId == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Sign in", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        final String finalCustomerId = customerId;
        SwingUtilities.invokeLater(() -> new CustomerGUI(finalCustomerId));
    }

    private void showRegistrationDialog(JFrame owner) {
        // Create a registration dialog with the same ModernUI styling
        JDialog regDialog = new JDialog(owner, "Sign Up - New Customer", true);
        ModernUI.setupLaf();

        JPanel bg = ModernUI.gradientBackground();
        bg.setLayout(new GridBagLayout());

        JPanel card = new JPanel(new BorderLayout()); // Changed to BorderLayout
        card.setBackground(new Color(255,255,255,245));
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        card.setPreferredSize(new Dimension(500, 580)); // Increased size

        JLabel title = new JLabel("Create New Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create form fields
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridLayout(9, 2, 10, 10));
        
        JTextField customerIdField = new JTextField();
        JTextField fnameField = new JTextField();
        JTextField mnameField = new JTextField();
        JTextField lnameField = new JTextField();
        JTextField instagramField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        // Style fields
        ModernUI.styleField(customerIdField);
        ModernUI.styleField(fnameField);
        ModernUI.styleField(mnameField);
        ModernUI.styleField(lnameField);
        ModernUI.styleField(instagramField);
        ModernUI.styleField(cityField);
        ModernUI.styleField(streetField);
        ModernUI.styleField(emailField);
        ModernUI.styleField(phoneField);

        formPanel.add(new JLabel("Customer ID* (C + numbers):"));
        formPanel.add(customerIdField);
        formPanel.add(new JLabel("First Name*:"));
        formPanel.add(fnameField);
        formPanel.add(new JLabel("Middle Name:"));
        formPanel.add(mnameField);
        formPanel.add(new JLabel("Last Name*:"));
        formPanel.add(lnameField);
        formPanel.add(new JLabel("Instagram*:"));
        formPanel.add(instagramField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("Street:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("Email*:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone*:"));
        formPanel.add(phoneField);

        JButton registerBtn = ModernUI.primaryButton("Register");
        JButton backToLoginBtn = new JButton("Back to Login");
        JButton cancelBtn = new JButton("Cancel");
        
        // Create a panel for buttons with proper spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(backToLoginBtn);
        buttonPanel.add(registerBtn);

        JLabel hint = new JLabel("* Required fields. Already have an account? Click 'Back to Login'.");
        hint.setForeground(new Color(90,90,90));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel hintPanel = new JPanel();
        hintPanel.setOpaque(false);
        hintPanel.add(hint);

        // Create a panel for the form with scrolling if needed
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.add(formPanel, BorderLayout.CENTER);
        
        // Create a wrapper for the form to add some padding
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        formWrapper.add(formContainer, BorderLayout.NORTH);
        
        // Add everything to card
        card.add(title, BorderLayout.NORTH);
        card.add(formWrapper, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(hintPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        card.add(bottomPanel, BorderLayout.SOUTH);

        bg.add(card, new GridBagConstraints());

        regDialog.setContentPane(bg);
        regDialog.setSize(1050, 750); // Increased size
        regDialog.setLocationRelativeTo(owner);

        // Button actions
        cancelBtn.addActionListener(e -> {
            regDialog.dispose();
            owner.setVisible(true);
        });
        
        backToLoginBtn.addActionListener(e -> {
            regDialog.dispose();
            new LoginCustomer(owner);
        });
        
        registerBtn.addActionListener(e -> {
            // Call the registration method from CustomerGUI
            if (registerNewCustomer(
                customerIdField.getText().trim(),
                fnameField.getText().trim(),
                mnameField.getText().trim(),
                lnameField.getText().trim(),
                instagramField.getText().trim(),
                cityField.getText().trim(),
                streetField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                regDialog
            )) {
                // Registration successful, go to login
                regDialog.dispose();
                new LoginCustomer(owner);
            }
        });

        regDialog.setVisible(true);
    }

    private boolean registerNewCustomer(String customerId, String fname, String mname, String lname,
                                      String instagram, String city, String street, String email, 
                                      String phone, JDialog dialog) {
        // Validation
        if (customerId.isEmpty() || fname.isEmpty() || lname.isEmpty() || 
            instagram.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please fill all required fields (*)");
            return false;
        }

        if (!customerId.toUpperCase().matches("^C\\d+$")) {
            JOptionPane.showMessageDialog(dialog, "Customer ID must start with 'C' followed by numbers (e.g., C001, C123)");
            return false;
        }

        // Check if customer ID exists
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT Customer_id FROM CUSTOMER WHERE Customer_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, customerId.toUpperCase());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(dialog, "Customer ID already exists! Please choose a different one.");
                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error checking Customer ID availability");
            return false;
        }

        // Insert new customer
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO CUSTOMER (Customer_id, Fname, Mname, Lname, Instagram_user, City, Street_name, Cemail, Cphone_number) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, customerId.toUpperCase());
            stmt.setString(2, fname);
            stmt.setString(3, mname);
            stmt.setString(4, lname);
            stmt.setString(5, instagram);
            stmt.setString(6, city);
            stmt.setString(7, street);
            stmt.setString(8, email);
            stmt.setString(9, phone);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(dialog, "Registration successful! You can now sign in.");
            return true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error registering: " + ex.getMessage());
            return false;
        }
    }
}