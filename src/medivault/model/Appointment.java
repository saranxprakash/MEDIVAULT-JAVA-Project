package medivault.model;

import java.io.Serializable;

public class Appointment implements Serializable {

    public enum Status {
        Pending, Confirm, Complete, Cancelled
    }

    private Status status;

    private String appointmentID, patientID, patient_name,
            doctorID, doctor_name;

    private String date, time;

    // Added for compatibility
    private String notes;

    public Appointment() {
        this.status = Status.Pending;
        this.notes = "";
    }

    public Appointment(String appointmentId, String patientId, String patientName,
                       String doctorId, String doctorName,
                       String date, String time) {

        this();

        this.appointmentID = appointmentId;
        this.patientID = patientId;
        this.patient_name = patientName;
        this.doctorID = doctorId;
        this.doctor_name = doctorName;
        this.date = date;
        this.time = time;
    }

    // Added constructor for database compatibility
    public Appointment(String appointmentId,
                       String patientId,
                       String doctorId,
                       String dateTimeKey) {

        this();

        this.appointmentID = appointmentId;
        this.patientID = patientId;
        this.doctorID = doctorId;

        String[] parts = dateTimeKey.split(" ");

        if (parts.length >= 2) {
            this.date = parts[0];
            this.time = parts[1];
        }
    }

    // Getters and Setters
    public String getAppointmentId() {
        return appointmentID;
    }

    public void setAppointmentId(String id) {
        this.appointmentID = id;
    }

    public String getPatientId() {
        return patientID;
    }

    public void setPatientId(String id) {
        this.patientID = id;
    }

    public String getPatientName() {
        return patient_name;
    }

    public void setPatientName(String n) {
        this.patient_name = n;
    }

    public String getDoctorId() {
        return doctorID;
    }

    public void setDoctorId(String id) {
        this.doctorID = id;
    }

    public String getDoctorName() {
        return doctor_name;
    }

    public void setDoctorName(String n) {
        this.doctor_name = n;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Added for compatibility
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper: combines date + time into one key
    public String getDateTimeKey() {
        return date + " " + time;
    }

    @Override
    public String toString() {
        return "Appointment{" + appointmentID
                + ", patient=" + patient_name
                + ", doctor=" + doctor_name
                + ", " + date + " " + time + "}";
    }
}