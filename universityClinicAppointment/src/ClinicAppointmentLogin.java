import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class ClinicAppointmentLogin extends JFrame {

    private JTextField idField;
    private JPasswordField passField;

    public ClinicAppointmentLogin() {
        setTitle("University Clinic Appointment - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650); // Slightly more height
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // --- LEFT PANEL (Branding) ---
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(AppColors.ACCENT);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.fill = GridBagConstraints.CENTER;

        // --- LOGO ---
        try {
            ImageIcon logoIcon = new ImageIcon("src/images/logo.png");
            Image scaledLogo = logoIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(ImageUtils.makeTransparent(scaledLogo)));
            gbcLeft.gridy = 0;
            gbcLeft.insets = new Insets(0, 0, 30, 0);
            leftPanel.add(logoLabel, gbcLeft);
        } catch (Exception e) {}

        // --- BRANDING TEXT ---
        JLabel brandLabel = new JLabel("<html><div style='text-align: center;'>University Clinic<br>Appointment</div></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brandLabel.setForeground(Color.WHITE);
        gbcLeft.gridy = 1;
        gbcLeft.insets = new Insets(0, 40, 0, 40);
        leftPanel.add(brandLabel, gbcLeft);

        JLabel descLabel = new JLabel("Secure healthcare management for students.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(210, 225, 245));
        gbcLeft.gridy = 2;
        gbcLeft.insets = new Insets(20, 40, 0, 40);
        leftPanel.add(descLabel, gbcLeft);

        // --- RIGHT PANEL (Form) ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 60, 10, 60);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel welcomeLabel = new JLabel("Sign In");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(AppColors.TEXT_MAIN);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 60, 5, 60);
        rightPanel.add(welcomeLabel, gbc);

        JLabel subText = new JLabel("Enter your credentials to access the clinic system");
        subText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subText.setForeground(AppColors.TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 30, 60);
        rightPanel.add(subText, gbc);

        // Student ID
        gbc.gridy = 2; gbc.insets = new Insets(15, 60, 5, 60);
        rightPanel.add(new JLabel("Student ID / Username"), gbc);
        idField = createField();
        idField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Format: 2024-12446");
        gbc.gridy = 3; gbc.insets = new Insets(5, 60, 5, 60);
        rightPanel.add(idField, gbc);

        // Password
        gbc.gridy = 4; gbc.insets = new Insets(15, 60, 5, 60);
        rightPanel.add(new JLabel("Password"), gbc);
        passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(0, 45));
        passField.setBackground(AppColors.BG_LIGHT);
        passField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(AppColors.BORDER, 1), new EmptyBorder(0, 15, 0, 15)));
        gbc.gridy = 5; gbc.insets = new Insets(5, 60, 10, 60);
        rightPanel.add(passField, gbc);

        // Login Button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setBackground(AppColors.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(0, 50));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 60, 10, 60);
        rightPanel.add(loginBtn, gbc);


        // --- ADMIN LOGIN BUTTON ---
        JButton adminLink = new JButton("Clinic Administrator Login");
        adminLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminLink.setForeground(AppColors.TEXT_MUTED);
        adminLink.setBorderPainted(false);
        adminLink.setContentAreaFilled(false);
        adminLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(60, 60, 20, 60);
        rightPanel.add(adminLink, gbc);

        // Listeners
        loginBtn.addActionListener(e -> performLogin());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin(); }
        };
        idField.addKeyListener(enterKey);
        passField.addKeyListener(enterKey);

        adminLink.addActionListener(e -> {
            new ClinicAppointmentAdminLogin().setVisible(true);
            dispose();
        });

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel);
    }

    private void performLogin() {
        String studentID = idField.getText().trim();
        String pass = new String(passField.getPassword());
        DatabaseManager.User user = DatabaseManager.validateUser(studentID, pass);
        
        if (user != null) {
            if ("ADMIN".equals(user.role)) {
                new ClinicAppointmentAdmin(user).setVisible(true);
            } else {
                new ClinicAppointmentDashboard(user).setVisible(true);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid ID or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 45));
        f.setBackground(AppColors.BG_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(AppColors.BORDER, 1), new EmptyBorder(0, 15, 0, 15)));
        return f;
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatIntelliJLaf.setup();
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new ClinicAppointmentLogin().setVisible(true));
    }
}
