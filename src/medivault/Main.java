package medivault;

import medivault.util.UITheme;
import medivault.view.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {

        UITheme.applyGlobalDefaults();

        try {
            UIManager.setLookAndFeel(
                    "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();

            // Allow dragging the undecorated window
            final Point[] drag = {null};
            frame.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    drag[0] = e.getPoint();
                }
            });
            frame.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (drag[0] != null) {
                        Point loc = frame.getLocation();
                        frame.setLocation(
                                loc.x + e.getX() - drag[0].x,
                                loc.y + e.getY() - drag[0].y);
                    }
                }
            });

            frame.setVisible(true);
        });
    }
}