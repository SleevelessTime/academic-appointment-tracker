import javax.swing.*;

public class AcademicAppointmentSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize database schema if it's the first run (optional, good practice)
            // DatabaseSchemaManager.initialize(); // You'd create this class

            String[] options = {"Student", "Professor"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Select Your Role",
                    "Academic Appointment System",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                new StudentLoginForm().setVisible(true);
            } else if (choice == 1) {
                new ProfessorLoginForm().setVisible(true);
            }
            // else choice == -1 (closed dialog), do nothing
        });
    }
}