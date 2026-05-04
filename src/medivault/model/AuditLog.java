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
    private final String    timestamp;   // human-readable, stored as string for simplicity
    private final Category  category;
    private final String    action;      // e.g. "ADD", "UPDATE", "DELETE", "LOGIN", "PASSWORD_CHANGE"
    private final String    target;      // e.g. "Patient P003 — Rahul Verma"
    private final String    details;     // extra info

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");

    public AuditLog(String id, Category category,
                    String action, String target, String details) {
        this.id        = id;
        this.timestamp = LocalDateTime.now().format(FMT);
        this.category  = category;
        this.action    = action;
        this.target    = target;
        this.details   = details == null ? "" : details;
    }

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