import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class NotificationCenter extends JFrame {
    private User user;
    private DefaultListModel<Notification> notificationListModel;
    private JList<Notification> notificationJList;
    private NotificationDAO notificationDAO; // DAO nesnesi burada tanımlı

    public NotificationCenter(User user) {
        this.user = user;
        this.notificationDAO = new NotificationDAO(); // DAO nesnesi burada oluşturuluyor

        setTitle("Notification Center - " + user.getUsername());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        notificationListModel = new DefaultListModel<>();
        notificationJList = new JList<>(notificationListModel);
        // notificationJList.setCellRenderer(new NotificationCellRenderer()); // İsteğe bağlı özel renderer

        loadNotifications();

        JScrollPane scrollPane = new JScrollPane(notificationJList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markAsReadButton = new JButton("Mark as Read");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");

        markAsReadButton.addActionListener(e -> markSelectedAsRead());
        deleteButton.addActionListener(e -> deleteSelectedNotification());
        refreshButton.addActionListener(e -> loadNotifications());

        buttonPanel.add(refreshButton);
        buttonPanel.add(markAsReadButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        notificationJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Çift tıklama
                    int index = notificationJList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        Notification selectedNotification = notificationListModel.getElementAt(index);
                        if (selectedNotification != null && !selectedNotification.isRead()) {
                            notificationDAO.markNotificationAsRead(selectedNotification.getId()); // Doğru çağrı
                            loadNotifications(); // Listeyi yenile
                        }
                    }
                }
            }
        });
    }

    private void loadNotifications() {
        notificationListModel.clear();
        // notificationDAO nesnesi zaten oluşturulmuş olmalı
        List<Notification> notifications = notificationDAO.getNotificationsForUser(user.getId()); // Doğru çağrı
        if (notifications != null) {
            for (Notification notification : notifications) {
                notificationListModel.addElement(notification);
            }
        }
        if (notificationListModel.isEmpty()) {
            // Eğer hiç bildirim yoksa JList'e bir bilgi mesajı eklenebilir.
            // Örneğin: notificationListModel.addElement(new Notification(-1, -1, "No new notifications.", false, ""));
            // Ancak bu, Notification sınıfının bu tür bir yapıcıya sahip olmasını gerektirir.
            // Şimdilik boş bırakıyorum, JList boş görünecektir.
        }
    }

    private void markSelectedAsRead() {
        Notification selectedNotification = notificationJList.getSelectedValue();
        if (selectedNotification != null && !selectedNotification.isRead()) {
            notificationDAO.markNotificationAsRead(selectedNotification.getId()); // Doğru çağrı
            loadNotifications();
        } else if (selectedNotification == null) {
            JOptionPane.showMessageDialog(this, "Please select a notification to mark as read.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedNotification() {
        Notification selectedNotification = notificationJList.getSelectedValue();
        if (selectedNotification != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this notification?\n\"" + selectedNotification.getMessage() + "\"",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                notificationDAO.deleteNotification(selectedNotification.getId());
                loadNotifications();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a notification to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}