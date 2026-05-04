package medivault.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SessionManager — tracks admin inactivity and fires a timeout callback.
 *
 * Usage:
 *   SessionManager.start(frame, 10, () -> { // logout logic });
 *
 * Reset on every user interaction via:
 *   SessionManager.reset();
 *
 * Stop cleanly on logout:
 *   SessionManager.stop();
 */
public class SessionManager {

    // ── Config ────────────────────────────────────────────────────
    private static final int WARN_SECONDS_BEFORE = 60; // show warning 60s before timeout

    // ── State ─────────────────────────────────────────────────────
    private static Timer      countdownTimer;
    private static Timer      warningTimer;
    private static int        timeoutMillis;
    private static Runnable   onTimeout;
    private static JFrame     ownerFrame;
    private static long       lastActivityTime;
    private static boolean    warningShown = false;

    // ── Public API ────────────────────────────────────────────────

    /**
     * Start session tracking.
     * @param frame          The AdminDashboard frame (used to attach global listener)
     * @param timeoutMinutes Inactivity period before auto-logout
     * @param onTimeout      Callback to execute on timeout (runs on EDT)
     */
    public static void start(JFrame frame, int timeoutMinutes, Runnable onTimeout) {
        stop(); // cancel any previous session

        ownerFrame         = frame;
        SessionManager.onTimeout = onTimeout;
        timeoutMillis      = timeoutMinutes * 60 * 1000;
        lastActivityTime   = System.currentTimeMillis();
        warningShown       = false;

        // Attach mouse + key listeners to every component in the frame
        attachActivityListeners(frame);

        // Poll every second to check elapsed time
        countdownTimer = new Timer(1000, e -> checkInactivity());
        countdownTimer.start();
    }

    /** Reset the inactivity clock — call this whenever the user does something. */
    public static void reset() {
        lastActivityTime = System.currentTimeMillis();
        warningShown     = false;
        // dismiss any lingering warning dialog
        if (warningTimer != null) warningTimer.stop();
    }

    /** Stop tracking — call on explicit logout. */
    public static void stop() {
        if (countdownTimer != null) { countdownTimer.stop(); countdownTimer = null; }
        if (warningTimer   != null) { warningTimer.stop();   warningTimer   = null; }
    }

    // ── Internal ──────────────────────────────────────────────────

    private static void checkInactivity() {
        long elapsed = System.currentTimeMillis() - lastActivityTime;
        long remaining = timeoutMillis - elapsed;

        if (remaining <= 0) {
            // Timed out
            stop();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ownerFrame,
                        "You have been logged out due to 10 minutes of inactivity.",
                        "Session Expired",
                        JOptionPane.WARNING_MESSAGE);
                if (onTimeout != null) onTimeout.run();
            });
        } else if (remaining <= WARN_SECONDS_BEFORE * 1000L && !warningShown) {
            // Show 60-second warning
            warningShown = true;
            SwingUtilities.invokeLater(() -> showWarningDialog((int)(remaining / 1000)));
        }
    }

    private static void showWarningDialog(int secondsLeft) {
        JDialog dlg = new JDialog(ownerFrame, "Session Warning", false); // non-modal
        dlg.setSize(360, 150);
        dlg.setLocationRelativeTo(ownerFrame);
        dlg.setLayout(new BorderLayout());

        JLabel msg = new JLabel(
                "<html><div style='text-align:center; padding:12px'>"
                + "⚠  Your session will expire in <b>" + secondsLeft + " seconds</b>"
                + " due to inactivity.<br>Move your mouse or press any key to stay logged in."
                + "</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dlg.add(msg, BorderLayout.CENTER);

        JButton stayBtn = new JButton("Stay Logged In");
        stayBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stayBtn.setBackground(new Color(0x3D, 0xB8, 0x9E));
        stayBtn.setForeground(Color.WHITE);
        stayBtn.setBorderPainted(false);
        stayBtn.setFocusPainted(false);
        stayBtn.addActionListener(e -> { reset(); dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.add(stayBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);

        // Auto-close the warning when session fully expires
        warningTimer = new Timer(WARN_SECONDS_BEFORE * 1000, e -> dlg.dispose());
        warningTimer.setRepeats(false);
        warningTimer.start();

        dlg.setVisible(true);
    }

    /**
     * Walk the entire component tree and attach mouse + key listeners
     * that call reset() on any interaction.
     */
    private static void attachActivityListeners(Component comp) {
        AWTEventListener activityListener = event -> reset();

        // Use Toolkit global listener — catches ALL awt events in the JVM
        Toolkit.getDefaultToolkit().addAWTEventListener(
                activityListener,
                AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);
    }
}
