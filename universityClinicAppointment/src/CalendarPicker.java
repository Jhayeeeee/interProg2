import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A premium calendar popup that strictly blocks past dates and non-available dates.
 */
public class CalendarPicker extends JDialog {

    // ── Colour tokens (matches AppColors palette) ─────────────────────────
    private static final Color BG           = Color.WHITE;
    private static final Color HEADER_BG    = AppColors.NAV_BG;
    private static final Color HEADER_FG    = Color.WHITE;
    private static final Color TODAY_RING   = AppColors.ACCENT;
    private static final Color SELECTED_BG  = AppColors.ACCENT;
    private static final Color SELECTED_FG  = Color.WHITE;
    private static final Color DAY_FG       = AppColors.TEXT_MAIN;
    private static final Color PAST_FG      = new Color(220, 220, 220);
    private static final Color PAST_BG      = new Color(248, 250, 252);
    private static final Color HOVER_BG     = new Color(220, 252, 231);
    private static final Color WEEKEND_FG   = AppColors.DANGER;
    private static final Color BORDER_C     = AppColors.BORDER;

    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    // ── State ─────────────────────────────────────────────────────────────
    private LocalDate today;
    private LocalDate selected;
    private YearMonth viewing;
    private LocalDate result = null;
    private List<String> availableDates;

    private JLabel monthLabel;
    private JPanel gridPanel;
    private JButton prevBtn, nextBtn;

    // ─────────────────────────────────────────────────────────────────────
    private CalendarPicker(Frame owner, LocalDate initial, List<String> availableDates) {
        super(owner, "Select Date", true);
        this.today          = LocalDate.now();
        this.availableDates = availableDates; // Keep it null if passed as null
        this.selected       = (initial != null) ? initial : today;
        this.viewing        = YearMonth.from(selected);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setBorder(new LineBorder(BORDER_C, 2));
        getRootPane().putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG);
        contentPanel.add(buildDayLabels(), BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        gridPanel.setBackground(BG);
        gridPanel.setBorder(new EmptyBorder(4, 12, 10, 12));
        contentPanel.add(gridPanel, BorderLayout.CENTER);
        
        root.add(contentPanel, BorderLayout.CENTER);

        // Footer with Today button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(5, 0, 10, 0));

        JButton todayBtn = new JButton("Go to Today");
        todayBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        todayBtn.setForeground(AppColors.ACCENT);
        todayBtn.setBackground(new Color(240, 253, 244));
        todayBtn.setFocusPainted(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 20; margin: 5,15,5,15");
        
        todayBtn.addActionListener(e -> {
            viewing = YearMonth.now();
            buildGrid();
        });
        footer.add(todayBtn);
        root.add(footer, BorderLayout.SOUTH);

        buildGrid();
        pack();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(10, 14, 10, 14));

        prevBtn = arrowBtn("\u2039");
        nextBtn = arrowBtn("\u203A");

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        monthLabel.setForeground(HEADER_FG);

        prevBtn.addActionListener(e -> { viewing = viewing.minusMonths(1); buildGrid(); });
        nextBtn.addActionListener(e -> { viewing = viewing.plusMonths(1); buildGrid(); });

        header.add(prevBtn,      BorderLayout.WEST);
        header.add(monthLabel,  BorderLayout.CENTER);
        header.add(nextBtn,      BorderLayout.EAST);
        return header;
    }

    private JButton arrowBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 20));
        b.setForeground(HEADER_FG);
        b.setBackground(HEADER_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(40, 40));
        return b;
    }

    private JPanel buildDayLabels() {
        JPanel p = new JPanel(new GridLayout(1, 7, 2, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(10, 12, 5, 12));
        for (int i = 0; i < 7; i++) {
            JLabel l = new JLabel(DAY_NAMES[i], SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 11));
            l.setForeground(i == 0 || i == 6 ? WEEKEND_FG : new Color(148, 163, 184));
            p.add(l);
        }
        return p;
    }

    private void buildGrid() {
        gridPanel.removeAll();
        monthLabel.setText(viewing.format(MONTH_FMT));

        YearMonth currentMonth = YearMonth.now();
        prevBtn.setEnabled(viewing.isAfter(currentMonth));

        int daysInMonth = viewing.lengthOfMonth();
        LocalDate firstOfMonth = viewing.atDay(1);
        int startDow = firstOfMonth.getDayOfWeek().getValue() % 7; 

        for (int i = 0; i < startDow; i++) gridPanel.add(new JLabel(""));

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = viewing.atDay(day);
            String dateStr = date.toString();
            boolean isAdmin     = (availableDates == null);
            boolean isPast      = date.isBefore(today);
            boolean isToday     = date.equals(today);
            boolean isSelected  = date.equals(selected);
            boolean isAvailable = isAdmin || (availableDates != null && availableDates.contains(dateStr));
            boolean isWeekend   = (date.getDayOfWeek().getValue() % 7 == 0 || date.getDayOfWeek().getValue() == 6);

            JButton btn = new JButton(String.valueOf(day)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int sz = Math.min(w, h) - 4;
                    int x = (w - sz) / 2, y = (h - sz) / 2;

                    if (!isAdmin && (isPast || !isAvailable)) {
                        g2.setColor(PAST_BG);
                        g2.fillRect(0, 0, w, h);
                    }
                    
                    if (!isAdmin && isAvailable && !isPast) {
                        g2.setColor(new Color(232, 245, 233)); // Light green
                        g2.fillOval(x, y, sz, sz);
                        g2.setColor(new Color(129, 199, 132));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawOval(x, y, sz, sz);
                    }

                    if (isSelected) {
                        g2.setColor(SELECTED_BG);
                        g2.fillOval(x, y, sz, sz);
                    } else if (isToday) {
                        g2.setColor(TODAY_RING);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawOval(x + 1, y + 1, sz - 2, sz - 2);
                    } else if (getModel().isRollover() && (!isPast || isAdmin)) {
                        g2.setColor(HOVER_BG);
                        g2.fillOval(x, y, sz, sz);
                    }
                    
                    if (isPast && !isAdmin) {
                        g2.setColor(PAST_FG);
                        g2.setStroke(new BasicStroke(1.2f));
                        g2.drawLine(x + 6, h / 2, x + sz - 6, h / 2);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            btn.setFont(new Font("SansSerif", isToday ? Font.BOLD : Font.PLAIN, 14));
            btn.setPreferredSize(new Dimension(45, 45));
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 0,0,0,0; padding: 0,0,0,0");

            if (!isAdmin && (isPast || !isAvailable)) {
                btn.setForeground(PAST_FG);
                btn.setEnabled(false);
            } else {
                btn.setForeground(isSelected ? SELECTED_FG : (isWeekend ? WEEKEND_FG : DAY_FG));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> {
                    result = date;
                    dispose();
                });
            }
            gridPanel.add(btn);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
        pack();
    }

    public static LocalDate showPopup(Component anchor, LocalDate initial, List<String> availableDates) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(anchor);
        CalendarPicker picker = new CalendarPicker(owner, initial, availableDates);
        Point loc = anchor.getLocationOnScreen();
        picker.setLocation(loc.x, loc.y + anchor.getHeight() + 5);
        picker.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) { picker.dispose(); }
        });
        picker.setVisible(true);
        return picker.result;
    }
}
