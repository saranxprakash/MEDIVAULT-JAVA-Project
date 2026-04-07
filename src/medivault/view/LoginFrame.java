package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Doctor;
import medivault.model.Patient;
import medivault.util.UITheme;

import javax.swing.*;
import java.awt.*;

    public class LoginFrame extends JFrame {

        // The controller — all button actions go through this
        private final Medivaultsystem system = new Medivaultsystem();

        public LoginFrame() {
            setTitle("MediVault — Login");
            setSize(900, 580);
            setDefaultCloseOperation(EXIT_ON_CLOSE); // closes app when window is closed
            setLocationRelativeTo(null);             // centres the window on screen
            setResizable(false);

            // Root panel uses BorderLayout — left brand + right tabs
            JPanel root = new JPanel(new BorderLayout());

            root.add(buildBrandPanel(), BorderLayout.WEST);
            root.add(buildRightPanel(), BorderLayout.CENTER);

            setContentPane(root);
        }
        private JPanel buildBrandPanel() {
            JPanel p = new JPanel();
            p.setBackground(UITheme.PRIMARY_DARK);
            p.setPreferredSize(new Dimension(280, 0));
            // BoxLayout stacks children top to bottom
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(60, 30, 40, 30));

            // App icon using text
            JLabel icon = new JLabel("MV");
            icon.setFont(new Font("Segoe UI", Font.BOLD, 48));
            icon.setForeground(UITheme.ACCENT);
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("MediVault");
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel sub = new JLabel("Hospital Management");
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(new Color(0xCC, 0xE4, 0xFF));
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Box.createVerticalGlue() pushes content to centre
            p.add(Box.createVerticalGlue());
            p.add(icon);
            p.add(Box.createVerticalStrut(10));  // 10px gap
            p.add(title);
            p.add(Box.createVerticalStrut(6));
            p.add(sub);
            p.add(Box.createVerticalStrut(30));

            // Feature list
            String[] features = {
                    "Patient Registration",
                    "Appointment Booking",
                    "Doctor Dashboard",
                    "Prescription Tracking",
                    "Billing Management"
            };
            for (String f : features) {
                JLabel lbl = new JLabel("✓  " + f);
                lbl.setFont(UITheme.FONT_SMALL);
                lbl.setForeground(new Color(0xCC, 0xE4, 0xFF));
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(lbl);
                p.add(Box.createVerticalStrut(5));
            }

            p.add(Box.createVerticalGlue());
            return p;
        }
        private JPanel buildRightPanel() {
            JPanel right = new JPanel(new BorderLayout());
            right.setBackground(UITheme.CARD_BG);
            right.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            JLabel heading = UITheme.heading("Welcome to MediVault");
            heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(UITheme.FONT_LABEL);
            tabs.addTab("Patient Login",    buildPatientLoginPanel());
            tabs.addTab("Doctor Login",     buildDoctorLoginPanel());
            tabs.addTab("Register Patient", buildRegisterPanel());

            right.add(heading, BorderLayout.NORTH);
            right.add(tabs,    BorderLayout.CENTER);
            return right;
        }
        private JPanel buildPatientLoginPanel() {
            JPanel p = buildFormPanel();

            JTextField     emailField = UITheme.styledField();
            JPasswordField passField  = UITheme.styledPassword();

            // Error label — starts blank, shows red message on failed login
            JLabel errLabel = UITheme.muted(" ");
            errLabel.setForeground(UITheme.DANGER);

            JButton loginBtn = UITheme.primaryButton("Login as Patient");
            loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Hint for the demo credentials
            JLabel hint = UITheme.muted("Demo: rahul@email.com / patient123");
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);

            addFormRow(p, "Email Address:", emailField);
            addFormRow(p, "Password:",      passField);
            p.add(Box.createVerticalStrut(6));
            p.add(errLabel);
            p.add(Box.createVerticalStrut(10));
            p.add(loginBtn);
            p.add(Box.createVerticalStrut(12));
            p.add(hint);

            // What happens when Login button is clicked
            loginBtn.addActionListener(e -> {
                String email = emailField.getText().trim();
                String pass  = new String(passField.getPassword());

                Patient patient = system.loginPatient(email, pass);

                if (patient == null) {
                    // Login failed — show error
                    errLabel.setText("Invalid email or password.");
                } else {
                    // Login success — open patient dashboard
                    new PatientDashboard(patient, system).setVisible(true);
                    dispose(); // close this login window
                }
            });

            return p;
        }
        private JPanel buildDoctorLoginPanel() {
            JPanel p = buildFormPanel();

            JTextField     emailField = UITheme.styledField();
            JPasswordField passField  = UITheme.styledPassword();

            JLabel errLabel = UITheme.muted(" ");
            errLabel.setForeground(UITheme.DANGER);

            JButton loginBtn = UITheme.primaryButton("Login as Doctor");
            loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel hint = UITheme.muted("Demo: anita@medivault.com / doctor123");
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);

            addFormRow(p, "Doctor Email:", emailField);
            addFormRow(p, "Password:",     passField);
            p.add(Box.createVerticalStrut(6));
            p.add(errLabel);
            p.add(Box.createVerticalStrut(10));
            p.add(loginBtn);
            p.add(Box.createVerticalStrut(12));
            p.add(hint);

            loginBtn.addActionListener(e -> {
                String email = emailField.getText().trim();
                String pass  = new String(passField.getPassword());

                Doctor doctor = system.loginDoctor(email, pass);

                if (doctor == null) {
                    errLabel.setText("Invalid email or password.");
                } else {
                    new DoctorDashboard(doctor, system).setVisible(true);
                    dispose();
                }
            });

            return p;
        }
        private JPanel buildRegisterPanel() {
            JPanel p = buildFormPanel();

            JTextField     nameField  = UITheme.styledField();
            JTextField     ageField   = UITheme.styledField();
            JTextField     phoneField = UITheme.styledField();
            JTextField     emailField = UITheme.styledField();
            JPasswordField passField  = UITheme.styledPassword();

            // Gender dropdown
            JComboBox<String> genderBox =
                    new JComboBox<>(new String[]{"Male", "Female", "Other"});
            genderBox.setFont(UITheme.FONT_LABEL);

            JLabel errLabel = UITheme.muted(" ");
            errLabel.setForeground(UITheme.DANGER);

            JButton regBtn = UITheme.successButton("Create Account");
            regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            addFormRow(p, "Full Name:", nameField);
            addFormRow(p, "Age:",       ageField);
            addFormRow(p, "Gender:",    genderBox);
            addFormRow(p, "Phone:",     phoneField);
            addFormRow(p, "Email:",     emailField);
            addFormRow(p, "Password:",  passField);
            p.add(Box.createVerticalStrut(4));
            p.add(errLabel);
            p.add(Box.createVerticalStrut(8));
            p.add(regBtn);

            regBtn.addActionListener(e -> {
                // Age must be a number — catch the error if not
                int age;
                try {
                    age = Integer.parseInt(ageField.getText().trim());
                } catch (NumberFormatException ex) {
                    errLabel.setText("Age must be a number.");
                    return; // stop here — don't proceed
                }

                String err = system.registerPatient(
                        nameField.getText().trim(),
                        (String) genderBox.getSelectedItem(),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passField.getPassword()),
                        age);

                if (err != null) {
                    // Show the error returned by the controller
                    errLabel.setText(err);
                } else {
                    // Success
                    JOptionPane.showMessageDialog(
                            this,
                            "Account created! You can now log in.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    errLabel.setForeground(UITheme.SUCCESS);
                    errLabel.setText("Account created successfully!");
                }
            });

            return p;
        }
        // Creates a blank vertical form panel
        private JPanel buildFormPanel() {
            JPanel p = new JPanel();
            p.setBackground(UITheme.CARD_BG);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
            return p;
        }

        // Adds a label + field pair as one row in the form
        private void addFormRow(JPanel p, String labelText, JComponent field) {
            JLabel lbl = UITheme.label(labelText);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(lbl);
            p.add(Box.createVerticalStrut(4));

            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Make text fields stretch to full width
            if (field instanceof JTextField || field instanceof JPasswordField) {
                field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            }
            p.add(field);
            p.add(Box.createVerticalStrut(10));
        }
        // end of LoginFrame
}
