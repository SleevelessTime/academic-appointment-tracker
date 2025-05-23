import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private int id;
    private int studentId;
    private int professorId;
    private LocalDateTime appointmentDateTime;
    private String status;

    // For UI display purposes, not directly in DB
    private String studentName;
    private String professorName;

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Appointment(int id, int studentId, int professorId, String appointmentTimeStr, String status) {
        this.id = id;
        this.studentId = studentId;
        this.professorId = professorId;
        try {
            this.appointmentDateTime = LocalDateTime.parse(appointmentTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing appointment time: " + appointmentTimeStr + " - " + e.getMessage());
            this.appointmentDateTime = null; // Or handle error appropriately
        }
        this.status = status;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public int getProfessorId() { return professorId; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public String getFormattedAppointmentTime() {
        return appointmentDateTime != null ? appointmentDateTime.format(DATETIME_FORMATTER) : "Invalid Date";
    }
    public String getStatus() { return status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }

    @Override
    public String toString() {
        // Example: "Appointment with Prof. Smith on 2023-12-01 10:00 - pending"
        return "Student ID: " + studentId + ", Prof ID: " + professorId +
                " at " + getFormattedAppointmentTime() +
                " (Status: " + status + ")";
    }
}