import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfessorLoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;


    public ProfessorLoginForm() {
        setTitle("Professor Login");
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
                loginProfessor();
            }
        });

        // Also allow login on Enter press in password field
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginProfessor();
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

    private void loginProfessor() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(ProfessorLoginForm.this, "Username and password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password) && "professor".equals(user.getRole())) {
            JOptionPane.showMessageDialog(ProfessorLoginForm.this, "Login successful! Welcome Prof. " + user.getUsername(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
            new ProfessorDashboard(user).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(ProfessorLoginForm.this, "Invalid username, password, or role!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}