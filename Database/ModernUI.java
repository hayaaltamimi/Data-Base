package Database;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ModernUI {

    private ModernUI(){}

    public static void setupLaf() {
        try { UIManager.setLookAndFeel(new FlatIntelliJLaf()); }
        catch (Throwable t) {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
            catch (Exception ignored) {}
        }
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
    }

    public static JPanel gradientBackground() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color c1 = new Color(18, 28, 48);
                Color c2 = new Color(40, 16, 64);
                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
    }

    public static JComponent header(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 22f));
        JLabel s = new JLabel(subtitle == null ? "" : subtitle);
        s.setForeground(new Color(255, 255, 255, 200));
        s.setFont(s.getFont().deriveFont(Font.PLAIN, 12f));
        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(s);
        return p;
    }

    public static JPanel smallCard(String title, String desc, String buttonText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(255,255,255,80), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(title);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 14f));
        JLabel d = new JLabel("<html><body style='width:260px;'>" + escape(desc) + "</body></html>");
        d.setForeground(new Color(70,70,70));
        d.setFont(d.getFont().deriveFont(Font.PLAIN, 12f));
        text.add(h);
        text.add(Box.createVerticalStrut(6));
        text.add(d);

        JButton b = primaryButton(buttonText);
        b.setName("action");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(b);

        card.add(text, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    public static JPanel sidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 14, 16, 14));
        p.setPreferredSize(new Dimension(240, 10));
        return p;
    }

    public static JComponent sidebarHeader(String title) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        t.setBorder(new EmptyBorder(0, 2, 8, 2));
        wrap.add(t, BorderLayout.CENTER);
        JSeparator sep = new JSeparator();
        sep.setBorder(new EmptyBorder(6, 0, 10, 0));
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    public static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        return b;
    }

    public static JPanel card(String title, JPanel content) {
        return cardInternal(title, content);
    }

    public static JPanel card(String title, JScrollPane content) {
        return cardInternal(title, content);
    }

    private static JPanel cardInternal(String title, JComponent content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));
        outer.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,30), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        if (title != null && !title.isBlank()) {
            JLabel h = new JLabel(title);
            h.setFont(h.getFont().deriveFont(Font.BOLD, 13f));
            h.setBorder(new EmptyBorder(0, 2, 10, 2));
            card.add(h, BorderLayout.NORTH);
        }

        content.setBorder(new EmptyBorder(0,0,0,0));
        card.add(content, BorderLayout.CENTER);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    public static void styleField(JTextField f) {
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,35),1,true), new EmptyBorder(10,12,10,12)));
    }

    public static JPanel formRow(String label, JComponent field) {
        JPanel r = new JPanel(new BorderLayout(10,0));
        r.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(new Color(255,255,255,220));
        r.add(l, BorderLayout.WEST);
        r.add(field, BorderLayout.CENTER);
        return r;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    public static void showTableDialog(Component parent, String title, java.sql.ResultSet rs) throws java.sql.SQLException {
        javax.swing.table.DefaultTableModel model = toTableModel(rs);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        JDialog d = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel bg = gradientBackground();
        bg.setLayout(new BorderLayout());
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(16,16,16,16));
        card.setOpaque(false);
        card.add(ModernUI.card(title, sp), BorderLayout.CENTER);
        bg.add(card, BorderLayout.CENTER);
        d.setContentPane(bg);
        d.setSize(920, 520);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    public static javax.swing.table.DefaultTableModel toTableModel(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        String[] colNames = new String[cols];
        for (int i = 1; i <= cols; i++) colNames[i-1] = md.getColumnLabel(i);
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) row[i-1] = rs.getObject(i);
            rows.add(row);
        }
        Object[][] data = rows.toArray(new Object[0][]);
        return new javax.swing.table.DefaultTableModel(data, colNames) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }


    public static JTable makeResultsTable() {
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{}){
            public boolean isCellEditable(int r, int c){ return false; }
        });
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        return table;
    }

    public static void populateTable(JTable table, java.sql.ResultSet rs) throws java.sql.SQLException {
        table.setModel(toTableModel(rs));
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
    }

    public static void populateSingleColumn(JTable table, String title, String text) {
        String[] cols = new String[]{title == null ? "Result" : title};
        String[] lines = (text == null ? "" : text).split("\\R");
        Object[][] data = new Object[lines.length][1];
        for (int i = 0; i < lines.length; i++) data[i][0] = lines[i];
        table.setModel(new javax.swing.table.DefaultTableModel(data, cols){
            public boolean isCellEditable(int r, int c){ return false; }
        });
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
    }

}
