import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentAppointmentViewer extends JFrame {
    private User student;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private AppointmentDAO appointmentDAO;

    public StudentAppointmentViewer(User student) {
        this.student = student;
        this.appointmentDAO = new AppointmentDAO();

        setTitle("My Appointments - " + student.getUsername());
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Professor", "Date & Time", "Status", "Cancel"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only cancel button column is "editable" in a sense
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return JButton.class;
                return super.getColumnClass(columnIndex);
            }
        };
        appointmentsTable = new JTable(tableModel);

        // Custom renderer and editor for the button column
        appointmentsTable.getColumn("Cancel").setCellRenderer(new ButtonRenderer());
        appointmentsTable.getColumn("Cancel").setCellEditor(new ButtonEditor(new JCheckBox(), "Cancel", this::cancelAppointment));


        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> loadAppointments());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAppointments();
    }

    private void loadAppointments() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Appointment> appointments = appointmentDAO.getStudentAppointments(student.getId());
        for (Appointment apt : appointments) {
            Object[] row = new Object[]{
                    apt.getId(),
                    apt.getProfessorName() != null ? apt.getProfessorName() : "Prof ID: " + apt.getProfessorId(),
                    apt.getFormattedAppointmentTime(),
                    apt.getStatus(),
                    "Cancel" // Button text
            };
            tableModel.addRow(row);
        }
    }

    private void cancelAppointment(int appointmentId) {
        // Find the appointment to check its status
        Appointment appointmentToCancel = null;
        List<Appointment> appointments = appointmentDAO.getStudentAppointments(student.getId()); // Re-fetch or find from a local list
        for(Appointment app : appointments){
            if(app.getId() == appointmentId){
                appointmentToCancel = app;
                break;
            }
        }

        if (appointmentToCancel != null && !"approved".equalsIgnoreCase(appointmentToCancel.getStatus())) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel appointment ID: " + appointmentId + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                appointmentDAO.deleteAppointment(appointmentId);
                // Notify professor (optional)
                NotificationDAO notificationDAO = new NotificationDAO();
                notificationDAO.addNotification(appointmentToCancel.getProfessorId(),
                        "Appointment ID " + appointmentId + " for " + appointmentToCancel.getFormattedAppointmentTime() +
                                " has been cancelled by student " + student.getUsername());
                loadAppointments(); // Refresh table
            }
        } else if (appointmentToCancel != null && "approved".equalsIgnoreCase(appointmentToCancel.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot cancel an already approved appointment. Please contact the professor.", "Cancellation Denied", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Appointment not found or cannot be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner classes for JTable button functionality
    interface TableButtonAction {
        void execute(int id);
    }
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private TableButtonAction action;
        private int currentId;

        public ButtonEditor(JCheckBox checkBox, String text, TableButtonAction action) {
            super(checkBox);
            this.action = action;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            currentId = (Integer) table.getModel().getValueAt(row, 0); // Assumes ID is in column 0
            isPushed = true;
            return button;
        }
        public Object getCellEditorValue() {
            if (isPushed && currentId != -1) {
                action.execute(currentId);
            }
            isPushed = false;
            return label; // or currentId if you need to return the ID
        }
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}