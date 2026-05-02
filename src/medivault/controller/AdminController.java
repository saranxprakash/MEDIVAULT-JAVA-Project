package medivault.controller;

import medivault.db.DatabaseManager;
import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import java.util.*;

/**
 * AdminController — single source of truth for all admin operations.
 * The AdminDashboard calls only these methods; it never touches DatabaseManager directly.
 */
public class AdminController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    // ═══════════════════════════════════════════════════════════════
    //  PATIENT CRUD
    // ═══════════════════════════════════════════════════════════════

    /** Validate + add a new patient. Returns null on success, error string on failure. */
    public String addPatient(String name, int age, String gender,
                             String phone, String email,
                             String password, double bill) {
        if (name.isBlank())     return "Name is required.";
        if (phone.isBlank())    return "Phone is required.";
        if (email.isBlank())    return "Email is required.";
        if (password.isBlank()) return "Password is required.";
        if (db.findPatientByemail(email) != null) return "Email already registered.";

        String id = db.generatePatientId();
        Patient p = new Patient(id, name, age, gender, phone, email, password);
        p.setBillAmount(bill);
        db.addPatient(p);
        return null;
    }

    /**
     * Update every editable field of an existing patient.
     * Returns null on success, error string on failure.
     */
    public String updatePatient(String patientId, String name, int age,
                                String gender, String phone, String email,
                                String symptoms, String medicine,
                                int treatmentDays, double bill, boolean billPaid) {
        Patient p = db.getPatient(patientId);
        if (p == null) return "Patient not found.";
        if (name.isBlank())  return "Name is required.";
        if (phone.isBlank()) return "Phone is required.";
        if (email.isBlank()) return "Email is required.";

        // Check email uniqueness (allow same patient to keep their email)
        Patient existing = db.findPatientByemail(email);
        if (existing != null && !existing.getPatientId().equals(patientId))
            return "Email already used by another patient.";

        p.setName(name);
        p.setAge(age);
        p.setGender(gender);
        p.setPhone(phone);
        p.setEmail(email);
        p.setSymptoms(symptoms);
        p.setPrescribedMedicine(medicine);
        p.setTreatmentDurationDays(treatmentDays);
        p.setBillAmount(bill);
        p.setBillPaid(billPaid);
        db.updatePatient(p);
        return null;
    }

    /** Delete patient + cascade appointments. Returns error string or null. */
    public String deletePatient(String patientId) {
        if (!db.deletePatient(patientId)) return "Patient not found.";
        return null;
    }

    public List<Patient> getAllPatients()               { return db.getAllPatients(); }
    public List<Patient> searchPatients(String query)  { return db.searchPatients(query); }
    public Patient       getPatient(String id)         { return db.getPatient(id); }

    // ═══════════════════════════════════════════════════════════════
    //  DOCTOR CRUD
    // ═══════════════════════════════════════════════════════════════

    /** Validate + add a new doctor. Returns null on success, error string on failure. */
    public String addDoctor(String name, String specialization,
                            String phone, String email, String password) {
        if (name.isBlank())           return "Name is required.";
        if (specialization.isBlank()) return "Specialisation is required.";
        if (phone.isBlank())          return "Phone is required.";
        if (email.isBlank())          return "Email is required.";
        if (password.isBlank())       return "Password is required.";
        if (db.findDoctorByEmail(email) != null) return "Email already registered.";

        String id = db.generateDoctorId();
        Doctor d  = new Doctor(id, name, specialization, phone, email, password);
        db.addDoctor(d);
        return null;
    }

    /**
     * Update every editable field of an existing doctor.
     * Returns null on success, error string on failure.
     */
    public String updateDoctor(String doctorId, String name, String specialization,
                               String phone, String email) {
        Doctor d = db.getDoctor(doctorId);
        if (d == null) return "Doctor not found.";
        if (name.isBlank())           return "Name is required.";
        if (specialization.isBlank()) return "Specialisation is required.";
        if (phone.isBlank())          return "Phone is required.";
        if (email.isBlank())          return "Email is required.";

        Doctor existing = db.findDoctorByEmail(email);
        if (existing != null && !existing.getDoctorId().equals(doctorId))
            return "Email already used by another doctor.";

        d.setName(name);
        d.setSpecialization(specialization);
        d.setPhone(phone);
        d.setEmail(email);
        db.saveAll();          // DatabaseManager doesn't have updateDoctor, save directly
        return null;
    }

    /** Delete doctor + cascade appointments. Returns error string or null. */
    public String deleteDoctor(String doctorId) {
        if (!db.deleteDoctor(doctorId)) return "Doctor not found.";
        return null;
    }

    public List<Doctor> getAllDoctors()  { return db.getAllDoctors(); }
    public Doctor       getDoctor(String id) { return db.getDoctor(id); }

    // ═══════════════════════════════════════════════════════════════
    //  APPOINTMENT CRUD
    // ═══════════════════════════════════════════════════════════════

    public List<Appointment> getAllAppointments() { return db.getAllAppointments(); }

    /**
     * Update appointment status (Scheduled → Completed / Cancelled).
     * Returns null on success, error string on failure.
     */
    public String updateAppointmentStatus(String appointmentId,
                                          Appointment.Status newStatus) {
        List<Appointment> all = db.getAllAppointments();
        for (Appointment a : all) {
            if (a.getAppointmentId().equals(appointmentId)) {
                a.setStatus(newStatus);
                db.updateAppointment(a);
                return null;
            }
        }
        return "Appointment not found.";
    }

    /** Hard-delete an appointment. Returns null on success. */
    public String deleteAppointment(String appointmentId) {
        if (!db.deleteAppointment(appointmentId)) return "Appointment not found.";
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  BILLING
    // ═══════════════════════════════════════════════════════════════

    /** Update only the bill amount + paid status for a patient. */
    public String updateBilling(String patientId, double newAmount, boolean paid) {
        Patient p = db.getPatient(patientId);
        if (p == null) return "Patient not found.";
        p.setBillAmount(newAmount);
        p.setBillPaid(paid);
        db.updatePatient(p);
        return null;
    }

    public double getTotalRevenue()                        { return db.getTotalRevenue(); }
    public double getPaidRevenue() {
        return db.getAllPatients().stream()
                .filter(Patient::isBillPaid)
                .mapToDouble(Patient::getBillAmount).sum();
    }
    public double getUnpaidRevenue() {
        return db.getAllPatients().stream()
                .filter(p -> !p.isBillPaid())
                .mapToDouble(Patient::getBillAmount).sum();
    }
    public long getPaidCount() {
        return db.getAllPatients().stream().filter(Patient::isBillPaid).count();
    }
    public long getUnpaidCount() {
        return db.getAllPatients().stream().filter(p -> !p.isBillPaid()).count();
    }

    // ═══════════════════════════════════════════════════════════════
    //  OVERVIEW STATS
    // ═══════════════════════════════════════════════════════════════

    public int getTotalPatients()     { return db.getAllPatients().size(); }
    public int getTotalDoctors()      { return db.getAllDoctors().size(); }
    public int getTotalAppointments() { return db.getAllAppointments().size(); }

    public Map<String, Integer> getAppointmentStatusCounts() {
        return db.getAppointmentStatusCounts();
    }

    /** Returns doctorName → appointmentCount map for the overview chart. */
    public Map<String, Integer> getAppointmentsPerDoctorByName() {
        Map<String, Integer> raw = db.getAppointmentsPerDoctor(); // doctorId → count
        Map<String, Integer> named = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : raw.entrySet()) {
            Doctor d = db.getDoctor(e.getKey());
            String label = (d != null) ? d.getName() : e.getKey();
            named.put(label, e.getValue());
        }
        return named;
    }

    /** List of all patients sorted by bill descending — for billing report. */
    public List<Patient> getPatientsSortedByBill() {
        List<Patient> list = new ArrayList<>(db.getAllPatients());
        list.sort((a, b) -> Double.compare(b.getBillAmount(), a.getBillAmount()));
        return list;
    }
}

