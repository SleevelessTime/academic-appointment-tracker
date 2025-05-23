import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    // Randevu oluştur
    public void createAppointment(int studentId, int professorId, String appointmentDateTimeStr) {
        String query = "INSERT INTO appointments (student_id, professor_id, appointment_time, status) VALUES (?, ?, ?, 'pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, professorId);
            stmt.setString(3, appointmentDateTimeStr); // "YYYY-MM-DD HH:MM"
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Öğrencinin randevularını getir
    public List<Appointment> getStudentAppointments(int studentId) {
        String query = "SELECT a.id, a.student_id, a.professor_id, a.appointment_time, a.status, p.username as professor_name " +
                "FROM appointments a JOIN users p ON a.professor_id = p.id WHERE a.student_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment apt = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getInt("professor_id"),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                );
                apt.setProfessorName(rs.getString("professor_name"));
                appointments.add(apt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Öğretim üyesinin gelen randevu taleplerini getir
    public List<Appointment> getProfessorAppointments(int professorId) {
        String query = "SELECT a.id, a.student_id, a.professor_id, a.appointment_time, a.status, s.username as student_name " +
                "FROM appointments a JOIN users s ON a.student_id = s.id WHERE a.professor_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment apt = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getInt("professor_id"),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                );
                apt.setStudentName(rs.getString("student_name"));
                appointments.add(apt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Randevu durumunu güncelle (örneğin: onayla veya reddet)
    public void updateAppointmentStatus(int appointmentId, String status) {
        String query = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Randevu sil
    public void deleteAppointment(int appointmentId) {
        String query = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Öğretim üyesinin belirli bir tarihteki onaylanmış randevu saatlerini getir (sadece saat kısmı)
    public List<String> getBookedTimeSlotsForDate(int professorId, LocalDate date) {
        // SQLite's SUBSTR can be used if date is stored as 'YYYY-MM-DD HH:MM'
        // or use date functions if available and type is appropriate
        String query = "SELECT appointment_time FROM appointments " +
                "WHERE professor_id = ? AND status = 'approved' AND date(appointment_time) = ?";
        List<String> bookedSlots = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, professorId);
            stmt.setString(2, date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // YYYY-MM-DD
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String dateTimeStr = rs.getString("appointment_time");
                // Extract time part HH:MM
                bookedSlots.add(dateTimeStr.substring(11, 16));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSlots;
    }
}