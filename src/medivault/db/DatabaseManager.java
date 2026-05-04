package medivault.db;

import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    // File paths
    private static final String Data_dir = "data/";
    private static final String Patient_file = Data_dir + "Patient.dat";
    private static final String Doctor_file = Data_dir + "Doctor.dat";
    private static final String Appoint_file = Data_dir + "Appointment.dat";

    // In-memory maps
    private Map<String, Patient> patient = new HashMap<>();
    private Map<String, Doctor> doctor = new HashMap<>();
    private Map<String, Appointment> appointments = new HashMap<>();

    private static DatabaseManager instance;

    private DatabaseManager() {
        new File(Data_dir).mkdirs();
        loadAll();
        seedDemoData();
    }

    // ── Persistence ─────────────────────────────────────────────

    public void saveAll() {
        saveFile(Patient_file, patient);
        saveFile(Doctor_file, doctor);
        saveFile(Appoint_file, appointments);
    }

    private void saveFile(String path, Object data) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.out.println("Couldn't save file: " + path);
        }
    }

    public void loadAll() {
        Map<?, ?> p = (Map<?, ?>) loadFile(Patient_file);
        Map<?, ?> d = (Map<?, ?>) loadFile(Doctor_file);
        Map<?, ?> a = (Map<?, ?>) loadFile(Appoint_file);

        if (p != null) patient = (Map<String, Patient>) p;
        if (d != null) doctor = (Map<String, Doctor>) d;
        if (a != null) appointments = (Map<String, Appointment>) a;
    }

    private Object loadFile(String path) {
        File f = new File(path);
        if (!f.exists()) return null;
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(path))) {
            return ois.readObject();
        } catch (Exception e) {
            System.out.println("Couldn't load the file: " + path);
            return null;
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ── Seed Data ────────────────────────────────────────────────

    private void seedDemoData() {
        if (!doctor.isEmpty()) return;

        Doctor d1 = new Doctor("D001", "Dr. Ankitha",
                "Cardiology", "9876543210",
                "anita@medivault.com", "doctor123");
        Doctor d2 = new Doctor("D002", "Dr. Rajan Mehta",
                "Neurology", "9876543211",
                "rajan@medivault.com", "doctor123");
        Doctor d3 = new Doctor("D003", "Dr. Priya Nair",
                "Pediatrics", "9876543212",
                "priya@medivault.com", "doctor123");

        doctor.put(d1.getDoctorId(), d1);
        doctor.put(d2.getDoctorId(), d2);
        doctor.put(d3.getDoctorId(), d3);

        Patient p1 = new Patient("P001", "Rahul Verma", 35,
                "Male", "9988776655",
                "rahul@email.com", "patient123");
        p1.setSymptoms("Chest pain, shortness of breath");
        p1.setPrescribedMedicine("Aspirin 75mg");
        p1.setBillAmount(4500.00);
        p1.addMedicalHistoryEntry("2025-01-10: Initial consultation");
        patient.put(p1.getPatientId(), p1);

        saveAll();
    }

    // ── Patient Operations ───────────────────────────────────────

    public boolean addPatient(Patient p) {
        if (patient.containsKey(p.getPatientId())) return false;
        patient.put(p.getPatientId(), p);
        saveAll();
        return true;
    }

    public boolean updatePatient(Patient p) {
        if (!patient.containsKey(p.getPatientId())) return false;
        patient.put(p.getPatientId(), p);
        saveAll();
        return true;
    }

    /**
     * ADMIN: Remove a patient and all their appointments.
     * Returns false if the patient ID does not exist.
     */
    public boolean deletePatient(String patientId) {
        if (!patient.containsKey(patientId)) return false;
        patient.remove(patientId);
        // Cascade-delete appointments belonging to this patient
        appointments.entrySet().removeIf(e ->
                e.getValue().getPatientId().equals(patientId));
        saveAll();
        return true;
    }

    public Patient getPatient(String id) {
        return patient.get(id);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patient.values());
    }

    public Patient findPatientByemail(String email) {
        for (Patient p : patient.values()) {
            if (p.getEmail().equalsIgnoreCase(email)) return p;
        }
        return null;
    }

    public List<Patient> searchPatients(String query) {
        List<Patient> results = new ArrayList<>();
        String q = query.toLowerCase();
        for (Patient p : patient.values()) {
            if (p.getName().toLowerCase().contains(q)
                    || p.getPatientId().toLowerCase().contains(q)
                    || p.getPhone().contains(q)) {
                results.add(p);
            }
        }
        return results;
    }

    // ── Doctor Operations ────────────────────────────────────────

    public Doctor getDoctor(String id) {
        return doctor.get(id);
    }

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(doctor.values());
    }

    public Doctor findDoctorByEmail(String email) {
        for (Doctor d : doctor.values()) {
            if (d.getEmail().equalsIgnoreCase(email)) return d;
        }
        return null;
    }

    /**
     * ADMIN: Add a new doctor.
     * Returns false if the doctor ID already exists.
     */
    public boolean addDoctor(Doctor d) {
        if (doctor.containsKey(d.getDoctorId())) return false;
        doctor.put(d.getDoctorId(), d);
        saveAll();
        return true;
    }

    /**
     * ADMIN: Remove a doctor and all their appointments.
     * Returns false if the doctor ID does not exist.
     */
    public boolean deleteDoctor(String doctorId) {
        if (!doctor.containsKey(doctorId)) return false;
        doctor.remove(doctorId);
        // Cascade-delete appointments for this doctor
        appointments.entrySet().removeIf(e ->
                e.getValue().getDoctorId().equals(doctorId));
        saveAll();
        return true;
    }

    /** ADMIN: Generate next doctor ID like D004, D005 … */
    public String generateDoctorId() {
        int max = 0;
        for (String key : doctor.keySet()) {
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("D%03d", max + 1);
    }

    // ── Appointment Operations ───────────────────────────────────

    public boolean addAppointment(Appointment a) {
        for (Appointment existing : appointments.values()) {
            if (existing.getDoctorId().equals(a.getDoctorId())
                    && existing.getDateTimeKey().equals(a.getDateTimeKey())
                    && existing.getStatus() != Appointment.Status.Cancelled) {
                return false; // slot taken
            }
        }
        appointments.put(a.getAppointmentId(), a);
        saveAll();
        return true;
    }

    public boolean updateAppointment(Appointment a) {
        if (!appointments.containsKey(a.getAppointmentId())) return false;
        appointments.put(a.getAppointmentId(), a);
        saveAll();
        return true;
    }

    /** ADMIN: Hard-delete an appointment by ID. */
    public boolean deleteAppointment(String appointmentId) {
        if (!appointments.containsKey(appointmentId)) return false;
        appointments.remove(appointmentId);
        saveAll();
        return true;
    }

    public List<Appointment> getAppointmentsForPatients(String patientId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments.values()) {
            if (a.getPatientId().equals(patientId)) list.add(a);
        }
        return list;
    }

    public List<Appointment> getAppointmentsForDoctor(String doctorId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments.values()) {
            if (a.getDoctorId().equals(doctorId)) list.add(a);
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments.values());
    }

    // ── Billing Overview (Admin) ─────────────────────────────────

    /** Total revenue: sum of all patient bill amounts. */
    public double getTotalRevenue() {
        double total = 0;
        for (Patient p : patient.values()) total += p.getBillAmount();
        return total;
    }

    /** Count of appointments by status. */
    public Map<String, Integer> getAppointmentStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Pending",   0);
        counts.put("Confirm",   0);
        counts.put("Complete",  0);
        counts.put("Cancelled", 0);
        for (Appointment a : appointments.values()) {
            String key = a.getStatus().toString();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    /** Number of appointments per doctor (doctorId → count). */
    public Map<String, Integer> getAppointmentsPerDoctor() {
        Map<String, Integer> map = new HashMap<>();
        for (Appointment a : appointments.values()) {
            String did = a.getDoctorId();
            map.put(did, map.getOrDefault(did, 0) + 1);
        }
        return map;
    }

    // ── ID Generators ────────────────────────────────────────────

    public String generatePatientId() {
        int max = 0;
        for (String key : patient.keySet()) {
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("P%03d", max + 1);
    }

    public String generateAppointmentId() {
        int max = 0;
        for (String key : appointments.keySet()) {
            try {
                int num = Integer.parseInt(key.substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("A%03d", max + 1);
    }
}