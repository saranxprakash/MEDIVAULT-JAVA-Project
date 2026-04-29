package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Doctor;
import medivault.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    private final Medivaultsystem system = new Medivaultsystem();

    private static final Color TEAL        = new Color(0x3D, 0xB8, 0x9E);
    private static final Color TEAL_DARK   = new Color(0x2A, 0x9D, 0x87);
    private static final Color TEAL_LIGHT  = new Color(0xE8, 0xF5, 0xF2);
    private static final Color BG_PAGE     = new Color(0xEA, 0xF0, 0xEE);
    private static final Color WHITE       = Color.WHITE;
    private static final Color TEXT_DARK   = new Color(0x2D, 0x3A, 0x3A);
    private static final Color TEXT_MUTED  = new Color(0x9A, 0xAA, 0xA8);
    private static final Color FIELD_BG    = new Color(0xE4, 0xF3, 0xEF);
    private static final Color YELLOW      = new Color(0xF5, 0xC5, 0x18);
    private static final Color RED_ACCENT  = new Color(0xE8, 0x5D, 0x75);
    private static final Color ERR_COLOR   = new Color(0xE5, 0x39, 0x35);

    private String currentMode = "register";
    private JPanel rightContainer;
    private JLabel leftTitle;
    private JLabel leftSub;
    private JButton leftActionBtn;

    public LoginFrame() {
        setTitle("MediVault");
        setUndecorated(true);
        setSize(960, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setShape(new RoundRectangle2D.Double(0, 0, 960, 600, 30, 30));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(YELLOW);
                g2.fillOval(-70, getHeight() - 150, 220, 220);
                g2.setColor(RED_ACCENT);
                g2.fillOval(getWidth() - 130, -90, 220, 220);
                g2.setColor(new Color(0x5D, 0xC8, 0xB0, 55));
                g2.fillOval(-40, getHeight() - 240, 170, 170);
            }
        };
        root.setBackground(BG_PAGE);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Close button
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setForeground(TEXT_MUTED);
        closeBtn.setBackground(BG_PAGE);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(closeBtn);
        root.add(topBar, BorderLayout.NORTH);

        // Main card
        JPanel card = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        card.add(buildLeftPanel());
        card.add(buildRightContainer());

        JPanel centreWrapper = new JPanel(new GridBagLayout());
        centreWrapper.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1; gc.weighty = 1;
        centreWrapper.add(card, gc);
        root.add(centreWrapper, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Left teal panel ──────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth() + 24, getHeight(), 24, 24);
                g2.setColor(new Color(255, 255, 255, 25));
                drawDiamond(g2, 50, 90, 20);
                drawDiamond(g2, getWidth() - 40, getHeight() - 110, 16);
                g2.fillOval(-50, getHeight() - 150, 200, 200);
                g2.dispose();
            }
            private void drawDiamond(Graphics2D g, int cx, int cy, int r) {
                g.fillPolygon(
                        new int[]{cx, cx+r, cx, cx-r},
                        new int[]{cy-r, cy, cy+r, cy}, 4);
            }
        };
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        leftTitle = new JLabel("Welcome Back!");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        leftTitle.setForeground(WHITE);
        leftTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftSub = new JLabel(
                "<html><div style='text-align:center'>To keep connected with us please"
                        + "<br>login with your personal info</div></html>");
        leftSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftSub.setForeground(new Color(255, 255, 255, 200));
        leftSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftSub.setHorizontalAlignment(SwingConstants.CENTER);

        leftActionBtn = buildOutlineButton("SIGN IN");
        leftActionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftActionBtn.addActionListener(e -> switchMode());

        inner.add(leftTitle);
        inner.add(Box.createVerticalStrut(14));
        inner.add(leftSub);
        inner.add(Box.createVerticalStrut(32));
        inner.add(leftActionBtn);

        p.add(inner);
        return p;
    }

    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                }
                g2.setColor(WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 46));
        btn.setMaximumSize(new Dimension(190, 46));
        return btn;
    }

    // ── Right container ──────────────────────────────────────────────────

    private JPanel buildRightContainer() {
        rightContainer = new JPanel(new CardLayout());
        rightContainer.setBackground(WHITE);
        rightContainer.add(buildRegisterPanel(), "register");
        rightContainer.add(buildLoginPanel(),    "login");
        showMode("register");
        return rightContainer;
    }

    private void switchMode() {
        showMode(currentMode.equals("register") ? "login" : "register");
    }

    private void showMode(String mode) {
        currentMode = mode;
        ((CardLayout) rightContainer.getLayout()).show(rightContainer, mode);
        if (mode.equals("login")) {
            leftTitle.setText("Hello, Friend!");
            leftSub.setText(
                    "<html><div style='text-align:center'>Enter your personal details"
                            + "<br>and start your journey with us</div></html>");
            leftActionBtn.setText("SIGN UP");
        } else {
            leftTitle.setText("Welcome Back!");
            leftSub.setText(
                    "<html><div style='text-align:center'>To keep connected with us please"
                            + "<br>login with your personal info</div></html>");
            leftActionBtn.setText("SIGN IN");
        }
        leftTitle.repaint();
        leftSub.repaint();
        leftActionBtn.repaint();
    }

    // ── Register panel ───────────────────────────────────────────────────
    private JPanel buildRegisterPanel() {
        // Outer white panel
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(WHITE);

        // Inner form with fixed width — this is what was missing before
        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 480));
        form.setMaximumSize(new Dimension(340, 480));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEAL_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("or use your email for registration:");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField     nameField  = makePlaceholderField("Name");
        JTextField     ageField   = makePlaceholderField("Age");
        JTextField     phoneField = makePlaceholderField("Phone");
        JTextField     emailField = makePlaceholderField("Email");
        JPasswordField passField  = makePlaceholderPassword("Password");
        JComboBox<String> genderBox = makeStyledCombo(
                new String[]{"Male","Female","Other"});

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(ERR_COLOR);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signUpBtn = makeTealButton("SIGN UP");
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(16));
        form.add(nameField);
        form.add(Box.createVerticalStrut(9));
        form.add(ageField);
        form.add(Box.createVerticalStrut(9));
        form.add(genderBox);
        form.add(Box.createVerticalStrut(9));
        form.add(phoneField);
        form.add(Box.createVerticalStrut(9));
        form.add(emailField);
        form.add(Box.createVerticalStrut(9));
        form.add(passField);
        form.add(Box.createVerticalStrut(8));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(12));
        form.add(signUpBtn);

        signUpBtn.addActionListener(e -> {
            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException ex) {
                errLabel.setText("Age must be a number.");
                return;
            }
            String err = system.registerPatient(
                    nameField.getText().trim(),
                    (String) genderBox.getSelectedItem(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    new String(passField.getPassword()),
                    age);
            if (err != null) {
                errLabel.setText(err);
            } else {
                errLabel.setForeground(TEAL_DARK);
                errLabel.setText("Account created! Please sign in.");
                clearField(nameField,  "Name");
                clearField(ageField,   "Age");
                clearField(phoneField, "Phone");
                clearField(emailField, "Email");
                passField.setText("");
                Timer t = new Timer(1400, ev -> showMode("login"));
                t.setRepeats(false);
                t.start();
            }
        });

        outer.add(form);
        return outer;
    }

    // ── Login panel ──────────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(WHITE);

        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 400));
        form.setMaximumSize(new Dimension(340, 400));

        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEAL_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Patient / Doctor toggle
        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        toggleRow.setBackground(WHITE);
        toggleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JToggleButton patBtn = makeToggle("Patient", true);
        JToggleButton docBtn = makeToggle("Doctor",  false);
        ButtonGroup   grp   = new ButtonGroup();
        grp.add(patBtn); grp.add(docBtn);
        toggleRow.add(patBtn); toggleRow.add(docBtn);

        JTextField     emailField = makePlaceholderField("Email");
        JPasswordField passField  = makePlaceholderPassword("Password");

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(ERR_COLOR);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signInBtn = makeTealButton("SIGN IN");
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);


        form.add(title);
        form.add(Box.createVerticalStrut(18));
        form.add(toggleRow);
        form.add(Box.createVerticalStrut(20));
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));
        form.add(passField);
        form.add(Box.createVerticalStrut(8));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(14));
        form.add(signInBtn);
        form.add(Box.createVerticalStrut(20));
        form.add(Box.createVerticalStrut(4));

        signInBtn.addActionListener(e -> {
            String email = getRealText(emailField, "Email");
            String pass  = new String(passField.getPassword());
            errLabel.setForeground(ERR_COLOR);

            if (patBtn.isSelected()) {
                Patient pat = system.loginPatient(email, pass);
                if (pat == null) {
                    errLabel.setText("Invalid email or password.");
                } else {
                    new PatientDashboard(pat, system).setVisible(true);
                    dispose();
                }
            } else {
                Doctor doc = system.loginDoctor(email, pass);
                if (doc == null) {
                    errLabel.setText("Invalid email or password.");
                } else {
                    new DoctorDashboard(doc, system).setVisible(true);
                    dispose();
                }
            }
        });

        outer.add(form);
        return outer;
    }

    // ── Component builders ────────────────────────────────────────────────
    private JTextField makePlaceholderField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_MUTED);
        f.setBackground(FIELD_BG);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(11, 16, 11, 16));
        // Fixed size — same as form width so it never shifts
        f.setPreferredSize(new Dimension(340, 46));
        f.setMaximumSize(new Dimension(340, 46));
        f.setMinimumSize(new Dimension(340, 46));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(TEXT_MUTED);
                }
            }
        });
        return f;
    }

    private JPasswordField makePlaceholderPassword(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_MUTED);
        f.setBackground(FIELD_BG);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(11, 16, 11, 16));
        f.setPreferredSize(new Dimension(340, 46));
        f.setMaximumSize(new Dimension(340, 46));
        f.setMinimumSize(new Dimension(340, 46));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.setEchoChar((char) 0);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(f.getPassword()).equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_DARK);
                    f.setEchoChar('•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getPassword().length == 0) {
                    f.setEchoChar((char) 0);
                    f.setText(placeholder);
                    f.setForeground(TEXT_MUTED);
                }
            }
        });
        return f;
    }

    private JComboBox<String> makeStyledCombo(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(FIELD_BG);
        box.setForeground(TEXT_DARK);
        box.setPreferredSize(new Dimension(340, 46));
        box.setMaximumSize(new Dimension(340, 46));
        box.setMinimumSize(new Dimension(340, 46));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(0xB8, 0xDE, 0xD4), 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        return box;
    }

    private JButton makeTealButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? TEAL_DARK : TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 46));
        btn.setMaximumSize(new Dimension(240, 46));
        return btn;
    }

    private JToggleButton makeToggle(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? TEAL : FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(isSelected() ? WHITE : TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        // Repaint both when selection changes
        btn.addItemListener(e -> {
            btn.repaint();
            btn.getParent().repaint();
        });
        return btn;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void clearField(JTextField f, String placeholder) {
        f.setText(placeholder);
        f.setForeground(TEXT_MUTED);
    }

    // Returns empty string if field still shows placeholder
    private String getRealText(JTextField f, String placeholder) {
        String t = f.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }

} // end of LoginFrame
