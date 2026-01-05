// OnlineLanguagePlatform.java
//
// SINGLE FILE PROJECT – GUI + JDBC + OOP + MULTITHREADING
//
// Before running, create MySQL DB:
//
// CREATE DATABASE IF NOT EXISTS language_platform;
// USE language_platform;
//
// CREATE TABLE users (
//   id INT AUTO_INCREMENT PRIMARY KEY,
//   name VARCHAR(100) NOT NULL,
//   email VARCHAR(100) UNIQUE NOT NULL,
//   password VARCHAR(100) NOT NULL,
//   role ENUM('ADMIN','INSTRUCTOR','LEARNER') NOT NULL
// );
//
// CREATE TABLE lessons (
//   id INT AUTO_INCREMENT PRIMARY KEY,
//   title VARCHAR(200) NOT NULL,
//   content TEXT,
//   instructor_id INT,
//   FOREIGN KEY (instructor_id) REFERENCES users(id)
// );
//
// CREATE TABLE progress (
//   id INT AUTO_INCREMENT PRIMARY KEY,
//   learner_id INT,
//   lesson_id INT,
//   completion_percent INT,
//   last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//                  ON UPDATE CURRENT_TIMESTAMP,
//   UNIQUE KEY uq_learner_lesson (learner_id, lesson_id),
//   FOREIGN KEY (learner_id) REFERENCES users(id),
//   FOREIGN KEY (lesson_id) REFERENCES lessons(id)
// );
//
// Insert at least one Admin, Instructor, Learner manually for testing.

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OnlineLanguagePlatform {

    // ============================
    //        MODEL LAYER
    // ============================

    public enum Role {
        ADMIN, INSTRUCTOR, LEARNER
    }

    public interface ProgressTrackable {
        void updateProgress(int lessonId, int percent);
    }

    public static abstract class User {
        protected int id;
        protected String name;
        protected String email;
        protected String password;
        protected Role role;

        public User(int id, String name, String email, String password, Role role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Role getRole() { return role; }

        public abstract String getDashboardTitle(); // polymorphism
    }

    public static class Admin extends User {
        public Admin(int id, String name, String email, String password) {
            super(id, name, email, password, Role.ADMIN);
        }

        @Override
        public String getDashboardTitle() {
            return "Admin Dashboard - " + name;
        }
    }

    public static class Instructor extends User {
        public Instructor(int id, String name, String email, String password) {
            super(id, name, email, password, Role.INSTRUCTOR);
        }

        @Override
        public String getDashboardTitle() {
            return "Instructor Dashboard - " + name;
        }
    }

    public static class Learner extends User implements ProgressTrackable {

        public Learner(int id, String name, String email, String password) {
            super(id, name, email, password, Role.LEARNER);
        }

        @Override
        public String getDashboardTitle() {
            return "Learner Dashboard - " + name;
        }

        @Override
        public void updateProgress(int lessonId, int percent) {
            // logical behavior; actual DB update via service
            System.out.println("Learner " + name +
                    " updated progress for lesson " + lessonId + " to " + percent + "%");
        }
    }

    public static class Lesson {
        private int id;
        private String title;
        private String content;
        private int instructorId;

        public Lesson(int id, String title, String content, int instructorId) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.instructorId = instructorId;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public int getInstructorId() { return instructorId; }

        @Override
        public String toString() {
            return id + " - " + title;
        }
    }

    public static class ProgressRecord {
        private int id;
        private int learnerId;
        private int lessonId;
        private int completionPercent;
        private LocalDateTime lastUpdated;

        public ProgressRecord(int id, int learnerId, int lessonId,
                              int completionPercent, LocalDateTime lastUpdated) {
            this.id = id;
            this.learnerId = learnerId;
            this.lessonId = lessonId;
            this.completionPercent = completionPercent;
            this.lastUpdated = lastUpdated;
        }

        public int getId() { return id; }
        public int getLearnerId() { return learnerId; }
        public int getLessonId() { return lessonId; }
        public int getCompletionPercent() { return completionPercent; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }

    // ============================
    //     JDBC / DB CONNECTION
    // ============================

    public static class DBConnectionManager {
        private static final String URL =
                "jdbc:mysql://localhost:3306/language_platform";
        private static final String USER = "root";    // change
        private static final String PASSWORD = "root"; // change

        private DBConnectionManager() {}

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // ============================
    //          DAO LAYER
    // ============================

    public static class UserDAO {

        public User findByEmailAndPassword(String email, String password)
                throws SQLException {
            String sql = "SELECT * FROM users WHERE email=? AND password=?";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Role role = Role.valueOf(rs.getString("role"));
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        String pwd = rs.getString("password");

                        switch (role) {
                            case ADMIN:
                                return new Admin(id, name, email, pwd);
                            case INSTRUCTOR:
                                return new Instructor(id, name, email, pwd);
                            case LEARNER:
                                return new Learner(id, name, email, pwd);
                            default:
                                return null;
                        }
                    }
                }
            }
            return null;
        }

        public java.util.List<User> findAll() throws SQLException {
            java.util.List<User> list = new ArrayList<User>();
            String sql = "SELECT * FROM users";
            try (Connection conn = DBConnectionManager.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    Role role = Role.valueOf(rs.getString("role"));
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String pwd = rs.getString("password");

                    switch (role) {
                        case ADMIN:
                            list.add(new Admin(id, name, email, pwd));
                            break;
                        case INSTRUCTOR:
                            list.add(new Instructor(id, name, email, pwd));
                            break;
                        case LEARNER:
                            list.add(new Learner(id, name, email, pwd));
                            break;
                        default:
                            break;
                    }
                }
            }
            return list;
        }

        public void save(User user) throws SQLException {
            String sql = "INSERT INTO users(name, email, password, role) " +
                    "VALUES(?,?,?,?)";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole().name());
                ps.executeUpdate();
            }
        }

        public void delete(int id) throws SQLException {
            String sql = "DELETE FROM users WHERE id=?";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    public static class LessonDAO {

        public java.util.List<Lesson> findAll() throws SQLException {
            java.util.List<Lesson> list = new ArrayList<Lesson>();
            String sql = "SELECT * FROM lessons";
            try (Connection conn = DBConnectionManager.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    Lesson lesson = new Lesson(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getInt("instructor_id")
                    );
                    list.add(lesson);
                }
            }
            return list;
        }

        public void save(Lesson lesson) throws SQLException {
            String sql = "INSERT INTO lessons(title, content, instructor_id) " +
                    "VALUES(?,?,?)";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, lesson.getTitle());
                ps.setString(2, lesson.getContent());
                ps.setInt(3, lesson.getInstructorId());
                ps.executeUpdate();
            }
        }
    }

    public static class ProgressDAO {

        public void saveOrUpdateProgress(int learnerId, int lessonId, int percent)
                throws SQLException {
            String sql =
                    "INSERT INTO progress(learner_id, lesson_id, completion_percent) " +
                    "VALUES(?,?,?) " +
                    "ON DUPLICATE KEY UPDATE completion_percent = VALUES(completion_percent)";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, learnerId);
                ps.setInt(2, lessonId);
                ps.setInt(3, percent);
                ps.executeUpdate();
            }
        }

        public java.util.List<ProgressRecord> findByLearner(int learnerId)
                throws SQLException {
            java.util.List<ProgressRecord> list =
                    new ArrayList<ProgressRecord>();
            String sql = "SELECT * FROM progress WHERE learner_id=?";
            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, learnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("last_updated");
                        LocalDateTime ldt = null;
                        if (ts != null) {
                            ldt = ts.toLocalDateTime();
                        }
                        ProgressRecord pr = new ProgressRecord(
                                rs.getInt("id"),
                                rs.getInt("learner_id"),
                                rs.getInt("lesson_id"),
                                rs.getInt("completion_percent"),
                                ldt
                        );
                        list.add(pr);
                    }
                }
            }
            return list;
        }
    }

    // ============================
    //        SERVICE LAYER
    // ============================

    public static class AuthService {
        private final UserDAO userDAO = new UserDAO();

        public User login(String email, String password) throws Exception {
            User user = userDAO.findByEmailAndPassword(email, password);
            if (user == null) {
                throw new Exception("Invalid email or password");
            }
            return user;
        }
    }

    public static class LessonService {
        private final LessonDAO lessonDAO = new LessonDAO();

        // Collections & Generics – cache for lessons
        private final Map<Integer, Lesson> lessonCache =
                new HashMap<Integer, Lesson>();

        public java.util.List<Lesson> getAllLessons() throws SQLException {
            java.util.List<Lesson> lessons = lessonDAO.findAll();
            lessonCache.clear();
            for (Lesson l : lessons) {
                lessonCache.put(l.getId(), l);
            }
            return lessons;
        }

        public Lesson getLessonById(int id) {
            return lessonCache.get(id);
        }

        public void createLesson(String title, String content, int instructorId)
                throws SQLException {
            Lesson lesson = new Lesson(0, title, content, instructorId);
            lessonDAO.save(lesson);
        }
    }

    public static class ProgressService {
        private final ProgressDAO progressDAO = new ProgressDAO();

        // synchronized – safe for multithreaded access
        public synchronized void updateProgress(int learnerId, int lessonId,
                                                int percent) throws SQLException {
            progressDAO.saveOrUpdateProgress(learnerId, lessonId, percent);
        }

        public java.util.List<ProgressRecord> getProgressForLearner(int learnerId)
                throws SQLException {
            return progressDAO.findByLearner(learnerId);
        }
    }

    // ============================
    //   MULTITHREADING TASK
    // ============================

    public static class ProgressAutoSaveTask extends Thread {
        private final ProgressService progressService;
        private final int learnerId;
        private final int lessonId;

        private volatile boolean running = true;
        private volatile int currentPercent = 0;

        public ProgressAutoSaveTask(ProgressService progressService,
                                    int learnerId, int lessonId) {
            this.progressService = progressService;
            this.learnerId = learnerId;
            this.lessonId = lessonId;
            setName("ProgressAutoSaveThread");
        }

        public void setCurrentPercent(int percent) {
            this.currentPercent = percent;
        }

        public void stopTask() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    progressService.updateProgress(
                            learnerId, lessonId, currentPercent);
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException ex) {
                    // stopping is normal
                } catch (Exception e) {
                    System.err.println("Auto-save error: " + e.getMessage());
                }
            }
        }
    }

    // ============================
    //          GUI LAYER
    // ============================

    // ---------- LOGIN FRAME ----------
    public static class LoginFrame extends JFrame {

        private JTextField emailField;
        private JPasswordField passwordField;
        private final AuthService authService = new AuthService();

        public LoginFrame() {
            setTitle("Online Language Learning Platform - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 250);
            setLocationRelativeTo(null);

            initComponents();
        }

        private void initComponents() {
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

            panel.add(new JLabel("Email:"));
            emailField = new JTextField();
            panel.add(emailField);

            panel.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            panel.add(passwordField);

            JButton loginBtn = new JButton("Login");
            JButton exitBtn = new JButton("Exit");

            loginBtn.addActionListener(e -> handleLogin());
            exitBtn.addActionListener(e -> System.exit(0));

            panel.add(loginBtn);
            panel.add(exitBtn);

            add(panel, BorderLayout.CENTER);
        }

        private void handleLogin() {
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());

            try {
                User user = authService.login(email, pwd);
                JOptionPane.showMessageDialog(this, "Welcome, " + user.getName());
                openDashboard(user);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void openDashboard(User user) {
            SwingUtilities.invokeLater(() -> {
                JFrame dashboard;
                if (user.getRole() == Role.ADMIN) {
                    dashboard = new AdminDashboardFrame((Admin) user);
                } else if (user.getRole() == Role.INSTRUCTOR) {
                    dashboard = new InstructorDashboardFrame((Instructor) user);
                } else {
                    dashboard = new LearnerDashboardFrame((Learner) user);
                }
                dashboard.setVisible(true);
            });
            dispose();
        }
    }

    // ---------- ADMIN DASHBOARD ----------
    public static class AdminDashboardFrame extends JFrame {

        private final Admin admin;
        private final UserDAO userDAO = new UserDAO();
        private final LessonService lessonService = new LessonService();

        private JTable userTable;
        private JTable lessonTable;

        public AdminDashboardFrame(Admin admin) {
            this.admin = admin;
            setTitle(admin.getDashboardTitle());
            setSize(800, 600);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initUI();
            loadUsers();
            loadLessons();
        }

        private void initUI() {
            JTabbedPane tabs = new JTabbedPane();

            // User Management
            JPanel userPanel = new JPanel(new BorderLayout());
            userTable = new JTable();
            userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
            JButton refreshUsersBtn = new JButton("Refresh Users");
            refreshUsersBtn.addActionListener(e -> loadUsers());
            userPanel.add(refreshUsersBtn, BorderLayout.SOUTH);
            tabs.add("User Management", userPanel);

            // Lesson Management
            JPanel lessonPanel = new JPanel(new BorderLayout());
            lessonTable = new JTable();
            lessonPanel.add(new JScrollPane(lessonTable), BorderLayout.CENTER);
            JButton refreshLessonsBtn = new JButton("Refresh Lessons");
            refreshLessonsBtn.addActionListener(e -> loadLessons());
            lessonPanel.add(refreshLessonsBtn, BorderLayout.SOUTH);
            tabs.add("Lesson Management", lessonPanel);

            // System Settings (placeholder)
            JPanel settingsPanel = new JPanel();
            settingsPanel.add(new JLabel(
                    "System settings can be configured here (placeholder)."));
            tabs.add("System Settings", settingsPanel);

            // Activity Monitoring (placeholder)
            JPanel activityPanel = new JPanel();
            activityPanel.add(new JLabel(
                    "Real-time activity monitoring (placeholder)."));
            tabs.add("Activity Monitoring", activityPanel);

            add(tabs);
        }

        private void loadUsers() {
            try {
                java.util.List<User> users = userDAO.findAll();
                String[] cols = {"ID", "Name", "Email", "Role"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                for (User u : users) {
                    model.addRow(new Object[]{
                            u.getId(), u.getName(), u.getEmail(), u.getRole()
                    });
                }
                userTable.setModel(model);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading users: " + e.getMessage());
            }
        }

        private void loadLessons() {
            try {
                java.util.List<Lesson> lessons = lessonService.getAllLessons();
                String[] cols = {"ID", "Title", "Instructor ID"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                for (Lesson l : lessons) {
                    model.addRow(new Object[]{
                            l.getId(), l.getTitle(), l.getInstructorId()
                    });
                }
                lessonTable.setModel(model);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading lessons: " + e.getMessage());
            }
        }
    }

    // ---------- INSTRUCTOR DASHBOARD ----------
    public static class InstructorDashboardFrame extends JFrame {

        private final Instructor instructor;
        private final LessonService lessonService = new LessonService();

        private JTextField lessonTitleField;
        private JTextArea lessonContentArea;

        public InstructorDashboardFrame(Instructor instructor) {
            this.instructor = instructor;
            setTitle(instructor.getDashboardTitle());
            setSize(700, 500);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initUI();
        }

        private void initUI() {
            JTabbedPane tabs = new JTabbedPane();

            // Lesson Creation
            JPanel createLessonPanel = new JPanel(new BorderLayout());
            JPanel top = new JPanel(new GridLayout(2, 2));
            top.add(new JLabel("Lesson Title:"));
            lessonTitleField = new JTextField();
            top.add(lessonTitleField);

            createLessonPanel.add(top, BorderLayout.NORTH);

            lessonContentArea = new JTextArea(10, 40);
            createLessonPanel.add(new JScrollPane(lessonContentArea),
                    BorderLayout.CENTER);

            JButton saveLessonBtn = new JButton("Create Lesson");
            saveLessonBtn.addActionListener(e -> createLesson());
            createLessonPanel.add(saveLessonBtn, BorderLayout.SOUTH);
            tabs.add("Lesson Creation", createLessonPanel);

            // Provide Feedback (placeholder)
            JPanel feedbackPanel = new JPanel();
            feedbackPanel.add(new JLabel(
                    "Provide feedback to learners here (placeholder)."));
            tabs.add("Feedback", feedbackPanel);

            // Track Learner Progress (placeholder)
            JPanel progressPanel = new JPanel();
            progressPanel.add(new JLabel(
                    "View learner progress reports here (placeholder)."));
            tabs.add("Learner Progress", progressPanel);

            add(tabs);
        }

        private void createLesson() {
            String title = lessonTitleField.getText().trim();
            String content = lessonContentArea.getText();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Lesson title is required");
                return;
            }

            try {
                lessonService.createLesson(title, content, instructor.getId());
                JOptionPane.showMessageDialog(this,
                        "Lesson created successfully");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating lesson: " + e.getMessage());
            }
        }
    }

    // ---------- LEARNER DASHBOARD ----------
    public static class LearnerDashboardFrame extends JFrame {

        private final Learner learner;
        private final LessonService lessonService = new LessonService();
        private final ProgressService progressService = new ProgressService();

        private JTable lessonsTable;
        private JTable progressTable;
        private JSlider progressSlider;

        private ProgressAutoSaveTask autoSaveTask;

        public LearnerDashboardFrame(Learner learner) {
            this.learner = learner;
            setTitle(learner.getDashboardTitle());
            setSize(800, 600);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initUI();
            loadLessons();
            loadProgress();
        }

        private void initUI() {
            JTabbedPane tabs = new JTabbedPane();

            // Lesson Participation
            JPanel lessonPanel = new JPanel(new BorderLayout());
            lessonsTable = new JTable();
            lessonPanel.add(new JScrollPane(lessonsTable), BorderLayout.CENTER);

            JPanel bottom = new JPanel();
            bottom.add(new JLabel("Progress:"));
            progressSlider = new JSlider(0, 100, 0);
            progressSlider.setMajorTickSpacing(25);
            progressSlider.setPaintTicks(true);
            bottom.add(progressSlider);

            JButton startBtn = new JButton("Start Auto-Save");
            JButton stopBtn = new JButton("Stop Auto-Save");

            startBtn.addActionListener(e -> startAutoSave());
            stopBtn.addActionListener(e -> stopAutoSave());

            bottom.add(startBtn);
            bottom.add(stopBtn);

            lessonPanel.add(bottom, BorderLayout.SOUTH);
            tabs.add("Lessons", lessonPanel);

            // Progress Tracking
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressTable = new JTable();
            progressPanel.add(new JScrollPane(progressTable),
                    BorderLayout.CENTER);
            JButton refreshProgressBtn = new JButton("Refresh Progress");
            refreshProgressBtn.addActionListener(e -> loadProgress());
            progressPanel.add(refreshProgressBtn, BorderLayout.SOUTH);
            tabs.add("Progress Tracking", progressPanel);

            // Interactions (placeholder)
            JPanel interactions = new JPanel();
            interactions.add(new JLabel(
                    "Interact with other learners (messages/forum placeholder)."));
            tabs.add("Interactions", interactions);

            // Profile Management (simplified)
            JPanel profilePanel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField nameField = new JTextField(learner.getName());
            JTextField emailField = new JTextField(learner.getEmail());
            profilePanel.add(new JLabel("Name:"));
            profilePanel.add(nameField);
            profilePanel.add(new JLabel("Email:"));
            profilePanel.add(emailField);
            JButton updateProfileBtn = new JButton("Update Profile (Demo)");
            updateProfileBtn.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,
                            "Profile updated (demo only)."));
            profilePanel.add(updateProfileBtn);
            tabs.add("Profile", profilePanel);

            add(tabs);
        }

        private void loadLessons() {
            try {
                java.util.List<Lesson> lessons = lessonService.getAllLessons();
                String[] cols = {"ID", "Title", "Instructor ID"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                for (Lesson l : lessons) {
                    model.addRow(new Object[]{
                            l.getId(), l.getTitle(), l.getInstructorId()
                    });
                }
                lessonsTable.setModel(model);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading lessons: " + e.getMessage());
            }
        }

        private void loadProgress() {
            try {
                java.util.List<ProgressRecord> list =
                        progressService.getProgressForLearner(learner.getId());
                String[] cols = {"Lesson ID", "Completion %", "Last Updated"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                for (ProgressRecord pr : list) {
                    model.addRow(new Object[]{
                            pr.getLessonId(),
                            pr.getCompletionPercent(),
                            pr.getLastUpdated()
                    });
                }
                progressTable.setModel(model);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading progress: " + e.getMessage());
            }
        }

        private void startAutoSave() {
            int row = lessonsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Select a lesson first");
                return;
            }
            int lessonId = (int) lessonsTable.getValueAt(row, 0);

            if (autoSaveTask != null) {
                autoSaveTask.stopTask();
            }

            autoSaveTask = new ProgressAutoSaveTask(
                    progressService, learner.getId(), lessonId);
            autoSaveTask.start();

            progressSlider.addChangeListener(e -> {
                if (autoSaveTask != null) {
                    autoSaveTask.setCurrentPercent(progressSlider.getValue());
                    learner.updateProgress(lessonId,
                            progressSlider.getValue());
                }
            });

            JOptionPane.showMessageDialog(this,
                    "Auto-save started for lesson " + lessonId);
        }

        private void stopAutoSave() {
            if (autoSaveTask != null) {
                autoSaveTask.stopTask();
                autoSaveTask = null;
                JOptionPane.showMessageDialog(this, "Auto-save stopped");
            }
        }
    }

    // ============================
    //           MAIN
    // ============================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}

