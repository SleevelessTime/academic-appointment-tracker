import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MessageCenter extends JFrame {
    private User user;
    private JTextArea messageArea;
    private DefaultListModel<Message> messageListModel;
    private JList<Message> messageJList;


    public MessageCenter(User user) {
        this.user = user;
        setTitle("Message Center - " + user.getUsername());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        messageListModel = new DefaultListModel<>();
        messageJList = new JList<>(messageListModel);
        messageJList.setCellRenderer(new MessageCellRenderer()); // Custom renderer for better display

        loadMessages();

        JScrollPane scrollPane = new JScrollPane(messageJList);
        add(scrollPane, BorderLayout.CENTER);

        // Optional: Add a refresh button
        JButton refreshButton = new JButton("Refresh Messages");
        refreshButton.addActionListener(e -> loadMessages());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadMessages() {
        messageListModel.clear();
        MessageDAO messageDAO = new MessageDAO();
        UserDAO userDAO = new UserDAO(); // To get sender usernames

        List<Message> messages = messageDAO.getMessagesForUser(user.getId());
        for (Message msg : messages) {
            User sender = userDAO.getUserById(msg.getSenderId());
            if (sender != null) {
                msg.setSenderUsername(sender.getUsername());
            } else {
                msg.setSenderUsername("Unknown Sender (ID: " + msg.getSenderId() + ")");
            }
            messageListModel.addElement(msg);
        }
        if (messages.isEmpty()){
            // Placeholder or message if no messages
            // messageListModel.addElement(new Message(-1, -1, -1, "No messages.", LocalDateTime.now().toString()));
        }
    }

    // Custom cell renderer for JList to display messages nicely
    class MessageCellRenderer extends JPanel implements ListCellRenderer<Message> {
        private JLabel senderLabel;
        private JLabel timestampLabel;
        private JTextArea contentArea;

        public MessageCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel topPanel = new JPanel(new BorderLayout());
            senderLabel = new JLabel();
            senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD));
            timestampLabel = new JLabel();
            timestampLabel.setForeground(Color.GRAY);

            topPanel.add(senderLabel, BorderLayout.WEST);
            topPanel.add(timestampLabel, BorderLayout.EAST);

            contentArea = new JTextArea();
            contentArea.setWrapStyleWord(true);
            contentArea.setLineWrap(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false); // Make it transparent to show panel background
            contentArea.setFont(UIManager.getFont("Label.font"));


            add(topPanel, BorderLayout.NORTH);
            add(contentArea, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message message, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            senderLabel.setText("From: " + message.getSenderUsername());
            timestampLabel.setText(message.getFormattedTimestamp());
            contentArea.setText(message.getContent());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                contentArea.setBackground(list.getSelectionBackground());
                contentArea.setForeground(list.getSelectionForeground());

            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                contentArea.setBackground(list.getBackground());
                contentArea.setForeground(list.getForeground());
            }
            return this;
        }
    }
}