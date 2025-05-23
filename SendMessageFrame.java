import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SendMessageFrame extends JFrame {
    private User currentUser; // Artık sadece professor değil, student da olabilir
    private JComboBox<Object> recipientComboBox;
    private JTextArea messageTextArea;
    private JButton sendButton;
    private MessageDAO messageDAO;
    private UserDAO userDAO;

    private static final String ALL_STUDENTS_OPTION = "All Students (Professor Only)";

    public SendMessageFrame(User currentUser) { // Constructor parametresi genelleştirildi
        this.currentUser = currentUser;
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();

        setTitle("Send New Message - " + currentUser.getUsername());
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        JLabel recipientLabel = new JLabel("To:");
        recipientComboBox = new JComboBox<>();
        populateRecipients(); // Bu metod artık currentUser'ın rolüne göre çalışacak

        topPanel.add(recipientLabel);
        topPanel.add(recipientComboBox);

        messageTextArea = new JTextArea();
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder("Message Content:"));

        sendButton = new JButton("Send Message");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        bottomPanel.add(sendButton);

        add(topPanel, BorderLayout.NORTH);
        add(messageScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void populateRecipients() {
        recipientComboBox.removeAllItems();

        if ("professor".equals(currentUser.getRole())) {
            // Profesör ise, "All Students" seçeneğini ve tek tek öğrencileri ekle
            recipientComboBox.addItem(ALL_STUDENTS_OPTION);
            List<User> students = userDAO.getStudents();
            for (User student : students) {
                recipientComboBox.addItem(student); // JComboBox User.toString() kullanacak
            }
        } else if ("student".equals(currentUser.getRole())) {
            // Öğrenci ise, sadece profesörleri ekle
            List<User> professors = userDAO.getProfessors();
            for (User professor : professors) {
                recipientComboBox.addItem(professor);
            }
            if (professors.isEmpty()) {
                recipientComboBox.addItem("No professors available");
                sendButton.setEnabled(false); // Gönderecek kimse yoksa butonu devre dışı bırak
            }
        }
    }

    private void sendMessage() {
        Object selectedRecipientObj = recipientComboBox.getSelectedItem();
        String messageContent = messageTextArea.getText().trim();

        if (selectedRecipientObj == null || "No professors available".equals(selectedRecipientObj.toString())) {
            JOptionPane.showMessageDialog(this, "Please select a valid recipient.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (messageContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Message content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("professor".equals(currentUser.getRole())) {
            if (selectedRecipientObj.equals(ALL_STUDENTS_OPTION)) {
                messageDAO.broadcastMessageToAllStudents(currentUser.getId(), messageContent);
                JOptionPane.showMessageDialog(this, "Message broadcasted to all students successfully!", "Message Sent", JOptionPane.INFORMATION_MESSAGE);
            } else if (selectedRecipientObj instanceof User) {
                User studentRecipient = (User) selectedRecipientObj;
                messageDAO.sendMessage(currentUser.getId(), studentRecipient.getId(), messageContent);
                // Öğrenciye bildirim gönder
                NotificationDAO notificationDAO = new NotificationDAO();
                notificationDAO.addNotification(studentRecipient.getId(), "New message from Prof. " + currentUser.getUsername());
                JOptionPane.showMessageDialog(this, "Message sent successfully to " + studentRecipient.getUsername() + "!", "Message Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if ("student".equals(currentUser.getRole())) {
            if (selectedRecipientObj instanceof User) {
                User professorRecipient = (User) selectedRecipientObj;
                messageDAO.sendMessage(currentUser.getId(), professorRecipient.getId(), messageContent);
                // Hocaya bildirim gönder
                NotificationDAO notificationDAO = new NotificationDAO();
                notificationDAO.addNotification(professorRecipient.getId(), "New message from student " + currentUser.getUsername());
                JOptionPane.showMessageDialog(this, "Message sent successfully to Prof. " + professorRecipient.getUsername() + "!", "Message Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        messageTextArea.setText("");
        // dispose(); // İsteğe bağlı: mesaj gönderildikten sonra pencereyi kapat
    }
}