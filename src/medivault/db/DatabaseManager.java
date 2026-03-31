package medivault.db;

import medivault.model.Appointment;
import medivault.model.Doctor;
import medivault.model.Patient;

import java.io.*;
import java.util.*;

public class  DatabaseManager {

    //file path is stored in here
    private static final String Data_dir="data/";
    private static final String Patient_file=Data_dir+"Patient.dat";
    private static final String Doctor_file=Data_dir+"Doctor.dat";
    private static final String Appoint_file=Data_dir+"Appointment.dat";

    //in memory hash function to store and get
    //store details in a key value pair
    private Map<String,Patient>patient=new HashMap<>();
    private Map<String,Doctor>doctor=new HashMap<>();
    private Map<String,Appointment>appointments=new HashMap<>();

    private static DatabaseManager instance;

    //constructor creation
    private DatabaseManager(){
        new File(Data_dir).mkdirs();//create a folder
        loadAll();                   // load any saved data from disk
        seedDemoData();             //add demo doctors if first run
    }

    //saving the file
    public void saveAll(){
        saveFile(Patient_file,patient);
        saveFile(Doctor_file,doctor);
        saveFile(Appoint_file,appointments);
    }
    private void saveFile(String path,Object data){
        try(ObjectOutputStream oos=
                    new ObjectOutputStream(new FileOutputStream(path))){
            oos.writeObject(data);  //convert object to bytes and file
        } catch (IOException e) {
            System.out.println("Couldn't save file:"+path);
        }
    }

    //loading all the data
    public void loadAll() {
        Map<?, ?> p = (Map<?, ?>) loadFile(Patient_file);
        Map<?, ?> d = (Map<?, ?>) loadFile(Doctor_file);
        Map<?, ?> a = (Map<?, ?>) loadFile(Appoint_file);

        if (p != null) patient = (Map<String, Patient>) p;
        if (d != null) doctor = (Map<String, Doctor>) d;
        if (a != null) appointments = (Map<String, Appointment>) a;

    }

    private Object loadFile(String path){
        File f=new File(path);
        if(!f.exists()) return null; //if file not created
        try (ObjectInputStream ois=
                     new ObjectOutputStream(new FileInputStream(path))){
            return ois.readObject();
        } catch (Exception e) {
            System.out.println("couldn't load the file:"+path);
            return null;
        }
    }
    public static DatabaseManager getInstance(){
        if(instance==null){
            instance=new DatabaseManager();
        }
        return instance;
    }

    //Runs once on first launch when doctor map is empty
    private void seedDemoData(){
        if(!doctor.isEmpty()) return ; //if it is already added or else
        Doctor d1 = new Doctor("D001", "Dr. Ankitha",
                "Cardiology", "9876543210",
                "anita@medivault.com", "doctor123");
        Doctor d2 = new Doctor("D002", "Dr. Rajan Mehta",
                "Neurology", "9876543211",
                "rajan@medivault.com", "doctor123");
        Doctor d3 = new Doctor("D003", "Dr. Priya Nair",
                "Pediatrics", "9876543212",
                "priya@medivault.com", "doctor123");

        doctor.put(d1.getDoctorId(),d1);
        doctor.put(d2.getDoctorId(),d2);
        doctor.put(d3.getDoctorId(),d3);

        Patient p1 = new Patient("P001", "Rahul Verma", 35,
                "Male", "9988776655",
                "rahul@email.com", "patient123");
        p1.setSymptoms("Chest pain, shortness of breath");
        p1.setPrescribedMedicine("Aspirin 75mg");
        p1.setBillAmount(4500.00);
        p1.addMedicalHistoryEntry("2025-01-10: Initial consultation");
        patients.put(p1.getPatientId(), p1);

        saveAll();
    }
}
