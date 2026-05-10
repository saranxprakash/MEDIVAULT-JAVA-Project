package medivault.db;

import medivault.model.AuditLog;
import medivault.model.AuditLog.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton that writes, loads and queries AuditLog entries.
 * Data is stored permanently in MySQL database — medivault.audit_log table.
 *
 * Yeh class DatabaseManager ka shared connection use karti hai,
 * isliye alag se koi setup nahi chahiye.
 */
public class AuditLogManager {

    private static AuditLogManager instance;

    // ── Singleton ────────────────────────────────────────────────

    private AuditLogManager() {
        createTableIfNotExists();
    }

    public static AuditLogManager getInstance() {
        if (instance == null) {
            instance = new AuditLogManager();
        }
        return instance;
    }

    // DatabaseManager se shared connection lena
    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Database Setup ───────────────────────────────────────────

    private void createTableIfNotExists() {
        String sql =
            "CREATE TABLE IF NOT EXISTS audit_log ("
            + "  id          VARCHAR(10)  PRIMARY KEY,"
            + "  category    VARCHAR(20)  NOT NULL,"
            + "  action      VARCHAR(50)  NOT NULL,"
            + "  target      TEXT         NOT NULL,"
            + "  details     TEXT,"
            + "  timestamp   VARCHAR(30)  NOT NULL"
            + ")";

        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("AuditLog: table creation failed — " + e.getMessage());
        } finally {
            closeStatement(stmt);
        }
    }

    // ── Write ────────────────────────────────────────────────────

    /**
     * Log an admin action.
     * @param category  PATIENT / DOCTOR / APPOINTMENT / BILLING / SECURITY
     * @param action    Short verb: "ADD", "UPDATE", "DELETE", "LOGIN", "LOGOUT",
     *                  "PASSWORD_CHANGE", "SESSION_TIMEOUT"
     * @param target    What was affected, e.g. "Patient P003 — Rahul Verma"
     * @param details   Optional extra info (pass null or "" if none)
     */
    public synchronized void log(Category category,
                                  String action,
                                  String target,
                                  String details) {
        String id        = generateNextId();
        String timestamp = java.time.LocalDateTime.now().toString();

        if (details == null) {
            details = "";
        }

        String sql =
            "INSERT INTO audit_log (id, category, action, target, details, timestamp)"
            + " VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = null;
        try {
            pstmt = getConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, category.name());
            pstmt.setString(3, action);
            pstmt.setString(4, target);
            pstmt.setString(5, details);
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("AuditLog: could not save log — " + e.getMessage());
        } finally {
            closePreparedStatement(pstmt);
        }
    }

    // Convenience overload — no details
    public void log(Category category, String action, String target) {
        log(category, action, target, "");
    }

    // ── Read ─────────────────────────────────────────────────────

    /** All logs, newest first. */
    public List<AuditLog> getAll() {
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC";
        return runQuery(sql, new String[]{});
    }

    /** Filter by category. */
    public List<AuditLog> getByCategory(Category category) {
        String sql = "SELECT * FROM audit_log WHERE category = ? ORDER BY timestamp DESC";
        return runQuery(sql, new String[]{ category.name() });
    }

    /** Simple keyword search across action + target + details. */
    public List<AuditLog> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        String sql  =
            "SELECT * FROM audit_log "
            + "WHERE LOWER(action)  LIKE ? "
            + "   OR LOWER(target)  LIKE ? "
            + "   OR LOWER(details) LIKE ? "
            + "ORDER BY timestamp DESC";
        return runQuery(sql, new String[]{ like, like, like });
    }

    /** Most recent N entries. */
    public List<AuditLog> getRecent(int n) {
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ?";

        List<AuditLog> result   = new ArrayList<AuditLog>();
        PreparedStatement pstmt = null;
        ResultSet rs            = null;

        try {
            pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, n);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("AuditLog: could not fetch recent — " + e.getMessage());
        } finally {
            closeResultSet(rs);
            closePreparedStatement(pstmt);
        }

        return result;
    }

    /** Total number of log entries in the database. */
    public int getTotalCount() {
        String sql    = "SELECT COUNT(*) FROM audit_log";
        int count     = 0;
        Statement stmt = null;
        ResultSet rs   = null;

        try {
            stmt = getConnection().createStatement();
            rs   = stmt.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("AuditLog: could not count — " + e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }

        return count;
    }

    /** Clear all logs (admin-only action, khud log karo pehle). */
    public synchronized void clearAll() {
        String sql     = "DELETE FROM audit_log";
        Statement stmt = null;

        try {
            stmt = getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("AuditLog: could not clear — " + e.getMessage());
        } finally {
            closeStatement(stmt);
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    private List<AuditLog> runQuery(String sql, String[] params) {
        List<AuditLog> result   = new ArrayList<AuditLog>();
        PreparedStatement pstmt = null;
        ResultSet rs            = null;

        try {
            pstmt = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("AuditLog: query failed — " + e.getMessage());
        } finally {
            closeResultSet(rs);
            closePreparedStatement(pstmt);
        }

        return result;
    }

    /** ResultSet ki ek row ko AuditLog object mein convert karo. */
    private AuditLog mapRow(ResultSet rs) throws SQLException {
        String id        = rs.getString("id");
        String catStr    = rs.getString("category");
        String action    = rs.getString("action");
        String target    = rs.getString("target");
        String details   = rs.getString("details");
        String timestamp = rs.getString("timestamp");

        Category category = Category.valueOf(catStr);

        return new AuditLog(id, category, action, target, details, timestamp);
    }

    /**
     * Agle ID ke liye DB mein count check karo.
     * Format: L0001, L0002, ...
     */
    private String generateNextId() {
        String sql     = "SELECT COUNT(*) FROM audit_log";
        int count      = 0;
        Statement stmt = null;
        ResultSet rs   = null;

        try {
            stmt = getConnection().createStatement();
            rs   = stmt.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("AuditLog: could not generate ID — " + e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }

        return String.format("L%04d", count + 1);
    }

    // ── Resource Closing ─────────────────────────────────────────
    // Note: Connection close nahi karte — wo DatabaseManager handle karta hai

    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private void closePreparedStatement(PreparedStatement pstmt) {
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
}