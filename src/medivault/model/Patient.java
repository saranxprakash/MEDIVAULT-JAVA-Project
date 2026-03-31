package medivault.model;

import java.io.Serializable;
import java.util.*;


public class Patient implements Serializable {
    //declare patient variable that are required and keep all private

    private String PatientID,name,gender,email,phone,symptoms,prescribedMedicine;
    private String prescribe_medicine,password;
    private int age,treatment_duration;
    private double billamt;
    private boolean billpaid;
    private List<String> medical_history;

    public Patient() {
        this.medical_history = new ArrayList<>();
    }

    public Patient(String patientId, String name, int age,
                   String gender, String phone, String email, String password) {
        this();                      // calls the empty constructor above first
        this.PatientID= patientId;
        this.name      = name;
        this.age       = age;
        this.gender    = gender;
        this.phone     = phone;
        this.email     = email;
        this.password  = password;
    }

    // 3. Getters and Setters
    public String getPatientId()             { return PatientID; }
    public void   setPatientId(String id)    { this.PatientID = id; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public int    getAge()                   { return age; }
    public void   setAge(int age)            { this.age = age; }

    public String getGender()                { return gender; }
    public void   setGender(String gender)   { this.gender = gender; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String phone)     { this.phone = phone; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public String getSymptoms()              { return symptoms; }
    public void   setSymptoms(String s)      { this.symptoms = s; }

    public String getPrescribedMedicine()            { return prescribedMedicine; }
    public void   setPrescribedMedicine(String med)  { this.prescribedMedicine = med; }

    public int    getTreatmentDurationDays()         { return treatment_duration; }
    public void   setTreatmentDurationDays(int days) { this.treatment_duration = days; }

    public double getBillAmount()            { return billamt; }
    public void   setBillAmount(double bill) { this.billamt= bill; }

    public boolean isBillPaid()              { return billpaid; }
    public void    setBillPaid(boolean paid) { this.billpaid = paid; }

    public List<String> getMedicalHistory()  { return medical_history;}
    public void addMedicalHistoryEntry(String entry) {
        medical_history.add(entry);
    }
    @Override
    public String toString(){
        return "patient{id="+PatientID+"\n name="+name+"\nAge="+age+"}";
    }
}
