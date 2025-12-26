package Database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class ManagerGUI extends JFrame {

    private String loggedInManagerId;
    private String managerBranchId;
    private JTable resultsTable;

    public ManagerGUI() {
        ModernUI.setupLaf();
        setTitle("Manager Menu");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        if (loginManager()) {
            createManagerGUI();
        } else {
            dispose();
        }
    }
    public ManagerGUI(String managerId) {
        ModernUI.setupLaf();
        setTitle("Manager");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        loggedInManagerId = managerId;

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
            dispose();
            return;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Branch_id FROM BRANCH WHERE Branch_manager_id = ?"
            );
            ps.setString(1, loggedInManagerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                managerBranchId = rs.getString(1);
            } else {
                JOptionPane.showMessageDialog(this, "This employee is not registered as a branch manager.");
                dispose();
                return;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage());
            dispose();
            return;
        }

        createManagerGUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private boolean loginManager() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField empIdField = new JTextField();
        JTextField emailField = new JTextField();

        panel.add(new JLabel("Manager Employee ID:"));
        panel.add(empIdField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Manager Login", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return false;

        String empId = empIdField.getText().trim();
        String email = emailField.getText().trim();

        if (empId.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Employee ID and Email");
            return false;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return false;
        }

        try (conn) {
            String sql = "SELECT e.Employee_id, b.Branch_id " +
                         "FROM EMPLOYEE e " +
                         "JOIN BRANCH b ON b.Branch_manager_id = e.Employee_id " +
                         "WHERE e.Employee_id = ? AND e.Eemail = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, empId);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInManagerId = empId;
                managerBranchId = rs.getString("Branch_id");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "You are not registered as a branch manager or credentials are wrong.");
                return false;
            }
        } catch (SQLException ex) {
            showDbError(ex, "during login");
            return false;
        }
    }

    private void createManagerGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        top.setBackground(new Color(20, 24, 32));

        JLabel title = new JLabel("Photography Equipment Rental");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel subtitle = new JLabel("Manager: " + loggedInManagerId + "   •   Branch: " + managerBranchId);
        subtitle.setForeground(new Color(200, 205, 215));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titleBox = new JPanel(new GridLayout(2,1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        JButton logoutBtn = ModernUI.primaryButton("Logout");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new MainMenu().setVisible(true);
            }
        });
        top.add(titleBox, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        JPanel sidebar = ModernUI.sidebar();
        JButton navEmployees = ModernUI.navButton("Employees");
        JButton navEquipment  = ModernUI.navButton("Equipment");
        JButton navCustomers  = ModernUI.navButton("Customers");
        JButton navRentals    = ModernUI.navButton("Rentals");
        JButton navBranch     = ModernUI.navButton("Branch info");

        sidebar.add(ModernUI.sidebarHeader("Dashboard"));
        sidebar.add(navEmployees);
        sidebar.add(navEquipment);
        sidebar.add(navCustomers);
        sidebar.add(navRentals);
    
        sidebar.add(navBranch);
        sidebar.add(Box.createVerticalGlue());

        CardLayout cardsLayout = new CardLayout();
        JPanel cards = new JPanel(cardsLayout);
        cards.setOpaque(false);

        cards.add(ModernUI.card("Manage Employees", createEmployeePanel()), "EMP");
        cards.add(ModernUI.card("Manage Equipment", createEquipmentPanel()), "EQ");
        cards.add(ModernUI.card("Manage Customers", createCustomerPanel()), "CUST");
        cards.add(ModernUI.card("Manage Rentals", createRentalPanel()), "RENT");
        cards.add(ModernUI.card("Branch Info", createBranchPanel()), "BRANCH");

        
resultsTable = ModernUI.makeResultsTable();
JScrollPane consoleScroll = new JScrollPane(resultsTable);
consoleScroll.setBorder(BorderFactory.createEmptyBorder());
JPanel consoleCard = ModernUI.card("", consoleScroll);

        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cards, consoleCard);
        vertical.setResizeWeight(0.72);
        vertical.setBorder(null);

        JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, vertical);
        horizontal.setResizeWeight(0.18);
        horizontal.setBorder(null);

        root.add(top, BorderLayout.NORTH);
        root.add(horizontal, BorderLayout.CENTER);

        setContentPane(root);
        setLocationRelativeTo(null);
        setVisible(true);

        navEmployees.addActionListener(e -> cardsLayout.show(cards, "EMP"));
        navEquipment.addActionListener(e -> cardsLayout.show(cards, "EQ"));
        navCustomers.addActionListener(e -> cardsLayout.show(cards, "CUST"));
        navRentals.addActionListener(e -> cardsLayout.show(cards, "RENT"));
        navBranch.addActionListener(e -> cardsLayout.show(cards, "BRANCH"));
        cardsLayout.show(cards, "EMP");

}

    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addEmpBtn = new JButton("Add Employee");
        JButton updateEmpBtn = new JButton("Update Employee");
        JButton deleteEmpBtn = new JButton("Delete Employee");
        JButton viewEmpBranchBtn = new JButton("View Employees in My Branch");

        addEmpBtn.addActionListener(e -> addEmployee());
        updateEmpBtn.addActionListener(e -> updateEmployee());
        deleteEmpBtn.addActionListener(e -> deleteEmployee());
        viewEmpBranchBtn.addActionListener(e -> queryEmployeesInBranch());

        panel.add(addEmpBtn);
        panel.add(updateEmpBtn);
        panel.add(deleteEmpBtn);
        panel.add(viewEmpBranchBtn);

        return panel;
    }

    private void addEmployee() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        JTextField idField = new JTextField();
        JTextField fField = new JTextField();
        JTextField mField = new JTextField();
        JTextField lField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField salaryField = new JTextField();

        panel.add(new JLabel("Employee ID*:"));
        panel.add(idField);
        panel.add(new JLabel("First Name*:"));
        panel.add(fField);
        panel.add(new JLabel("Middle Name:"));
        panel.add(mField);
        panel.add(new JLabel("Last Name*:"));
        panel.add(lField);
        panel.add(new JLabel("Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone*:"));
        panel.add(phoneField);
        panel.add(new JLabel("Salary*:"));
        panel.add(salaryField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String id = idField.getText().trim();
        String fname = fField.getText().trim();
        String mname = mField.getText().trim();
        String lname = lField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String salaryStr = salaryField.getText().trim();

        if (id.isEmpty() || fname.isEmpty() || lname.isEmpty() || email.isEmpty() || phone.isEmpty() || salaryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All * fields are required.");
            return;
        }

        if (!isValidName(fname) || !isValidName(lname) || (!mname.isEmpty() && !isValidName(mname))) {
            JOptionPane.showMessageDialog(this, "Names can contain letters and spaces only.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 or 12 digits.");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Salary must be a number.");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sqlManager = "SELECT Salary FROM EMPLOYEE WHERE Employee_id = ?";
            PreparedStatement mgrStmt = conn.prepareStatement(sqlManager);
            mgrStmt.setString(1, loggedInManagerId);
            ResultSet mgrRs = mgrStmt.executeQuery();
            double managerSalary = 0;
            if (mgrRs.next()) {
                managerSalary = mgrRs.getDouble("Salary");
            } else {
                JOptionPane.showMessageDialog(this, "Manager salary not found. Cannot validate employee salary.");
                return;
            }

            if (salary > managerSalary) {
                JOptionPane.showMessageDialog(this, "Employee salary cannot be higher than the branch manager's salary.");
                return;
            }

            String sql = "INSERT INTO EMPLOYEE " +
                         "(Employee_id, Fname, Mname, Lname, Eemail, Ephone_number, Salary, Employee_branch_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, fname);
            stmt.setString(3, mname);
            stmt.setString(4, lname);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setDouble(7, salary);
            stmt.setString(8, managerBranchId);

            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Employee added successfully.\nID: " + id : "Failed to add employee."));} catch (SQLException ex) {
            showDbError(ex, "adding employee");
        }
    }

    private void updateEmployee() {
        String empId = JOptionPane.showInputDialog(this, "Enter Employee ID to update:", "Update Employee", JOptionPane.QUESTION_MESSAGE);
        if (empId == null || empId.trim().isEmpty()) return;
        empId = empId.trim();

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField salaryField = new JTextField();

        panel.add(new JLabel("New Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("New Phone*:"));
        panel.add(phoneField);
        panel.add(new JLabel("New Salary*:"));
        panel.add(salaryField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Employee " + empId, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String salaryStr = salaryField.getText().trim();

        if (email.isEmpty() || phone.isEmpty() || salaryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }
        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 or 12 digits.");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Salary must be a number.");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sqlManager = "SELECT Salary FROM EMPLOYEE WHERE Employee_id = ?";
            PreparedStatement mgrStmt = conn.prepareStatement(sqlManager);
            mgrStmt.setString(1, loggedInManagerId);
            ResultSet mgrRs = mgrStmt.executeQuery();
            double managerSalary = 0;
            if (mgrRs.next()) {
                managerSalary = mgrRs.getDouble("Salary");
            } else {
                JOptionPane.showMessageDialog(this, "Manager salary not found. Cannot validate employee salary.");
                return;
            }

            if (!empId.equals(loggedInManagerId) && salary > managerSalary) {
                JOptionPane.showMessageDialog(this, "Employee salary cannot be higher than the branch manager's salary.");
                return;
            }

            String sql = "UPDATE EMPLOYEE SET Eemail = ?, Ephone_number = ?, Salary = ? WHERE Employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setDouble(3, salary);
            stmt.setString(4, empId);

            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Employee updated successfully." : "Employee not found."));} catch (SQLException ex) {
            showDbError(ex, "updating employee");
        }
    }

    private void deleteEmployee() {
        String empId = JOptionPane.showInputDialog(this, "Enter Employee ID to delete:", "Delete Employee", JOptionPane.QUESTION_MESSAGE);
        if (empId == null || empId.trim().isEmpty()) return;
        empId = empId.trim();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete employee " + empId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "DELETE FROM EMPLOYEE WHERE Employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, empId);
            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Employee deleted successfully." : "Employee not found."));} catch (SQLException ex) {
            showDbError(ex, "deleting employee");
        }
    }

    private void queryEmployeesInBranch() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT Employee_id, Fname, Mname, Lname, Eemail, Ephone_number, Salary " +
                         "FROM EMPLOYEE WHERE Employee_branch_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, managerBranchId);
            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            showDbError(ex, "retrieving employees in branch");
        }
    }

    private JPanel createEquipmentPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addEqBtn = new JButton("Add Equipment");
        JButton updateEqBtn = new JButton("Update Equipment");
        JButton deleteEqBtn = new JButton("Delete Equipment");
        JButton viewAvailableEqBtn = new JButton("View Available Equipment");
        JButton mostRentedEqBtn = new JButton("Most Rented Equipment");
        JButton outOfStockEqBtn = new JButton("Out of Stock Equipment");

        addEqBtn.addActionListener(e -> addEquipment());
        updateEqBtn.addActionListener(e -> updateEquipment());
        deleteEqBtn.addActionListener(e -> deleteEquipment());
        viewAvailableEqBtn.addActionListener(e -> queryAvailableEquipment());
        mostRentedEqBtn.addActionListener(e -> runMostRentedEquipment());
        outOfStockEqBtn.addActionListener(e -> runOutOfStockEquipment());

        panel.add(addEqBtn);
        panel.add(updateEqBtn);
        panel.add(deleteEqBtn);
        panel.add(viewAvailableEqBtn);
        panel.add(mostRentedEqBtn);
        panel.add(outOfStockEqBtn);

        return panel;
    }

    private void addEquipment() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField feeField = new JTextField();
        JTextField qtyField = new JTextField();

        panel.add(new JLabel("Equipment ID*:"));
        panel.add(idField);
        panel.add(new JLabel("Name*:"));
        panel.add(nameField);
        panel.add(new JLabel("Type:"));
        panel.add(typeField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Price*:"));
        panel.add(priceField);
        panel.add(new JLabel("Rental Fee*:"));
        panel.add(feeField);
        panel.add(new JLabel("Available Quantity*:"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Equipment", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String type = typeField.getText().trim();
        String desc = descField.getText().trim();
        String priceStr = priceField.getText().trim();
        String feeStr = feeField.getText().trim();
        String qtyStr = qtyField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty() || feeStr.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All * fields are required.");
            return;
        }

        double price, fee;
        int qty;
        try {
            price = Double.parseDouble(priceStr);
            fee = Double.parseDouble(feeStr);
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price, fee and quantity must be numeric.");
            return;
        }

        if (qty < 0) {
            JOptionPane.showMessageDialog(this, "Available quantity cannot be negative.");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "INSERT INTO EQUIPMENT " +
                         "(Equipment_id, Qname, Type, Description, Price, Rental_fee, Available_quantity) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, type);
            stmt.setString(4, desc);
            stmt.setDouble(5, price);
            stmt.setDouble(6, fee);
            stmt.setInt(7, qty);

            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Equipment added successfully.\nID: " + id : "Failed to add equipment."));} catch (SQLException ex) {
            showDbError(ex, "adding equipment");
        }
    }

    private void updateEquipment() {
        String eqId = JOptionPane.showInputDialog(this, "Enter Equipment ID to update:", "Update Equipment", JOptionPane.QUESTION_MESSAGE);
        if (eqId == null || eqId.trim().isEmpty()) return;
        eqId = eqId.trim();

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField priceField = new JTextField();
        JTextField feeField = new JTextField();
        JTextField qtyField = new JTextField();

        panel.add(new JLabel("New Price*:"));
        panel.add(priceField);
        panel.add(new JLabel("New Rental Fee*:"));
        panel.add(feeField);
        panel.add(new JLabel("New Available Quantity*:"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Equipment " + eqId, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String priceStr = priceField.getText().trim();
        String feeStr = feeField.getText().trim();
        String qtyStr = qtyField.getText().trim();

        if (priceStr.isEmpty() || feeStr.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        double price, fee;
        int qty;
        try {
            price = Double.parseDouble(priceStr);
            fee = Double.parseDouble(feeStr);
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price, fee and quantity must be numeric.");
            return;
        }

        if (qty < 0) {
            JOptionPane.showMessageDialog(this, "Available quantity cannot be negative.");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "UPDATE EQUIPMENT SET Price = ?, Rental_fee = ?, Available_quantity = ? WHERE Equipment_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, price);
            stmt.setDouble(2, fee);
            stmt.setInt(3, qty);
            stmt.setString(4, eqId);

            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Equipment updated successfully." : "Equipment not found."));} catch (SQLException ex) {
            showDbError(ex, "updating equipment");
        }
    }

    private void deleteEquipment() {
        String eqId = JOptionPane.showInputDialog(this, "Enter Equipment ID to delete:", "Delete Equipment", JOptionPane.QUESTION_MESSAGE);
        if (eqId == null || eqId.trim().isEmpty()) return;
        eqId = eqId.trim();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete equipment " + eqId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "DELETE FROM EQUIPMENT WHERE Equipment_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, eqId);
            int rows = stmt.executeUpdate();
            ModernUI.populateSingleColumn(resultsTable, "Message", String.valueOf(rows > 0 ? "Equipment deleted successfully." : "Equipment not found."));} catch (SQLException ex) {
            showDbError(ex, "deleting equipment");
        }
    }

    private void queryAvailableEquipment() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT Equipment_id, Qname, Type, Description, Rental_fee, Available_quantity " +
                         "FROM EQUIPMENT WHERE Available_quantity > 0 ORDER BY Type, Qname";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            showDbError(ex, "retrieving available equipment");
        }
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton viewCustomersBtn = new JButton("View All Customers");
        JButton customerSummaryBtn = new JButton("Customer Spending Summary");

        viewCustomersBtn.addActionListener(e -> viewAllCustomers());
        customerSummaryBtn.addActionListener(e -> runCustomerSpendingSummary());

        panel.add(viewCustomersBtn);
        panel.add(customerSummaryBtn);

        return panel;
    }

    private void viewAllCustomers() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT Customer_id, Fname, Mname, Lname, Instagram_user, City, Street_name, Cemail, Cphone_number " +
                         "FROM CUSTOMER ORDER BY Fname, Lname";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);
        } catch (SQLException ex) {
            showDbError(ex, "retrieving customers");
        }
    }

    private JPanel createRentalPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton fullRentalReportBtn = new JButton("Full Rental Report (Customer + Equipment + Bill)");
        JButton activeRentalsBtn = new JButton("Current Active Rentals");

        fullRentalReportBtn.addActionListener(e -> runRentalCustomerEquipmentBill());
        activeRentalsBtn.addActionListener(e -> runActiveRentals());

        panel.add(fullRentalReportBtn);
        panel.add(activeRentalsBtn);

        return panel;
    }

    private JPanel createBranchPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton branchInfoBtn = new JButton("View My Branch Info");
        JButton branchStockBtn = new JButton("View My Branch Stock");

        branchInfoBtn.addActionListener(e -> viewMyBranchInfo());
        branchStockBtn.addActionListener(e -> viewMyBranchStock());

        panel.add(branchInfoBtn);
        panel.add(branchStockBtn);

        return panel;
    }

    private void runRentalCustomerEquipmentBill() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql =
                "SELECT c.Customer_id, c.Fname, c.Mname, c.Lname, " +
                "       r.Rental_num, r.Receive_datetime, r.Return_datetime, " +
                "       e.Equipment_id, e.Qname, e.Type, m.Equipment_quantity, " +
                "       b.Bill_id, b.Bill_date, b.Total_amount, b.Payment_method " +
                "FROM RENTAL r " +
                "JOIN CUSTOMER c ON r.Rental_customer_id = c.Customer_id " +
                "JOIN MAKES m ON m.M_rental_number = r.Rental_num " +
                "JOIN EQUIPMENT e ON m.M_equipment_id = e.Equipment_id " +
                "LEFT JOIN BILL b ON b.Bill_rental_num = r.Rental_num " +
                "ORDER BY r.Receive_datetime DESC, r.Rental_num";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);

        } catch (SQLException ex) {
            showDbError(ex, "retrieving rental details (Q4)");
        }
    }

    private void runActiveRentals() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT c.Customer_id, c.Fname, c.Mname, c.Lname, " +
                         "       r.Rental_num, r.Receive_datetime, r.Return_datetime, " +
                         "       e.Equipment_id, e.Qname, e.Type, " +
                         "       m.Equipment_quantity, " +
                         "       b.Bill_id, b.Total_amount " +
                         "FROM RENTAL r " +
                         "JOIN CUSTOMER c ON r.Rental_customer_id = c.Customer_id " +
                         "JOIN MAKES m ON r.Rental_num = m.M_rental_number " +
                         "JOIN EQUIPMENT e ON m.M_equipment_id = e.Equipment_id " +
                         "LEFT JOIN BILL b ON b.Bill_rental_num = r.Rental_num " +
                         "WHERE r.Return_datetime IS NULL " +
                         "ORDER BY r.Receive_datetime";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);
        } catch (SQLException ex) {
            showDbError(ex, "retrieving active rentals");
        }
    }

    private void runCustomerSpendingSummary() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT c.Customer_id, c.Fname, c.Mname, c.Lname, " +
                         "       COUNT(DISTINCT r.Rental_num) AS Total_rentals, " +
                         "       COALESCE(SUM(m.Equipment_quantity), 0) AS Total_items_rented, " +
                         "       COALESCE(SUM(b.Total_amount), 0) AS Total_money_spent " +
                         "FROM CUSTOMER c " +
                         "LEFT JOIN RENTAL r ON r.Rental_customer_id = c.Customer_id " +
                         "LEFT JOIN MAKES m ON m.M_rental_number = r.Rental_num " +
                         "LEFT JOIN BILL b ON b.Bill_rental_num = r.Rental_num " +
                         "GROUP BY c.Customer_id, c.Fname, c.Mname, c.Lname " +
                         "ORDER BY Total_money_spent DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);
        } catch (SQLException ex) {
            showDbError(ex, "retrieving customer spending summary");
        }
    }

    private void runMostRentedEquipment() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT e.Equipment_id, e.Qname, e.Type, " +
                         "       SUM(m.Equipment_quantity) AS Total_rented_quantity " +
                         "FROM EQUIPMENT e " +
                         "JOIN MAKES m ON e.Equipment_id = m.M_equipment_id " +
                         "GROUP BY e.Equipment_id, e.Qname, e.Type " +
                         "ORDER BY Total_rented_quantity DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            showDbError(ex, "retrieving most rented equipment");
        }
    }

    private void runOutOfStockEquipment() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT Equipment_id, Qname, Type, Description, Price, Rental_fee, Available_quantity " +
                         "FROM EQUIPMENT " +
                         "WHERE Available_quantity = 0 " +
                         "ORDER BY Qname";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            showDbError(ex, "retrieving out of stock equipment");
        }
    }

    private void viewMyBranchInfo() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT Branch_id, City, Street_name, BPhone_number, Branch_manager_id " +
                         "FROM BRANCH WHERE Branch_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, managerBranchId);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);
        } catch (SQLException ex) {
            showDbError(ex, "retrieving branch info");
        }
    }

    private void viewMyBranchStock() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showConnectionError();
            return;
        }

        try (conn) {
            String sql = "SELECT s.Stock_branch_id, s.Stock_equipment_id, e.Qname, e.Type " +
                         "FROM STOCK s " +
                         "JOIN EQUIPMENT e ON s.Stock_equipment_id = e.Equipment_id " +
                         "WHERE s.Stock_branch_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, managerBranchId);
            ResultSet rs = stmt.executeQuery();

            // FIXED: Use table instead of text
            ModernUI.populateTable(resultsTable, rs);
        } catch (SQLException ex) {
            showDbError(ex, "retrieving branch stock");
        }
    }

    private boolean isValidName(String name) {
        return Pattern.matches("^[a-zA-Z\\s]+$", name);
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    private boolean isValidPhone(String phone) {
        return Pattern.matches("^[0-9]{10}$|^[0-9]{12}$", phone);
    }

    private void showDbError(SQLException ex, String action) {
        String msg = ex.getMessage();
        JOptionPane.showMessageDialog(this, "Database error " + action + ":" + msg, "Database Error", JOptionPane.ERROR_MESSAGE);
        ModernUI.populateSingleColumn(resultsTable, "Error", "Database error " + action + ":" + msg);
    }

    private void showConnectionError() {
        JOptionPane.showMessageDialog(this, "Could not connect to database. Please check connection settings.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        ModernUI.populateSingleColumn(resultsTable, "Error", "Connection error: could not connect to database.");
    }
    }