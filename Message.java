import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private LocalDateTime timestamp; // Changed to LocalDateTime
    private String senderUsername; // For display

    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Message(int id, int senderId, int receiverId, String content, String timestampStr) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        try {
            // Assuming timestamp from DB is compatible or needs specific parsing.
            // If DB stores it as 'YYYY-MM-DD HH:MM:SS.SSS' from SQLite CURRENT_TIMESTAMP
            if (timestampStr.contains(".")) { // Handle fractional seconds if present
                timestampStr = timestampStr.substring(0, timestampStr.indexOf("."));
            }
            this.timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing message timestamp: " + timestampStr + " - " + e.getMessage());
            // Fallback or default if parsing fails
            this.timestamp = LocalDateTime.now();
        }
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(TIMESTAMP_FORMATTER) : "Invalid Timestamp";
    }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    @Override
    public String toString() {
        return "From: " + (senderUsername != null ? senderUsername : "ID:" + senderId) +
                " | To: ID:" + receiverId + // Could fetch receiver username too if needed
                " | At: " + getFormattedTimestamp() +
                "\nMessage: " + content;
    }
}