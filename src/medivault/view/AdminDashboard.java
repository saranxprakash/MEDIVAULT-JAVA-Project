package medivault.view;

import medivault.controller.AdminController;
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
 * AdminDashboard — five tabs driven entirely by AdminController.
 *  1. Overview      — live KPI cards + appointment status breakdown
 *  2. Patients      — full CRUD (add / edit / delete / search)
 *  3. Doctors       — full CRUD (add / edit / delete)
 *  4. Appointments  — view all, update status, delete
 *  5. Billing       — view/edit per-patient bill, mark paid, summary report
 */
public class AdminDashboard extends JFrame {

    // ── Palette (teal-light, matches LoginFrame) ─────────────────
    private static final Color TEAL        = new Color(0x3D, 0xB8, 0x9E);
    private static final Color TEAL_DARK   = new Color(0x2A, 0x9D, 0x87);
    private static final Color BG          = new Color(0xF4, 0xF6, 0xF9);
    private static final Color SIDEBAR_BG  = new Color(0x2A, 0x9D, 0x87);
    private static final Color SIDEBAR_DARK= new Color(0x1F, 0x7A, 0x68);
    private static final Color WHITE       = Color.WHITE;
    private static final Color TEXT_DARK   = new Color(0x2D, 0x3A, 0x3A);
    private static final Color TEXT_MUTED  = new Color(0x6B, 0x72, 0x80);
    private static final Color CARD_BG     = WHITE;
    private static final Color ROW_ALT     = new Color(0xF0, 0xFA, 0xF7);
    private static final Color SEL_BG      = new Color(0x3D, 0xB8, 0x9E, 55);
    private static final Color DANGER      = new Color(0xE5, 0x39, 0x35);
    private static final Color WARNING     = new Color(0xF5, 0x7C, 0x00);
    private static final Color SUCCESS     = new Color(0x2E, 0x7D, 0x32);
    private static final Color INDIGO      = new Color(0x5C, 0x6B, 0xC0);
    private static final Color ERR_COLOR   = new Color(0xE5, 0x39, 0x35);

    private final AdminController ctrl = new AdminController();

    // Tables
    private JTable patientTable, doctorTable, appointmentTable, billingTable;
    private DefaultTableModel patientModel, doctorModel, appointmentModel, billingModel;

    // Overview KPI labels
    private JLabel lblPatients, lblDoctors, lblAppts, lblRevenue;
    private JLabel lblScheduled, lblCompleted, lblCancelled;

    private JTabbedPane tabs;

    public AdminDashboard() {
        setTitle("MediVault — Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setBackground(BG);
        initUI();
        refreshAll();
    }

    // ═══════════════════════════════════════════════════════════════
    //  ROOT LAYOUT
    // ═══════════════════════════════════════════════════════════════

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        tabs = buildTabs();
        root.add(buildSidebar(tabs), BorderLayout.WEST);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════════

    private JPanel buildSidebar(JTabbedPane tabs) {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(230, 0));

        sb.add(Box.createVerticalStrut(28));

        JLabel logo = lbl("  ⚕  MediVault", new Font("Segoe UI", Font.BOLD, 19), WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(logo);

        JLabel role = lbl("  Administrator Panel",
                new Font("Segoe UI", Font.PLAIN, 11), new Color(255, 255, 255, 160));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(role);

        sb.add(Box.createVerticalStrut(28));
        sb.add(sidebarSection("MENU"));

        String[][] nav = {
            {"📊", "Overview"}, {"🧑", "Patients"},
            {"👨‍⚕", "Doctors"}, {"📅", "Appointments"}, {"💰", "Billing"}
        };
        for (int i = 0; i < nav.length; i++) {
            final int idx = i;
            JButton btn = sidebarNavBtn(nav[i][0] + "   " + nav[i][1]);
            btn.addActionListener(e -> tabs.setSelectedIndex(idx));
            sb.add(btn);
        }

        sb.add(Box.createVerticalGlue());
        sb.add(sidebarSection("ACCOUNT"));

        JButton logout = sidebarNavBtn("🚪   Logout");
        logout.setForeground(new Color(255, 160, 160));
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Logout from Admin Dashboard?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) dispose();
        });
        sb.add(logout);
        sb.add(Box.createVerticalStrut(20));
        return sb;
    }

    // ═══════════════════════════════════════════════════════════════
    //  TAB CONTAINER (hidden header — navigation via sidebar)
    // ═══════════════════════════════════════════════════════════════

    private JTabbedPane buildTabs() {
        JTabbedPane tp = new JTabbedPane();
        tp.setBackground(BG);
        tp.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabAreaHeight(int p, int c, int h) { return 0; }
            @Override protected void paintTabArea(Graphics g, int tp, int sel) {}
        });
        tp.addTab("Overview",     buildOverviewTab());
        tp.addTab("Patients",     buildPatientsTab());
        tp.addTab("Doctors",      buildDoctorsTab());
        tp.addTab("Appointments", buildAppointmentsTab());
        tp.addTab("Billing",      buildBillingTab());
        return tp;
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB 1 — OVERVIEW
    // ─────────────────────────────────────────────────────────────

    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));
        p.add(pageHeader("System Overview", "Live snapshot of your MediVault data"),
                BorderLayout.NORTH);

        lblPatients = new JLabel("—"); lblDoctors = new JLabel("—");
        lblAppts    = new JLabel("—"); lblRevenue  = new JLabel("—");

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setBackground(BG);
        kpiRow.add(kpiCard("Total Patients",    lblPatients, "🧑", TEAL));
        kpiRow.add(kpiCard("Total Doctors",     lblDoctors,  "👨‍⚕", INDIGO));
        kpiRow.add(kpiCard("Appointments",      lblAppts,    "📅", WARNING));
        kpiRow.add(kpiCard("Total Revenue (₹)", lblRevenue,  "💰", SUCCESS));

        lblScheduled = new JLabel("—"); lblCompleted = new JLabel("—");
        lblCancelled = new JLabel("—");

        JPanel statusCard = whiteCard();
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.add(lbl("Appointment Status Breakdown",
                new Font("Segoe UI", Font.BOLD, 14), TEXT_DARK));
        statusCard.add(Box.createVerticalStrut(14));
        statusCard.add(statusRow("📌  Scheduled", lblScheduled, INDIGO));
        statusCard.add(Box.createVerticalStrut(8));
        statusCard.add(statusRow("✅  Completed", lblCompleted, SUCCESS));
        statusCard.add(Box.createVerticalStrut(8));
        statusCard.add(statusRow("❌  Cancelled", lblCancelled, DANGER));
        statusCard.add(Box.createVerticalGlue());

        JButton refreshBtn = tealBtn("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshAll());

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(BG);
        center.add(kpiRow,     BorderLayout.NORTH);
        center.add(statusCard, BorderLayout.CENTER);
        center.add(refreshBtn, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel statusRow(String label, JLabel valueLabel, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(WHITE);
        row.add(lbl(label, new Font("Segoe UI", Font.PLAIN, 13), TEXT_MUTED), BorderLayout.WEST);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(color);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB 2 — PATIENTS (full CRUD)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildPatientsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        JTextField searchField = searchField("Search by name / ID / phone…");
        JButton searchBtn = tealBtn("Search");
        JButton addBtn    = tealBtn("+ Add");
        JButton editBtn   = outlineBtn("✏ Edit", INDIGO);
        JButton deleteBtn = outlineBtn("🗑 Delete", DANGER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);
        toolbar.add(searchField); toolbar.add(searchBtn);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);

        String[] cols = {"ID","Name","Age","Gender","Phone","Email","Bill (₹)","Paid"};
        patientModel  = tableModel(cols);
        patientTable  = styledTable(patientModel);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(BG);
        wrapper.add(pageHeader("Patient Management", "Add, edit, delete and search patients"), BorderLayout.NORTH);
        wrapper.add(toolbar, BorderLayout.CENTER);  // will get pushed by scroll
        // rebuild with correct layout
        wrapper.removeAll();
        wrapper.setLayout(new BorderLayout(0, 10));

        JPanel top2 = new JPanel(new BorderLayout(0, 8));
        top2.setBackground(BG);
        top2.add(pageHeader("Patient Management","Add, edit, delete and search patients"), BorderLayout.NORTH);
        top2.add(toolbar, BorderLayout.SOUTH);

        wrapper.add(top2, BorderLayout.NORTH);
        wrapper.add(darkScroll(patientTable), BorderLayout.CENTER);
        page.add(wrapper, BorderLayout.CENTER);

        // ── Actions ──
        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            boolean ph = q.equals("Search by name / ID / phone…") || q.isEmpty();
            populatePatients(ph ? ctrl.getAllPatients() : ctrl.searchPatients(q));
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchBtn.doClick();
            }
        });
        addBtn.addActionListener(e -> showAddPatientDialog());
        editBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row < 0) { info("Select a patient to edit."); return; }
            showEditPatientDialog(ctrl.getPatient((String) patientModel.getValueAt(row, 0)));
        });
        deleteBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row < 0) { info("Select a patient to delete."); return; }
            String id   = (String) patientModel.getValueAt(row, 0);
            String name = (String) patientModel.getValueAt(row, 1);
            if (confirmDel("patient " + name + " (" + id + ") and their appointments")) {
                String err = ctrl.deletePatient(id);
                if (err != null) { err(err); return; }
                refreshAll(); info("Patient deleted.");
            }
        });
        return page;
    }

    private void showAddPatientDialog() {
        JDialog dlg = dlg("Add New Patient", 500, 520);
        JPanel form = formPanel();

        JTextField fName  = tf(); JTextField fAge   = tf();
        JTextField fPhone = tf(); JTextField fEmail = tf();
        JPasswordField fPass = pf();
        JComboBox<String> fGender = combo("Male","Female","Other");
        JTextField fBill = tf("0.00");

        row(form,"Full Name*",      fName);
        row(form,"Age*",            fAge);
        row(form,"Gender",          fGender);
        row(form,"Phone*",          fPhone);
        row(form,"Email*",          fEmail);
        row(form,"Password*",       fPass);
        row(form,"Bill Amount (₹)", fBill);

        JLabel errLbl = errLbl(); JButton save = tealBtn("Save Patient");
        save.addActionListener(e -> {
            try {
                String err = ctrl.addPatient(fName.getText().trim(),
                        Integer.parseInt(fAge.getText().trim()),
                        (String) fGender.getSelectedItem(),
                        fPhone.getText().trim(), fEmail.getText().trim(),
                        new String(fPass.getPassword()),
                        Double.parseDouble(fBill.getText().trim()));
                if (err != null) { errLbl.setText(err); return; }
                refreshAll(); dlg.dispose(); info("Patient added.");
            } catch (NumberFormatException ex) { errLbl.setText("Age and Bill must be numbers."); }
        });
        assembleDlg(dlg, form, errLbl, save);
        dlg.setVisible(true);
    }

    private void showEditPatientDialog(Patient p) {
        if (p == null) { err("Patient data not found."); return; }
        JDialog dlg = dlg("Edit Patient — " + p.getPatientId(), 520, 660);
        JPanel form = formPanel();

        JTextField fName  = tf(p.getName());
        JTextField fAge   = tf(String.valueOf(p.getAge()));
        JComboBox<String> fGender = combo("Male","Female","Other");
        fGender.setSelectedItem(p.getGender());
        JTextField fPhone    = tf(p.getPhone());
        JTextField fEmail    = tf(p.getEmail());
        JTextField fSymptoms = tf(nv(p.getSymptoms()));
        JTextField fMed      = tf(nv(p.getPrescribedMedicine()));
        JTextField fDays     = tf(String.valueOf(p.getTreatmentDurationDays()));
        JTextField fBill     = tf(String.format("%.2f", p.getBillAmount()));
        JCheckBox  fPaid     = new JCheckBox("Bill Paid");
        fPaid.setSelected(p.isBillPaid());
        fPaid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fPaid.setBackground(WHITE);

        row(form,"Name*",           fName);   row(form,"Age*",      fAge);
        row(form,"Gender",          fGender); row(form,"Phone*",    fPhone);
        row(form,"Email*",          fEmail);  row(form,"Symptoms",  fSymptoms);
        row(form,"Medicine",        fMed);    row(form,"Tx Days",   fDays);
        row(form,"Bill Amount (₹)", fBill);   row(form,"",          fPaid);

        JLabel errLbl = errLbl(); JButton save = tealBtn("Save Changes");
        save.addActionListener(e -> {
            try {
                String err = ctrl.updatePatient(
                        p.getPatientId(), fName.getText().trim(),
                        Integer.parseInt(fAge.getText().trim()),
                        (String) fGender.getSelectedItem(),
                        fPhone.getText().trim(), fEmail.getText().trim(),
                        fSymptoms.getText().trim(), fMed.getText().trim(),
                        fDays.getText().trim().isEmpty() ? 0
                                : Integer.parseInt(fDays.getText().trim()),
                        Double.parseDouble(fBill.getText().trim()),
                        fPaid.isSelected());
                if (err != null) { errLbl.setText(err); return; }
                refreshAll(); dlg.dispose(); info("Patient updated.");
            } catch (NumberFormatException ex) { errLbl.setText("Age, Days and Bill must be numbers."); }
        });
        assembleDlg(dlg, form, errLbl, save);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB 3 — DOCTORS (full CRUD)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildDoctorsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        JButton addBtn    = tealBtn("+ Add");
        JButton editBtn   = outlineBtn("✏ Edit", INDIGO);
        JButton deleteBtn = outlineBtn("🗑 Delete", DANGER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);

        String[] cols = {"ID","Name","Specialisation","Phone","Email"};
        doctorModel = tableModel(cols);
        doctorTable = styledTable(doctorModel);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(BG);
        top.add(pageHeader("Doctor Management","Add, update and remove doctors"), BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        page.add(top, BorderLayout.NORTH);
        page.add(darkScroll(doctorTable), BorderLayout.CENTER);

        addBtn.addActionListener(e -> showAddDoctorDialog());
        editBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row < 0) { info("Select a doctor to edit."); return; }
            showEditDoctorDialog(ctrl.getDoctor((String) doctorModel.getValueAt(row, 0)));
        });
        deleteBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row < 0) { info("Select a doctor to delete."); return; }
            String id   = (String) doctorModel.getValueAt(row, 0);
            String name = (String) doctorModel.getValueAt(row, 1);
            if (confirmDel("doctor " + name + " (" + id + ") and their appointments")) {
                String er = ctrl.deleteDoctor(id);
                if (er != null) { err(er); return; }
                refreshAll(); info("Doctor deleted.");
            }
        });
        return page;
    }

    private void showAddDoctorDialog() {
        JDialog dlg = dlg("Add New Doctor", 480, 420);
        JPanel form = formPanel();

        JTextField fName = tf(); JTextField fSpec  = tf();
        JTextField fPhone = tf(); JTextField fEmail = tf();
        JPasswordField fPass = pf();

        row(form,"Full Name*",      fName); row(form,"Specialisation*", fSpec);
        row(form,"Phone*",          fPhone); row(form,"Email*",          fEmail);
        row(form,"Password*",       fPass);

        JLabel errLbl = errLbl(); JButton save = tealBtn("Save Doctor");
        save.addActionListener(e -> {
            String er = ctrl.addDoctor(fName.getText().trim(), fSpec.getText().trim(),
                    fPhone.getText().trim(), fEmail.getText().trim(),
                    new String(fPass.getPassword()));
            if (er != null) { errLbl.setText(er); return; }
            refreshAll(); dlg.dispose(); info("Doctor added.");
        });
        assembleDlg(dlg, form, errLbl, save);
        dlg.setVisible(true);
    }

    private void showEditDoctorDialog(Doctor d) {
        if (d == null) { err("Doctor not found."); return; }
        JDialog dlg = dlg("Edit Doctor — " + d.getDoctorId(), 480, 380);
        JPanel form = formPanel();

        JTextField fName  = tf(d.getName());
        JTextField fSpec  = tf(d.getSpecialization());
        JTextField fPhone = tf(d.getPhone());
        JTextField fEmail = tf(d.getEmail());

        row(form,"Name*",           fName); row(form,"Specialisation*", fSpec);
        row(form,"Phone*",          fPhone); row(form,"Email*",          fEmail);

        JLabel errLbl = errLbl(); JButton save = tealBtn("Save Changes");
        save.addActionListener(e -> {
            String er = ctrl.updateDoctor(d.getDoctorId(), fName.getText().trim(),
                    fSpec.getText().trim(), fPhone.getText().trim(), fEmail.getText().trim());
            if (er != null) { errLbl.setText(er); return; }
            refreshAll(); dlg.dispose(); info("Doctor updated.");
        });
        assembleDlg(dlg, form, errLbl, save);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB 4 — APPOINTMENTS
    // ─────────────────────────────────────────────────────────────

    private JPanel buildAppointmentsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        JButton statusBtn  = outlineBtn("✏ Change Status", INDIGO);
        JButton deleteBtn  = outlineBtn("🗑 Delete", DANGER);
        JButton refreshBtn = tealBtn("↻ Refresh");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);
        toolbar.add(statusBtn); toolbar.add(deleteBtn); toolbar.add(refreshBtn);

        String[] cols = {"Appt ID","Patient ID","Doctor ID","Date / Time","Status"};
        appointmentModel = tableModel(cols);
        appointmentTable = styledTable(appointmentModel);

        // Status colour renderer
        appointmentTable.getColumnModel().getColumn(4).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    String s = v == null ? "" : v.toString();
                    setForeground("Completed".equals(s) ? SUCCESS
                            : "Cancelled".equals(s) ? DANGER : INDIGO);
                    setBackground(sel ? SEL_BG : (r % 2 == 0 ? WHITE : ROW_ALT));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    return this;
                }
            });

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(BG);
        top.add(pageHeader("Appointment Management",
                "View, update status and delete appointments"), BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        page.add(top, BorderLayout.NORTH);
        page.add(darkScroll(appointmentTable), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> populateAppointments(ctrl.getAllAppointments()));

        statusBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row < 0) { info("Select an appointment."); return; }
            String id = (String) appointmentModel.getValueAt(row, 0);
            String[] opts = {"Scheduled","Completed","Cancelled"};
            String choice = (String) JOptionPane.showInputDialog(this,
                    "New status for " + id + ":", "Update Status",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (choice == null) return;
            String er = ctrl.updateAppointmentStatus(id, Appointment.Status.valueOf(choice));
            if (er != null) { err(er); return; }
            populateAppointments(ctrl.getAllAppointments());
            refreshOverview();
        });

        deleteBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row < 0) { info("Select an appointment."); return; }
            String id = (String) appointmentModel.getValueAt(row, 0);
            if (confirmDel("appointment " + id)) {
                String er = ctrl.deleteAppointment(id);
                if (er != null) { err(er); return; }
                populateAppointments(ctrl.getAllAppointments());
                refreshOverview(); info("Appointment deleted.");
            }
        });
        return page;
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB 5 — BILLING
    // ─────────────────────────────────────────────────────────────

    private JLabel billingLblTotal, billingLblPaid, billingLblUnpaid;

    private JPanel buildBillingTab() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Summary KPI cards
        billingLblTotal  = new JLabel("—");
        billingLblPaid   = new JLabel("—");
        billingLblUnpaid = new JLabel("—");

        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiRow.setBackground(BG);
        kpiRow.add(kpiCard("Total Revenue (₹)", billingLblTotal,  "💰", SUCCESS));
        kpiRow.add(kpiCard("Paid (₹)",          billingLblPaid,   "✅", TEAL));
        kpiRow.add(kpiCard("Unpaid (₹)",        billingLblUnpaid, "⏳", DANGER));

        JButton editBtn    = outlineBtn("✏ Edit Bill", INDIGO);
        JButton markPaid   = tealBtn("✅ Mark Paid");
        JButton reportBtn  = outlineBtn("📄 Report", WARNING);
        JButton refreshBtn = tealBtn("↻ Refresh");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);
        toolbar.add(editBtn); toolbar.add(markPaid);
        toolbar.add(reportBtn); toolbar.add(refreshBtn);

        String[] cols = {"Patient ID","Name","Bill (₹)","Paid","Symptoms","Medicine"};
        billingModel = tableModel(cols);
        billingTable = styledTable(billingModel);

        // Paid column colour
        billingTable.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setForeground("Yes".equals(v) ? SUCCESS : DANGER);
                    setBackground(sel ? SEL_BG : (r % 2 == 0 ? WHITE : ROW_ALT));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    return this;
                }
            });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setBackground(BG);
        top.add(pageHeader("Billing Management",
                "View, edit and generate billing reports"), BorderLayout.NORTH);
        top.add(kpiRow, BorderLayout.CENTER);
        top.add(toolbar, BorderLayout.SOUTH);

        page.add(top, BorderLayout.NORTH);
        page.add(darkScroll(billingTable), BorderLayout.CENTER);

        // Actions
        refreshBtn.addActionListener(e -> { populateBilling(); refreshBillingSummary(); });

        editBtn.addActionListener(e -> {
            int row = billingTable.getSelectedRow();
            if (row < 0) { info("Select a patient."); return; }
            showEditBillDialog(ctrl.getPatient((String) billingModel.getValueAt(row, 0)));
        });

        markPaid.addActionListener(e -> {
            int row = billingTable.getSelectedRow();
            if (row < 0) { info("Select a patient."); return; }
            Patient pat = ctrl.getPatient((String) billingModel.getValueAt(row, 0));
            if (pat == null) return;
            String er = ctrl.updateBilling(pat.getPatientId(), pat.getBillAmount(), true);
            if (er != null) { err(er); return; }
            populateBilling(); refreshBillingSummary(); info("Marked as paid.");
        });

        reportBtn.addActionListener(e -> showBillingReport());
        return page;
    }

    private void showEditBillDialog(Patient p) {
        if (p == null) { err("Patient not found."); return; }
        JDialog dlg = dlg("Edit Bill — " + p.getName(), 380, 250);
        JPanel form = formPanel();

        JTextField fBill = tf(String.format("%.2f", p.getBillAmount()));
        JCheckBox  fPaid = new JCheckBox("Mark as Paid");
        fPaid.setSelected(p.isBillPaid());
        fPaid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fPaid.setBackground(WHITE);

        row(form,"Bill Amount (₹)", fBill);
        row(form,"", fPaid);

        JLabel errLbl = errLbl(); JButton save = tealBtn("Save");
        save.addActionListener(e -> {
            try {
                String er = ctrl.updateBilling(p.getPatientId(),
                        Double.parseDouble(fBill.getText().trim()), fPaid.isSelected());
                if (er != null) { errLbl.setText(er); return; }
                populateBilling(); refreshBillingSummary(); refreshOverview();
                dlg.dispose(); info("Bill updated.");
            } catch (NumberFormatException ex) { errLbl.setText("Enter a valid number."); }
        });
        assembleDlg(dlg, form, errLbl, save);
        dlg.setVisible(true);
    }

    private void showBillingReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════════\n");
        sb.append("          MEDIVAULT  BILLING  REPORT          \n");
        sb.append("══════════════════════════════════════════════\n\n");
        sb.append(String.format("  Total Revenue :  ₹ %,.2f%n",  ctrl.getTotalRevenue()));
        sb.append(String.format("  Paid          :  ₹ %,.2f  (%d patients)%n",
                ctrl.getPaidRevenue(), ctrl.getPaidCount()));
        sb.append(String.format("  Unpaid        :  ₹ %,.2f  (%d patients)%n%n",
                ctrl.getUnpaidRevenue(), ctrl.getUnpaidCount()));
        sb.append("──────────────────────────────────────────────\n");
        sb.append(String.format("  %-8s  %-22s  %10s  %s%n",
                "ID","Name","Bill (₹)","Status"));
        sb.append("──────────────────────────────────────────────\n");
        for (Patient pat : ctrl.getPatientsSortedByBill()) {
            sb.append(String.format("  %-8s  %-22s  %10.2f  %s%n",
                    pat.getPatientId(), trunc(pat.getName(), 22),
                    pat.getBillAmount(), pat.isBillPaid() ? "PAID" : "UNPAID"));
        }
        sb.append("══════════════════════════════════════════════\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(0xF8, 0xFB, 0xF9));
        area.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(580, 440));

        JDialog d = new JDialog(this, "Billing Report", true);
        d.setLayout(new BorderLayout());
        d.add(sp, BorderLayout.CENTER);

        JButton close = tealBtn("Close");
        close.addActionListener(ev -> d.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(BG); btnRow.add(close);
        d.add(btnRow, BorderLayout.SOUTH);

        d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DATA REFRESH
    // ═══════════════════════════════════════════════════════════════

    private void refreshAll() {
        populatePatients(ctrl.getAllPatients());
        populateDoctors(ctrl.getAllDoctors());
        populateAppointments(ctrl.getAllAppointments());
        populateBilling();
        refreshOverview();
        refreshBillingSummary();
    }

    private void refreshOverview() {
        lblPatients.setText(String.valueOf(ctrl.getTotalPatients()));
        lblDoctors.setText(String.valueOf(ctrl.getTotalDoctors()));
        lblAppts.setText(String.valueOf(ctrl.getTotalAppointments()));
        lblRevenue.setText(String.format("₹ %,.2f", ctrl.getTotalRevenue()));
        Map<String, Integer> sc = ctrl.getAppointmentStatusCounts();
        lblScheduled.setText(String.valueOf(sc.getOrDefault("Scheduled", 0)));
        lblCompleted.setText(String.valueOf(sc.getOrDefault("Completed", 0)));
        lblCancelled.setText(String.valueOf(sc.getOrDefault("Cancelled", 0)));
    }

    private void refreshBillingSummary() {
        if (billingLblTotal == null) return;
        billingLblTotal.setText(String.format("₹ %,.2f",  ctrl.getTotalRevenue()));
        billingLblPaid.setText(String.format("₹ %,.2f",   ctrl.getPaidRevenue()));
        billingLblUnpaid.setText(String.format("₹ %,.2f", ctrl.getUnpaidRevenue()));
    }

    private void populatePatients(List<Patient> list) {
        patientModel.setRowCount(0);
        for (Patient p : list)
            patientModel.addRow(new Object[]{ p.getPatientId(), p.getName(),
                p.getAge(), p.getGender(), p.getPhone(), p.getEmail(),
                String.format("%.2f", p.getBillAmount()), p.isBillPaid() ? "Yes" : "No" });
    }

    private void populateDoctors(List<Doctor> list) {
        doctorModel.setRowCount(0);
        for (Doctor d : list)
            doctorModel.addRow(new Object[]{ d.getDoctorId(), d.getName(),
                d.getSpecialization(), d.getPhone(), d.getEmail() });
    }

    private void populateAppointments(List<Appointment> list) {
        appointmentModel.setRowCount(0);
        for (Appointment a : list)
            appointmentModel.addRow(new Object[]{ a.getAppointmentId(),
                a.getPatientId(), a.getDoctorId(),
                a.getDateTimeKey(), a.getStatus().toString() });
    }

    private void populateBilling() {
        billingModel.setRowCount(0);
        for (Patient p : ctrl.getPatientsSortedByBill())
            billingModel.addRow(new Object[]{ p.getPatientId(), p.getName(),
                String.format("%.2f", p.getBillAmount()),
                p.isBillPaid() ? "Yes" : "No",
                nv(p.getSymptoms()), nv(p.getPrescribedMedicine()) });
    }

    // ═══════════════════════════════════════════════════════════════
    //  UI HELPERS  (short names to reduce noise)
    // ═══════════════════════════════════════════════════════════════

    private JPanel pageHeader(String title, String sub) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG); p.setBorder(new EmptyBorder(0,0,8,0));
        p.add(lbl(title, new Font("Segoe UI",Font.BOLD,22), TEXT_DARK));
        p.add(Box.createVerticalStrut(2));
        p.add(lbl(sub,   new Font("Segoe UI",Font.PLAIN,13), TEXT_MUTED));
        return p;
    }

    private JPanel kpiCard(String title, JLabel val, String icon, Color accent) {
        JPanel c = whiteCard();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,4,0,0,accent),
                new EmptyBorder(18,18,18,18)));
        JLabel ic = lbl(icon, new Font("Segoe UI Emoji",Font.PLAIN,28), accent);
        ic.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setFont(new Font("Segoe UI",Font.BOLD,28));
        val.setForeground(accent); val.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = lbl(title, new Font("Segoe UI",Font.PLAIN,12), TEXT_MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(ic); c.add(Box.createVerticalStrut(8)); c.add(val); c.add(t);
        return c;
    }

    private JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row) ? SEL_BG : row%2==0 ? WHITE : ROW_ALT);
                c.setForeground(TEXT_DARK);
                return c;
            }
        };
        t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setRowHeight(32);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE2,0xE8,0xF0));
        t.setSelectionBackground(SEL_BG); t.setSelectionForeground(TEXT_DARK);
        t.setIntercellSpacing(new Dimension(0,1)); t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(TEAL); h.setForeground(WHITE);
        h.setFont(new Font("Segoe UI",Font.BOLD,12));
        h.setPreferredSize(new Dimension(0,36));
        h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,TEAL_DARK));
        return t;
    }

    private JScrollPane darkScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDD,0xE8,0xE4),1));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    private JButton tealBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(TEAL); b.setForeground(WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,18,8,18));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(TEAL_DARK); }
            public void mouseExited(MouseEvent e)  { b.setBackground(TEAL); }
        });
        return b;
    }

    private JButton outlineBtn(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(WHITE); b.setForeground(color);
        b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color,1,true),
                new EmptyBorder(6,14,6,14)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),20));
            }
            public void mouseExited(MouseEvent e) { b.setBackground(WHITE); }
        });
        return b;
    }

    private JButton sidebarNavBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI",Font.PLAIN,13));
        b.setForeground(new Color(255,255,255,210));
        b.setBackground(SIDEBAR_BG);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11,22,11,22));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(SIDEBAR_DARK); b.setForeground(WHITE);
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(SIDEBAR_BG); b.setForeground(new Color(255,255,255,210));
            }
        });
        return b;
    }

    private JPanel sidebarSection(String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,22,6));
        p.setBackground(SIDEBAR_BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl(label, new Font("Segoe UI",Font.BOLD,10), new Color(255,255,255,120)));
        return p;
    }

    private JTextField searchField(String ph) {
        JTextField f = new JTextField(22);
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setForeground(TEXT_MUTED); f.setText(ph);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCC,0xDD,0xD8),1,true),
                new EmptyBorder(6,12,6,12)));
        f.setBackground(WHITE);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(TEXT_MUTED); }
            }
        });
        return f;
    }

    // ── Dialog micro-helpers ──────────────────────────────────────

    private JDialog dlg(String title, int w, int h) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(w, h); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(WHITE); return d;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridLayout(0,2,12,12));
        p.setBackground(WHITE); p.setBorder(new EmptyBorder(20,24,12,24)); return p;
    }

    private void row(JPanel form, String label, JComponent field) {
        form.add(lbl(label, new Font("Segoe UI",Font.PLAIN,13), TEXT_MUTED));
        styleFormField(field); form.add(field);
    }

    private void styleFormField(JComponent f) {
        if (f instanceof JTextField || f instanceof JPasswordField) {
            f.setFont(new Font("Segoe UI",Font.PLAIN,13));
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xCC,0xDD,0xD8),1,true),
                    new EmptyBorder(6,10,6,10)));
            f.setBackground(new Color(0xF4,0xFB,0xF9));
        }
    }

    private void assembleDlg(JDialog dlg, JPanel form, JLabel errLbl, JButton save) {
        JPanel south = new JPanel(new BorderLayout(0,6));
        south.setBackground(WHITE); south.setBorder(new EmptyBorder(0,24,20,24));
        errLbl.setHorizontalAlignment(SwingConstants.CENTER);
        south.add(errLbl, BorderLayout.NORTH);
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btn.setBackground(WHITE); btn.add(save); south.add(btn, BorderLayout.CENTER);
        dlg.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null); sp.getViewport().setBackground(WHITE);
        dlg.add(sp, BorderLayout.CENTER); dlg.add(south, BorderLayout.SOUTH);
    }

    private JTextField   tf()          { return new JTextField(); }
    private JTextField   tf(String v)  { return new JTextField(v); }
    private JPasswordField pf()        { return new JPasswordField(); }
    private JComboBox<String> combo(String... items) { return new JComboBox<>(items); }
    private JLabel errLbl() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(ERR_COLOR); return l;
    }
    private JPanel whiteCard() {
        JPanel p = new JPanel(); p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD,0xE8,0xE4),1,true),
                new EmptyBorder(18,20,18,20))); return p;
    }
    private DefaultTableModel tableModel(String[] cols) {
        return new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int r,int c) { return false; }
        };
    }
    private JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }
    private String nv(String s)          { return s == null ? "" : s; }
    private String trunc(String s,int n) { return s.length()<=n ? s : s.substring(0,n-1)+"…"; }

    private boolean confirmDel(String what) {
        return JOptionPane.showConfirmDialog(this,
                "Permanently delete " + what + "?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg,"MediVault Admin",JOptionPane.INFORMATION_MESSAGE);
    }
    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg,"Error",JOptionPane.ERROR_MESSAGE);
    }
}