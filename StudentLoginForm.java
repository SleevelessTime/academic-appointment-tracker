import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentLoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;


    public StudentLoginForm() {
        setTitle("Student Login");
        setSize(400, 250); // Increased height for back button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 30, 100, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 30, 180, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 70, 100, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 70, 180, 25);
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(150, 110, 100, 30);
        add(loginButton);

        backButton = new JButton("Back");
        backButton.setBounds(50, 110, 80, 30);
        add(backButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginStudent();
            }
        });

        // Also allow login on Enter press in password field
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginStudent();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                AcademicAppointmentSystem.main(null); // Go back to role selection
            }
        });
    }

    private void loginStudent() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(StudentLoginForm.this, "Username and password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password) && "student".equals(user.getRole())) {
            JOptionPane.showMessageDialog(StudentLoginForm.this, "Login successful! Welcome " + user.getUsername(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
            new StudentDashboard(user).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(StudentLoginForm.this, "Invalid username, password, or role!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}