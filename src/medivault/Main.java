package medivault;

import medivault.util.UITheme;
import medivault.view.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Apply global colour defaults
        UITheme.applyGlobalDefaults();

        // Try to use Nimbus look and feel — cleaner than the default
        try {
            UIManager.setLookAndFeel(
                    "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // If Nimbus isn't available, default Metal is fine
        }

        // IMPORTANT — always start Swing on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}