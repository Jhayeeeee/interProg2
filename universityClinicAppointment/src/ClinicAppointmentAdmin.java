import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class ClinicAppointmentAdmin extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DatabaseManager.User adminUser;
    
    private DefaultTableModel appointmentTableModel;
    private LocalDate viewDate = LocalDate.now();
    private LocalDate scheduleDate = LocalDate.now();
    
    private JLabel currentHoursLabel;
    private JTextField startInput, endInput, doctorInput, slotLimitInput;

    private DefaultTableModel studentTableModel;
    private DefaultTableModel historyTableModel;
    private JTextField studentSearchField;

    public ClinicAppointmentAdmin(DatabaseManager.User user) {
        this.adminUser = user;
        setTitle("University Clinic - Admin Control Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_LIGHT);
        setContentPane(root);

        // --- ENHANCED SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(AppColors.NAV_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JPanel navMenu = new JPanel(new MigLayout("insets 25 15 20 15, wrap 1", "[fill]", "[]30[]10[]10[]"));
        navMenu.setOpaque(false);
        JLabel logoLabel = new JLabel("Clinic Admin");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(Color.WHITE);
        navMenu.add(logoLabel, "wrap 40");

        JButton btnAppt = createSideButton("\uD83D\uDCC5  Daily Appointments", true);
        JButton btnHistory = createSideButton("\uD83D\uDCDC  History Activity", false);
        JButton btnStud = createSideButton("\uD83D\uDC64  Manage Accounts", false);
        navMenu.add(btnAppt); navMenu.add(btnHistory); navMenu.add(btnStud);
        sidebar.add(navMenu, BorderLayout.NORTH);

        JPanel navFooter = new JPanel(new MigLayout("insets 20, wrap 1", "[fill]", "push[]"));
        navFooter.setOpaque(false);
        JSeparator sep = new JSeparator(); sep.setForeground(new Color(255, 255, 255, 40));
        navFooter.add(sep, "growx, wrap 20");
        JButton btnLogout = new JButton("\uD83D\uAAAA  Logout System");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnLogout.setForeground(Color.WHITE); btnLogout.setBackground(AppColors.DANGER);
        btnLogout.setFocusPainted(false); btnLogout.setBorderPainted(false); btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(0, 50)); btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        btnLogout.addActionListener(e -> { new ClinicAppointmentLogin().setVisible(true); dispose(); });
        navFooter.add(btnLogout, "h 50!");
        sidebar.add(navFooter, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.add(createAppointmentsView(), "APPT");
        contentPanel.add(createHistoryView(), "HISTORY");
        contentPanel.add(createStudentManagementView(), "STUDENT");
        root.add(contentPanel, BorderLayout.CENTER);

        btnAppt.addActionListener(e -> { cardLayout.show(contentPanel, "APPT"); resetSideButtons(btnAppt, btnHistory, btnStud); refreshAppointmentTable(); });
        btnHistory.addActionListener(e -> { cardLayout.show(contentPanel, "HISTORY"); resetSideButtons(btnHistory, btnAppt, btnStud); refreshHistoryTable(); });
        btnStud.addActionListener(e -> { cardLayout.show(contentPanel, "STUDENT"); resetSideButtons(btnStud, btnAppt, btnHistory); refreshStudentTable(""); });
    }

    private JPanel createAppointmentsView() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20"));
        panel.setOpaque(false);
        JLabel title = new JLabel("Appointments & Clinic Scheduling");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, "wrap 20");

        JPanel viewBar = new JPanel(new MigLayout("insets 10", "[]10[]20[grow]"));
        viewBar.setBackground(new Color(240, 244, 250)); viewBar.setBorder(new LineBorder(AppColors.BORDER, 1));
        viewBar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        JButton viewCalBtn = new JButton("\uD83D\uDCC5  View Date: " + viewDate.format(dtf));
        viewCalBtn.setFont(new Font("Segoe UI", Font.BOLD, 14)); viewCalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewCalBtn.setBackground(Color.WHITE);
        viewCalBtn.addActionListener(e -> {
            LocalDate picked = CalendarPicker.showPopup(viewCalBtn, viewDate, null);
            if (picked != null) { viewDate = picked; viewCalBtn.setText("\uD83D\uDCC5  View Date: " + picked.format(dtf)); refreshAppointmentTable(); }
        });
        currentHoursLabel = new JLabel("Status: Fetching...");
        currentHoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        viewBar.add(new JLabel("Quick View:")); viewBar.add(viewCalBtn, "w 220!, h 40!"); viewBar.add(currentHoursLabel, "right");

        JPanel schedBar = new JPanel(new MigLayout("insets 12", "[]10[]15[]5[]2[]5[]15[]5[]15[]5[]5[grow]"));
        schedBar.setBackground(Color.WHITE); schedBar.setBorder(new LineBorder(AppColors.BORDER, 1));
        schedBar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        JButton schedCalBtn = new JButton("\uD83D\uDCC5  Setup Date: " + scheduleDate.format(dtf));
        schedCalBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14)); schedCalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startInput = new JTextField("08:00 AM"); endInput = new JTextField("05:00 PM");
        doctorInput = new JTextField(); doctorInput.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Doctor...");
        slotLimitInput = new JTextField("10"); slotLimitInput.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Limit");
        JButton setBtn = new JButton("Save Schedule"); setBtn.setBackground(AppColors.SUCCESS); setBtn.setForeground(Color.WHITE);
        JButton closeBtn = new JButton("Close Day"); closeBtn.setBackground(AppColors.DANGER); closeBtn.setForeground(Color.WHITE);
        schedCalBtn.addActionListener(e -> {
            LocalDate picked = CalendarPicker.showPopup(schedCalBtn, scheduleDate, null);
            if (picked != null) { scheduleDate = picked; schedCalBtn.setText("\uD83D\uDCC5  Setup Date: " + picked.format(dtf)); }
        });
        setBtn.addActionListener(e -> {
            if (doctorInput.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Doctor's name."); return; }
            int limit = 10; try { limit = Integer.parseInt(slotLimitInput.getText().trim()); } catch (Exception ex) {}
            DatabaseManager.setClinicAvailability(scheduleDate.toString(), startInput.getText(), endInput.getText(), doctorInput.getText().trim(), limit);
            refreshAppointmentTable(); JOptionPane.showMessageDialog(this, "Schedule saved for " + scheduleDate);
        });
        closeBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Mark " + scheduleDate + " CLOSED?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DatabaseManager.setClinicClosed(scheduleDate.toString()); refreshAppointmentTable();
            }
        });
        schedBar.add(new JLabel("Set Hours:")); schedBar.add(schedCalBtn, "w 180!, h 38!");
        schedBar.add(new JLabel("From:")); schedBar.add(startInput, "w 85!, h 38!"); schedBar.add(new JLabel("-")); schedBar.add(endInput, "w 85!, h 38!");
        schedBar.add(new JLabel("Doctor:")); schedBar.add(doctorInput, "w 130!, h 38!");
        schedBar.add(new JLabel("Slots:")); schedBar.add(slotLimitInput, "w 50!, h 38!");
        schedBar.add(setBtn, "h 38!"); schedBar.add(closeBtn, "h 38!");

        panel.add(schedBar, "growx, wrap 20");
        panel.add(viewBar, "growx, wrap 20");

        String[] columns = {"ID", "Student", "Concern", "Pre-Check Assessment", "Status"};
        appointmentTableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int col) { return col == 4; } };
        JTable table = new JTable(appointmentTableModel); styleTable(table);
        table.getTableHeader().setReorderingAllowed(false);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Done"});
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusCombo));
        appointmentTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 4) {
                int id = (int) appointmentTableModel.getValueAt(e.getFirstRow(), 0);
                String st = (String) appointmentTableModel.getValueAt(e.getFirstRow(), 4);
                if ("Done".equals(st)) {
                    String d = JOptionPane.showInputDialog(this, "Enter Dentist in charge:");
                    if (d != null && !d.trim().isEmpty()) DatabaseManager.updateAppointmentStatusWithDentist(id, st, d);
                } else DatabaseManager.updateAppointmentStatus(id, st);
                SwingUtilities.invokeLater(() -> refreshAppointmentTable());
            }
        });
        panel.add(new JScrollPane(table), "grow");
        refreshAppointmentTable();
        return panel;
    }

    private JPanel createHistoryView() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20"));
        panel.setOpaque(false);
        JLabel title = new JLabel("History Activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, "wrap 15");
        String[] columns = {"ID", "Student", "Date", "Concern", "Dentist", "Pre-Check"};
        historyTableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(historyTableModel); styleTable(table);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), "grow");
        return panel;
    }

    private JPanel createStudentManagementView() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20"));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("Manage Student Accounts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, "wrap 15");

        // --- ADD ACCOUNT PANEL ---
        JPanel addPanel = new JPanel(new MigLayout("insets 20", "[]10[grow]20[]10[grow]20[]10[grow]20[]"));
        addPanel.setBackground(Color.WHITE); addPanel.setBorder(new LineBorder(AppColors.BORDER, 1));
        addPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        JTextField sidIn = new JTextField(); sidIn.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "2024-12446");
        JTextField nameIn = new JTextField(); JTextField passIn = new JTextField();
        JButton addBtn = new JButton("Create Account"); addBtn.setBackground(AppColors.ACCENT); addBtn.setForeground(Color.WHITE);
        addPanel.add(new JLabel("Student ID:")); addPanel.add(sidIn, "w 150!"); 
        addPanel.add(new JLabel("Full Name:")); addPanel.add(nameIn, "w 220!"); 
        addPanel.add(new JLabel("Password:")); addPanel.add(passIn, "w 150!"); addPanel.add(addBtn, "h 40!");
        panel.add(addPanel, "growx, wrap 10");

        // --- SEARCH PANEL ---
        JPanel searchBar = new JPanel(new MigLayout("insets 10 0 10 0", "[grow][]"));
        searchBar.setOpaque(false);
        studentSearchField = new JTextField();
        studentSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "\uD83D\uDD0D Search by Student ID or Name...");
        studentSearchField.setPreferredSize(new Dimension(0, 45));
        studentSearchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshStudentTable(studentSearchField.getText().trim()); }
        });
        searchBar.add(studentSearchField, "growx");
        panel.add(searchBar, "growx, wrap 5");

        String[] columns = {"Student ID", "Full Name", "Password", "Action"};
        studentTableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(studentTableModel); styleTable(table);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), "grow");
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    String sid = (String) studentTableModel.getValueAt(row, 0);
                    String name = (String) studentTableModel.getValueAt(row, 1);
                    if (JOptionPane.showConfirmDialog(null, "DELETE student " + name + " (" + sid + ")?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        DatabaseManager.deleteUser(sid);
                        refreshStudentTable(studentSearchField.getText().trim());
                    }
                }
            }
        });

        addBtn.addActionListener(e -> {
            String sid = sidIn.getText().trim();
            String name = nameIn.getText().trim();
            String pass = passIn.getText().trim();
            if (!sid.matches("\\d{4}-\\d{5}")) { JOptionPane.showMessageDialog(this, "Invalid ID Format!\nPlease use: YYYY-NNNNN (e.g., 2024-12446)", "Format Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!sid.isEmpty() && !name.isEmpty() && !pass.isEmpty()) {
                if (DatabaseManager.registerUser(sid, name, pass)) { sidIn.setText(""); nameIn.setText(""); passIn.setText(""); refreshStudentTable(""); JOptionPane.showMessageDialog(this, "Account Created Successfully!"); }
                else JOptionPane.showMessageDialog(this, "Student ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } else JOptionPane.showMessageDialog(this, "Please fill all fields.");
        });
        refreshStudentTable("");
        return panel;
    }

    private void refreshAppointmentTable() {
        if (appointmentTableModel == null) return;
        appointmentTableModel.setRowCount(0);
        String[] sched = DatabaseManager.getClinicSchedule(viewDate.toString());
        if (sched != null) {
            if ("CLOSED".equals(sched[1])) { currentHoursLabel.setText("Clinic Status: CLOSED"); currentHoursLabel.setForeground(AppColors.DANGER); }
            else { 
                int count = DatabaseManager.getAppointmentCountForDate(viewDate.toString());
                int limit = sched.length >= 5 ? Integer.parseInt(sched[4]) : 0;
                currentHoursLabel.setText("Dr. " + (sched.length >= 4 ? sched[3] : "TBD") + " (" + count + "/" + limit + " Slots filled)");
                currentHoursLabel.setForeground(count >= limit ? AppColors.DANGER : AppColors.SUCCESS);
            }
        } else { currentHoursLabel.setText("Status: No Hours Set for " + viewDate); currentHoursLabel.setForeground(AppColors.TEXT_MUTED); }
        List<DatabaseManager.Appointment> list = DatabaseManager.getAppointmentsByDate(viewDate.toString());
        for (DatabaseManager.Appointment a : list) appointmentTableModel.addRow(new Object[]{a.id, a.studentName, a.concern, a.preCheckData, a.status});
    }

    private void refreshHistoryTable() {
        if (historyTableModel == null) return;
        historyTableModel.setRowCount(0);
        List<DatabaseManager.Appointment> list = DatabaseManager.getAllCompletedAppointments();
        for (DatabaseManager.Appointment a : list) historyTableModel.addRow(new Object[]{a.id, a.studentName, a.date, a.concern, a.dentistName, a.preCheckData});
    }

    private void refreshStudentTable(String query) {
        if (studentTableModel == null) return;
        studentTableModel.setRowCount(0);
        String lowerQuery = query.toLowerCase();
        try (BufferedReader br = new BufferedReader(new FileReader("data/users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4 && "STUDENT".equals(parts[3])) {
                    String sid = parts[0]; String name = parts[1];
                    if (query.isEmpty() || sid.toLowerCase().contains(lowerQuery) || name.toLowerCase().contains(lowerQuery)) {
                        studentTableModel.addRow(new Object[]{sid, name, parts[2], "\uD83D\uDDD1\uFE0F Delete"});
                    }
                }
            }
        } catch (IOException e) {}
    }

    private JButton createSideButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 15));
        btn.setForeground(active ? Color.WHITE : new Color(180, 200, 230));
        btn.setBackground(active ? new Color(255, 255, 255, 30) : AppColors.NAV_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return btn;
    }

    private void resetSideButtons(JButton active, JButton... others) {
        active.setFont(new Font("Segoe UI", Font.BOLD, 15)); active.setForeground(Color.WHITE); active.setBackground(new Color(255, 255, 255, 30));
        for (JButton b : others) { b.setFont(new Font("Segoe UI", Font.PLAIN, 15)); b.setForeground(new Color(180, 200, 230)); b.setBackground(AppColors.NAV_BG); }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 15, 0, 15)); setHorizontalAlignment(JLabel.CENTER);
                if (table.getColumnName(col).equals("Action") && value != null && value.toString().contains("Delete")) {
                    c.setForeground(AppColors.DANGER); setFont(getFont().deriveFont(Font.BOLD));
                } else if (col == table.getColumnCount()-1 && value != null) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("Done")) { c.setForeground(new Color(40, 167, 69)); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (status.equalsIgnoreCase("Pending")) { c.setForeground(new Color(255, 165, 0)); setFont(getFont().deriveFont(Font.BOLD)); }
                } else { c.setForeground(AppColors.TEXT_MAIN); setFont(getFont().deriveFont(Font.PLAIN)); }
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }
}
