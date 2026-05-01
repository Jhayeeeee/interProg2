import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClinicAppointmentDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DatabaseManager.User currentUser;

    private DefaultTableModel tableModel;
    private JTable appointmentTable;
    private LocalDate selectedDate = LocalDate.now();
    
    private JTextArea concernArea;
    private JButton bookBtn;
    private JLabel timeLabel, doctorLabel, slotsLabel;
    private JCheckBox feverCb, coughCb, medsCb;
    private JComboBox<String> durationCombo;
    private JLabel notificationLabel;

    public ClinicAppointmentDashboard(DatabaseManager.User user) {
        this.currentUser = user;
        setTitle("University Clinic - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_LIGHT);
        setContentPane(root);

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(AppColors.NAV_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JPanel navMenu = new JPanel(new MigLayout("insets 25 15 20 15, wrap 1", "[fill]", "[]10[]25[]10[]push"));
        navMenu.setOpaque(false);
        JLabel logoLabel = new JLabel("Clinic Student");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(Color.WHITE);
        navMenu.add(logoLabel, "wrap 10");
        JLabel nameLabel = new JLabel("<html>Logged in as:<br><b>" + currentUser.name + "</b></html>");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(180, 200, 230));
        navMenu.add(nameLabel, "wrap 30");

        JButton btnHome = createSideButton("\uD83D\uDCCB  Appointment Log", true);
        JButton btnSettings = createSideButton("\u2699\uFE0F  Account Settings", false);
        navMenu.add(btnHome); navMenu.add(btnSettings);
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
        contentPanel.add(createDashboardHome(), "HOME");
        contentPanel.add(createSettingsView(), "SETTINGS");
        root.add(contentPanel, BorderLayout.CENTER);

        btnHome.addActionListener(e -> { cardLayout.show(contentPanel, "HOME"); resetSideButtons(btnHome, btnSettings); });
        btnSettings.addActionListener(e -> { cardLayout.show(contentPanel, "SETTINGS"); resetSideButtons(btnSettings, btnHome); });
    }

    private JPanel createDashboardHome() {
        JPanel mainContent = new JPanel(new MigLayout("fill, insets 30", "[500!]30[grow]", "[grow]"));
        mainContent.setOpaque(false);
        
        JPanel bookingPanel = createCardPanel("Schedule Appointment");
        JLabel dateLabel = new JLabel("1. Choose Available Date");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bookingPanel.add(dateLabel, "wrap 5");
        timeLabel = new JLabel("Operating Hours: Fetching...");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); timeLabel.setForeground(AppColors.SUCCESS);
        bookingPanel.add(timeLabel, "wrap 2");
        doctorLabel = new JLabel("Doctor in Charge: TBD");
        doctorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15)); doctorLabel.setForeground(AppColors.ACCENT);
        bookingPanel.add(doctorLabel, "wrap 5");
        slotsLabel = new JLabel("Slots Remaining: --");
        slotsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); slotsLabel.setForeground(AppColors.TEXT_MUTED);
        bookingPanel.add(slotsLabel, "wrap 10");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        JButton calBtn = new JButton("\uD83D\uDCC5  " + selectedDate.format(dtf));
        calBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        calBtn.setBackground(Color.WHITE); calBtn.setBorder(new LineBorder(AppColors.BORDER, 2));
        calBtn.setCursor(new Cursor(Cursor.HAND_CURSOR)); calBtn.setHorizontalAlignment(SwingConstants.LEFT);
        calBtn.addActionListener(e -> {
            List<String> available = DatabaseManager.getAvailableDates();
            LocalDate picked = CalendarPicker.showPopup(calBtn, selectedDate, available);
            if (picked != null) { selectedDate = picked; calBtn.setText("\uD83D\uDCC5  " + picked.format(dtf)); updateOperatingHours(); }
        });
        bookingPanel.add(calBtn, "growx, h 55!, wrap 20");

        JLabel preCheckTitle = new JLabel("2. Symptom Pre-Check");
        preCheckTitle.setFont(new Font("Segoe UI", Font.BOLD, 18)); bookingPanel.add(preCheckTitle, "wrap 10");
        feverCb = new JCheckBox("Do you have a fever?"); feverCb.setFont(new Font("Segoe UI", Font.PLAIN, 16)); feverCb.setOpaque(false);
        bookingPanel.add(feverCb, "wrap 5");
        coughCb = new JCheckBox("Do you have a cough or colds?"); coughCb.setFont(new Font("Segoe UI", Font.PLAIN, 16)); coughCb.setOpaque(false);
        bookingPanel.add(coughCb, "wrap 5");
        medsCb = new JCheckBox("Have you taken any medication?"); medsCb.setFont(new Font("Segoe UI", Font.PLAIN, 16)); medsCb.setOpaque(false);
        bookingPanel.add(medsCb, "wrap 10");
        JLabel durLabel = new JLabel("How long have you felt this way?");
        durLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); bookingPanel.add(durLabel, "wrap 5");
        String[] durOptions = {"Less than 24 hours", "1-3 days", "4-7 days", "More than a week"};
        durationCombo = new JComboBox<>(durOptions); durationCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bookingPanel.add(durationCombo, "growx, h 40!, wrap 20");

        JLabel concernLabel = new JLabel("3. Your Medical Concern");
        concernLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); bookingPanel.add(concernLabel, "wrap 8");
        concernArea = new JTextArea(); concernArea.setLineWrap(true); concernArea.setWrapStyleWord(true); concernArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        concernArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Type here clearly..."); concernArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane concernScroll = new JScrollPane(concernArea); concernScroll.setBorder(new LineBorder(AppColors.BORDER, 2));
        bookingPanel.add(concernScroll, "growx, h 180!, wrap 25");

        bookBtn = createFlatButton("Schedule Appointment Now", AppColors.ACCENT);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        bookBtn.addActionListener(e -> {
            bookBtn.setEnabled(false);
            if (DatabaseManager.hasPendingAppointment(currentUser.studentID)) { JOptionPane.showMessageDialog(this, "You have an existing PENDING appointment.", "Active Appointment Found", JOptionPane.WARNING_MESSAGE); bookBtn.setEnabled(true); return; }
            String concern = concernArea.getText().trim(); if (concern.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter your concern."); bookBtn.setEnabled(true); return; }
            String res = "Fever: " + (feverCb.isSelected()?"Yes":"No") + ", Cough: " + (coughCb.isSelected()?"Yes":"No") + ", Meds: " + (medsCb.isSelected()?"Yes":"No") + ", Dur: " + durationCombo.getSelectedItem();
            String dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (DatabaseManager.addAppointment(currentUser.studentID, currentUser.name, dateStr, concern, res)) {
                concernArea.setText(""); feverCb.setSelected(false); coughCb.setSelected(false); medsCb.setSelected(false); refreshTable(); updateOperatingHours(); JOptionPane.showMessageDialog(this, "Appointment Successful!"); bookBtn.setEnabled(true);
            } else { JOptionPane.showMessageDialog(this, "Sorry, all slots for this day are FULL.", "Booking Limit Reached", JOptionPane.ERROR_MESSAGE); bookBtn.setEnabled(true); }
        });
        bookingPanel.add(bookBtn, "growx, h 65!");
        mainContent.add(bookingPanel, "growy");

        JPanel historyPanel = createCardPanel("My Appointments History");
        
        // Notification Area
        notificationLabel = new JLabel("");
        notificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        notificationLabel.setOpaque(true);
        notificationLabel.setBackground(new Color(230, 242, 255));
        notificationLabel.setForeground(AppColors.APPROVED);
        notificationLabel.setBorder(new EmptyBorder(10, 15, 10, 15));
        notificationLabel.setVisible(false);
        historyPanel.add(notificationLabel, "growx, h 40!, wrap 10");

        String[] cols = {"Date", "Concern", "Status", "Action", "ID"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        appointmentTable = new JTable(tableModel); styleTable(); appointmentTable.getTableHeader().setReorderingAllowed(false);
        appointmentTable.getColumnModel().getColumn(4).setMinWidth(0); appointmentTable.getColumnModel().getColumn(4).setMaxWidth(0);
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = appointmentTable.rowAtPoint(e.getPoint()); int col = appointmentTable.columnAtPoint(e.getPoint());
                if (row < 0 || col != 3) return;
                String status = (String) tableModel.getValueAt(row, 2); if (!"Pending".equalsIgnoreCase(status)) return;
                int apptID = (int) tableModel.getValueAt(row, 4);
                Object[] options = {"Edit Concern", "Cancel Appointment", "Back"};
                int choice = JOptionPane.showOptionDialog(null, "Manage Appointment ID: " + apptID, "Manage", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);
                if (choice == 0) {
                    String oldConcern = (String) tableModel.getValueAt(row, 1);
                    String newConcern = JOptionPane.showInputDialog("Update Medical Concern:", oldConcern);
                    if (newConcern != null && !newConcern.trim().isEmpty()) { DatabaseManager.updateAppointmentData(apptID, newConcern.trim(), "Updated by Student"); refreshTable(); }
                } else if (choice == 1) {
                    if (JOptionPane.showConfirmDialog(null, "Cancel this appointment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { DatabaseManager.deleteAppointment(apptID); refreshTable(); updateOperatingHours(); }
                }
            }
        });
        historyPanel.add(new JScrollPane(appointmentTable), "grow");
        mainContent.add(historyPanel, "grow");
        updateOperatingHours(); refreshTable();
        return mainContent;
    }

    private JPanel createSettingsView() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 40", "[fill]", "[]20[]push"));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("Account Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(AppColors.NAV_BG);
        panel.add(title, "wrap");

        // Main Settings Card
        JPanel card = new JPanel(new MigLayout("insets 40, wrap 2", "[220!]40[grow]", "[]35[]35[]35[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(AppColors.BORDER, 1));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        // --- ROW: Student ID ---
        JLabel idLabel = new JLabel("Student ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(idLabel, "right");
        JTextField idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        idField.setPreferredSize(new Dimension(0, 50));
        idField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Student ID...");
        card.add(idField, "growx");

        // --- ROW: Current Password ---
        JLabel currLabel = new JLabel("Current Password:");
        currLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(currLabel, "right");
        JPasswordField currField = new JPasswordField();
        currField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        currField.setPreferredSize(new Dimension(0, 50));
        card.add(currField, "growx");

        // --- ROW: New Password ---
        JLabel nextLabel = new JLabel("New Password:");
        nextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(nextLabel, "right");
        JPasswordField nextField = new JPasswordField();
        nextField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nextField.setPreferredSize(new Dimension(0, 50));
        card.add(nextField, "growx");

        // --- ROW: Action Button ---
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(AppColors.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        saveBtn.setFocusPainted(false); saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        
        saveBtn.addActionListener(e -> {
            String sid = idField.getText().trim();
            String currentPass = new String(currField.getPassword());
            String newPass = new String(nextField.getPassword());
            
            if (sid.isEmpty() || currentPass.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields to change your password.", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!sid.equals(currentUser.studentID)) {
                JOptionPane.showMessageDialog(this, "The Student ID entered does not match your logged-in account.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (DatabaseManager.updateUserPassword(sid, currentPass, newPass)) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!");
                idField.setText(""); currField.setText(""); nextField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect current password. Verification failed.", "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(saveBtn, "span 2, right, w 200!, h 50!");

        panel.add(card, "w 1000!, aligny top");
        return panel;
    }

    private void updateOperatingHours() {
        String dateStr = selectedDate.toString();
        String[] sched = DatabaseManager.getClinicSchedule(dateStr);
        int currentBooked = DatabaseManager.getAppointmentCountForDate(dateStr);
        if (sched != null && "CLOSED".equals(sched[1])) {
            timeLabel.setText("Status: CLOSED FOR TODAY"); timeLabel.setForeground(AppColors.DANGER);
            doctorLabel.setText("Doctor: None"); slotsLabel.setText("Slots: 0/0"); bookBtn.setEnabled(false);
        } else if (sched == null) {
            timeLabel.setText("Status: Not Scheduled"); timeLabel.setForeground(AppColors.TEXT_MUTED);
            doctorLabel.setText("Doctor: Not Assigned"); slotsLabel.setText("Slots: N/A"); bookBtn.setEnabled(false);
        } else {
            int limit = sched.length >= 5 ? Integer.parseInt(sched[4]) : 0;
            timeLabel.setText("Hours: " + sched[1] + " - " + sched[2]); timeLabel.setForeground(AppColors.SUCCESS);
            doctorLabel.setText("Doctor in Charge: " + (sched.length >= 4 ? sched[3] : "TBD"));
            slotsLabel.setText("Slots Used: " + currentBooked + " / " + limit);
            slotsLabel.setForeground(currentBooked >= limit ? AppColors.DANGER : AppColors.TEXT_MUTED);
            bookBtn.setEnabled(currentBooked < limit);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<DatabaseManager.Appointment> list = DatabaseManager.getStudentAppointments(currentUser.studentID);
        boolean hasApproved = false;
        String approvedDate = "";
        
        for (DatabaseManager.Appointment a : list) {
            String actionText = "Pending".equalsIgnoreCase(a.status) ? "Manage \u2699\uFE0F" : "--";
            tableModel.addRow(new Object[]{a.date, a.concern, a.status, actionText, a.id});
            if ("Approved".equalsIgnoreCase(a.status)) {
                hasApproved = true;
                approvedDate = a.date;
            }
        }
        
        if (hasApproved) {
            notificationLabel.setText("\u2705 Your appointment for " + approvedDate + " has been APPROVED!");
            notificationLabel.setVisible(true);
        } else {
            notificationLabel.setVisible(false);
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20"));
        panel.setBackground(Color.WHITE); panel.setBorder(new LineBorder(AppColors.BORDER, 2));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        JLabel titleLabel = new JLabel(title); titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(AppColors.ACCENT); panel.add(titleLabel, "wrap 15");
        return panel;
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return btn;
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

    private void styleTable() {
        appointmentTable.setRowHeight(50); appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        appointmentTable.setShowGrid(false); appointmentTable.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader header = appointmentTable.getTableHeader();
        header.setBackground(new Color(245, 247, 250)); header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setPreferredSize(new Dimension(0, 50));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 15, 0, 15));
                if (col == 2 && value != null) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("Done")) { c.setForeground(new Color(40, 167, 69)); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (status.equalsIgnoreCase("Approved")) { c.setForeground(AppColors.APPROVED); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (status.equalsIgnoreCase("Pending")) { c.setForeground(new Color(255, 165, 0)); setFont(getFont().deriveFont(Font.BOLD)); }
                } else if (col == 3 && "Manage \u2699\uFE0F".equals(value)) { c.setForeground(AppColors.ACCENT); setFont(getFont().deriveFont(Font.BOLD)); }
                else { c.setForeground(AppColors.TEXT_MAIN); }
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                return c;
            }
        };
        appointmentTable.setDefaultRenderer(Object.class, renderer);
    }
}
