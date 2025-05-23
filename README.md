# academic-appointment-tracker
A system for scheduling, tracking, and managing academic appointments between faculty and students.

## 1. Description
Academic Appointment System is a Java Swing-based desktop application designed to facilitate the scheduling and management of appointments between students and professors. It provides separate interfaces for students and professors, each tailored to their specific needs within an academic environment.

The system allows professors to set their availability, and students to book appointments during these available slots. It also includes features for managing appointment requests, sending notifications, and exchanging messages.

## 2. Features

### General
*   **Role-based access:** Distinct functionalities for 'Student' and 'Professor' roles.
*   **User Authentication:** Login for both students and professors.
*   **Database Integration:** Uses SQLite to store and manage all application data.

### Student Features
*   **Dashboard:** Central hub for student activities.
*   **Schedule Appointments:**
    *   Select a professor.
    *   Choose a date.
    *   View available time slots for the selected professor and date.
    *   Request an appointment.
*   **View My Appointments:**
    *   List all requested appointments with their status (pending, approved, rejected).
    *   Option to cancel pending or non-approved appointments.
*   **Notifications:** Receive notifications regarding appointment status changes or messages.
*   **Messaging:**
    *   View messages received from professors.
    *   Send messages to professors.

### Professor Features
*   **Dashboard:** Central hub for professor activities.
*   **Manage Availability:**
    *   Set and update available time slots for specific dates.
    *   View and delete existing availability slots.
*   **Manage Appointment Requests:**
    *   View incoming appointment requests from students.
    *   Approve or reject requests.
    *   Notifications are sent to students upon status change.
*   **Notifications:** Receive notifications for new appointment requests or messages.
*   **Messaging:**
    *   View messages received from students.
    *   Send messages to individual students.
    *   Broadcast messages to all students.

## 3. Technologies Used
*   **Programming Language:** Java (JDK 8 or higher recommended)
*   **GUI:** Java Swing
*   **Database:** SQLite
*   **Database Connectivity:** JDBC (SQLite JDBC Driver)
*   **Date/Time API:** `java.time` (LocalDate, LocalDateTime, LocalTime)

## 4. Database Schema
The application uses an SQLite database named `identifier.sqlite`. The schema consists of the following tables:

*   **`users`**: Stores user information (ID, username, password, role).
    ```sql
    CREATE TABLE users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL UNIQUE,
        password TEXT NOT NULL,
        role TEXT NOT NULL CHECK(role IN ('student', 'professor'))
    );
    ```
*   **`appointments`**: Stores appointment details (ID, student ID, professor ID, time, status).
    ```sql
    CREATE TABLE appointments (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        professor_id INTEGER NOT NULL,
        appointment_time TEXT NOT NULL, -- ISO-8601 format: YYYY-MM-DD HH:MM
        status TEXT NOT NULL DEFAULT 'pending', -- pending, approved, rejected
        FOREIGN KEY(student_id) REFERENCES users(id),
        FOREIGN KEY(professor_id) REFERENCES users(id)
    );
    ```
*   **`availability`**: Stores professor availability slots (ID, professor ID, time).
    ```sql
    CREATE TABLE availability (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        professor_id INTEGER NOT NULL,
        available_time TEXT NOT NULL, -- ISO-8601 format: YYYY-MM-DD HH:MM
        FOREIGN KEY(professor_id) REFERENCES users(id)
    );
    ```
*   **`notifications`**: Stores notifications for users (ID, user ID, message, read status).
    ```sql
    CREATE TABLE notifications (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        message TEXT NOT NULL,
        is_read INTEGER DEFAULT 0, -- 0 (okunmadı), 1 (okundu)
        -- IMPORTANT: The Java code (NotificationDAO, Notification.java) expects a 'timestamp' column.
        -- Add it to your schema for full functionality (see Setup instructions below):
        -- timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, 
        FOREIGN KEY(user_id) REFERENCES users(id)
    );
    ```
*   **`messages`**: Stores messages between users (ID, sender ID, receiver ID, content, timestamp).
    ```sql
    CREATE TABLE messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sender_id INTEGER NOT NULL,
        receiver_id INTEGER NOT NULL,
        content TEXT NOT NULL,
        timestamp TEXT NOT NULL, -- ISO-8601 format: YYYY-MM-DD HH:MM:SS
        FOREIGN KEY(sender_id) REFERENCES users(id),
        FOREIGN KEY(receiver_id) REFERENCES users(id)
    );
    ```

## 5. Setup and Installation

### Prerequisites
1.  **Java Development Kit (JDK):** Version 8 or newer.
2.  **SQLite JDBC Driver:** This is typically managed by your IDE (like IntelliJ IDEA or Eclipse) if you add it as a library. If running manually, ensure `sqlite-jdbc.jar` is in your classpath.
3.  **SQLite Database Browser (Optional):** Tools like "DB Browser for SQLite" can be helpful for inspecting the database.

### Steps
1.  **Clone or Download the Project:** Get all the `.java` files.
2.  **Database Setup:**
    *   The `DatabaseConnection.java` class attempts to connect to/create an SQLite database file at a **hardcoded path**: `C:/Users/Oguzhan/IdeaProjects/AcademicAppointmentSystem/identifier.sqlite`.
        *   **You MUST either create this directory structure OR modify the path in `DatabaseConnection.java` to a suitable location on your system.** A relative path like `jdbc:sqlite:identifier.sqlite` is recommended to create the database file in the project's root directory.
    *   When the application runs for the first time and `DatabaseConnection.getConnection()` is called, the `identifier.sqlite` file will be created if it doesn't exist at the specified path.
    *   **Create Tables:** Open the newly created (or existing) `identifier.sqlite` file with an SQLite browser or command-line tool. Execute the SQL statements provided in the "Database Schema" section (Section 4) to create the necessary tables.
        *   **Crucial Modification for `notifications` table:** As noted in the schema and comments in `NotificationDAO.java`, the `notifications` table in the provided SQL schema is missing a `timestamp` column which the Java code expects. You **must** add this column for the notification feature to work correctly.
          You can either:
          *   Modify the `CREATE TABLE notifications` statement before running it:
            ```sql
            CREATE TABLE notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, -- ADDED LINE
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
            ```
          *   Or, if the table is already created without it, use `ALTER TABLE`:
            ```sql
            ALTER TABLE notifications ADD COLUMN timestamp DATETIME DEFAULT CURRENT_TIMESTAMP;
            ```

3.  **Compile the Java Files:**
    *   **Using an IDE (Recommended):**
        *   Create a new Java project in IntelliJ IDEA, Eclipse, or NetBeans.
        *   Add all the `.java` files to your project's `src` directory.
        *   Ensure the SQLite JDBC driver is added to the project's libraries/dependencies.
        *   The IDE will typically compile the files automatically.
    *   **Using Command Line:**
        *   Navigate to the directory containing the `.java` files.
        *   Compile: `javac *.java` (assuming all files are in the same directory and the JDBC driver is in the classpath).

4.  **Run the Application:**
    *   **Main Class:** `AcademicAppointmentSystem.java`
    *   Run this class from your IDE or using the command line: `java AcademicAppointmentSystem` (ensure the JDBC driver is in the classpath if running from command line).

5.  **Create Initial Users (Important):**
    The system does not have a built-in registration feature. You need to manually add users (at least one student and one professor) to the `users` table using an SQLite tool *after* creating the tables.
    Example:
    ```sql
    -- Add a professor
    INSERT INTO users (username, password, role) VALUES ('prof_smith', 'pass123', 'professor');
    -- Add a student
    INSERT INTO users (username, password, role) VALUES ('student_john', 'pass123', 'student');
    ```
    **Note:** Passwords are currently stored as plain text. This is a security risk for real applications.

## 6. File Structure Overview
The project consists of several Java classes, each responsible for a specific part of the application:

*   `AcademicAppointmentSystem.java`: Main entry point, displays role selection.
*   `User.java`: Model class for user data.
*   `UserDAO.java`: Data Access Object for user-related database operations.
*   `Appointment.java`: Model class for appointment data.
*   `AppointmentDAO.java`: DAO for appointment-related database operations.
*   `Availability.java`: Model class for availability data.
*   `AvailabilityDAO.java`: DAO for availability-related database operations.
*   `Notification.java`: Model class for notification data.
*   `NotificationDAO.java`: DAO for notification-related database operations.
*   `Message.java`: Model class for message data.
*   `MessageDAO.java`: DAO for message-related database operations.
*   `DatabaseConnection.java`: Handles SQLite database connection.
*   **GUI Classes (Login Forms & Dashboards):**
    *   `StudentLoginForm.java`, `ProfessorLoginForm.java`
    *   `StudentDashboard.java`, `ProfessorDashboard.java`
*   **GUI Classes (Feature Specific):**
    *   `AppointmentScheduler.java`: For students to schedule appointments.
    *   `StudentAppointmentViewer.java`: For students to view their appointments.
    *   `AvailabilityManager.java`: For professors to manage their availability.
    *   `ProfessorAppointmentManager.java`: For professors to manage appointment requests.
    *   `NotificationCenter.java`: For users to view notifications.
    *   `MessageCenter.java`: For users to view messages.
    *   `SendMessageFrame.java`: For users to send messages.

## 7. How to Use

1.  **Start the Application:** Run `AcademicAppointmentSystem.java`.
2.  **Select Role:** Choose "Student" or "Professor".
3.  **Login:** Enter the credentials of a user you previously inserted into the database.
    *   If login is successful, the respective dashboard (Student or Professor) will open.

### Student Workflow
1.  **Schedule Appointment:**
    *   Click "Schedule New Appointment".
    *   Select a professor, enter a date (YYYY-MM-DD), and click "View Slots".
    *   Select an available time slot and click "Schedule".
2.  **View Appointments:**
    *   Click "View My Appointments" to see appointment statuses and cancel pending ones.
3.  **Check Notifications/Messages:**
    *   Use "View Notifications" and "View My Messages".
    *   Use "Send Message to Professor" to communicate.

### Professor Workflow
1.  **Manage Availability:**
    *   Click "Manage My Availability".
    *   Enter a date, click "View/Refresh Date" to see existing slots for that date.
    *   Select a time from the dropdown and click "Add Slot".
    *   Select an existing slot from the list and click "Delete Selected Slot".
2.  **Manage Appointments:**
    *   Click "View/Manage Appointment Requests".
    *   Approve or Reject pending requests using the buttons in the table.
3.  **Check Notifications/Messages:**
    *   Use "View Notifications" and "View Messages".
    *   Use "Send New Message" to communicate with individual students or broadcast to all.

## 8. Troubleshooting & Known Issues

*   **Database Path:** The database path in `DatabaseConnection.java` is hardcoded. You **must** adjust this path or ensure the specified directory structure exists for the application to work. Using a relative path (e.g., `jdbc:sqlite:identifier.sqlite`) is highly recommended for portability.
*   **Missing `timestamp` Column in `notifications` Table:** The provided SQL schema for the `notifications` table does **not** include a `timestamp` column. However, `NotificationDAO.java` and `Notification.java` expect this column for querying, sorting, and display.
    *   **Solution:** You **must** add a `timestamp DATETIME DEFAULT CURRENT_TIMESTAMP` column to your `notifications` table schema (see Section 5.2 "Database Setup" for instructions). Failure to do so will likely result in SQL errors when accessing notifications (e.g., "no such column: timestamp").
*   **Plain Text Passwords:** Passwords are stored in plain text in the database (as noted in `UserDAO.addUser`). This is a significant security vulnerability in a production system. Passwords should be hashed.
*   **No Registration:** Users cannot register through the application. They must be added manually to the `users` table in the SQLite database.
*   **Error Handling:** While some error handling exists (e.g., for unique username constraints during user addition, some input validations), it could be made more comprehensive and user-friendly across the application.
*   **UI Rigidity:** The use of `setLayout(null)` (absolute positioning) makes the UI rigid and not responsive to window resizing or different screen resolutions. Using layout managers (e.g., `BorderLayout`, `FlowLayout`, `GridBagLayout`, `GroupLayout`) is generally preferred for more robust GUIs.

## 9. Potential Future Enhancements
*   Implement password hashing (e.g., using bcrypt or Argon2).
*   Add a user registration feature.
*   Use a relative path for the database file by default or make it configurable via a properties file.
*   Refactor UI to use standard Java Swing layout managers for better responsiveness.
*   Implement more detailed logging for debugging and auditing.
*   Add email or real-time in-app indicators for new notifications/messages.
*   Create a proper `DatabaseSchemaManager` class to initialize or migrate the database schema automatically on application startup.
*   Write unit and integration tests for DAOs and business logic.
*   Improve date and time input with JDatePicker or similar calendar components.
