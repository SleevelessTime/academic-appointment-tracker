import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDAO {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Müsaitlik ekle (belirli bir tarih ve saat slotu)
    public void addAvailability(int professorId, LocalDateTime dateTime) {
        String query = "INSERT INTO availability (professor_id, available_time) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            stmt.setString(2, dateTime.format(DATETIME_FORMATTER));
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Handle potential duplicate entry if unique constraint exists on (professor_id, available_time)
            if (e.getErrorCode() == 19) { // SQLite constraint violation
                System.err.println("Availability slot already exists: " + dateTime.format(DATETIME_FORMATTER));
            } else {
                e.printStackTrace();
            }
        }
    }

    // Öğretim üyesinin belirli bir tarihteki müsaitliklerini (saat slotlarını) getir
    public List<String> getAvailableTimeSlotsForDate(int professorId, LocalDate date) {
        String query = "SELECT available_time FROM availability WHERE professor_id = ? AND date(available_time) = ?";
        List<String> timeSlots = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            stmt.setString(2, date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // YYYY-MM-DD
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime availableDateTime = LocalDateTime.parse(rs.getString("available_time"), DATETIME_FORMATTER);
                timeSlots.add(availableDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Sort time slots for consistent display
        timeSlots.sort(String::compareTo);
        return timeSlots;
    }

    // Öğretim üyesinin belirli bir tarihteki tüm müsaitlik nesnelerini getir
    public List<Availability> getAvailabilitiesForDate(int professorId, LocalDate date) {
        String query = "SELECT id, professor_id, available_time FROM availability WHERE professor_id = ? AND date(available_time) = ? ORDER BY available_time";
        List<Availability> availabilities = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            stmt.setString(2, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availabilities.add(new Availability(
                        rs.getInt("id"),
                        rs.getInt("professor_id"),
                        rs.getString("available_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availabilities;
    }


    // Müsaitlik sil (belirli bir tarih ve saat slot'una göre)
    public void deleteAvailability(int professorId, LocalDateTime dateTime) {
        String query = "DELETE FROM availability WHERE professor_id = ? AND available_time = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            stmt.setString(2, dateTime.format(DATETIME_FORMATTER));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}