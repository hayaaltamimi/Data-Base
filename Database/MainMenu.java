package Database;
import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Photography Equipment Rental System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 10, 10));

        JButton managerBtn = new JButton("Manager Menu");
        JButton customerBtn = new JButton("Customer Menu");

        add(managerBtn);
        add(customerBtn);

        // open manager GUI
        managerBtn.addActionListener(e -> new ManagerGUI());

        // open customer GUI
        customerBtn.addActionListener(e -> new CustomerGUI());

        setLocationRelativeTo(null); // center window
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
