package medivault.view;

import medivault.controller.Medivaultsystem;
import medivault.model.Doctor;

import javax.swing.*;

public class DoctorDashboard extends JFrame {

    public DoctorDashboard(Doctor doctor, Medivaultsystem system) {
        setTitle("MediVault — Dr. " + doctor.getName());
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Temporary placeholder — will be built in Phase 7
        JLabel placeholder = new JLabel(
                "Doctor Dashboard — coming in Phase 7!",
                SwingConstants.CENTER);
        placeholder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        add(placeholder);
    }
}
