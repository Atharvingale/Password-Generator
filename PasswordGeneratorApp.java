import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class PasswordGeneratorApp {
    private static final PasswordManager passwordManager = new PasswordManager();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            clearScreen();
            displayDateTime();
            displayMenu();
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        createNewPassword();
                        break;
                    case "2":
                        viewPasswords();
                        break;
                    case "3":
                        retrievePassword();
                        break;
                    case "4":
                        removePassword();
                        break;
                    case "5":
                        System.out.println("Thank you for using Password Generator!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void createNewPassword() throws SQLException {
        String[] passwordDetails = getPasswordDetails();
        String password = passwordManager.generatePassword(
            passwordDetails[0],
            Integer.parseInt(passwordDetails[1]),
            Integer.parseInt(passwordDetails[2]),
            Integer.parseInt(passwordDetails[3]),
            Integer.parseInt(passwordDetails[4])
        );
        
        passwordManager.appendToFile(null, passwordDetails[0], "", "", password);
        System.out.println("\nPassword for " + passwordDetails[0] + " is: " + password);
        displayPasswords();
    }

    private static void viewPasswords() throws SQLException {
        displayPasswords();
    }

    private static void retrievePassword() throws SQLException {
        System.out.print("Enter app name to find password: ");
        String appName = scanner.nextLine();
        
        PasswordManager.PasswordEntry entry = passwordManager.findPassword(null, appName);
        if (entry != null) {
            System.out.println("\nPassword for " + entry.getAppName() + " is: " + entry.getPassword());
        } else {
            System.out.println("\nApp / website not found in the database.");
        }
    }

    private static void removePassword() throws SQLException {
        System.out.print("Enter the password to remove: ");
        String passwordToRemove = scanner.nextLine();
        
        passwordManager.removePassword(null, passwordToRemove);
        System.out.println("\nUPDATED LIST");
        displayPasswords();
    }

    private static void displayPasswords() throws SQLException {
        List<PasswordManager.PasswordEntry> entries = passwordManager.readFile(null);
        System.out.println("\nStored Passwords:");
        System.out.println("----------------------------------------");
        System.out.printf("%-20s %-30s %-15s %-20s %-10s %-20s%n", 
            "Website", "Link", "Username", "Password", "Length", "Date/Time");
        System.out.println("----------------------------------------");
        
        for (PasswordManager.PasswordEntry entry : entries) {
            System.out.printf("%-20s %-30s %-15s %-20s %-10d %-20s%n",
                entry.getAppName(),
                entry.getLink(),
                entry.getUsername(),
                entry.getPassword(),
                entry.getLength(),
                entry.getDateTime());
        }
        System.out.println("----------------------------------------");
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Error clearing screen: " + e.getMessage());
        }
    }

    private static void displayDateTime() {
        System.out.println(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("|||  Welcome to Password Generator  |||");
    }

    private static void displayMenu() {
        System.out.println("    1. Generate and Save New Password");
        System.out.println("    2. View All Passwords");
        System.out.println("    3. Search Password");
        System.out.println("    4. Remove Password");
        System.out.println("    5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static String[] getPasswordDetails() {
        String[] details = new String[5];
        System.out.print("Enter name of App or Website: ");
        details[0] = scanner.nextLine();
        System.out.print("Enter the number of upper case letters: ");
        details[1] = scanner.nextLine();
        System.out.print("Enter the number of lower case letters: ");
        details[2] = scanner.nextLine();
        System.out.print("Enter the number of numbers: ");
        details[3] = scanner.nextLine();
        System.out.print("Enter the number of symbols: ");
        details[4] = scanner.nextLine();
        return details;
    }
}