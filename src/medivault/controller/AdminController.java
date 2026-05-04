package medivault.controller;

import medivault.db.AuditLogManager;
import medivault.db.DatabaseManager;
import medivault.model.Admin;
import medivault.model.Appointment;
import medivault.model.AuditLog;
import medivault.model.AuditLog.Category;
import medivault.model.Doctor;
import medivault.model.Patient;

import java.util.*;

/**
 * AdminController — all business logic for the admin module.
 * Every mutating operation is automatically audit-logged.
 */
public class AdminController {

    private final DatabaseManager  db  = DatabaseManager.getInstance();
    private final AuditLogManager  aud = AuditLogManager.getInstance();

    // ═══════════════════════════════════════════════════════════════
    //  SECURITY
    // ═══════════════════════════════════════════════════════════════

    public void logLogin()  {
        aud.log(Category.SECURITY, "LOGIN",  "Admin", "Successful login");
    }
    public void logLogout(String reason) {
        aud.log(Category.SECURITY, "LOGOUT", "Admin", reason);
    }

    /**
     * Change admin password.
     * Returns null on success, error string on failure.
     */
    public String changeAdminPassword(String currentPassword,
                                      String newPassword,
                                      String confirmPassword) {
        if (!Admin.authenticate("admin", currentPassword))
            return "Current password is incorrect.";
        if (newPassword == null || newPassword.length() < 6)
            return "New password must be at least 6 characters.";
        if (!newPassword.equals(confirmPassword))
            return "New passwords do not match.";
        if (newPassword.equals(currentPassword))
            return "New password must be different from the current one.";

        Admin.updatePassword(newPassword);
        aud.log(Category.SECURITY, "PASSWORD_CHANGE", "Admin",
                "Admin password changed successfully");
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  AUDIT LOG
    // ═══════════════════════════════════════════════════════════════

    public List<AuditLog> getAllLogs()                   { return aud.getAll(); }
    public List<AuditLog> searchLogs(String keyword)     { return aud.search(keyword); }
    public List<AuditLog> getLogsByCategory(Category c)  { return aud.getByCategory(c); }
    public List<AuditLog> getRecentLogs(int n)           { return aud.getRecent(n); }
    public int            getTotalLogCount()              { return aud.getTotalCount(); }

    public void clearAuditLog() {
        aud.log(Category.SECURITY, "CLEAR_LOG", "AuditLog",
                "All logs cleared by admin");
        aud.clearAll();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PATIENT CRUD
    // ═══════════════════════════════════════════════════════════════

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
        aud.log(Category.PATIENT, "ADD",
                "Patient " + id + " — " + name,
                "Age=" + age + ", Phone=" + phone + ", Bill=Rs." + bill);
        return null;
    }

    public String updatePatient(String patientId, String name, int age,
                                String gender, String phone, String email,
                                String symptoms, String medicine,
                                int treatmentDays, double bill, boolean billPaid) {
        Patient p = db.getPatient(patientId);
        if (p == null)       return "Patient not found.";
        if (name.isBlank())  return "Name is required.";
        if (phone.isBlank()) return "Phone is required.";
        if (email.isBlank()) return "Email is required.";

        Patient existing = db.findPatientByemail(email);
        if (existing != null && !existing.getPatientId().equals(patientId))
            return "Email already used by another patient.";

        String oldName = p.getName();
        p.setName(name);      p.setAge(age);          p.setGender(gender);
        p.setPhone(phone);    p.setEmail(email);       p.setSymptoms(symptoms);
        p.setPrescribedMedicine(medicine);
        p.setTreatmentDurationDays(treatmentDays);
        p.setBillAmount(bill); p.setBillPaid(billPaid);
        db.updatePatient(p);

        aud.log(Category.PATIENT, "UPDATE",
                "Patient " + patientId + " — " + oldName,
                "Name→" + name + ", Bill→Rs." + bill + ", Paid→" + billPaid);
        return null;
    }

    public String deletePatient(String patientId) {
        Patient p = db.getPatient(patientId);
        if (p == null) return "Patient not found.";
        String name = p.getName();
        if (!db.deletePatient(patientId)) return "Delete failed.";
        aud.log(Category.PATIENT, "DELETE",
                "Patient " + patientId + " — " + name,
                "All appointments also removed");
        return null;
    }

    public List<Patient> getAllPatients()              { return db.getAllPatients(); }
    public List<Patient> searchPatients(String query) { return db.searchPatients(query); }
    public Patient       getPatient(String id)        { return db.getPatient(id); }

    // ═══════════════════════════════════════════════════════════════
    //  DOCTOR CRUD
    // ═══════════════════════════════════════════════════════════════

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
        aud.log(Category.DOCTOR, "ADD",
                "Doctor " + id + " — " + name,
                "Spec=" + specialization + ", Phone=" + phone);
        return null;
    }

    public String updateDoctor(String doctorId, String name,
                               String specialization, String phone, String email) {
        Doctor d = db.getDoctor(doctorId);
        if (d == null)                return "Doctor not found.";
        if (name.isBlank())           return "Name is required.";
        if (specialization.isBlank()) return "Specialisation is required.";
        if (phone.isBlank())          return "Phone is required.";
        if (email.isBlank())          return "Email is required.";

        Doctor existing = db.findDoctorByEmail(email);
        if (existing != null && !existing.getDoctorId().equals(doctorId))
            return "Email already used by another doctor.";

        String oldName = d.getName();
        d.setName(name); d.setSpecialization(specialization);
        d.setPhone(phone); d.setEmail(email);
        db.saveAll();
        aud.log(Category.DOCTOR, "UPDATE",
                "Doctor " + doctorId + " — " + oldName,
                "Name→" + name + ", Spec→" + specialization);
        return null;
    }

    public String deleteDoctor(String doctorId) {
        Doctor d = db.getDoctor(doctorId);
        if (d == null) return "Doctor not found.";
        String name = d.getName();
        if (!db.deleteDoctor(doctorId)) return "Delete failed.";
        aud.log(Category.DOCTOR, "DELETE",
                "Doctor " + doctorId + " — " + name,
                "All appointments also removed");
        return null;
    }

    public List<Doctor> getAllDoctors()          { return db.getAllDoctors(); }
    public Doctor       getDoctor(String id)     { return db.getDoctor(id); }

    // ═══════════════════════════════════════════════════════════════
    //  APPOINTMENT CRUD
    // ═══════════════════════════════════════════════════════════════

    public List<Appointment> getAllAppointments() { return db.getAllAppointments(); }

    public String updateAppointmentStatus(String appointmentId,
                                          Appointment.Status newStatus) {
        for (Appointment a : db.getAllAppointments()) {
            if (a.getAppointmentId().equals(appointmentId)) {
                Appointment.Status old = a.getStatus();
                a.setStatus(newStatus);
                db.updateAppointment(a);
                aud.log(Category.APPOINTMENT, "UPDATE_STATUS",
                        "Appointment " + appointmentId
                        + " (" + a.getPatientName() + " / " + a.getDoctorName() + ")",
                        old + " → " + newStatus);
                return null;
            }
        }
        return "Appointment not found.";
    }

    public String deleteAppointment(String appointmentId) {
        Appointment target = null;
        for (Appointment a : db.getAllAppointments())
            if (a.getAppointmentId().equals(appointmentId)) { target = a; break; }
        if (target == null || !db.deleteAppointment(appointmentId))
            return "Appointment not found.";
        aud.log(Category.APPOINTMENT, "DELETE",
                "Appointment " + appointmentId
                + " (" + target.getPatientName() + " / " + target.getDoctorName() + ")",
                target.getDate() + " " + target.getTime());
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  BILLING
    // ═══════════════════════════════════════════════════════════════

    public String updateBilling(String patientId, double newAmount, boolean paid) {
        Patient p = db.getPatient(patientId);
        if (p == null) return "Patient not found.";
        double  oldAmt  = p.getBillAmount();
        boolean oldPaid = p.isBillPaid();
        p.setBillAmount(newAmount);
        p.setBillPaid(paid);
        db.updatePatient(p);
        aud.log(Category.BILLING, "UPDATE",
                "Patient " + patientId + " — " + p.getName(),
                "Bill Rs." + oldAmt + "→Rs." + newAmount
                + ", Paid " + oldPaid + "→" + paid);
        return null;
    }

    public double getTotalRevenue()  { return db.getTotalRevenue(); }
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

    public Map<String, Integer> getAppointmentsPerDoctorByName() {
        Map<String, Integer> raw   = db.getAppointmentsPerDoctor();
        Map<String, Integer> named = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : raw.entrySet()) {
            Doctor d = db.getDoctor(e.getKey());
            named.put(d != null ? d.getName() : e.getKey(), e.getValue());
        }
        return named;
    }

    public List<Patient> getPatientsSortedByBill() {
        List<Patient> list = new ArrayList<>(db.getAllPatients());
        list.sort((a, b) -> Double.compare(b.getBillAmount(), a.getBillAmount()));
        return list;
    }
}