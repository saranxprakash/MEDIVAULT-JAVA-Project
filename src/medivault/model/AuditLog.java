package medivault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One immutable audit log entry.
 * Category values: PATIENT, DOCTOR, APPOINTMENT, BILLING, SECURITY
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category { PATIENT, DOCTOR, APPOINTMENT, BILLING, SECURITY }

    private final String    id;
    private final String    timestamp;
    private final Category  category;
    private final String    action;
    private final String    target;
    private final String    details;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");

    // ── Constructor 1 ─────────────────────────────────────────────
    // Naya entry banate waqt use karo — timestamp apne aap set hoti hai

    public AuditLog(String id, Category category,
                    String action, String target, String details) {
        this.id        = id;
        this.timestamp = LocalDateTime.now().format(FMT);
        this.category  = category;
        this.action    = action;
        this.target    = target;
        this.details   = details == null ? "" : details;
    }

    // ── Constructor 2 ─────────────────────────────────────────────
    // Database se load karte waqt use karo — timestamp DB se aati hai

    public AuditLog(String id, Category category,
                    String action, String target, String details, String timestamp) {
        this.id        = id;
        this.timestamp = timestamp == null ? LocalDateTime.now().format(FMT) : timestamp;
        this.category  = category;
        this.action    = action;
        this.target    = target;
        this.details   = details == null ? "" : details;
    }

    // ── Getters ───────────────────────────────────────────────────

    public String   getId()        { return id; }
    public String   getTimestamp() { return timestamp; }
    public Category getCategory()  { return category; }
    public String   getAction()    { return action; }
    public String   getTarget()    { return target; }
    public String   getDetails()   { return details; }

    @Override
    public String toString() {
        return "[" + timestamp + "]  " + category + "  |  "
                + action + "  →  " + target
                + (details.isEmpty() ? "" : "  (" + details + ")");
    }
}