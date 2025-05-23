import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Availability {
    private int id; // from DB
    private int professorId;
    private LocalDateTime dateTime;

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public Availability(int professorId, LocalDateTime dateTime) {
        this.professorId = professorId;
        this.dateTime = dateTime;
    }

    // Constructor for loading from DB
    public Availability(int id, int professorId, String dateTimeString) {
        this.id = id;
        this.professorId = professorId;
        try {
            this.dateTime = LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing availability dateTimeString: " + dateTimeString + " - " + e.getMessage());
            this.dateTime = null; // Or handle error appropriately
        }
    }

    // Getters
    public int getId() { return id; }
    public int getProfessorId() { return professorId; }
    public LocalDateTime getDateTime() { return dateTime; }

    public String getFormattedDateTime() {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "Invalid DateTime";
    }

    public String getFormattedTime() {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "Invalid Time";
    }

    public String getFormattedDate() {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "Invalid Date";
    }

    @Override
    public String toString() {
        // Used in JList display, shows only time for a given date in AvailabilityManager
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "Invalid";
    }
}