import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityManager extends JFrame {
    private User professor;
    private DefaultListModel<Availability> availabilityListModel;
    private JList<Availability> availabilityList; // Shows time for selected date
    private JTextField dateField; // For YYYY-MM-DD input
    private JComboBox<String> timeSlotComboBox; // For HH:MM input
    private JButton addButton, deleteButton, viewDateButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final AvailabilityDAO availabilityDAO = new AvailabilityDAO();
    private LocalDate currentlySelectedDate;

    public AvailabilityManager(User professor) {
        this.professor = professor;

        setTitle("Manage Availability - Prof. " + professor.getUsername());
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateLabel.setBounds(20, 20, 150, 25);
        add(dateLabel);

        dateField = new JTextField();
        dateField.setBounds(180, 20, 100, 25);
        add(dateField);
        dateField.setText(LocalDate.now().format(dateFormatter)); // Default to today

        viewDateButton = new JButton("View/Refresh Date");
        viewDateButton.setBounds(290, 20, 150, 25);
        add(viewDateButton);

        JLabel existingSlotsLabel = new JLabel("Your Slots for Selected Date:");
        existingSlotsLabel.setBounds(20, 60, 200, 25);
        add(existingSlotsLabel);

        availabilityListModel = new DefaultListModel<>();
        availabilityList = new JList<>(availabilityListModel); // Will display only time part
        JScrollPane scrollPane = new JScrollPane(availabilityList);
        scrollPane.setBounds(20, 90, 200, 250);
        add(scrollPane);

        JLabel addLabel = new JLabel("Add Time Slot (HH:MM):");
        addLabel.setBounds(250, 60, 200, 25);
        add(addLabel);

        timeSlotComboBox = new JComboBox<>();
        populateTimeSlotComboBox(); // Fills with 20-min intervals
        timeSlotComboBox.setBounds(250, 90, 120, 25);
        add(timeSlotComboBox);

        addButton = new JButton("Add Slot");
        addButton.setBounds(250, 130, 120, 25);
        add(addButton);

        deleteButton = new JButton("Delete Selected Slot");
        deleteButton.setBounds(250, 170, 180, 25);
        add(deleteButton);

        viewDateButton.addActionListener(e -> loadAvailabilityForSelectedDate());
        addButton.addActionListener(e -> addAvailabilitySlot());
        deleteButton.addActionListener(e -> deleteAvailabilitySlot());

        // Initial load for today's date
        loadAvailabilityForSelectedDate();
    }

    private void populateTimeSlotComboBox() {
        timeSlotComboBox.removeAllItems();
        LocalTime time = LocalTime.of(8, 0); // Start from 08:00
        LocalTime endTime = LocalTime.of(17, 0); // Until 17:00
        while (time.isBefore(endTime) || time.equals(endTime)) {
            timeSlotComboBox.addItem(time.format(timeFormatter));
            time = time.plusMinutes(20);
        }
    }

    private void loadAvailabilityForSelectedDate() {
        availabilityListModel.clear();
        String dateStr = dateField.getText().trim();
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            currentlySelectedDate = LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Availability> availabilities = availabilityDAO.getAvailabilitiesForDate(professor.getId(), currentlySelectedDate);
        for (Availability avail : availabilities) {
            availabilityListModel.addElement(avail); // JList will use avail.toString() which shows HH:mm
        }
    }

    private void addAvailabilitySlot() {
        if (currentlySelectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select and view a date first.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String timeSlotStr = (String) timeSlotComboBox.getSelectedItem();
        if (timeSlotStr == null || timeSlotStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a time slot to add.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timeSlotStr, timeFormatter);
            LocalDateTime newAvailabilityDateTime = LocalDateTime.of(currentlySelectedDate, selectedTime);
            availabilityDAO.addAvailability(professor.getId(), newAvailabilityDateTime);
            loadAvailabilityForSelectedDate(); // Refresh list
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAvailabilitySlot() {
        Availability selectedAvailability = availabilityList.getSelectedValue();
        if (selectedAvailability == null) {
            JOptionPane.showMessageDialog(this, "Please select a time slot from the list to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the slot: " + selectedAvailability.getFormattedDateTime() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            availabilityDAO.deleteAvailability(professor.getId(), selectedAvailability.getDateTime());
            loadAvailabilityForSelectedDate(); // Refresh list
        }
    }
}