import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // Bildirim ekle (timestamp otomatik olarak DB tarafından CURRENT_TIMESTAMP ile eklenecek)
    public void addNotification(int userId, String message) {
        String query = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("NotificationDAO.addNotification: Database connection is null.");
                return;
            }
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException in NotificationDAO.addNotification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Kullanıcının bildirimlerini getir (en yeniden eskiye sıralı)
    public List<Notification> getNotificationsForUser(int userId) {
        // SORGUDAKİ SÜTUN ADLARI VERİTABANI ŞEMASIYLA EŞLEŞMELİ
        // Eğer timestamp sütunu yoksa, sorgudan çıkarılmalı veya eklendikten sonra bu sorgu kullanılmalı.
        // Şu anki hata mesajına göre 'timestamp' sütunu yok.
        // Veritabanı şemanızı kontrol edin ve `notifications` tablosuna `timestamp DATETIME DEFAULT CURRENT_TIMESTAMP` sütununu ekleyin.
        // Şema düzeltildikten sonra bu sorgu çalışacaktır.
        String query = "SELECT id, user_id, message, is_read, timestamp FROM notifications " +
                "WHERE user_id = ? ORDER BY timestamp DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("NotificationDAO.getNotificationsForUser: Database connection is null.");
                return notifications;
            }
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        rs.getString("timestamp") // Bu sütun DB'de olmalı
                ));
            }
        } catch (SQLException e) {
            // Hata mesajı burada loglanıyor, bu iyi.
            System.err.println("SQLException in NotificationDAO.getNotificationsForUser: " + e.getMessage());
            e.printStackTrace(); // Stack trace'i görmek önemli
            // Kullanıcıya daha anlamlı bir mesaj gösterilebilir, ancak bu katmanda JOption göstermek ideal değil.
            // Bu hata üst katmanlara iletilip orada handle edilebilir.
        }
        return notifications;
    }

    public void markNotificationAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("NotificationDAO.markNotificationAsRead: Database connection is null.");
                return;
            }
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException in NotificationDAO.markNotificationAsRead: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteNotification(int notificationId) {
        String query = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("NotificationDAO.deleteNotification: Database connection is null.");
                return;
            }
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException in NotificationDAO.deleteNotification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getUnreadNotificationCount(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("NotificationDAO.getUnreadNotificationCount: Database connection is null.");
                return 0;
            }
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SQLException in NotificationDAO.getUnreadNotificationCount: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}