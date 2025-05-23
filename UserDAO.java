import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane; // JOptionPane için import eklendi

public class UserDAO {

    // Kullanıcıyı kullanıcı adına göre getir
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("UserDAO.getUserByUsername: Database connection is null.");
                return null;
            }
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("SQLException in UserDAO.getUserByUsername: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Tüm öğretim üyelerini getir
    public List<User> getProfessors() {
        String query = "SELECT * FROM users WHERE role = 'professor' ORDER BY username";
        List<User> professors = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("UserDAO.getProfessors: Database connection is null.");
                return professors;
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                professors.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQLException in UserDAO.getProfessors: " + e.getMessage());
            e.printStackTrace();
        }
        return professors;
    }

    // Tüm öğrencileri getir
    public List<User> getStudents() {
        String query = "SELECT * FROM users WHERE role = 'student' ORDER BY username";
        List<User> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("UserDAO.getStudents: Database connection is null.");
                return students;
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQLException in UserDAO.getStudents: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    // Yeni kullanıcı ekle
    public void addUser(String username, String password, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("UserDAO.addUser: Database connection is null.");
                return;
            }
            stmt.setString(1, username);
            stmt.setString(2, password); // Gerçek uygulamada şifreler hashlenmeli
            stmt.setString(3, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 && e.getMessage().contains("UNIQUE constraint failed: users.username") ) { // SQLite unique constraint violation for username
                JOptionPane.showMessageDialog(null, "Username '" + username + "' already exists. Please choose a different username.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("SQLException in UserDAO.addUser: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Kullanıcıyı ID'ye göre getir
    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("UserDAO.getUserById: Database connection is null.");
                return null;
            }
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("SQLException in UserDAO.getUserById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}