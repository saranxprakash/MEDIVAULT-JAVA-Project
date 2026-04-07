package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Patient;

import javax.swing.*;

public class PatientDashboard extends JFrame {

    public PatientDashboard(Patient patient, Medivaultsystem system) {
        setTitle("MediVault — Patient: " + patient.getName());
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Temporary placeholder — will be built in Phase 6
        JLabel placeholder = new JLabel(
                "Patient Dashboard — coming in Phase 6!",
                SwingConstants.CENTER);
        placeholder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        add(placeholder);
    }
}