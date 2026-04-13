package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Appointment;
import medivault.model.Patient;
import medivault.model.Doctor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class DoctorDashboard extends JFrame {

    private final Doctor          doctor;
    private final Medivaultsystem system;
    private final JPanel          contentPanel = new JPanel(new CardLayout());

    // Same teal theme as login and patient dashboard
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

    public DoctorDashboard(Doctor doctor, Medivaultsystem system) {
        this.doctor = doctor;
        this.system = system;

        setTitle("MediVault — Doctor Portal");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PAGE);

        root.add(buildHeader(),   BorderLayout.NORTH);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(contentPanel,    BorderLayout.CENTER);

        contentPanel.setBackground(BG_PAGE);
        contentPanel.add(buildOverviewPanel(),     "OVERVIEW");
        contentPanel.add(buildTodayPanel(),        "TODAY");
        contentPanel.add(buildAllApptsPanel(),     "ALL_APPTS");
        contentPanel.add(buildPatientListPanel(),  "PATIENTS");
        contentPanel.add(buildPrescribePanel(),    "PRESCRIBE");

        showCard("OVERVIEW");
        setContentPane(root);
    }

    private void showCard(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
    }

    // ── Header ──────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(TEAL);
        h.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("Doctor Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(WHITE);

        JLabel info = new JLabel(
                doctor.getName() + "   |   " + doctor.getSpecialization());
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(new Color(255, 255, 255, 200));

        h.add(title, BorderLayout.WEST);
        h.add(info,  BorderLayout.EAST);
        return h;
    }

    // ── Sidebar ──────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
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

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        side.add(sep);
        side.add(Box.createVerticalStrut(20));

        String[][] nav = {
                {"  Overview",             "OVERVIEW"},
                {"  Today's Schedule",     "TODAY"},
                {"  All Appointments",     "ALL_APPTS"},
                {"  Patient List",         "PATIENTS"},
                {"  Update Prescription",  "PRESCRIBE"},
        };

        for (String[] item : nav) {
            JButton btn = buildSidebarBtn(item[0]);
            String card = item[1];
            btn.addActionListener(e -> showCard(card));
            side.add(btn);
            side.add(Box.createVerticalStrut(6));
        }

        side.add(Box.createVerticalGlue());

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

    // ── Overview panel ───────────────────────────────────────────────────

    private JPanel buildOverviewPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Doctor Overview"));
        p.add(Box.createVerticalStrut(18));

        // Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        int todayCount = system
                .getTodayAppointmentsForDoctor(doctor.getDoctorId()).size();
        int totalAppts = system
                .getAppointmentsForDoctor(doctor.getDoctorId()).size();
        int totalPats  = system.getAllPatients().size();

        statsRow.add(buildStatCard(
                "Today's Appointments", String.valueOf(todayCount), TEAL));
        statsRow.add(buildStatCard(
                "Total Appointments",   String.valueOf(totalAppts), YELLOW));
        statsRow.add(buildStatCard(
                "Total Patients",       String.valueOf(totalPats),  TEAL_DARK));

        p.add(statsRow);
        p.add(Box.createVerticalStrut(24));
        p.add(sectionTitle("Doctor Details"));
        p.add(Box.createVerticalStrut(12));

        JPanel infoCard = buildWhiteCard();
        infoCard.setLayout(new GridLayout(3, 4, 10, 12));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        String[][] fields = {
                {"Doctor ID",       doctor.getDoctorId()},
                {"Name",            doctor.getName()},
                {"Specialization",  doctor.getSpecialization()},
                {"Phone",           doctor.getPhone()},
                {"Email",           doctor.getEmail()},
                {"",                ""},
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

    // ── Today's Schedule panel ───────────────────────────────────────────

    private JPanel buildTodayPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PAGE);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel h = sectionTitle("Today's Schedule");
        h.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(h, BorderLayout.NORTH);

        String[] cols = {"Appt. ID", "Patient Name", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<Appointment> todayList = system
                .getTodayAppointmentsForDoctor(doctor.getDoctorId());

        if (todayList.isEmpty()) {
            // Show a friendly empty state
            JLabel empty = new JLabel(
                    "No appointments scheduled for today.",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(TEXT_MUTED);
            p.add(empty, BorderLayout.CENTER);
        } else {
            for (Appointment a : todayList) {
                model.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getPatientName(),
                        a.getTime(),
                        a.getStatus()
                });
            }
            JTable table = buildStyledTable(model);
            JScrollPane scroll = new JScrollPane(table);
            styleScrollPane(scroll);
            p.add(scroll, BorderLayout.CENTER);
        }
        return p;
    }

    // ── All Appointments panel ───────────────────────────────────────────

    private JPanel buildAllApptsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PAGE);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel h = sectionTitle("All Appointments");
        h.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(h, BorderLayout.NORTH);

        String[] cols = {"Appt. ID", "Patient", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (Appointment a : system
                .getAppointmentsForDoctor(doctor.getDoctorId())) {
            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getPatientName(),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
            });
        }

        JTable table = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Patient List panel ───────────────────────────────────────────────

    private JPanel buildPatientListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PAGE);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Top row — title on left, search bar on right
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.setBorder(new EmptyBorder(0, 0, 16, 0));

        topRow.add(sectionTitle("Patient List"), BorderLayout.WEST);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        JTextField searchField = buildInputField("Search by name, ID or phone...");
        searchField.setMaximumSize(new Dimension(280, 40));
        searchField.setPreferredSize(new Dimension(280, 40));

        JButton searchBtn = buildTealButton("Search");
        searchBtn.setPreferredSize(new Dimension(100, 40));
        searchBtn.setMaximumSize(new Dimension(100, 40));

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchBtn,   BorderLayout.EAST);
        topRow.add(searchRow,      BorderLayout.EAST);
        p.add(topRow,              BorderLayout.NORTH);

        // Table
        String[] cols = {
                "Patient ID", "Name", "Age", "Gender",
                "Phone", "Bill (Rs.)", "Bill Paid?"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Helper to populate table
        Runnable populate = () -> {
            model.setRowCount(0); // clear existing rows
            String query = searchField.getText().trim();

            // If search field still shows placeholder, get all patients
            boolean isPlaceholder = query.equals(
                    "Search by name, ID or phone...");
            List<Patient> list = (query.isEmpty() || isPlaceholder)
                    ? system.getAllPatients()
                    : system.searchPatient(query);

            for (Patient pt : list) {
                model.addRow(new Object[]{
                        pt.getPatientId(),
                        pt.getName(),
                        pt.getAge(),
                        pt.getGender(),
                        pt.getPhone(),
                        String.format("%.2f", pt.getBillAmount()),
                        pt.isBillPaid() ? "Yes" : "No"
                });
            }
        };

        populate.run(); // load all patients on first open

        // Search triggers
        searchBtn.addActionListener(e -> populate.run());
        searchField.addActionListener(e -> populate.run()); // Enter key

        JTable table = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Update Prescription panel ────────────────────────────────────────

    private JPanel buildPrescribePanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        p.add(sectionTitle("Update Patient Prescription"));
        p.add(Box.createVerticalStrut(18));

        JPanel form = buildWhiteCard();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(560, 500));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields
        JTextField patientIdField = buildInputField("Patient ID  (e.g. P001)");

        JTextArea symptomsArea = buildTextArea("Symptoms");
        JTextArea medicineArea = buildTextArea("Prescribed medicine");

        JTextField durationField = buildInputField("Treatment duration in days  (e.g. 30)");
        JTextField billField     = buildInputField("Bill amount  (e.g. 4500)");
        JTextField noteField     = buildInputField("History note  (e.g. Follow-up after ECG)");

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = buildTealButton("Save Prescription");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        formRow(form, "Patient ID",            patientIdField);
        formRow(form, "Symptoms",              wrapScroll(symptomsArea));
        formRow(form, "Prescribed Medicine",   wrapScroll(medicineArea));
        formRow(form, "Duration (days)",       durationField);
        formRow(form, "Bill Amount (Rs.)",     billField);
        formRow(form, "History Note",          noteField);
        form.add(Box.createVerticalStrut(6));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(saveBtn);

        p.add(form);

        saveBtn.addActionListener(e -> {
            // Validate duration is a number
            int duration;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
            } catch (NumberFormatException ex) {
                errLabel.setForeground(DANGER);
                errLabel.setText("Duration must be a whole number.");
                return;
            }

            // Validate bill is a number
            double bill;
            try {
                bill = Double.parseDouble(billField.getText().trim());
            } catch (NumberFormatException ex) {
                errLabel.setForeground(DANGER);
                errLabel.setText("Bill amount must be a number.");
                return;
            }

            String err = system.updatePrescription(
                    patientIdField.getText().trim(),
                    symptomsArea.getText().trim(),
                    medicineArea.getText().trim(),
                    duration,
                    bill,
                    noteField.getText().trim());

            if (err != null) {
                errLabel.setForeground(DANGER);
                errLabel.setText(err);
            } else {
                errLabel.setForeground(TEAL_DARK);
                errLabel.setText("Prescription saved successfully!");
                // Clear all fields after save
                patientIdField.setText("Patient ID  (e.g. P001)");
                patientIdField.setForeground(TEXT_MUTED);
                symptomsArea.setText("");
                medicineArea.setText("");
                durationField.setText("Treatment duration in days  (e.g. 30)");
                durationField.setForeground(TEXT_MUTED);
                billField.setText("Bill amount  (e.g. 4500)");
                billField.setForeground(TEXT_MUTED);
                noteField.setText("History note  (e.g. Follow-up after ECG)");
                noteField.setForeground(TEXT_MUTED);
            }
        });

        return p;
    }

    // ── Shared helpers ───────────────────────────────────────────────────

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
        JLabel l = new JLabel(text == null || text.isEmpty() ? "—" : text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

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

    private JTextArea buildTextArea(String placeholder) {
        JTextArea ta = new JTextArea(3, 20);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta.setBackground(FIELD_BG);
        ta.setForeground(TEXT_MUTED);
        ta.setBorder(new EmptyBorder(10, 14, 10, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setText(placeholder);
        ta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (ta.getText().equals(placeholder)) {
                    ta.setText("");
                    ta.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (ta.getText().isEmpty()) {
                    ta.setText(placeholder);
                    ta.setForeground(TEXT_MUTED);
                }
            }
        });
        return ta;
    }

    // Wraps a JTextArea in a scroll pane for use in forms
    private JScrollPane wrapScroll(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(
                new Color(0xB2, 0xD8, 0xCE), 1));
        sp.getViewport().setBackground(FIELD_BG);
        return sp;
    }

    private void formRow(JPanel p, String label, JComponent field) {
        JLabel l = mutedLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(12));
    }

    private void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(
                new Color(0xB2, 0xD8, 0xCE), 1));
        sp.getViewport().setBackground(WHITE);
    }

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
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(TEAL);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(
                BorderFactory.createEmptyBorder());
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

} // end of DoctorDashboard
