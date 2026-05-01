package medivault.view;

import medivault.model.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Admin login dialog — design matches LoginFrame's teal palette exactly.
 * Opens as a modal dialog on top of LoginFrame.
 */
public class AdminLoginFrame extends JDialog {

    private static final Color TEAL       = new Color(0x3D, 0xB8, 0x9E);
    private static final Color TEAL_DARK  = new Color(0x2A, 0x9D, 0x87);
    private static final Color BG_PAGE    = new Color(0xEA, 0xF0, 0xEE);
    private static final Color WHITE      = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(0x2D, 0x3A, 0x3A);
    private static final Color TEXT_MUTED = new Color(0x9A, 0xAA, 0xA8);
    private static final Color FIELD_BG   = new Color(0xE4, 0xF3, 0xEF);
    private static final Color ERR_COLOR  = new Color(0xE5, 0x39, 0x35);

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errLabel;

    public AdminLoginFrame(Frame parent) {
        super(parent, "Admin Login — MediVault", true);
        setUndecorated(true);
        setSize(420, 500);
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, 420, 500, 24, 24));
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF5, 0xC5, 0x18, 80));
                g2.fillOval(-40, getHeight() - 100, 160, 160);
                g2.setColor(new Color(0xE8, 0x5D, 0x75, 60));
                g2.fillOval(getWidth() - 80, -50, 160, 160);
            }
        };
        root.setBackground(BG_PAGE);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setForeground(TEXT_MUTED);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        topBar.add(closeBtn);
        root.add(topBar, BorderLayout.NORTH);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(300, 360));
        form.setMaximumSize(new Dimension(300, 360));

        JLabel iconWrap = new JLabel("🛡", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE8, 0xF5, 0xF2));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        iconWrap.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconWrap.setPreferredSize(new Dimension(64, 64));
        iconWrap.setMaximumSize(new Dimension(64, 64));
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Admin Portal");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(TEAL_DARK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Enter your administrator credentials");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = makePlaceholderField("Username");
        passwordField = makePlaceholderPassword("Password");

        errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(ERR_COLOR);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = makeTealButton("LOGIN AS ADMIN");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        usernameField.addKeyListener(enter);
        passwordField.addKeyListener(enter);

        form.add(iconWrap);
        form.add(Box.createVerticalStrut(16));
        form.add(heading);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(24));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(10));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(16));
        form.add(loginBtn);

        card.add(form);
        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.equals("Username")) username = "";
        if (password.equals("Password")) password = "";

        if (Admin.authenticate(username, password)) {
            dispose();
            new AdminDashboard().setVisible(true);
        } else {
            errLabel.setText("Invalid username or password.");
            passwordField.setText("");
            passwordField.setEchoChar((char) 0);
            passwordField.setText("Password");
            passwordField.setForeground(TEXT_MUTED);
        }
    }

    private JTextField makePlaceholderField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13)); f.setForeground(TEXT_MUTED);
        f.setBackground(FIELD_BG); f.setOpaque(false);
        f.setBorder(new EmptyBorder(11, 16, 11, 16));
        f.setPreferredSize(new Dimension(300, 46)); f.setMaximumSize(new Dimension(300, 46));
        f.setMinimumSize(new Dimension(300, 46)); f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_DARK); } }
            public void focusLost(FocusEvent e)   { if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_MUTED); } }
        });
        return f;
    }

    private JPasswordField makePlaceholderPassword(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13)); f.setForeground(TEXT_MUTED);
        f.setBackground(FIELD_BG); f.setOpaque(false);
        f.setBorder(new EmptyBorder(11, 16, 11, 16));
        f.setPreferredSize(new Dimension(300, 46)); f.setMaximumSize(new Dimension(300, 46));
        f.setMinimumSize(new Dimension(300, 46)); f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.setEchoChar((char) 0); f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (String.valueOf(f.getPassword()).equals(placeholder)) { f.setText(""); f.setForeground(TEXT_DARK); f.setEchoChar('•'); } }
            public void focusLost(FocusEvent e)   { if (f.getPassword().length == 0) { f.setEchoChar((char) 0); f.setText(placeholder); f.setForeground(TEXT_MUTED); } }
        });
        return f;
    }

    private JButton makeTealButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? TEAL_DARK : TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 46)); btn.setMaximumSize(new Dimension(240, 46));
        return btn;
    }
}