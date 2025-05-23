import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfessorDashboard extends JFrame {
    private User professor;
    private JButton viewAppointmentsButton; // New button to view and manage appointments

    public ProfessorDashboard(User professor) {
        this.professor = professor;

        setTitle("Professor Dashboard - " + professor.getUsername());
        setSize(600, 450); // Increased size a bit
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null); // Using null layout, consider alternatives for more complex UIs

        JLabel welcomeLabel = new JLabel("Welcome, Prof. " + professor.getUsername());
        welcomeLabel.setBounds(50, 20, 300, 25);
        add(welcomeLabel);

        JButton availabilityButton = new JButton("Manage My Availability");
        availabilityButton.setBounds(50, 60, 220, 30);
        add(availabilityButton);

        viewAppointmentsButton = new JButton("View/Manage Appointment Requests");
        viewAppointmentsButton.setBounds(50, 110, 280, 30);
        add(viewAppointmentsButton);

        JButton notificationsButton = new JButton("View Notifications");
        notificationsButton.setBounds(50, 160, 220, 30);
        add(notificationsButton);

        JButton messagesButton = new JButton("View Messages");
        messagesButton.setBounds(50, 210, 220, 30);
        add(messagesButton);

        JButton sendMessageButton = new JButton("Send New Message");
        sendMessageButton.setBounds(50, 260, 220, 30);
        add(sendMessageButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(50, 320, 100, 30);
        add(logoutButton);


        availabilityButton.addActionListener(e -> new AvailabilityManager(professor).setVisible(true));

        viewAppointmentsButton.addActionListener(e -> new ProfessorAppointmentManager(professor).setVisible(true));

        notificationsButton.addActionListener(e -> new NotificationCenter(professor).setVisible(true));

        messagesButton.addActionListener(e -> new MessageCenter(professor).setVisible(true));

        sendMessageButton.addActionListener(e -> new SendMessageFrame(professor).setVisible(true));

        logoutButton.addActionListener(e -> {
            dispose();
            AcademicAppointmentSystem.main(null); // Restart role selection
        });
    }
}