package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class PatientDashboard extends JFrame {

    private final Patient         patient;
    private final Medivaultsystem system;
    private final JPanel          contentPanel = new JPanel(new CardLayout());

    // Teal theme — matching the login screen exactly
    private static final Color TEAL       = new Color(0x3D, 0xB8, 0x9E);
    private static final Color TEAL_DARK  = new Color(0x2A, 0x9D, 0x87);
    private static final Color TEAL_LIGHT = new Color(0xE8, 0xF5, 0xF2);
    private static final Color BG_PAGE    = new Color(0xEA, 0xF0, 0xEE);
    private static final Color SIDEBAR_BG = new Color(0x2A, 0x9D, 0x87);
    private static final Color WHITE      = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(0x2D, 0x3A, 0x3A);
    private static final Color TEXT_MUTED = new Color(0x9A, 0xAA, 0xA8);
    private static final Color FIELD_BG   = new Color(0xEA, 0xF5, 0xF2);
    private static final Color DANGER     = new Color(0xE5, 0x39, 0x35);
    private static final Color WARNING    = new Color(0xF5, 0x7C, 0x00);
    private static final Color YELLOW     = new Color(0xF5, 0xC5, 0x18);

    public PatientDashboard(Patient patient, Medivaultsystem system) {
        this.patient = patient;
        this.system  = system;

        setTitle("MediVault — Patient Portal");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PAGE);

        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildHeader(),   BorderLayout.NORTH);
        root.add(contentPanel,    BorderLayout.CENTER);

        contentPanel.setBackground(BG_PAGE);
        contentPanel.add(buildOverviewPanel(),     "OVERVIEW");
        contentPanel.add(buildMedicalPanel(),      "MEDICAL");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(buildBookPanel(),         "BOOK");
        contentPanel.add(buildBillingPanel(),      "BILLING");

        showCard("OVERVIEW");
        setContentPane(root);
    }

    private void showCard(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
    }

    // ── Header ─────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(TEAL);
        h.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("Patient Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(WHITE);

        JLabel user = new JLabel(
                patient.getName() + "   |   ID: " + patient.getPatientId());
        user.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        user.setForeground(new Color(255, 255, 255, 200));

        h.add(title, BorderLayout.WEST);
        h.add(user,  BorderLayout.EAST);
        return h;
    }

    // ── Sidebar ────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Decorative circles matching login screen style
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-30, getHeight() - 160, 180, 180);
                g2.fillOval(20, -40, 100, 100);
            }
        };
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(220, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(30, 14, 24, 14));

        // App logo area
        JLabel logo = new JLabel("MV");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("MediVault");
        appName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appName.setForeground(new Color(255, 255, 255, 180));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        side.add(logo);
        side.add(Box.createVerticalStrut(2));
        side.add(appName);
        side.add(Box.createVerticalStrut(30));

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        side.add(sep);
        side.add(Box.createVerticalStrut(20));

        // Nav buttons
        String[][] nav = {
                {"  Overview",          "OVERVIEW"},
                {"  Medical Record",    "MEDICAL"},
                {"  My Appointments",   "APPOINTMENTS"},
                {"  Book Appointment",  "BOOK"},
                {"  Billing",           "BILLING"},
        };

        for (String[] item : nav) {
            JButton btn = buildSidebarBtn(item[0]);
            String card = item[1];
            btn.addActionListener(e -> showCard(card));
            side.add(btn);
            side.add(Box.createVerticalStrut(6));
        }

        side.add(Box.createVerticalGlue());

        // Logout
        JButton logout = buildSidebarBtn("  Logout");
        logout.setForeground(new Color(0xFF, 0xCC, 0xCC));
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        side.add(logout);
        return side;
    }

    private JButton buildSidebarBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(getForeground());
                g2.setFont(getFont());
                g2.drawString(getText(), 14,
                        (getHeight() + g2.getFontMetrics().getAscent()
                                - g2.getFontMetrics().getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(255, 255, 255, 220));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(192, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    // ── Overview panel ─────────────────────────────────────────────────

    private JPanel buildOverviewPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Overview"));
        p.add(Box.createVerticalStrut(18));

        // Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        int apptCount = system
                .getAppointmentsForPatient(patient.getPatientId()).size();

        statsRow.add(buildStatCard("Appointments",
                String.valueOf(apptCount), TEAL));
        statsRow.add(buildStatCard("Bill Amount",
                String.format("Rs. %.0f", patient.getBillAmount()),
                patient.isBillPaid() ? TEAL_DARK : WARNING));
        statsRow.add(buildStatCard("Treatment",
                patient.getTreatmentDurationDays() + " days", YELLOW));

        p.add(statsRow);
        p.add(Box.createVerticalStrut(24));
        p.add(sectionTitle("Personal Details"));
        p.add(Box.createVerticalStrut(12));

        JPanel infoCard = buildWhiteCard();
        infoCard.setLayout(new GridLayout(3, 4, 10, 12));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        String[][] fields = {
                {"Patient ID",  patient.getPatientId()},
                {"Full Name",   patient.getName()},
                {"Age",         String.valueOf(patient.getAge())},
                {"Gender",      patient.getGender()},
                {"Phone",       patient.getPhone()},
                {"Email",       patient.getEmail()},
        };
        for (String[] row : fields) {
            infoCard.add(mutedLabel(row[0]));
            infoCard.add(infoLabel(row[1]));
        }
        p.add(infoCard);
        return p;
    }

    private JPanel buildStatCard(String title, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Top accent stripe
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(18, 18, 14, 18));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(accent);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);

        c.add(lbl, BorderLayout.NORTH);
        c.add(val, BorderLayout.CENTER);
        return c;
    }

    // ── Medical panel ──────────────────────────────────────────────────

    private JPanel buildMedicalPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Medical Record & Prescriptions"));
        p.add(Box.createVerticalStrut(16));

        JPanel card = buildWhiteCard();
        card.setLayout(new GridLayout(0, 2, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        String[][] rows = {
                {"Symptoms",            patient.getSymptoms()},
                {"Prescribed Medicine", patient.getPrescribedMedicine()},
                {"Treatment Duration",  patient.getTreatmentDurationDays() + " days"},
        };
        for (String[] row : rows) {
            card.add(mutedLabel(row[0]));
            String v = (row[1] == null || row[1].isEmpty())
                    ? "Not yet prescribed" : row[1];
            card.add(infoLabel(v));
        }
        p.add(card);
        p.add(Box.createVerticalStrut(22));
        p.add(sectionTitle("Medical History Log"));
        p.add(Box.createVerticalStrut(10));

        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(WHITE);
        area.setForeground(TEXT_DARK);
        area.setBorder(new EmptyBorder(12, 14, 12, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        List<String> hist = patient.getMedicalHistory();
        if (hist == null || hist.isEmpty()) {
            area.setText("No history recorded yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String e : hist) sb.append(e).append("\n");
            area.setText(sb.toString());
        }

        JScrollPane scroll = new JScrollPane(area);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(0xB2, 0xD8, 0xCE), 1));
        scroll.getViewport().setBackground(WHITE);
        p.add(scroll);
        return p;
    }

    // ── Appointments panel ─────────────────────────────────────────────

    private JPanel buildAppointmentsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PAGE);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel h = sectionTitle("My Appointments");
        h.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(h, BorderLayout.NORTH);

        String[] cols = {"Appt. ID", "Doctor", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (Appointment a : system
                .getAppointmentsForPatient(patient.getPatientId())) {
            model.addRow(new Object[]{
                    a.getAppointmentId(), a.getDoctorName(),
                    a.getDate(), a.getTime(), a.getStatus()
            });
        }

        JTable table = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(0xB2, 0xD8, 0xCE), 1));
        scroll.getViewport().setBackground(WHITE);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Book Appointment panel ─────────────────────────────────────────

    private JPanel buildBookPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Book an Appointment"));
        p.add(Box.createVerticalStrut(18));

        JPanel form = buildWhiteCard();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(500, 400));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Doctor dropdown
        List<Doctor> doctors = system.getAllDoctors();
        String[] docLabels = doctors.stream()
                .map(d -> d.getName() + " — " + d.getSpecialization())
                .toArray(String[]::new);
        JComboBox<String> docBox = new JComboBox<>(docLabels);
        styleCombo(docBox);

        JTextField dateField = buildInputField("Date  (YYYY-MM-DD)");
        JComboBox<String> timeBox = new JComboBox<>(new String[]{
                "09:00","10:00","11:00","12:00","14:00","15:00","16:00","17:00"
        });
        styleCombo(timeBox);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton bookBtn = buildTealButton("Confirm Appointment");
        bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        formRow(form, "Select Doctor", docBox);
        formRow(form, "Date",          dateField);
        formRow(form, "Time Slot",     timeBox);
        form.add(Box.createVerticalStrut(8));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(bookBtn);

        p.add(form);

        bookBtn.addActionListener(e -> {
            int idx     = docBox.getSelectedIndex();
            String did  = idx >= 0 ? doctors.get(idx).getDoctorId() : "";
            String date = dateField.getText().trim();
            String time = (String) timeBox.getSelectedItem();

            // Ignore placeholder text
            if (date.equals("Date  (YYYY-MM-DD)")) date = "";

            String err = system.bookAppointment(
                    patient.getPatientId(), did, date, time);
            if (err != null) {
                errLabel.setForeground(DANGER);
                errLabel.setText(err);
            } else {
                errLabel.setForeground(TEAL_DARK);
                errLabel.setText("Booked successfully!");
                dateField.setText("Date  (YYYY-MM-DD)");
                dateField.setForeground(TEXT_MUTED);
            }
        });

        return p;
    }

    // ── Billing panel ──────────────────────────────────────────────────

    private JPanel buildBillingPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Billing Details"));
        p.add(Box.createVerticalStrut(18));

        JPanel card = buildWhiteCard();
        card.setLayout(new GridLayout(0, 2, 10, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        String[][] rows = {
                {"Patient",          patient.getName()},
                {"Total Amount",     String.format("Rs. %.2f",
                        patient.getBillAmount())},
                {"Payment Status",   patient.isBillPaid() ? "PAID" : "PENDING"},
                {"Medicine",         patient.getPrescribedMedicine() == null
                        ? "Not prescribed"
                        : patient.getPrescribedMedicine()},
        };

        for (String[] row : rows) {
            card.add(mutedLabel(row[0]));
            JLabel val = infoLabel(row[1]);
            if (row[0].equals("Total Amount")) {
                val.setForeground(WARNING);
                val.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }
            if (row[0].equals("Payment Status")) {
                val.setForeground(patient.isBillPaid() ? TEAL_DARK : DANGER);
                val.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
            card.add(val);
        }
        p.add(card);
        p.add(Box.createVerticalStrut(18));

        if (!patient.isBillPaid()) {
            JButton payBtn = buildTealButton("Mark as Paid");
            payBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            payBtn.addActionListener(e -> {
                system.markBillPaid(patient.getPatientId());
                JOptionPane.showMessageDialog(this,
                        "Bill marked as paid!",
                        "Payment Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
                showCard("OVERVIEW");
            });
            p.add(payBtn);
        } else {
            JLabel ok = infoLabel("Bill fully paid. Thank you!");
            ok.setForeground(TEAL_DARK);
            ok.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(ok);
        }
        return p;
    }

    // ── Shared helpers ─────────────────────────────────────────────────

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 17));
        l.setForeground(TEAL_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text == null ? "—" : text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    // White rounded card panel
    private JPanel buildWhiteCard() {
        JPanel c = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(18, 20, 18, 20));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // Teal rounded button — same as login screen
    private JButton buildTealButton(String text) {
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
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setMaximumSize(new Dimension(220, 44));
        return btn;
    }

    // Teal-background input field with placeholder
    private JTextField buildInputField(String placeholder) {
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
        f.setOpaque(false);
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_MUTED);
        f.setBorder(new EmptyBorder(10, 14, 10, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setText(placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(TEXT_MUTED);
                }
            }
        });
        return f;
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(FIELD_BG);
        box.setForeground(TEXT_DARK);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(0xB2, 0xD8, 0xCE), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
    }

    // Adds a label above a field inside a BoxLayout panel
    private void formRow(JPanel p, String label, JComponent field) {
        JLabel l = mutedLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(12));
    }

    // Builds a styled JTable with teal header
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(WHITE);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(TEAL_LIGHT);
        table.setSelectionForeground(TEXT_DARK);

        // Teal header
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(TEAL);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Alternating row colours
        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel,
                            boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(
                                t, val, sel, foc, row, col);
                        setBackground(sel ? TEAL_LIGHT
                                : (row % 2 == 0 ? WHITE : FIELD_BG));
                        setForeground(TEXT_DARK);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        return this;
                    }
                });
        return table;
    }

} // end of PatientDashboard

