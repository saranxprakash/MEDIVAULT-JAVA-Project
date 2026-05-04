package medivault.model;

import java.io.Serializable;

/**
 * Single-admin credentials store.
 * Password can be changed at runtime and survives restarts via AdminPasswordStore.
 * Defaults to admin / admin123 on first run.
 */
public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String ADMIN_USERNAME    = "admin";
    private static       String currentPassword   = "admin123";

    /** Verify credentials.  Username is always "admin". */
    public static boolean authenticate(String username, String password) {
        return ADMIN_USERNAME.equals(username)
                && currentPassword.equals(password);
    }

    /**
     * Update the in-memory password.
     * Persist by saving this class via AdminPasswordStore if you want it
     * to survive restarts (see AdminPasswordStore helper).
     */
    public static boolean updatePassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) return false;
        currentPassword = newPassword;
        AdminPasswordStore.save(newPassword);   // persists to disk
        return true;
    }

    /** Load a previously persisted password from disk (call once at startup). */
    public static void loadPersistedPassword() {
        String saved = AdminPasswordStore.load();
        if (saved != null && !saved.isBlank()) currentPassword = saved;
    }

    public static String getUsername() { return ADMIN_USERNAME; }
}