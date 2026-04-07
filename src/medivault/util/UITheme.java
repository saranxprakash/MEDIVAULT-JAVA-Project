
package medivault.util;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UITheme {

    // ── Colours ──────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(0x1A, 0x6B, 0xB4);
    public static final Color PRIMARY_DARK  = new Color(0x0D, 0x4A, 0x8A);
    public static final Color PRIMARY_LIGHT = new Color(0xE8, 0xF4, 0xFF);
    public static final Color ACCENT        = new Color(0x00, 0xC8, 0xA0);
    public static final Color SUCCESS       = new Color(0x2E, 0x7D, 0x32);
    public static final Color DANGER        = new Color(0xE5, 0x39, 0x35);
    public static final Color WARNING       = new Color(0xF5, 0x7C, 0x00);
    public static final Color BG            = new Color(0xF4, 0xF6, 0xF9);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TEXT_DARK     = new Color(0x1A, 0x1A, 0x2E);
    public static final Color TEXT_MUTED    = new Color(0x6B, 0x72, 0x80);
    public static final Color TABLE_HEADER  = new Color(0x1A, 0x6B, 0xB4);
    public static final Color TABLE_ROW_ALT = new Color(0xF0, 0xF7, 0xFF);

    // ── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_H = new Font("Segoe UI", Font.BOLD, 13);

    // ── Component factories ───────────────────────────────────────────

    // Creates a blue filled button
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);   // removes the dotted focus border
        btn.setBorderPainted(false);  // removes default button border
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // hand cursor on hover
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    // Creates a green button — used for success actions
    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(SUCCESS);
        return btn;
    }

    // Creates a red button — used for cancel/delete
    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        return btn;
    }

    // Creates a styled single-line text field
    public static JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBD, 0xC3, 0xCE), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setPreferredSize(new Dimension(220, 38));
        return f;
    }

    // Creates a styled password field
    public static JPasswordField styledPassword() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBD, 0xC3, 0xCE), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setPreferredSize(new Dimension(220, 38));
        return f;
    }

    // Creates a normal label
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    // Creates a small grey label — used for hints and error messages
    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    // Creates a bold heading label
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_DARK);
        return l;
    }

    // Creates a white card panel with a border
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDE, 0xE7), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return p;
    }

    // Creates a sidebar navigation button
    public static JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(0xCC, 0xE4, 0xFF));
        btn.setBackground(PRIMARY_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setMaximumSize(new Dimension(200, 42));
        return btn;
    }

    // Applies consistent styling to any JTable
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(FONT_TABLE_H);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    // Call this once in Main before opening any window
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Button.focus",     new Color(0, 0, 0, 0));
    }
}