package medivault.view;

import medivault.db.DatabaseManager;
import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * Full Admin Dashboard — four tabs:
 *   1. Overview   – live KPI cards + appointment status bar
 *   2. Patients   – table with add / delete / search
 *   3. Doctors    – table with add / delete
 *   4. Appointments – view all, hard-delete
 */
public class AdminDashboard extends JFrame {

    // ── Palette ──────────────────────────────────────────────────
    private static final Color BG        = new Color(15, 23, 42);
    private static final Color SIDEBAR   = new Color(11, 18, 35);
    private static final Color CARD      = new Color(22, 35, 60);
    private static final Color ACCENT    = new Color(20, 184, 166);
    private static final Color ACCENT2   = new Color(99, 102, 241);   // indigo
    private static final Color ACCENT3   = new Color(245, 158, 11);   // amber
    private static final Color ACCENT4   = new Color(239, 68, 68);    // red
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color ROW_ALT   = new Color(18, 29, 51);
    private static final Color SEL       = new Color(20, 184, 166, 60);

    private final DatabaseManager db = DatabaseManager.getInstance();

    // Tables
    private JTable patientTable, doctorTable, appointmentTable;
    private DefaultTableModel patientModel, doctorModel, appointmentModel;

    // Overview labels (updated on refresh)
    private JLabel lblTotalPatients, lblTotalDoctors,
                   lblTotalAppointments, lblTotalRevenue;
    private JLabel lblScheduled, lblCompleted, lblCancelled;

    public AdminDashboard() {
        setTitle("MediVault — Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setBackground(BG);
        initUI();
        refreshAll();
    }

    // ── Layout ───────────────────────────────────────────────────

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ──────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                ACCENT.darker()));

        sb.add(Box.createVerticalStrut(24));

        // Logo
        JLabel logo = new JLabel("  ⚕ MediVault");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(logo);

        JLabel role = new JLabel("  Administrator");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        role.setForeground(MUTED);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(role);

        sb.add(Box.createVerticalStrut(30));
        sb.add(sidebarDivider("NAVIGATION"));

        String[] labels = {"📊  Overview", "🧑‍⚕  Patients",
                           "👨‍⚕  Doctors", "📅  Appointments"};

        JTabbedPane tabPane = (JTabbedPane) ((JPanel)
                getContentPane()).getComponent(1); // not yet available

        // We store references to be triggered by sidebar buttons
        // Sidebar will toggle tabs via shared JTabbedPane reference after build
        // So we return the panel and wire later.

        sb.putClientProperty("navLabels", labels);

        sb.add(Box.createVerticalGlue());

        // Logout
        JButton logout = sidebarButton("🚪  Logout", ACCENT4);
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) dispose();
        });
        sb.add(logout);
        sb.add(Box.createVerticalStrut(16));

        return sb;
    }

    // ── Content (tabbed) ─────────────────────────────────────────

    private JTabbedPane buildContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setTabPlacement(JTabbedPane.TOP);

        // Minimal tab UI
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight          = BG;
                lightHighlight     = BG;
                shadow             = CARD;
                darkShadow         = BG;
                focus              = ACCENT;
            }
        });

        tabs.addTab("📊  Overview",     buildOverviewTab());
        tabs.addTab("🧑‍⚕  Patients",    buildPatientsTab());
        tabs.addTab("👨‍⚕  Doctors",     buildDoctorsTab());
        tabs.addTab("📅  Appointments", buildAppointmentsTab());

        // Wire sidebar nav buttons now that tabs exist
        wireSidebarNav(tabs);
        return tabs;
    }

    private void wireSidebarNav(JTabbedPane tabs) {
        JPanel sidebar = (JPanel) getContentPane().getComponent(0);
        sidebar.removeAll();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        sidebar.add(Box.createVerticalStrut(24));

        JLabel logo = new JLabel("  ⚕ MediVault");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        JLabel role = new JLabel("  Administrator");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        role.setForeground(MUTED);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(role);

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(sidebarDivider("NAVIGATION"));

        String[] labels = {"📊  Overview", "🧑‍⚕  Patients",
                           "👨‍⚕  Doctors", "📅  Appointments"};
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = sidebarNavButton(labels[i]);
            btn.addActionListener(e -> tabs.setSelectedIndex(idx));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = sidebarButton("🚪  Logout", ACCENT4);
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Logout from admin?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) dispose();
        });
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.revalidate();
    }

    // ── Tab 1: Overview ──────────────────────────────────────────

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JLabel header = new JLabel("System Overview");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(TEXT);
        panel.add(header, BorderLayout.NORTH);

        // KPI cards row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setBackground(BG);

        lblTotalPatients     = new JLabel("—");
        lblTotalDoctors      = new JLabel("—");
        lblTotalAppointments = new JLabel("—");
        lblTotalRevenue      = new JLabel("—");

        kpiRow.add(kpiCard("Total Patients",     lblTotalPatients,  "🧑‍⚕", ACCENT));
        kpiRow.add(kpiCard("Total Doctors",      lblTotalDoctors,   "👨‍⚕", ACCENT2));
        kpiRow.add(kpiCard("Appointments",       lblTotalAppointments, "📅", ACCENT3));
        kpiRow.add(kpiCard("Total Revenue (₹)",  lblTotalRevenue,   "💰", ACCENT4));

        // Status breakdown
        JPanel statusCard = new JPanel();
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setBackground(CARD);
        statusCard.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                new EmptyBorder(20, 24, 20, 24)));

        JLabel sTitle = new JLabel("Appointment Status Breakdown");
        sTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sTitle.setForeground(TEXT);
        statusCard.add(sTitle);
        statusCard.add(Box.createVerticalStrut(16));

        lblScheduled = new JLabel("Scheduled: —");
        lblCompleted = new JLabel("Completed: —");
        lblCancelled = new JLabel("Cancelled: —");

        for (JLabel lbl : new JLabel[]{lblScheduled, lblCompleted, lblCancelled}) {
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(TEXT);
            statusCard.add(lbl);
            statusCard.add(Box.createVerticalStrut(8));
        }

        // Refresh button
        JButton refresh = accentButton("↻  Refresh Overview", ACCENT);
        refresh.setAlignmentX(Component.LEFT_ALIGNMENT);
        refresh.addActionListener(e -> refreshAll());

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(BG);
        center.add(kpiRow, BorderLayout.NORTH);
        center.add(statusCard, BorderLayout.CENTER);
        center.add(refresh, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ── Tab 2: Patients ──────────────────────────────────────────

    private JPanel buildPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);

        JTextField searchField = darkField("Search by name / ID / phone…", 220);
        JButton searchBtn = accentButton("Search", ACCENT);
        JButton addBtn    = accentButton("+ Add Patient", ACCENT2);
        JButton deleteBtn = accentButton("🗑 Delete", ACCENT4);
        JButton refreshBtn = accentButton("↻", MUTED);

        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        // Table
        String[] cols = {"ID", "Name", "Age", "Gender", "Phone", "Email", "Bill (₹)"};
        patientModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        patientTable = styledTable(patientModel);

        JScrollPane scroll = darkScroll(patientTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        // ── Actions ──
        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            List<Patient> results = q.isEmpty()
                    ? db.getAllPatients() : db.searchPatients(q);
            populatePatientTable(results);
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchBtn.doClick();
            }
        });

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            populatePatientTable(db.getAllPatients());
        });

        addBtn.addActionListener(e -> showAddPatientDialog());

        deleteBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row < 0) { toast("Select a patient to delete."); return; }
            String id   = (String) patientModel.getValueAt(row, 0);
            String name = (String) patientModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete patient " + name + " (" + id + ") and all their appointments?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                db.deletePatient(id);
                populatePatientTable(db.getAllPatients());
                refreshOverview();
                toast("Patient deleted.");
            }
        });

        return panel;
    }

    // ── Tab 3: Doctors ───────────────────────────────────────────

    private JPanel buildDoctorsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);

        JButton addBtn    = accentButton("+ Add Doctor", ACCENT2);
        JButton deleteBtn = accentButton("🗑 Delete", ACCENT4);
        JButton refreshBtn = accentButton("↻ Refresh", MUTED);
        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        String[] cols = {"ID", "Name", "Specialisation", "Phone", "Email"};
        doctorModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        doctorTable = styledTable(doctorModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(darkScroll(doctorTable), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> populateDoctorTable(db.getAllDoctors()));

        addBtn.addActionListener(e -> showAddDoctorDialog());

        deleteBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row < 0) { toast("Select a doctor to delete."); return; }
            String id   = (String) doctorModel.getValueAt(row, 0);
            String name = (String) doctorModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete doctor " + name + " (" + id + ")?\n"
                  + "All their appointments will also be removed.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                db.deleteDoctor(id);
                populateDoctorTable(db.getAllDoctors());
                populateAppointmentTable(db.getAllAppointments());
                refreshOverview();
                toast("Doctor deleted.");
            }
        });

        return panel;
    }

    // ── Tab 4: Appointments ──────────────────────────────────────

    private JPanel buildAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);

        JButton deleteBtn  = accentButton("🗑 Delete", ACCENT4);
        JButton refreshBtn = accentButton("↻ Refresh", MUTED);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        String[] cols = {"Appt ID", "Patient ID", "Doctor ID", "Date/Time", "Status"};
        appointmentModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appointmentTable = styledTable(appointmentModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(darkScroll(appointmentTable), BorderLayout.CENTER);

        refreshBtn.addActionListener(e ->
                populateAppointmentTable(db.getAllAppointments()));

        deleteBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row < 0) { toast("Select an appointment to delete."); return; }
            String id = (String) appointmentModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Permanently delete appointment " + id + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                db.deleteAppointment(id);
                populateAppointmentTable(db.getAllAppointments());
                refreshOverview();
                toast("Appointment deleted.");
            }
        });

        return panel;
    }

    // ── Dialogs ──────────────────────────────────────────────────

    private void showAddPatientDialog() {
        JDialog dlg = styledDialog("Add New Patient", 480, 500);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField fName   = darkField("Full name", 180);
        JTextField fAge    = darkField("Age", 180);
        JTextField fGender = darkField("Male / Female / Other", 180);
        JTextField fPhone  = darkField("Phone", 180);
        JTextField fEmail  = darkField("Email", 180);
        JPasswordField fPass = new JPasswordField();
        styleField(fPass);
        JTextField fBill  = darkField("0.00", 180);

        addRow(form, "Name*",     fName);
        addRow(form, "Age*",      fAge);
        addRow(form, "Gender*",   fGender);
        addRow(form, "Phone*",    fPhone);
        addRow(form, "Email*",    fEmail);
        addRow(form, "Password*", fPass);
        addRow(form, "Bill (₹)",  fBill);

        JButton save = accentButton("Save Patient", ACCENT);
        save.addActionListener(e -> {
            try {
                String id  = db.generatePatientId();
                String name   = fName.getText().trim();
                int    age    = Integer.parseInt(fAge.getText().trim());
                String gender = fGender.getText().trim();
                String phone  = fPhone.getText().trim();
                String email  = fEmail.getText().trim();
                String pass   = new String(fPass.getPassword());
                double bill   = fBill.getText().trim().isEmpty() ? 0
                        : Double.parseDouble(fBill.getText().trim());

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    toast("Please fill all required fields."); return;
                }
                Patient p = new Patient(id, name, age, gender, phone, email, pass);
                p.setBillAmount(bill);
                db.addPatient(p);
                populatePatientTable(db.getAllPatients());
                refreshOverview();
                dlg.dispose();
                toast("Patient added: " + id);
            } catch (NumberFormatException ex) {
                toast("Age and Bill must be valid numbers.");
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(save, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAddDoctorDialog() {
        JDialog dlg = styledDialog("Add New Doctor", 480, 380);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField fName  = darkField("Full name", 180);
        JTextField fSpec  = darkField("Specialisation", 180);
        JTextField fPhone = darkField("Phone", 180);
        JTextField fEmail = darkField("Email", 180);
        JPasswordField fPass = new JPasswordField();
        styleField(fPass);

        addRow(form, "Name*",           fName);
        addRow(form, "Specialisation*", fSpec);
        addRow(form, "Phone*",          fPhone);
        addRow(form, "Email*",          fEmail);
        addRow(form, "Password*",       fPass);

        JButton save = accentButton("Save Doctor", ACCENT);
        save.addActionListener(e -> {
            String id    = db.generateDoctorId();
            String name  = fName.getText().trim();
            String spec  = fSpec.getText().trim();
            String phone = fPhone.getText().trim();
            String email = fEmail.getText().trim();
            String pass  = new String(fPass.getPassword());

            if (name.isEmpty() || spec.isEmpty() || phone.isEmpty()
                    || email.isEmpty() || pass.isEmpty()) {
                toast("Please fill all required fields."); return;
            }
            Doctor d = new Doctor(id, name, spec, phone, email, pass);
            if (db.addDoctor(d)) {
                populateDoctorTable(db.getAllDoctors());
                refreshOverview();
                dlg.dispose();
                toast("Doctor added: " + id);
            } else {
                toast("Failed to add doctor (ID conflict).");
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(save, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Data Refresh ─────────────────────────────────────────────

    private void refreshAll() {
        populatePatientTable(db.getAllPatients());
        populateDoctorTable(db.getAllDoctors());
        populateAppointmentTable(db.getAllAppointments());
        refreshOverview();
    }

    private void refreshOverview() {
        int pCount = db.getAllPatients().size();
        int dCount = db.getAllDoctors().size();
        int aCount = db.getAllAppointments().size();
        double rev = db.getTotalRevenue();

        lblTotalPatients.setText(String.valueOf(pCount));
        lblTotalDoctors.setText(String.valueOf(dCount));
        lblTotalAppointments.setText(String.valueOf(aCount));
        lblTotalRevenue.setText(String.format("₹ %,.2f", rev));

        Map<String, Integer> statusMap = db.getAppointmentStatusCounts();
        lblScheduled.setText("📌  Scheduled:  " + statusMap.getOrDefault("Scheduled", 0));
        lblCompleted.setText("✅  Completed:  " + statusMap.getOrDefault("Completed", 0));
        lblCancelled.setText("❌  Cancelled:  " + statusMap.getOrDefault("Cancelled", 0));
    }

    private void populatePatientTable(List<Patient> list) {
        patientModel.setRowCount(0);
        for (Patient p : list) {
            patientModel.addRow(new Object[]{
                p.getPatientId(), p.getName(), p.getAge(),
                p.getGender(), p.getPhone(), p.getEmail(),
                String.format("%.2f", p.getBillAmount())
            });
        }
    }

    private void populateDoctorTable(List<Doctor> list) {
        doctorModel.setRowCount(0);
        for (Doctor d : list) {
            doctorModel.addRow(new Object[]{
                d.getDoctorId(), d.getName(),
                d.getSpecialization(), d.getPhone(), d.getEmail()
            });
        }
    }

    private void populateAppointmentTable(List<Appointment> list) {
        appointmentModel.setRowCount(0);
        for (Appointment a : list) {
            appointmentModel.addRow(new Object[]{
                a.getAppointmentId(), a.getPatientId(),
                a.getDoctorId(), a.getDateTimeKey(),
                a.getStatus().toString()
            });
        }
    }

    // ── UI Helpers ───────────────────────────────────────────────

    private JPanel kpiCard(String title, JLabel valueLabel, String icon, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        ic.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(ic);
        card.add(Box.createVerticalStrut(10));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);

        return card;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(SEL);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD : ROW_ALT);
                }
                c.setForeground(TEXT);
                return c;
            }
        };
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(30, 46, 78));
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(SEL);
        table.setSelectionForeground(TEXT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(SIDEBAR);
        header.setForeground(ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT.darker()));

        return table;
    }

    private JScrollPane darkScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(CARD);
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));
        return sp;
    }

    private JButton accentButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JButton sidebarNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SIDEBAR);
        btn.setForeground(TEXT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(CARD); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(SIDEBAR); }
        });
        return btn;
    }

    private JButton sidebarButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(SIDEBAR);
        btn.setForeground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return btn;
    }

    private JPanel sidebarDivider(String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(SIDEBAR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(MUTED);
        lbl.setBorder(new EmptyBorder(0, 14, 0, 0));
        p.add(lbl);
        return p;
    }

    private JTextField darkField(String placeholder, int width) {
        JTextField tf = new JTextField(placeholder);
        tf.setForeground(MUTED);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText(""); tf.setForeground(TEXT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder); tf.setForeground(MUTED);
                }
            }
        });
        styleField(tf);
        tf.setPreferredSize(new Dimension(width, 36));
        return tf;
    }

    private void styleField(JComponent field) {
        field.setBackground(new Color(30, 46, 78));
        field.setForeground(TEXT);
        if (field instanceof JTextField) ((JTextField)field).setCaretColor(ACCENT);
        if (field instanceof JPasswordField) ((JPasswordField)field).setCaretColor(ACCENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                new EmptyBorder(4, 8, 4, 8)));
    }

    private JDialog styledDialog(String title, int w, int h) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);
        return dlg;
    }

    private void addRow(JPanel form, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);
        form.add(lbl);
        form.add(field);
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "MediVault Admin",
                JOptionPane.INFORMATION_MESSAGE);
    }
}