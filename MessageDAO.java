import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private static final DateTimeFormatter DB_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Send a message from one user to another
    public void sendMessage(int senderId, int receiverId, String content) {
        String query = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);
            // Store timestamp in a consistent format, e.g., ISO-8601 or a specific pattern
            stmt.setString(4, LocalDateTime.now().format(DB_TIMESTAMP_FORMATTER));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Professor broadcasts message to all students
    public void broadcastMessageToAllStudents(int professorId, String content) {
        UserDAO userDAO = new UserDAO();
        List<User> students = userDAO.getStudents();
        String timestamp = LocalDateTime.now().format(DB_TIMESTAMP_FORMATTER);

        String query = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            conn.setAutoCommit(false); // Start transaction

            for (User student : students) {
                stmt.setInt(1, professorId);
                stmt.setInt(2, student.getId());
                stmt.setString(3, content);
                stmt.setString(4, timestamp);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit(); // Commit transaction

        } catch (SQLException e) {
            e.printStackTrace();
            // Consider rollback on error
        }
    }


    // Get all messages for a specific user (where they are the receiver)
    public List<Message> getMessagesForUser(int userId) {
        // Query updated to fetch necessary fields and order by timestamp
        String query = "SELECT id, sender_id, receiver_id, content, timestamp FROM messages " +
                "WHERE receiver_id = ? ORDER BY timestamp DESC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("content"),
                        rs.getString("timestamp") // Pass the string as is
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}