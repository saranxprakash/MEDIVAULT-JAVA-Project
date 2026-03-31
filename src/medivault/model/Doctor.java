package medivault.model;

import java.io.Serializable;


public class Doctor implements Serializable {
    //declare doctor data variable
    private String doctorID,name,specialization,email;
    private String password;
    private String phone;

    public Doctor(String doctorID, String name, String specialization,
                  String phone, String email, String password) {
        this.doctorID       = doctorID;
        this.name           = name;
        this.specialization = specialization;
        this.phone          = phone;
        this.email          = email;
        this.password       = password;
    }

    // Getters and Setters
    public String getDoctorId()                  { return doctorID; }
    public void   setDoctorId(String id)         { this.doctorID = id; }

    public String getName()                      { return name; }
    public void   setName(String name)           { this.name = name; }

    public String getSpecialization()            { return specialization; }
    public void   setSpecialization(String spec) { this.specialization = spec; }

    public String getPhone()                     { return phone; }
    public void   setPhone(String phone)         { this.phone = phone; }

    public String getEmail()                     { return email; }
    public void   setEmail(String email)         { this.email = email; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    @Override
    public String toString(){
        return "Dr."+name+"("+specialization+")";
    }

}
