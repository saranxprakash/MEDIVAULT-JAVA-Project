package medivault;
import medivault.model.Patient;
import medivault.model.Doctor;
import medivault.model.Appointment;
public class Main {
    public static void main(String args[]){
        System.out.println("Medivault is starting....:)");
        Patient p = new Patient("P001", "Rahul Verma", 35,
                "Male","9988776655",
                "rahul@email.com", "patient123");

        System.out.println(p);
    }
}
