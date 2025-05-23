import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentScheduler extends JFrame {
    private User student;
    private JComboBox<User> professorDropdown;
    private JTextField dateField; // For YYYY-MM-DD input
    private JComboBox<String> timeSlotDropdown;
    private JButton scheduleButton;
    private JButton viewDateSlotsButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    public AppointmentScheduler(User student) {
        this.student = student;

        setTitle("Schedule Appointment - " + student.getUsername());
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel professorLabel = new JLabel("Select Professor:");
        professorLabel.setBounds(30, 30, 120, 25);
        add(professorLabel);

        professorDropdown = new JComboBox<>();
        populateProfessors();
        professorDropdown.setBounds(160, 30, 220, 25);
        add(professorDropdown);

        JLabel dateLabel = new JLabel("Enter Date (YYYY-MM-DD):");
        dateLabel.setBounds(30, 80, 180, 25);
        add(dateLabel);

        dateField = new JTextField();
        dateField.setBounds(220, 80, 100, 25);
        add(dateField);

        viewDateSlotsButton = new JButton("View Slots");
        viewDateSlotsButton.setBounds(330, 80, 100, 25);
        add(viewDateSlotsButton);

        JLabel timeSlotLabel = new JLabel("Select Time Slot:");
        timeSlotLabel.setBounds(30, 130, 120, 25);
        add(timeSlotLabel);

        timeSlotDropdown = new JComboBox<>();
        timeSlotDropdown.setBounds(160, 130, 180, 25);
        add(timeSlotDropdown);

        scheduleButton = new JButton("Schedule");
        scheduleButton.setBounds(150, 200, 120, 30);
        add(scheduleButton);

        viewDateSlotsButton.addActionListener(e -> populateTimeSlots());

        scheduleButton.addActionListener(e -> scheduleAppointment());
    }

    private void populateProfessors() {
        professorDropdown.removeAllItems();
        UserDAO userDAO = new UserDAO();
        List<User> professors = userDAO.getProfessors();
        for (User professor : professors) {
            professorDropdown.addItem(professor); // Store User object
        }
    }

    private void populateTimeSlots() {
        timeSlotDropdown.removeAllItems();
        User selectedProfessorUser = (User) professorDropdown.getSelectedItem();
        String dateStr = dateField.getText().trim();

        if (selectedProfessorUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a professor.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AvailabilityDAO availabilityDAO = new AvailabilityDAO();
        List<String> availableProfessorSlots = availabilityDAO.getAvailableTimeSlotsForDate(selectedProfessorUser.getId(), selectedDate);

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        List<String> bookedSlots = appointmentDAO.getBookedTimeSlotsForDate(selectedProfessorUser.getId(), selectedDate);

        List<String> trulyAvailableSlots = new ArrayList<>(availableProfessorSlots);
        trulyAvailableSlots.removeAll(bookedSlots);

        if (trulyAvailableSlots.isEmpty()) {
            timeSlotDropdown.addItem("No slots available for this date");
            timeSlotDropdown.setEnabled(false);
            scheduleButton.setEnabled(false);
        } else {
            for (String slot : trulyAvailableSlots) {
                timeSlotDropdown.addItem(slot); // slot is HH:MM
            }
            timeSlotDropdown.setEnabled(true);
            scheduleButton.setEnabled(true);
        }
    }


    private void scheduleAppointment() {
        User selectedProfessorUser = (User) professorDropdown.getSelectedItem();
        String dateStr = dateField.getText().trim();
        String selectedTimeSlot = (String) timeSlotDropdown.getSelectedItem();

        if (selectedProfessorUser == null || dateStr.isEmpty() || selectedTimeSlot == null || selectedTimeSlot.equals("No slots available for this date")) {
            JOptionPane.showMessageDialog(this, "Please select professor, date, and an available time slot.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalTime time = LocalTime.parse(selectedTimeSlot, timeFormatter);
        String appointmentDateTimeStr = selectedDate.format(dateFormatter) + " " + time.format(timeFormatter);

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        appointmentDAO.createAppointment(student.getId(), selectedProfessorUser.getId(), appointmentDateTimeStr);

        // Notify professor (optional, can be a system notification)
        NotificationDAO notificationDAO = new NotificationDAO();
        notificationDAO.addNotification(selectedProfessorUser.getId(),
                "New appointment request from student " + student.getUsername() +
                        " for " + appointmentDateTimeStr);


        JOptionPane.showMessageDialog(this, "Appointment requested successfully for " + appointmentDateTimeStr + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
        // Clear fields or close window
        timeSlotDropdown.removeAllItems();
        dateField.setText("");
        // dispose(); // or keep open for another appointment
    }
}