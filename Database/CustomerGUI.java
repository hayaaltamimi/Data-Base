package Database;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;


public class CustomerGUI extends JFrame {
    private String loggedInCustomerId;
    private JTable resultsTable;

    public CustomerGUI() {
        ModernUI.setupLaf();
        setTitle("Customer Menu");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        if (showLoginOrRegister()) {
            createCustomerGUI();
        } else {
            dispose();
        }
    }
    public CustomerGUI(String customerId) {
        ModernUI.setupLaf();
        setTitle("Customer");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        loggedInCustomerId = customerId;
        createCustomerGUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private boolean showLoginOrRegister() {
        String[] options = {"Login", "Register as New Customer", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "Welcome to Photography Rental System!",
            "Customer Menu",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        switch (choice) {
            case 0: return loginCustomer();
            case 1: return registerNewCustomer();
            default: return false;
        }
    }

    private boolean loginCustomer() {
        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField customerIdField = new JTextField();
        JTextField emailField = new JTextField();

        loginPanel.add(new JLabel("Customer ID:"));
        loginPanel.add(customerIdField);
        loginPanel.add(new JLabel("Email:"));
        loginPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, loginPanel, "Customer Login",
                                                  JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String inputCustomerId = customerIdField.getText().trim();
            String inputEmail = emailField.getText().trim();

            if (inputCustomerId.isEmpty() || inputEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Customer ID and Email");
                return false;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT Customer_id FROM CUSTOMER WHERE Customer_id = ? AND Cemail = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, inputCustomerId);
                stmt.setString(2, inputEmail);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    loggedInCustomerId = inputCustomerId;
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Customer ID or Email combination!");
                    return false;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during login: " + ex.getMessage());
                return false;
            }
        }
        return false;
    }

    private boolean registerNewCustomer() {
        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));

        JTextField customerIdField = new JTextField();
        JTextField fnameField = new JTextField();
        JTextField mnameField = new JTextField();
        JTextField lnameField = new JTextField();
        JTextField instagramField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        panel.add(new JLabel("Customer ID* (C + numbers):"));
        panel.add(customerIdField);
        panel.add(new JLabel("First Name*:"));
        panel.add(fnameField);
        panel.add(new JLabel("Middle Name:"));
        panel.add(mnameField);
        panel.add(new JLabel("Last Name*:"));
        panel.add(lnameField);
        panel.add(new JLabel("Instagram*:"));
        panel.add(instagramField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("Street:"));
        panel.add(streetField);
        panel.add(new JLabel("Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone*:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Register New Customer",
                                                  JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String customerId = customerIdField.getText().trim();

            if (customerId.isEmpty() ||
                fnameField.getText().trim().isEmpty() ||
                lnameField.getText().trim().isEmpty() ||
                instagramField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (*)");
                return false;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String checkSql = "SELECT Customer_id FROM CUSTOMER WHERE Customer_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, customerId.toUpperCase());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Customer ID already exists! Please choose a different one.");
                    return false;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error checking Customer ID availability");
                return false;
            }


            if (!customerId.toUpperCase().matches("^C\\d+$")) {
                JOptionPane.showMessageDialog(this, "Customer ID must start with 'C' followed by numbers (e.g., C001, C123)");
                return false;
            }


            if (!isValidName(fnameField.getText()) || !isValidName(lnameField.getText()) ||
                (!mnameField.getText().trim().isEmpty() && !isValidName(mnameField.getText()))) {
                JOptionPane.showMessageDialog(this, "Name fields can only contain letters!");
                return false;
            }


            if (!cityField.getText().trim().isEmpty() && !isValidCityStreet(cityField.getText())) {
                JOptionPane.showMessageDialog(this, "City can only contain letters and spaces!");
                return false;
            }


            if (!streetField.getText().trim().isEmpty() && !isValidCityStreet(streetField.getText())) {
                JOptionPane.showMessageDialog(this, "Street can only contain letters and spaces!");
                return false;
            }


            if (!isValidInstagram(instagramField.getText())) {
                JOptionPane.showMessageDialog(this, "Instagram username can only contain letters, numbers, dots (.), and underscores (_)!");
                return false;
            }


            if (!isValidEmail(emailField.getText())) {
                JOptionPane.showMessageDialog(this, "Invalid email format! Use: name@domain.com");
                return false;
            }

            if (!isValidPhone(phoneField.getText())) {
                JOptionPane.showMessageDialog(this, "Phone number must be 10 or 12 digits and contain only numbers!");
                return false;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO CUSTOMER (Customer_id, Fname, Mname, Lname, Instagram_user, City, Street_name, Cemail, Cphone_number) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, customerId.toUpperCase());
                stmt.setString(2, fnameField.getText().trim());
                stmt.setString(3, mnameField.getText().trim());
                stmt.setString(4, lnameField.getText().trim());
                stmt.setString(5, instagramField.getText().trim());
                stmt.setString(6, cityField.getText().trim());
                stmt.setString(7, streetField.getText().trim());
                stmt.setString(8, emailField.getText().trim());
                stmt.setString(9, phoneField.getText().trim());

                stmt.executeUpdate();

                loggedInCustomerId = customerId.toUpperCase();
                JOptionPane.showMessageDialog(this, "Registration successful!");
                return true;

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error registering: " + ex.getMessage());
                return false;
            }
        }
        return false;
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


    private boolean isValidCityStreet(String text) {
        return Pattern.matches("^[a-zA-Z\\s]+$", text);
    }


    private boolean isValidInstagram(String instagram) {
        return Pattern.matches("^[a-zA-Z0-9._]+$", instagram);
    }


    private void createCustomerGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        top.setBackground(new Color(20, 24, 32));

        JLabel title = new JLabel("Photography Equipment Rental");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel subtitle = new JLabel("Customer: " + loggedInCustomerId);
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
        JButton navProfile  = ModernUI.navButton("My profile");
        JButton navBrowse   = ModernUI.navButton("Browse equipment");
        JButton navRent     = ModernUI.navButton("Create rental");
        JButton navBills    = ModernUI.navButton("Bills");
        JButton navQueries  = ModernUI.navButton("Queries");

        sidebar.add(ModernUI.sidebarHeader("Menu"));
        sidebar.add(navProfile);
        sidebar.add(navBrowse);
        sidebar.add(navRent);
        sidebar.add(navBills);
        sidebar.add(navQueries);
        sidebar.add(Box.createVerticalGlue());

        CardLayout cardsLayout = new CardLayout();
        JPanel cards = new JPanel(cardsLayout);
        cards.setOpaque(false);

        cards.add(ModernUI.card("My Profile", createProfilePanel()), "PROF");
        cards.add(ModernUI.card("Browse Equipment", createEquipmentPanel()), "BROWSE");
        cards.add(ModernUI.card("Create Rental", createRentalPanel()), "RENT");
        cards.add(ModernUI.card("Bills", createBillPanel()), "BILLS");
        cards.add(ModernUI.card("Queries", createQueriesPanel()), "Q");

        
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

        navProfile.addActionListener(e -> cardsLayout.show(cards, "PROF"));
        navBrowse.addActionListener(e -> cardsLayout.show(cards, "BROWSE"));
        navRent.addActionListener(e -> cardsLayout.show(cards, "RENT"));
        navBills.addActionListener(e -> cardsLayout.show(cards, "BILLS"));
        navQueries.addActionListener(e -> cardsLayout.show(cards, "Q"));
        cardsLayout.show(cards, "BROWSE");

}


    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton viewProfileBtn = new JButton("View My Profile");

        JButton updateProfileBtn = new JButton("Update My Profile");
        JButton deleteAccountBtn = new JButton("Delete My Account");

        panel.add(viewProfileBtn);

        panel.add(updateProfileBtn);
        panel.add(deleteAccountBtn);

        viewProfileBtn.addActionListener(e -> viewMyProfile());

        updateProfileBtn.addActionListener(e -> updateMyProfile());
        deleteAccountBtn.addActionListener(e -> deleteMyAccount());

        return panel;
    }

    private void viewMyProfile() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Fname, Mname, Lname, Instagram_user, City, Street_name, Cemail, Cphone_number " +
                        "FROM CUSTOMER WHERE Customer_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String profile = "=== MY PROFILE ===\n" +
                               "Customer ID: " + loggedInCustomerId + "\n" +
                               "Name: " + rs.getString("Fname") + " " +
                               (rs.getString("Mname") != null ? rs.getString("Mname") + " " : "") +
                               rs.getString("Lname") + "\n" +
                               "Instagram: " + rs.getString("Instagram_user") + "\n" +
                               "Address: " + rs.getString("City") + ", " + rs.getString("Street_name") + "\n" +
                               "Email: " + rs.getString("Cemail") + "\n" +
                               "Phone: " + rs.getString("Cphone_number");
                ModernUI.populateSingleColumn(resultsTable, "Profile", profile);
            } else {
                ModernUI.populateSingleColumn(resultsTable, "Message", "Customer not found!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMyProfile() {

        String currentEmail = "", currentPhone = "", currentCity = "", currentStreet = "";
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Cemail, Cphone_number, City, Street_name FROM CUSTOMER WHERE Customer_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentEmail = rs.getString("Cemail");
                currentPhone = rs.getString("Cphone_number");
                currentCity = rs.getString("City");
                currentStreet = rs.getString("Street_name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField emailField = new JTextField(currentEmail);
        JTextField phoneField = new JTextField(currentPhone);
        JTextField cityField = new JTextField(currentCity);
        JTextField streetField = new JTextField(currentStreet);

        panel.add(new JLabel("Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone*:"));
        panel.add(phoneField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("Street:"));
        panel.add(streetField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Profile",
                                                  JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

        	if (emailField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
        	    JOptionPane.showMessageDialog(this, "Email and Phone are required!");
        	    return;
        	}

        	if (!cityField.getText().trim().isEmpty() && !isValidCityStreet(cityField.getText())) {
        	    JOptionPane.showMessageDialog(this, "City can only contain letters and spaces!");
        	    return;
        	}

        	if (!streetField.getText().trim().isEmpty() && !isValidCityStreet(streetField.getText())) {
        	    JOptionPane.showMessageDialog(this, "Street can only contain letters and spaces!");
        	    return;
        	}

        	if (!isValidEmail(emailField.getText())) {
        	    JOptionPane.showMessageDialog(this, "Invalid email format!");
        	    return;
        	}

        	if (!isValidPhone(phoneField.getText())) {
        	    JOptionPane.showMessageDialog(this, "Phone must contain only numbers and be 10 or 12 digits!");
        	    return;
        	}

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE CUSTOMER SET Cemail = ?, Cphone_number = ?, City = ?, Street_name = ? " +
                            "WHERE Customer_id = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, emailField.getText());
                stmt.setString(2, phoneField.getText());
                stmt.setString(3, cityField.getText());
                stmt.setString(4, streetField.getText());
                stmt.setString(5, loggedInCustomerId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ModernUI.populateSingleColumn(resultsTable, "Message", "Profile updated successfully.");} else {
                    ModernUI.populateSingleColumn(resultsTable, "Message", "Failed to update profile.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteMyAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete your account? This cannot be undone!",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM CUSTOMER WHERE Customer_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, loggedInCustomerId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Account deleted successfully!");
                    dispose();
                } else {
                    ModernUI.populateSingleColumn(resultsTable, "Message", "Failed to delete account.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createEquipmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton viewEquipmentBtn = new JButton("Browse Available Equipment");
        JButton searchEquipmentBtn = new JButton("Search Equipment by Type");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(viewEquipmentBtn);
        buttonPanel.add(searchEquipmentBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);

        viewEquipmentBtn.addActionListener(e -> browseAvailableEquipment());
        searchEquipmentBtn.addActionListener(e -> searchEquipmentByType());

        return panel;
    }

    private void browseAvailableEquipment() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Equipment_id, Qname, Type, Description, Rental_fee, Available_quantity " +
                        "FROM EQUIPMENT WHERE Available_quantity > 0";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error browsing equipment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchEquipmentByType() {
        String[] equipmentTypes = {"Camera", "Lens", "Drone", "Stabilizer", "Tripod", "Studio Flash", "All Types"};
        String selectedType = (String) JOptionPane.showInputDialog(this,
            "Select equipment type:",
            "Search Equipment",
            JOptionPane.QUESTION_MESSAGE,
            null,
            equipmentTypes,
            equipmentTypes[0]);

        if (selectedType != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                PreparedStatement stmt;

                if (selectedType.equals("All Types")) {
                    sql = "SELECT Equipment_id, Qname, Type, Description, Rental_fee, Available_quantity " +
                         "FROM EQUIPMENT WHERE Available_quantity > 0";
                    stmt = conn.prepareStatement(sql);
                } else {
                    sql = "SELECT Equipment_id, Qname, Type, Description, Rental_fee, Available_quantity " +
                         "FROM EQUIPMENT WHERE Type = ? AND Available_quantity > 0";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, selectedType);
                }

                ResultSet rs = stmt.executeQuery();

                
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error searching equipment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createRentalPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton createRentalBtn = new JButton("Create New Rental");
        JButton viewRentalHistoryBtn = new JButton("View My Rental History & Details");
        JButton cancelRentalBtn = new JButton("Cancel Rental");

        panel.add(createRentalBtn);
        panel.add(viewRentalHistoryBtn);
        panel.add(cancelRentalBtn);

        createRentalBtn.addActionListener(e -> createRental());
        viewRentalHistoryBtn.addActionListener(e -> viewRentalHistory());
        cancelRentalBtn.addActionListener(e -> cancelMyRental());

        return panel;
    }

      private void createRental() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));


        JTextArea itemsArea = new JTextArea(5, 30);
        JScrollPane itemsScroll = new JScrollPane(itemsArea);

  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDateTime = dateFormat.format(new Date());

        JTextField receiveDateTimeField = new JTextField(currentDateTime);
        JTextField returnDateTimeField = new JTextField();

        panel.add(new JLabel("Equipment and Quantity*:"));
        panel.add(new JLabel("One per line: EquipmentID,Quantity"));

        panel.add(new JLabel(""));
        panel.add(itemsScroll);

        panel.add(new JLabel("Receive DateTime (YYYY-MM-DD HH:MM)*:"));
        panel.add(receiveDateTimeField);
        panel.add(new JLabel("Return DateTime (YYYY-MM-DD HH:MM)*:"));
        panel.add(returnDateTimeField);
        panel.add(new JLabel(""));
        panel.add(new JLabel("*Dates must be today or future"));

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Rental",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String receiveInput = receiveDateTimeField.getText().trim();
            String returnInput = returnDateTimeField.getText().trim();


            if (itemsArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter at least one equipment and quantity.");
                return;
            }


            if (!isValidTimeFormat(receiveInput) || !isValidTimeFormat(returnInput)) {
                JOptionPane.showMessageDialog(this, "Invalid datetime format! Use YYYY-MM-DD HH:MM");
                return;
            }


            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                sdf.setLenient(false);

                Date receiveDate = sdf.parse(receiveInput);
                Date returnDate = sdf.parse(returnInput);

                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date today = dateOnlyFormat.parse(dateOnlyFormat.format(new Date()));
                Date receiveDateOnly = dateOnlyFormat.parse(dateOnlyFormat.format(receiveDate));

                if (receiveDateOnly.before(today)) {
                    JOptionPane.showMessageDialog(this, "Receive date must be today or in the future!");
                    return;
                }

                if (!returnDate.after(receiveDate)) {
                    JOptionPane.showMessageDialog(this, "Return date must be after receive date!");
                    return;
                }


                if (!isValidTimeFormat(receiveInput) || !isValidTimeFormat(returnInput)) {
                    JOptionPane.showMessageDialog(this, "Invalid time! Hours must be 00-23, minutes 00-59");
                    return;
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error parsing dates: " + ex.getMessage());
                return;
            }


            List<String> equipmentIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            String[] lines = itemsArea.getText().split("\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                String[] parts = trimmed.split(",");
                if (parts.length != 2) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid line: \"" + line + "\"\nUse format: EquipmentID,Quantity");
                    return;
                }

                String eqId = parts[0].trim();
                String qtyStr = parts[1].trim();

                int qty;
                try {
                    qty = Integer.parseInt(qtyStr);
                    if (qty <= 0) {
                        JOptionPane.showMessageDialog(this,
                                "Quantity must be > 0 in line: \"" + line + "\"");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Quantity must be a number in line: \"" + line + "\"");
                    return;
                }

                equipmentIds.add(eqId);
                quantities.add(qty);
            }

       try (Connection conn = DBConnection.getConnection()) {

              conn.setAutoCommit(false);

                try { String rentalNum = "R" + (System.currentTimeMillis() % 10000);

                    String receiveDateTime = receiveInput + ":00";
                    String returnDateTime = returnInput + ":00";

                   String rentalSql = "INSERT INTO RENTAL (Rental_num, Receive_datetime, Return_datetime, Rental_customer_id) " +
                                       "VALUES (?, ?, ?, ?)";
                    PreparedStatement rentalStmt = conn.prepareStatement(rentalSql);
                    rentalStmt.setString(1, rentalNum);
                    rentalStmt.setString(2, receiveDateTime);
                    rentalStmt.setString(3, returnDateTime);
                    rentalStmt.setString(4, loggedInCustomerId);
                    rentalStmt.executeUpdate();

                    long days = calculateRentalDays(receiveInput.substring(0, 10),
                            returnInput.substring(0, 10));

                    double totalAmount = 0.0;

                     for (int i = 0; i < equipmentIds.size(); i++) {
                        String eqId = equipmentIds.get(i);
                        int requestedQty = quantities.get(i);

                        String checkEquipmentSql =
                                "SELECT Available_quantity, Rental_fee FROM EQUIPMENT WHERE Equipment_id = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkEquipmentSql);
                        checkStmt.setString(1, eqId);
                        ResultSet rs = checkStmt.executeQuery();

                        if (!rs.next()) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "Equipment ID not found: " + eqId, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        int availableQty = rs.getInt("Available_quantity");
                        double rentalFee = rs.getDouble("Rental_fee");

                        if (availableQty < requestedQty) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "Not enough equipment available for " + eqId + ". Available: " + availableQty + ", Requested: " + requestedQty, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String makesSql = "INSERT INTO MAKES (Equipment_quantity, M_rental_number, M_equipment_id, M_customer_id) " +
                                          "VALUES (?, ?, ?, ?)";
                        PreparedStatement makesStmt = conn.prepareStatement(makesSql);
                        makesStmt.setInt(1, requestedQty);
                        makesStmt.setString(2, rentalNum);
                        makesStmt.setString(3, eqId);
                        makesStmt.setString(4, loggedInCustomerId);
                        makesStmt.executeUpdate();

                       String updateEquipmentSql =
                                "UPDATE EQUIPMENT SET Available_quantity = Available_quantity - ? WHERE Equipment_id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateEquipmentSql);
                        updateStmt.setInt(1, requestedQty);
                        updateStmt.setString(2, eqId);
                        updateStmt.executeUpdate();

                         totalAmount += rentalFee * requestedQty * days;
                    }

                     String billId = "B" + (System.currentTimeMillis() % 10000);
                    String billSql = "INSERT INTO BILL (Bill_id, Bill_date, Payment_method, Total_amount, Bill_rental_num) " +
                                     "VALUES (?, CURDATE(), 'Credit Card', ?, ?)";
                    PreparedStatement billStmt = conn.prepareStatement(billSql);
                    billStmt.setString(1, billId);
                    billStmt.setDouble(2, totalAmount);
                    billStmt.setString(3, rentalNum);
                    billStmt.executeUpdate();

                    conn.commit();

                    StringBuilder summary = new StringBuilder();
                    summary.append("Rental created successfully!\n")
                           .append("Rental Number: ").append(rentalNum).append("\n")
                           .append("Bill ID: ").append(billId).append("\n")
                           .append("Total Amount: $").append(totalAmount).append("\n")
                           .append("Rental Period: ").append(days).append(" days\n")
                           .append("Receive: ").append(receiveDateTime).append("\n")
                           .append("Return: ").append(returnDateTime).append("\n")
                           .append("\nEquipment Details:\n");

                    for (int i = 0; i < equipmentIds.size(); i++) {
                        summary.append("- ").append(equipmentIds.get(i))
                               .append(" | Qty: ").append(quantities.get(i))
                               .append("\n");
                    }

                    ModernUI.populateSingleColumn(resultsTable, "Rental", summary.toString());

                } catch (SQLException ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error creating rental: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating rental: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isValidTimeFormat(String datetime) {
        try {
             String timePart = datetime.substring(11);
            String[] timeComponents = timePart.split(":");

            if (timeComponents.length != 2) return false;

            int hours = Integer.parseInt(timeComponents[0]);
            int minutes = Integer.parseInt(timeComponents[1]);

            return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
        } catch (Exception e) {
            return false;
        }
    }


    private long calculateRentalDays(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            long diff = end.getTime() - start.getTime();
            return diff / (24 * 60 * 60 * 1000) + 1;
        } catch (Exception e) {
            return 1;
        }
    }


    private void viewRentalHistory() {
        try (Connection conn = DBConnection.getConnection()) {
        	String sql = "SELECT r.Rental_num, r.Receive_datetime, r.Return_datetime, " +
                    "e.Qname, e.Rental_fee, m.Equipment_quantity, " +
                    "b.Bill_id, b.Total_amount " +
                    "FROM RENTAL r " +
                    "JOIN MAKES m ON r.Rental_num = m.M_rental_number " +
                    "JOIN EQUIPMENT e ON m.M_equipment_id = e.Equipment_id " +
                    "LEFT JOIN BILL b ON r.Rental_num = b.Bill_rental_num " +
                    "WHERE r.Rental_customer_id = ? " +
                    "ORDER BY r.Receive_datetime DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);

            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error viewing rentals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void cancelMyRental() {
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT r.Rental_num, r.Receive_datetime, e.Qname " +
                             "FROM RENTAL r " +
                             "JOIN MAKES m ON r.Rental_num = m.M_rental_number " +
                             "JOIN EQUIPMENT e ON m.M_equipment_id = e.Equipment_id " +
                             "WHERE r.Rental_customer_id = ? AND (r.Return_datetime IS NULL OR r.Return_datetime > NOW())";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, loggedInCustomerId);
            ResultSet rs = checkStmt.executeQuery();

            StringBuilder rentalOptions = new StringBuilder("Your Current Rentals:\n");
            boolean hasRentals = false;
            while (rs.next()) {
                hasRentals = true;
                rentalOptions.append("- ").append(rs.getString("Rental_num"))
                            .append(" (")
                            .append(rs.getString("Qname"))
                            .append(", Receive: ")
                            .append(rs.getString("Receive_datetime"))
                            .append(")\n");
            }

            if (!hasRentals) {
                JOptionPane.showMessageDialog(this, "You don't have any current rentals to cancel.");
                return;
            }

            String rentalNum = JOptionPane.showInputDialog(this,
                rentalOptions.toString() + "\nEnter Rental Number to cancel:");

            if (rentalNum == null || rentalNum.trim().isEmpty()) {
                return;
            }

            rentalNum = rentalNum.trim();

            String verifySql = "SELECT m.M_equipment_id, m.Equipment_quantity " +
                              "FROM RENTAL r " +
                              "JOIN MAKES m ON r.Rental_num = m.M_rental_number " +
                              "WHERE r.Rental_num = ? AND r.Rental_customer_id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
            verifyStmt.setString(1, rentalNum);
            verifyStmt.setString(2, loggedInCustomerId);
            ResultSet verifyRs = verifyStmt.executeQuery();

            if (!verifyRs.next()) {
                JOptionPane.showMessageDialog(this, "Rental not found or you don't have permission to cancel it.");
                return;
            }

            String equipmentId = verifyRs.getString("M_equipment_id");
            int quantity = verifyRs.getInt("Equipment_quantity");

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel rental " + rentalNum + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                 conn.setAutoCommit(false);

                try {
                    String makesSql = "DELETE FROM MAKES WHERE M_rental_number = ? AND M_equipment_id = ? AND M_customer_id = ?";
                    PreparedStatement makesStmt = conn.prepareStatement(makesSql);
                    makesStmt.setString(1, rentalNum);
                    makesStmt.setString(2, equipmentId);
                    makesStmt.setString(3, loggedInCustomerId);
                    makesStmt.executeUpdate();

                    String deleteBillSql = "DELETE FROM BILL WHERE Bill_rental_num = ?";
                    PreparedStatement deleteBillStmt = conn.prepareStatement(deleteBillSql);
                    deleteBillStmt.setString(1, rentalNum);
                    deleteBillStmt.executeUpdate();

                     String rentalSql = "DELETE FROM RENTAL WHERE Rental_num = ?";
                    PreparedStatement rentalStmt = conn.prepareStatement(rentalSql);
                    rentalStmt.setString(1, rentalNum);
                    int rows = rentalStmt.executeUpdate();

                    if (rows > 0) {
                       String restoreSql = "UPDATE EQUIPMENT SET Available_quantity = Available_quantity + ? WHERE Equipment_id = ?";
                        PreparedStatement restoreStmt = conn.prepareStatement(restoreSql);
                        restoreStmt.setInt(1, quantity);
                        restoreStmt.setString(2, equipmentId);
                        restoreStmt.executeUpdate();

                        conn.commit();
                        ModernUI.populateSingleColumn(resultsTable, "Message", "Rental " + rentalNum + " cancelled successfully.");} else {
                        conn.rollback();
                        ModernUI.populateSingleColumn(resultsTable, "Message", "Failed to cancel rental.");
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cancelling rental: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton viewMyBillsBtn = new JButton("View My Bills");
        panel.add(viewMyBillsBtn, BorderLayout.NORTH);

        viewMyBillsBtn.addActionListener(e -> viewMyBills());

        return panel;
    }

    private void viewMyBills() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.Bill_id, b.Bill_date, b.Payment_method, b.Total_amount, " +
                        "r.Rental_num " +
                        "FROM BILL b " +
                        "JOIN RENTAL r ON b.Bill_rental_num = r.Rental_num " +
                        "WHERE r.Rental_customer_id = ? " +
                        "ORDER BY b.Bill_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);

            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error viewing bills: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
 }
     private JPanel createQueriesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton query3Btn = new JButton("View My Rental Summary");
        JButton query4Btn = new JButton("View Detailed Rental Report");

        panel.add(query3Btn);
        panel.add(query4Btn);

        query3Btn.addActionListener(e -> runQuery3());
        query4Btn.addActionListener(e -> runQuery4());

        return panel;
    }
   private void runQuery3() {
        if (loggedInCustomerId == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in to run this query.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT CUSTOMER.Fname, CUSTOMER.Mname, CUSTOMER.Lname, " +
                         "       CUSTOMER.Customer_id, MAKES.M_rental_number " +
                         "FROM CUSTOMER " +
                         "JOIN MAKES ON CUSTOMER.Customer_id = MAKES.M_customer_id " +
                         "WHERE CUSTOMER.Customer_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);
            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error running Query 3: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
   private void runQuery4() {
        if (loggedInCustomerId == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in to run this query.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT CUSTOMER.Fname, CUSTOMER.Mname, CUSTOMER.Lname, " +
                         "       CUSTOMER.Customer_id, " +
                         "       RENTAL.Rental_num, RENTAL.Receive_datetime, RENTAL.Return_datetime, " +
                         "       EQUIPMENT.Qname, EQUIPMENT.Type, EQUIPMENT.Rental_fee, " +
                         "       MAKES.Equipment_quantity, " +
                         "       BILL.Bill_id, BILL.Total_amount " +
                         "FROM CUSTOMER " +
                         "JOIN MAKES ON CUSTOMER.Customer_id = MAKES.M_customer_id " +
                         "JOIN RENTAL ON MAKES.M_rental_number = RENTAL.Rental_num " +
                         "JOIN EQUIPMENT ON MAKES.M_equipment_id = EQUIPMENT.Equipment_id " +
                         "LEFT JOIN BILL ON BILL.Bill_rental_num = RENTAL.Rental_num " +
                         "WHERE CUSTOMER.Customer_id = ? " +
                         "ORDER BY RENTAL.Receive_datetime DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInCustomerId);
            ResultSet rs = stmt.executeQuery();

            
            ModernUI.populateTable(resultsTable, rs);} catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error running Query 4: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
