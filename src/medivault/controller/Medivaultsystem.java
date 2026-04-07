package medivault.controller;
import medivault.db.DatabaseManager;
import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import javax.print.Doc;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Medivaultsystem {
    private final DatabaseManager db=DatabaseManager.getInstance();
    //this reference will be final and set once

    //LOGIN
    public Patient loginPatient(String email,String password){
        Patient p=db.findPatientByemail(email);
        if(p!=null && p.getPassword().equals(password)){
            return p;
        }
        return null;
    }

    public Doctor loginDoctor(String email,String password){
        Doctor d=db.findDoctorByEmail(email);
        if(d!=null && d.getPassword().equals(password)){
            return d;
        }
        return null;
    }

    //REGISTER
    public String registerPatient(String name,String gender,
                                  String phone,String email,String password,int age){
        if(name==null || name.trim().isEmpty()){
            return "Name cannot be empty.";
        }
        if(age<0 || age>100){
            return "Enter a valid age.";
        }
        if(phone==null || phone.trim().length()!=10){
            return"phone number should be 10 digit.";
        }
        if(email==null || !email.contains("@")){
            return "Enter a valid email.";
        }
        if(password==null || password.length()<6){
            return "password should be minimum 6 letter.";
        }

        //check for duplicate user registration
        if(db.findPatientByemail(email)!=null){
            return "This email is already registered try to login";
        }

        //after all the check
        String newID=db.generatePatientId(); //generate random id for patient
        //calling for a new object in Patient class
        Patient p=new Patient(newID,name.trim(),age,gender,phone.trim(),email.trim(),password);
        db.addPatient(p);
        return null;
    }

    //APPOINTMENT
    public String bookAppointment(String patientID,String doctorID,String date,String time){
        Patient p=db.getPatient(patientID);
        if(p==null){
            return "The user is not found!";
        }
        Doctor d=db.getDoctor(doctorID);
        if(d==null){
            return "Doctor not found";
        }
        if(date==null || date.isEmpty()){
            return "Please Select a date";
        }if(time==null || time.isEmpty()){
            return "Please Select a time";
        }
        try{
            LocalDate apptDate=LocalDate.parse(date);
            if(apptDate.isBefore(LocalDate.now())){
                return "Cannot book an appointment in the past";
            }
        } catch (Exception e) {
            return "Invalid format.Use YYYY-MM-DD";
        }

        String apptID=db.generateAppointmentId();
        Appointment a=new Appointment(apptID,patientID,p.getName(),
                doctorID,d.getDoctorId(),date,time);
        a.setStatus(Appointment.Status.Confirm);
        //booking is confirmed now check if slot is full

        boolean saved=db.addAppointment(a);
        if(!saved){
            return "The time slot is already booked please choose another time";
        }
        //adding the detail in the medical report
        p.addMedicalHistoryEntry(date+"Appointment booked with "+d.getName()+" at "+time);
        db.updatePatient(p);
        return null;

    }
    public String cancelAppointment(String appointmentID){
        for(Appointment a:db.getAllAppointments()){
            if(a.getAppointmentId().equals(appointmentID)){
                a.setStatus(Appointment.Status.Cancelled);
                db.updateAppointment(a);
                return null;
            }
        }
        return "Appointment not found.";
    }

    //PRESCRIPTIONS
    public String updatePrescription(String patientID,String symptoms,
                                     String medicine,int durationDay,double billamt,
                                     String note){
        Patient p=db.getPatient(patientID);
        if(p==null){
            return "Patient not found";
        }
        p.setSymptoms(symptoms);
        p.setPrescribedMedicine(medicine);
        p.setTreatmentDurationDays(durationDay);
        p.setBillAmount(billamt);

        //adding extra note to history
        String today=LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        p.addMedicalHistoryEntry(today+" "+note);

        db.updatePatient(p);
        return null;
    }

    public String markBillPaid(String patientID){
        Patient p=db.getPatient(patientID);
        if(p==null) return "Patient not found!";
        p.setBillPaid(true);
        db.updatePatient(p);
        return null;
    }

    public Patient getPatient(String id){return db.getPatient(id);}
    public Doctor getdoctor(String id){ return db.getDoctor(id);}
    public List<Patient>getAllPatients(){return db.getAllPatients();}
    public List<Doctor>getAllDoctors(){return db.getAllDoctors();}
    public List<Patient>searchPatient(String q){return db.searchPatients(q);}

    public List<Appointment>getAppointmentsForPatient(String patientID){
        return db.getAppointmentsForPatients(patientID);
    }
    public List<Appointment>getAppointmentsForDoctor(String doctorID){
        return db.getAppointmentsForDoctor(doctorID);
    }

    //return the appointment of a doctor on that specific day
    public List<Appointment>getTodayAppointmentsForDoctor(String doctorID){
        String today=LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<Appointment>todayList=new ArrayList<>();
        for(Appointment a:db.getAppointmentsForDoctor(doctorID)){
            if(a.getDate().equals(today)){
                todayList.add(a);
            }
        }
        return todayList;
    }

}
