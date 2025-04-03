import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PasswordManager {
    private static final String SYMBOLS = "!@#$%^&*()_+[]{};:.<>?/`~|";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DB_URL = "jdbc:sqlite:passwords.db";

    public PasswordManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS passwords (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                website TEXT NOT NULL,
                link TEXT,
                username TEXT,
                password TEXT NOT NULL,
                length INTEGER NOT NULL,
                datetime TEXT NOT NULL
            )
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static class PasswordEntry {
        private final String appName;
        private final String link;
        private final String username;
        private final String password;
        private final int length;
        private final String dateTime;

        public PasswordEntry(String appName, String link, String username, String password, int length, String dateTime) {
            this.appName = appName;
            this.link = link;
            this.username = username;
            this.password = password;
            this.length = length;
            this.dateTime = dateTime;
        }

        public String getAppName() { return appName; }
        public String getLink() { return link; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public int getLength() { return length; }
        public String getDateTime() { return dateTime; }

        @Override
        public String toString() {
            return String.format("%s,%s,%s,%s,%d,%s", appName, link, username, password, length, dateTime);
        }
    }

    public String generatePassword(String websiteName, int upperCase, int lowerCase, int numbers, int symbols) {
        List<Character> passwordChars = new ArrayList<>();
        
        // Add uppercase letters
        for (int i = 0; i < upperCase; i++) {
            passwordChars.add((char) ('A' + new Random().nextInt(26)));
        }
        
        // Add lowercase letters
        for (int i = 0; i < lowerCase; i++) {
            passwordChars.add((char) ('a' + new Random().nextInt(26)));
        }
        
        // Add numbers
        for (int i = 0; i < numbers; i++) {
            passwordChars.add((char) ('0' + new Random().nextInt(10)));
        }
        
        // Add symbols
        for (int i = 0; i < symbols; i++) {
            passwordChars.add(SYMBOLS.charAt(new Random().nextInt(SYMBOLS.length())));
        }
        
        Collections.shuffle(passwordChars);
        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public void createNewFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("website,link,username,password,length,date/time");
        }
    }

    public void appendToFile(String filename, String appName, String link, String username, String password) throws SQLException {
        String sql = "INSERT INTO passwords (website, link, username, password, length, datetime) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appName.replace(",", ""));
            pstmt.setString(2, link.replace(",", ""));
            pstmt.setString(3, username.replace(",", ""));
            pstmt.setString(4, password);
            pstmt.setInt(5, password.length());
            pstmt.setString(6, LocalDateTime.now().format(DATE_FORMATTER));
            pstmt.executeUpdate();
        }
    }

    public List<PasswordEntry> readFile(String filename) throws SQLException {
        List<PasswordEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM passwords ORDER BY datetime DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                entries.add(new PasswordEntry(
                    rs.getString("website"),
                    rs.getString("link"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getInt("length"),
                    rs.getString("datetime")
                ));
            }
        }
        return entries;
    }

    public void removePassword(String filename, String passwordToRemove) throws SQLException {
        String sql = "DELETE FROM passwords WHERE password = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passwordToRemove);
            pstmt.executeUpdate();
        }
    }

    public PasswordEntry findPassword(String filename, String searchTerm) throws SQLException {
        String sql = "SELECT * FROM passwords WHERE LOWER(website) LIKE ? OR LOWER(username) LIKE ? LIMIT 1";
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PasswordEntry(
                        rs.getString("website"),
                        rs.getString("link"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("length"),
                        rs.getString("datetime")
                    );
                }
            }
        }
        return null;
    }

    public boolean removePasswordByIdentifier(String filename, String identifier) throws SQLException {
        String sql = "DELETE FROM passwords WHERE LOWER(website) LIKE ? OR LOWER(username) LIKE ?";
        String searchPattern = "%" + identifier.toLowerCase() + "%";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}