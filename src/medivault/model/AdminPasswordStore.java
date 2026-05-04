package medivault.model;

import java.io.*;

/**
 * Tiny helper that reads / writes the admin password to  data/admin.pwd
 * Called only by Admin.updatePassword() and Admin.loadPersistedPassword().
 */
public class AdminPasswordStore {

    private static final String FILE = "data/admin.pwd";

    public static void save(String password) {
        try {
            new File("data").mkdirs();
            try (ObjectOutputStream oos =
                         new ObjectOutputStream(new FileOutputStream(FILE))) {
                oos.writeObject(password);
            }
        } catch (IOException e) {
            System.out.println("AdminPasswordStore: could not save — " + e.getMessage());
        }
    }

    public static String load() {
        File f = new File(FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (String) ois.readObject();
        } catch (Exception e) {
            System.out.println("AdminPasswordStore: could not load — " + e.getMessage());
            return null;
        }
    }
}
