import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class ClinicAppointmentAdminLogin extends JFrame {

    private JTextField idField;
    private JPasswordField passField;

    public ClinicAppointmentAdminLogin() {
        setTitle("University Clinic - Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Header
        JLabel logoLabel = new JLabel("\uD83D\uDD10 Admin Access", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(AppColors.NAV_BG);
        gbc.gridy = 0;
        panel.add(logoLabel, gbc);

        JLabel subText = new JLabel("Authorized personnel only", SwingConstants.CENTER);
        subText.setForeground(AppColors.TEXT_MUTED);
        gbc.gridy = 1;
        panel.add(subText, gbc);

        // Fields
        gbc.gridy = 2; gbc.insets = new Insets(30, 40, 5, 40);
        panel.add(new JLabel("Admin ID"), gbc);
        
        idField = new JTextField();
        idField.setPreferredSize(new Dimension(0, 45));
        idField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(AppColors.BORDER, 1), new EmptyBorder(0, 15, 0, 15)));
        gbc.gridy = 3; gbc.insets = new Insets(5, 40, 15, 40);
        panel.add(idField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(new JLabel("Password"), gbc);

        passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(0, 45));
        passField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(AppColors.BORDER, 1), new EmptyBorder(0, 15, 0, 15)));
        gbc.gridy = 5; gbc.insets = new Insets(5, 40, 30, 40);
        panel.add(passField, gbc);

        // Login Button
        JButton loginBtn = new JButton("Login as Admin");
        loginBtn.setBackground(AppColors.NAV_BG);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(0, 50));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        panel.add(loginBtn, gbc);

        // Back to Student
        JButton studentLink = new JButton("Back to Student Login");
        studentLink.setForeground(AppColors.ACCENT);
        studentLink.setBorderPainted(false);
        studentLink.setContentAreaFilled(false);
        studentLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        panel.add(studentLink, gbc);

        // Listeners
        loginBtn.addActionListener(e -> performAdminLogin());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) performAdminLogin(); }
        };
        idField.addKeyListener(enterKey);
        passField.addKeyListener(enterKey);

        studentLink.addActionListener(e -> {
            new ClinicAppointmentLogin().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private void performAdminLogin() {
        String adminID = idField.getText().trim();
        String pass = new String(passField.getPassword());
        DatabaseManager.User user = DatabaseManager.validateUser(adminID, pass);
        
        if (user != null && "ADMIN".equals(user.role)) {
            new ClinicAppointmentAdmin(user).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Access Denied. Invalid Admin Credentials.", "Security Alert", JOptionPane.ERROR_MESSAGE);
        }
    }
}
