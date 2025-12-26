package Database;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginCustomer extends JDialog {

    public LoginCustomer(JFrame owner) {
        super(owner, "Customer Sign In", true);
        ModernUI.setupLaf();

        JPanel bg = ModernUI.gradientBackground();
        bg.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(new Color(255,255,255,245));
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 260));

        JLabel title = new JLabel("Customer Sign In");
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
        JLabel l1 = new JLabel("Username");
        l1.setForeground(new Color(60,60,60));
        row1.add(l1, BorderLayout.NORTH);
        row1.add(user, BorderLayout.CENTER);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row2 = new JPanel(new BorderLayout(0, 6));
        row2.setOpaque(false);
        JLabel l2 = new JLabel("Password");
        l2.setForeground(new Color(60,60,60));
        row2.add(l2, BorderLayout.NORTH);
        row2.add(pass, BorderLayout.CENTER);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton signIn = ModernUI.primaryButton("Sign in");
        JButton cancel = new JButton("Cancel");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(signIn);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Enter your credentials to continue.");
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

        cancel.addActionListener(e -> dispose());
        signIn.addActionListener(e -> doLogin(user.getText().trim(), new String(pass.getPassword())));

        getRootPane().setDefaultButton(signIn);
        setVisible(true);
    }

    private void doLogin(String username, String password) {
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
        SwingUtilities.invokeLater(() -> new CustomerGUI(finalCustomerId));}
}
