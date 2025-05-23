import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentDashboard extends JFrame {
    private User student;
    private JButton viewMyAppointmentsButton;
    private JButton sendMessageToProfessorButton; // Yeni buton

    public StudentDashboard(User student) {
        this.student = student;

        setTitle("Student Dashboard - " + student.getUsername());
        setSize(600, 450); // Yüksekliği artırdık
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("Welcome, " + student.getUsername());
        welcomeLabel.setBounds(50, 20, 300, 25);
        add(welcomeLabel);

        JButton scheduleButton = new JButton("Schedule New Appointment");
        scheduleButton.setBounds(50, 60, 250, 30);
        add(scheduleButton);

        viewMyAppointmentsButton = new JButton("View My Appointments");
        viewMyAppointmentsButton.setBounds(50, 110, 250, 30);
        add(viewMyAppointmentsButton);

        JButton notificationsButton = new JButton("View Notifications");
        notificationsButton.setBounds(50, 160, 250, 30);
        add(notificationsButton);

        JButton messagesButton = new JButton("View My Messages");
        messagesButton.setBounds(50, 210, 250, 30);
        add(messagesButton);

        sendMessageToProfessorButton = new JButton("Send Message to Professor"); // Buton oluşturuldu
        sendMessageToProfessorButton.setBounds(50, 260, 250, 30); // Konumlandırıldı
        add(sendMessageToProfessorButton); // Eklendi

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(50, 320, 100, 30); // Logout butonu aşağı kaydırıldı
        add(logoutButton);

        scheduleButton.addActionListener(e -> new AppointmentScheduler(student).setVisible(true));

        viewMyAppointmentsButton.addActionListener(e -> new StudentAppointmentViewer(student).setVisible(true));

        notificationsButton.addActionListener(e -> {
            // Hata ayıklama için: DAO çağrılmadan önce bir mesaj gösterelim
            // System.out.println("Notifications button clicked by " + student.getUsername());
            new NotificationCenter(student).setVisible(true);
        });

        messagesButton.addActionListener(e -> new MessageCenter(student).setVisible(true));

        sendMessageToProfessorButton.addActionListener(e -> { // Yeni butonun ActionListener'ı
            // SendMessageFrame'i öğrenci için açacağız.
            // SendMessageFrame'in constructor'ı ve populateRecipients metodu öğrenci rolünü dikkate almalı.
            new SendMessageFrame(student).setVisible(true);
        });

        logoutButton.addActionListener(e -> {
            dispose();
            AcademicAppointmentSystem.main(null);
        });
    }
}