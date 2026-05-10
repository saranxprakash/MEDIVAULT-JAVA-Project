package medivault.db;

import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabaseManager — MySQL ke zariye permanent data storage.
 *
 * Setup ke liye mysql-connector-java jar chahiye.
 * Maven dependency:
 *   <dependency>
 *       <groupId>com.mysql</groupId>
 *       <artifactId>mysql-connector-j</artifactId>
 *       <version>8.3.0</version>
 *   </dependency>
 *
 * MySQL mein pehle yeh database banana hoga:
 *   CREATE DATABASE medivault;
 */
public class DatabaseManager {

    // ── MySQL Connection Settings ─────────────────────────────────
    // Apne MySQL server ke hisaab se yeh values badlo

    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "medivault";
    private static final String DB_USER     = "root";       // apna MySQL username
    private static final String DB_PASSWORD = "Sun!Moon@Star42"; // apna MySQL password

    private static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    private static DatabaseManager instance;
    private Connection connection;

    // ── Singleton ────────────────────────────────────────────────

    private DatabaseManager() {
        try {
            // MySQL driver load karo
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connection kholo
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("MySQL database se connection ho gaya.");

            createTables();
            seedDemoData();

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver nahi mila. mysql-connector-j.jar add karo.");
        } catch (SQLException e) {
            System.out.println("Database connection fail: " + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Connection getter — AuditLogManager bhi yahi connection use karega
    public Connection getConnection() {
        return connection;
    }

    // ── Tables Banana ────────────────────────────────────────────

    private void createTables() throws SQLException {
        Statement st = connection.createStatement();

        // Doctors table
        st.execute(
                "CREATE TABLE IF NOT EXISTS doctors ("
                        + "  doctor_id   VARCHAR(10)  PRIMARY KEY,"
                        + "  name        VARCHAR(100) NOT NULL,"
                        + "  specialty   VARCHAR(100),"
                        + "  phone       VARCHAR(15),"
                        + "  email       VARCHAR(100) UNIQUE,"
                        + "  password    VARCHAR(100)"
                        + ")"
        );

        // Patients table
        st.execute(
                "CREATE TABLE IF NOT EXISTS patients ("
                        + "  patient_id  VARCHAR(10)   PRIMARY KEY,"
                        + "  name        VARCHAR(100)  NOT NULL,"
                        + "  age         INT,"
                        + "  gender      VARCHAR(10),"
                        + "  phone       VARCHAR(15),"
                        + "  email       VARCHAR(100)  UNIQUE,"
                        + "  password    VARCHAR(100),"
                        + "  symptoms    TEXT,"
                        + "  medicine    TEXT,"
                        + "  bill_amount DOUBLE        DEFAULT 0.0,"
                        + "  med_history TEXT"
                        + ")"
        );

        // Appointments table
        st.execute(
                "CREATE TABLE IF NOT EXISTS appointments ("
                        + "  appointment_id  VARCHAR(10)  PRIMARY KEY,"
                        + "  patient_id      VARCHAR(10)  NOT NULL,"
                        + "  doctor_id       VARCHAR(10)  NOT NULL,"
                        + "  date_time_key   VARCHAR(50)  NOT NULL,"
                        + "  status          VARCHAR(20)  NOT NULL DEFAULT 'Pending',"
                        + "  notes           TEXT,"
                        + "  FOREIGN KEY (patient_id) REFERENCES patients(patient_id),"
                        + "  FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id)"
                        + ")"
        );

        st.close();
        System.out.println("Saari tables ready hain.");
    }

    // ── Demo Data ────────────────────────────────────────────────

    private void seedDemoData() throws SQLException {
        // Pehle check karo — agar doctors pehle se hain to seed mat karo
        Statement check = connection.createStatement();
        ResultSet rs = check.executeQuery("SELECT COUNT(*) AS cnt FROM doctors");
        rs.next();
        int count = rs.getInt("cnt");
        rs.close();
        check.close();

        if (count > 0) {
            return;
        }

        // Teen demo doctors
        Doctor d1 = new Doctor("D001", "Dr. Ankitha",
                "Cardiology", "9876543210",
                "anita@medivault.com", "doctor123");
        Doctor d2 = new Doctor("D002", "Dr. Rajan Mehta",
                "Neurology", "9876543211",
                "rajan@medivault.com", "doctor123");
        Doctor d3 = new Doctor("D003", "Dr. Priya Nair",
                "Pediatrics", "9876543212",
                "priya@medivault.com", "doctor123");

        addDoctor(d1);
        addDoctor(d2);
        addDoctor(d3);

        // Ek demo patient
        Patient p1 = new Patient("P001", "Rahul Verma", 35,
                "Male", "9988776655",
                "rahul@email.com", "patient123");
        p1.setSymptoms("Chest pain, shortness of breath");
        p1.setPrescribedMedicine("Aspirin 75mg");
        p1.setBillAmount(4500.00);
        p1.addMedicalHistoryEntry("2025-01-10: Initial consultation");

        addPatient(p1);
        System.out.println("Demo data add ho gaya.");
    }

    // ── Patient Operations ───────────────────────────────────────

    public boolean addPatient(Patient p) {
        String sql =
                "INSERT IGNORE INTO patients "
                        + "(patient_id, name, age, gender, phone, email, password,"
                        + " symptoms, medicine, bill_amount, med_history)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,  p.getPatientId());
            ps.setString(2,  p.getName());
            ps.setInt   (3,  p.getAge());
            ps.setString(4,  p.getGender());
            ps.setString(5,  p.getPhone());
            ps.setString(6,  p.getEmail());
            ps.setString(7,  p.getPassword());
            ps.setString(8,  p.getSymptoms());
            ps.setString(9,  p.getPrescribedMedicine());
            ps.setDouble(10, p.getBillAmount());
            ps.setString(11, historyListToText(p.getMedicalHistory()));
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("addPatient error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePatient(Patient p) {
        String sql =
                "UPDATE patients SET"
                        + "  name=?, age=?, gender=?, phone=?, email=?, password=?,"
                        + "  symptoms=?, medicine=?, bill_amount=?, med_history=?"
                        + " WHERE patient_id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,  p.getName());
            ps.setInt   (2,  p.getAge());
            ps.setString(3,  p.getGender());
            ps.setString(4,  p.getPhone());
            ps.setString(5,  p.getEmail());
            ps.setString(6,  p.getPassword());
            ps.setString(7,  p.getSymptoms());
            ps.setString(8,  p.getPrescribedMedicine());
            ps.setDouble(9,  p.getBillAmount());
            ps.setString(10, historyListToText(p.getMedicalHistory()));
            ps.setString(11, p.getPatientId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("updatePatient error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePatient(String patientId) {
        // Pehle uske appointments hata do
        deleteAppointmentsByPatient(patientId);

        String sql = "DELETE FROM patients WHERE patient_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, patientId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("deletePatient error: " + e.getMessage());
            return false;
        }
    }

    public Patient getPatient(String id) {
        String sql = "SELECT * FROM patients WHERE patient_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            Patient p = null;
            if (rs.next()) {
                p = rowToPatient(rs);
            }
            rs.close();
            ps.close();
            return p;

        } catch (SQLException e) {
            System.out.println("getPatient error: " + e.getMessage());
            return null;
        }
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<Patient>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(rowToPatient(rs));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getAllPatients error: " + e.getMessage());
        }
        return list;
    }

    public Patient findPatientByEmail(String email) {
        String sql = "SELECT * FROM patients WHERE LOWER(email)=LOWER(?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            Patient p = null;
            if (rs.next()) {
                p = rowToPatient(rs);
            }
            rs.close();
            ps.close();
            return p;

        } catch (SQLException e) {
            System.out.println("findPatientByEmail error: " + e.getMessage());
            return null;
        }
    }

    // Purana naam bhi support karo taki koi aur code break na ho
    public Patient findPatientByemail(String email) {
        return findPatientByEmail(email);
    }

    public List<Patient> searchPatients(String query) {
        List<Patient> results = new ArrayList<Patient>();
        String sql =
                "SELECT * FROM patients WHERE"
                        + "  LOWER(name)          LIKE LOWER(?)"
                        + "  OR LOWER(patient_id) LIKE LOWER(?)"
                        + "  OR phone             LIKE ?";
        try {
            String pattern = "%" + query + "%";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rowToPatient(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("searchPatients error: " + e.getMessage());
        }
        return results;
    }

    // ── Doctor Operations ────────────────────────────────────────

    public boolean addDoctor(Doctor d) {
        String sql =
                "INSERT IGNORE INTO doctors"
                        + " (doctor_id, name, specialty, phone, email, password)"
                        + " VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, d.getDoctorId());
            ps.setString(2, d.getName());
            ps.setString(3, d.getSpecialty());
            ps.setString(4, d.getPhone());
            ps.setString(5, d.getEmail());
            ps.setString(6, d.getPassword());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("addDoctor error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDoctor(Doctor d) {
        String sql =
                "UPDATE doctors SET"
                        + "  name=?, specialty=?, phone=?, email=?, password=?"
                        + " WHERE doctor_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialty());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.setString(5, d.getPassword());
            ps.setString(6, d.getDoctorId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("updateDoctor error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDoctor(String doctorId) {
        // Pehle uske appointments hata do
        deleteAppointmentsByDoctor(doctorId);

        String sql = "DELETE FROM doctors WHERE doctor_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, doctorId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("deleteDoctor error: " + e.getMessage());
            return false;
        }
    }

    public Doctor getDoctor(String id) {
        String sql = "SELECT * FROM doctors WHERE doctor_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            Doctor d = null;
            if (rs.next()) {
                d = rowToDoctor(rs);
            }
            rs.close();
            ps.close();
            return d;

        } catch (SQLException e) {
            System.out.println("getDoctor error: " + e.getMessage());
            return null;
        }
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<Doctor>();
        String sql = "SELECT * FROM doctors ORDER BY name";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(rowToDoctor(rs));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getAllDoctors error: " + e.getMessage());
        }
        return list;
    }

    public Doctor findDoctorByEmail(String email) {
        String sql = "SELECT * FROM doctors WHERE LOWER(email)=LOWER(?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            Doctor d = null;
            if (rs.next()) {
                d = rowToDoctor(rs);
            }
            rs.close();
            ps.close();
            return d;

        } catch (SQLException e) {
            System.out.println("findDoctorByEmail error: " + e.getMessage());
            return null;
        }
    }

    public String generateDoctorId() {
        int max = 0;
        List<Doctor> allDoctors = getAllDoctors();
        for (int i = 0; i < allDoctors.size(); i++) {
            String key = allDoctors.get(i).getDoctorId();
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) {
                    max = num;
                }
            } catch (NumberFormatException e) {
                // Ajeeb ID, skip karo
            }
        }
        return String.format("D%03d", max + 1);
    }

    // ── Appointment Operations ───────────────────────────────────

    public boolean addAppointment(Appointment a) {
        // Pehle check karo — kya wo slot already booked hai
        String checkSql =
                "SELECT COUNT(*) AS cnt FROM appointments"
                        + " WHERE doctor_id=? AND date_time_key=? AND status != 'Cancelled'";
        try {
            PreparedStatement check = connection.prepareStatement(checkSql);
            check.setString(1, a.getDoctorId());
            check.setString(2, a.getDateTimeKey());
            ResultSet rs = check.executeQuery();
            rs.next();
            int taken = rs.getInt("cnt");
            rs.close();
            check.close();

            if (taken > 0) {
                return false; // slot already le hua hai
            }

            String sql =
                    "INSERT INTO appointments"
                            + " (appointment_id, patient_id, doctor_id, date_time_key, status, notes)"
                            + " VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, a.getAppointmentId());
            ps.setString(2, a.getPatientId());
            ps.setString(3, a.getDoctorId());
            ps.setString(4, a.getDateTimeKey());
            ps.setString(5, a.getStatus().toString());
            ps.setString(6, a.getNotes());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("addAppointment error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointment(Appointment a) {
        String sql =
                "UPDATE appointments SET"
                        + "  patient_id=?, doctor_id=?, date_time_key=?, status=?, notes=?"
                        + " WHERE appointment_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, a.getPatientId());
            ps.setString(2, a.getDoctorId());
            ps.setString(3, a.getDateTimeKey());
            ps.setString(4, a.getStatus().toString());
            ps.setString(5, a.getNotes());
            ps.setString(6, a.getAppointmentId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("updateAppointment error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAppointment(String appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, appointmentId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("deleteAppointment error: " + e.getMessage());
            return false;
        }
    }

    private void deleteAppointmentsByPatient(String patientId) {
        String sql = "DELETE FROM appointments WHERE patient_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, patientId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("deleteAppointmentsByPatient error: " + e.getMessage());
        }
    }

    private void deleteAppointmentsByDoctor(String doctorId) {
        String sql = "DELETE FROM appointments WHERE doctor_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, doctorId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("deleteAppointmentsByDoctor error: " + e.getMessage());
        }
    }

    public Appointment getAppointment(String appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            Appointment a = null;
            if (rs.next()) {
                a = rowToAppointment(rs);
            }
            rs.close();
            ps.close();
            return a;

        } catch (SQLException e) {
            System.out.println("getAppointment error: " + e.getMessage());
            return null;
        }
    }

    public List<Appointment> getAppointmentsForPatients(String patientId) {
        List<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE patient_id=? ORDER BY date_time_key";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rowToAppointment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("getAppointmentsForPatients error: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentsForDoctor(String doctorId) {
        List<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE doctor_id=? ORDER BY date_time_key";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rowToAppointment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("getAppointmentsForDoctor error: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments ORDER BY date_time_key";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(rowToAppointment(rs));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getAllAppointments error: " + e.getMessage());
        }
        return list;
    }

    // ── Billing & Reports ────────────────────────────────────────

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(bill_amount), 0) AS total FROM patients";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            double total = rs.getDouble("total");
            rs.close();
            st.close();
            return total;
        } catch (SQLException e) {
            System.out.println("getTotalRevenue error: " + e.getMessage());
            return 0.0;
        }
    }

    public Map<String, Integer> getAppointmentStatusCounts() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        counts.put("Pending",   0);
        counts.put("Confirm",   0);
        counts.put("Complete",  0);
        counts.put("Cancelled", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM appointments GROUP BY status";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String status = rs.getString("status");
                int cnt = rs.getInt("cnt");
                counts.put(status, cnt);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getAppointmentStatusCounts error: " + e.getMessage());
        }
        return counts;
    }

    public Map<String, Integer> getAppointmentsPerDoctor() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String sql = "SELECT doctor_id, COUNT(*) AS cnt FROM appointments GROUP BY doctor_id";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                map.put(rs.getString("doctor_id"), rs.getInt("cnt"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getAppointmentsPerDoctor error: " + e.getMessage());
        }
        return map;
    }

    // ── ID Generators ────────────────────────────────────────────

    public String generatePatientId() {
        int max = 0;
        List<Patient> allPatients = getAllPatients();
        for (int i = 0; i < allPatients.size(); i++) {
            String key = allPatients.get(i).getPatientId();
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) {
                    max = num;
                }
            } catch (NumberFormatException e) {
                // Ajeeb ID, skip karo
            }
        }
        return String.format("P%03d", max + 1);
    }

    public String generateAppointmentId() {
        int max = 0;
        List<Appointment> all = getAllAppointments();
        for (int i = 0; i < all.size(); i++) {
            String key = all.get(i).getAppointmentId();
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) {
                    max = num;
                }
            } catch (NumberFormatException e) {
                // Ajeeb ID, skip karo
            }
        }
        return String.format("A%03d", max + 1);
    }

    // ── Row to Object Helpers ────────────────────────────────────

    private Patient rowToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient(
                rs.getString("patient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("password")
        );
        p.setSymptoms(rs.getString("symptoms"));
        p.setPrescribedMedicine(rs.getString("medicine"));
        p.setBillAmount(rs.getDouble("bill_amount"));

        String historyText = rs.getString("med_history");
        if (historyText != null && !historyText.isEmpty()) {
            String[] lines = historyText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    p.addMedicalHistoryEntry(line);
                }
            }
        }
        return p;
    }

    private Doctor rowToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getString("doctor_id"),
                rs.getString("name"),
                rs.getString("specialty"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("password")
        );
    }

    private Appointment rowToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment(
                rs.getString("appointment_id"),
                rs.getString("patient_id"),
                rs.getString("doctor_id"),
                rs.getString("date_time_key")
        );
        a.setStatus(Appointment.Status.valueOf(rs.getString("status")));
        a.setNotes(rs.getString("notes"));
        return a;
    }

    // ── Medical History Text Conversion ─────────────────────────

    private String historyListToText(List<String> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < historyList.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(historyList.get(i));
        }
        return sb.toString();
    }

    // ── Compatibility Methods ─────────────────────────────────────

    /**
     * Pehle .dat file system mein saveAll() use hota tha.
     * MySQL mein har operation turant save hota hai, isliye
     * yeh method kuch nahi karta — sirf purana code todne se bachata hai.
     */
    public void saveAll() {
        // MySQL mein alag se save karne ki zaroorat nahi
        // Har INSERT / UPDATE / DELETE apne aap commit ho jaata hai
    }

    // ── Connection Close ─────────────────────────────────────────

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection band ho gayi.");
            } catch (SQLException e) {
                System.out.println("Connection close error: " + e.getMessage());
            }
        }
    }
}