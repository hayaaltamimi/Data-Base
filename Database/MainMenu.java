package Database;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        ModernUI.setupLaf();
        setTitle("Photography Equipment Rental");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = ModernUI.gradientBackground();
        root.setLayout(new GridBagLayout());

        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(920, 520));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(28, 28, 18, 28));
        top.add(ModernUI.header("Photography Equipment Rental", "Rent cameras, lenses, and studio gear."), BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        center.setLayout(new GridLayout(1, 2, 16, 16));

        JPanel managerCard = ModernUI.smallCard("Manager Sign In", "Manage branches, employees, equipment, rentals, and reports.", "Sign in");
        JPanel customerCard = ModernUI.smallCard("Customer Sign In", "Browse equipment, create rentals, and view bills and history.", "Sign in");

        JButton mBtn = findAction(managerCard);
        JButton cBtn = findAction(customerCard);

        mBtn.addActionListener(e -> new LoginManager(this));
        cBtn.addActionListener(e -> new LoginCustomer(this));

        center.add(managerCard);
        center.add(customerCard);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(18, 28, 24, 28));
        JLabel foot = new JLabel("© RentalDB", SwingConstants.LEFT);
        foot.setForeground(new Color(255,255,255,160));
        bottom.add(foot, BorderLayout.WEST);

        shell.add(top, BorderLayout.NORTH);
        shell.add(center, BorderLayout.CENTER);
        shell.add(bottom, BorderLayout.SOUTH);

        root.add(shell, new GridBagConstraints());

        setContentPane(root);
        setSize(1100, 700);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton findAction(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton b && "action".equals(b.getName())) return b;
            if (comp instanceof Container cc) {
                JButton r = findAction(cc);
                if (r != null) return r;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
