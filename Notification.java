import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;

    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public Notification(int id, int userId, String message, boolean isRead, String timestampStr) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        try {
            if (timestampStr != null && timestampStr.contains(".")) {
                timestampStr = timestampStr.substring(0, timestampStr.indexOf("."));
            }
            this.timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER) : LocalDateTime.now();
        } catch (Exception e) {
            System.err.println("Error parsing notification timestamp: " + timestampStr + " - " + e.getMessage());
            this.timestamp = LocalDateTime.now();
        }
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(TIMESTAMP_FORMATTER) : "No Timestamp";
    }


    @Override
    public String toString() {
        return (isRead ? "(Read) " : "(Unread) ") + getFormattedTimestamp() + " - " + message;
    }
}