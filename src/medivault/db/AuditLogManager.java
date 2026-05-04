package medivault.db;

import medivault.model.AuditLog;
import medivault.model.AuditLog.Category;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton that writes, loads and queries AuditLog entries.
 * Logs are stored in  data/AuditLog.dat  alongside the other .dat files.
 */
public class AuditLogManager {

    private static final String FILE = "data/AuditLog.dat";
    private static AuditLogManager instance;

    private List<AuditLog> logs = new ArrayList<>();
    private int nextId = 1;

    // ── Singleton ────────────────────────────────────────────────
    private AuditLogManager() {
        new File("data").mkdirs();
        load();
    }

    public static AuditLogManager getInstance() {
        if (instance == null) instance = new AuditLogManager();
        return instance;
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
        String id = String.format("L%04d", nextId++);
        AuditLog entry = new AuditLog(id, category, action, target, details);
        logs.add(entry);
        save();
    }

    // Convenience overload — no details
    public void log(Category category, String action, String target) {
        log(category, action, target, "");
    }

    // ── Read ─────────────────────────────────────────────────────

    /** All logs, newest first. */
    public List<AuditLog> getAll() {
        List<AuditLog> copy = new ArrayList<>(logs);
        java.util.Collections.reverse(copy);
        return copy;
    }

    /** Filter by category. */
    public List<AuditLog> getByCategory(Category category) {
        return getAll().stream()
                .filter(l -> l.getCategory() == category)
                .collect(Collectors.toList());
    }

    /** Simple keyword search across action + target + details. */
    public List<AuditLog> search(String keyword) {
        String q = keyword.toLowerCase();
        return getAll().stream()
                .filter(l -> l.getAction().toLowerCase().contains(q)
                        || l.getTarget().toLowerCase().contains(q)
                        || l.getDetails().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /** Most recent N entries. */
    public List<AuditLog> getRecent(int n) {
        List<AuditLog> all = getAll();
        return all.subList(0, Math.min(n, all.size()));
    }

    public int getTotalCount() { return logs.size(); }

    /** Clear all logs (admin-only action, itself logged). */
    public void clearAll() {
        logs.clear();
        nextId = 1;
        save();
    }

    // ── Persistence ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                logs   = (List<AuditLog>) obj;
                nextId = logs.size() + 1;
            }
        } catch (Exception e) {
            System.out.println("AuditLog: could not load — " + e.getMessage());
        }
    }

    private void save() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(logs);
        } catch (IOException e) {
            System.out.println("AuditLog: could not save — " + e.getMessage());
        }
    }
}
