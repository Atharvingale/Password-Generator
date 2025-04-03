import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.datatransfer.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.util.regex.*;
import java.io.File;

// Update imports to include SQL
import java.sql.SQLException;

public class PasswordGeneratorGUI extends JFrame {
    private final PasswordManager passwordManager;
    private JTextArea displayArea;
    private JTextField websiteNameField;
    private JTextField websiteLinkField;
    private JTextField usernameField;
    private JTextField upperCaseField;
    private JTextField lowerCaseField;
    private JTextField numbersField;
    private JTextField symbolsField;
    private JTextField searchField;
    private JPasswordField generatedPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JProgressBar strengthIndicator;
    private JLabel strengthLabel;
    private Timer fadeTimer;
    private JLabel statusLabel;
    private static final String PASSWORD_FILE = "passwords.txt";
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Define colors for modern theme
    private final Color PRIMARY_COLOR = new Color(123, 78, 203);        // Lighter Deep Purple
    private final Color SECONDARY_COLOR = new Color(33, 33, 33);      // Dark Gray
    private final Color SUCCESS_COLOR = new Color(76, 175, 80);       // Material Green
    private final Color WARNING_COLOR = new Color(255, 152, 0);       // Material Orange
    private final Color DANGER_COLOR = new Color(244, 67, 54);        // Material Red
    private final Color BACKGROUND_COLOR = new Color(18, 18, 18);     // Deep Dark
    private final Color TEXT_COLOR = new Color(255, 255, 255);        // Pure White
    private final Color BUTTON_TEXT_COLOR = new Color(0, 0, 0);        // Pure black
    private final Color FIELD_BACKGROUND = new Color(30, 30, 30);     // Dark Input
    private final Color BUTTON_HOVER = new Color(143, 51, 182);       // Lighter Purple
    private final Color PANEL_BACKGROUND = new Color(24, 24, 26);     // Slightly lighter than background
    private final Color DISABLED_TEXT = new Color(158, 158, 158);     // Gray for disabled text
    private final Color ACCENT_COLOR = new Color(233, 30, 99);        // Pink accent
    private final Color SIDEBAR_COLOR = new Color(28, 28, 30);  
          // Sidebar background

    public PasswordGeneratorGUI() {
        passwordManager = new PasswordManager();
        initializeGUI();
        setLookAndFeel();
        try {
            displayPasswords();  // Changed from displayFileContents
        } catch (SQLException ex) {
            showError("Error loading passwords: " + ex.getMessage());
        }
    }

    private void initializePasswordFile() {
        File file = new File(PASSWORD_FILE);
        if (!file.exists()) {
            try {
                // Create file with header only
                passwordManager.createNewFile(PASSWORD_FILE);
                showTemporaryStatus("Password file created successfully!", SUCCESS_COLOR);
            } catch (IOException ex) {
                showError("Error creating password file: " + ex.getMessage());
            }
        }
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void initializeGUI() {
        setTitle("Password Manager Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(1200, 800));
        
        // Create main container with gradient background
        JPanel mainContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR,
                                                    w, h, new Color(40, 40, 40));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainContainer.setLayout(new BorderLayout(0, 0));
        setContentPane(mainContainer);

        // Create sidebar
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);

        // Create content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // Add cards
        contentPanel.add(createGeneratorPanel(), "GENERATOR");
        contentPanel.add(createPasswordListPanel(), "PASSWORDS");

        // Show initial card
        cardLayout.show(contentPanel, "GENERATOR");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Add logo/title
        JLabel titleLabel = new JLabel("Password Manager Pro");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(30));

        // Add navigation buttons
        String[][] navItems = {
            {"Generate Password", "GENERATOR"},
            {"Password List", "PASSWORDS"}
        };

        for (String[] item : navItems) {
            JButton navButton = createNavButton(item[0], item[1]);
            sidebar.add(navButton);
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(230, 45));
        
        button.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });

        return button;
    }

    private JPanel createGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Create top section for password generation
        JPanel topSection = new JPanel(new BorderLayout(20, 20));
        topSection.setOpaque(false);

        // Password generation controls
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        controlsPanel.setBorder(createModernBorder("Password Generator"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Add website details
        addLabelAndField(controlsPanel, "Website Name:", websiteNameField = createStyledTextField(20), gbc, 0);
        addLabelAndField(controlsPanel, "Website Link:", websiteLinkField = createStyledTextField(20), gbc, 1);
        addLabelAndField(controlsPanel, "Username:", usernameField = createStyledTextField(20), gbc, 2);

        // Add password requirements
        JPanel requirementsPanel = new JPanel(new GridBagLayout());
        requirementsPanel.setOpaque(false);
        requirementsPanel.setBorder(createModernBorder("Password Requirements"));

        addLabelAndField(requirementsPanel, "Uppercase Letters:", upperCaseField = createStyledTextField(5), gbc, 0);
        addLabelAndField(requirementsPanel, "Lowercase Letters:", lowerCaseField = createStyledTextField(5), gbc, 1);
        addLabelAndField(requirementsPanel, "Numbers:", numbersField = createStyledTextField(5), gbc, 2);
        addLabelAndField(requirementsPanel, "Symbols:", symbolsField = createStyledTextField(5), gbc, 3);

        // Add generated password section
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 10));
        passwordPanel.setOpaque(false);
        passwordPanel.setBorder(createModernBorder("Generated Password"));

        generatedPasswordField = new JPasswordField();
        styleTextField(generatedPasswordField);
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setForeground(TEXT_COLOR);
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                generatedPasswordField.setEchoChar((char) 0);
            } else {
                generatedPasswordField.setEchoChar('•');
            }
        });

        strengthIndicator = new JProgressBar(0, 100);
        strengthIndicator.setStringPainted(true);
        strengthIndicator.setForeground(SUCCESS_COLOR);
        strengthIndicator.setBackground(FIELD_BACKGROUND);

        strengthLabel = new JLabel("Password Strength: ");
        strengthLabel.setForeground(TEXT_COLOR);

        JPanel strengthPanel = new JPanel(new BorderLayout(10, 0));
        strengthPanel.setOpaque(false);
        strengthPanel.add(strengthLabel, BorderLayout.WEST);
        strengthPanel.add(strengthIndicator, BorderLayout.CENTER);
        
        passwordPanel.add(generatedPasswordField, BorderLayout.NORTH);
        passwordPanel.add(showPasswordCheckBox, BorderLayout.WEST);
        passwordPanel.add(strengthPanel, BorderLayout.SOUTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton generateButton = createStyledButton("Generate Password", e -> generateAndDisplayPassword());
        JButton saveButton = createStyledButton("Save Password", e -> savePassword());
        JButton copyButton = createStyledButton("Copy to Clipboard", e -> copyPasswordToClipboard());

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(copyButton);

        // Assemble the panel
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        mainContent.add(controlsPanel, gbc);

        gbc.gridy = 1;
        mainContent.add(requirementsPanel, gbc);

        gbc.gridy = 2;
        mainContent.add(passwordPanel, gbc);

        gbc.gridy = 3;
        mainContent.add(buttonPanel, gbc);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        mainContent.add(Box.createVerticalGlue(), gbc);

        // Add scrolling support
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        styleScrollPane(scrollPane);

        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add status label at the bottom
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPasswordListPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Search panel at the top
        JPanel searchPanel = createSearchPanel();
        panel.add(searchPanel, BorderLayout.NORTH);

        // Password list in the center
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        displayArea.setBackground(FIELD_BACKGROUND);
        displayArea.setForeground(TEXT_COLOR);
        displayArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(createModernBorder("Saved Passwords"));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        styleScrollPane(scrollPane);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton refreshButton = createStyledButton("Refresh List", e -> viewPasswords());
        JButton removeButton = createStyledButton("Remove Selected", e -> removePassword());

        buttonPanel.add(refreshButton);
        buttonPanel.add(removeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setSelectionColor(PRIMARY_COLOR);
        field.setSelectedTextColor(TEXT_COLOR);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR.darker(), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setBackground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }
        });

        return button;
    }

    private void showTemporaryStatus(String message, Color color) {
        if (statusLabel == null) {
            statusLabel = new JLabel();
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            statusLabel.setOpaque(true);
        }
        
        statusLabel.setText(message);
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
        
        // Create fade effect
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        fadeTimer = new Timer(50, new ActionListener() {
            private float alpha = 1.0f;
            private final Color originalColor = color;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.05f;
                if (alpha <= 0) {
                    statusLabel.setVisible(false);
                    ((Timer) e.getSource()).stop();
                } else {
                    statusLabel.setBackground(new Color(
                        originalColor.getRed(),
                        originalColor.getGreen(),
                        originalColor.getBlue(),
                        (int) (alpha * 200)
                    ));
                }
            }
        });
        
        statusLabel.setVisible(true);
        fadeTimer.setInitialDelay(2000);  // Show message for 2 seconds before fading
        fadeTimer.start();
    }

    private void copyPasswordToClipboard() {
        char[] passwordChars = generatedPasswordField.getPassword();
        if (passwordChars.length > 0) {
            String password = new String(passwordChars);
            StringSelection selection = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            showTemporaryStatus("Password copied to clipboard!", SUCCESS_COLOR);
            
            // Clear the password from memory
            java.util.Arrays.fill(passwordChars, '0');
        }
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        strengthIndicator.setValue(strength);
        
        // Update colors based on strength
        Color indicatorColor;
        String strengthText;
        
        if (strength < 25) {
            indicatorColor = DANGER_COLOR;
            strengthText = "Very Weak";
        } else if (strength < 50) {
            indicatorColor = WARNING_COLOR;
            strengthText = "Weak";
        } else if (strength < 75) {
            indicatorColor = new Color(255, 193, 7);  // Material Amber
            strengthText = "Moderate";
        } else if (strength < 90) {
            indicatorColor = SUCCESS_COLOR;
            strengthText = "Strong";
        } else {
            indicatorColor = new Color(0, 200, 83);  // Material Light Green
            strengthText = "Very Strong";
        }
        
        strengthLabel.setText(strengthText);
        strengthLabel.setForeground(indicatorColor);
        
        // Style the progress bar
        strengthIndicator.setForeground(indicatorColor);
        strengthIndicator.setBackground(FIELD_BACKGROUND);
        strengthIndicator.setBorder(BorderFactory.createEmptyBorder());
        
        // Add a subtle animation
        Timer timer = new Timer(20, new ActionListener() {
            private int currentValue = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentValue < strength) {
                    currentValue += 2;
                    strengthIndicator.setValue(currentValue);
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        
        // Length contribution
        strength += Math.min(password.length() * 4, 40);
        
        // Character variety contribution
        if (Pattern.compile("[A-Z]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[a-z]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[0-9]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) strength += 15;
        
        return Math.min(strength, 100);
    }

    private void generateAndDisplayPassword() {
        String password = generatePassword();
        if (password != null) {
            generatedPasswordField.setText(password);
            generatedPasswordField.setEchoChar('•');
            showPasswordCheckBox.setSelected(false);
            updatePasswordStrength(password);
            showTemporaryStatus("Password generated successfully!", SUCCESS_COLOR);
        }
    }

    private void savePassword() {
        try {
            String password = new String(generatedPasswordField.getPassword());
            if (password.isEmpty()) {
                showError("Please generate a password first");
                return;
            }

            if (websiteNameField.getText().isEmpty()) {
                showError("Please enter a website name");
                return;
            }

            String websiteName = websiteNameField.getText();
            String websiteLink = websiteLinkField.getText();
            String username = usernameField.getText();

            passwordManager.appendToFile(null, websiteName, websiteLink, username, password);
            displayMessage("Password saved successfully!");
            displayPasswords();
            showTemporaryStatus("Password saved!", SUCCESS_COLOR);
            
            // Clear input fields after successful save
            clearDisplay();
        } catch (SQLException ex) {
            showError("Error saving password: " + ex.getMessage());
        }
    }

    private void viewPasswords() {
        try {
            displayPasswords();
            showTemporaryStatus("Passwords loaded!", SUCCESS_COLOR);
        } catch (SQLException ex) {
            showError("Error viewing passwords: " + ex.getMessage());
        }
    }

    private void searchPassword() {
        try {
            String searchTerm = searchField.getText();
            if (searchTerm.isEmpty()) {
                showError("Please enter a search term");
                return;
            }

            PasswordManager.PasswordEntry entry = passwordManager.findPassword(null, searchTerm);
            if (entry != null) {
                displayMessage(String.format("Found entry:\nWebsite: %s\nUsername: %s\nPassword: %s\nDate: %s",
                    entry.getAppName(),
                    entry.getUsername(),
                    entry.getPassword(),
                    entry.getDateTime()));
                showTemporaryStatus("Password found!", SUCCESS_COLOR);
            } else {
                showTemporaryStatus("No matching password found.", WARNING_COLOR);
            }
        } catch (SQLException ex) {
            showError("Error searching password: " + ex.getMessage());
        }
    }

    private void removePassword() {
        try {
            String input = JOptionPane.showInputDialog(this, "Enter website name or username to remove:");
            if (input == null || input.isEmpty()) {
                showError("Please enter a website name or username");
                return;
            }

            boolean removed = passwordManager.removePasswordByIdentifier(null, input);
            if (removed) {
                displayMessage("Password entry removed successfully!");
                displayPasswords();
                showTemporaryStatus("Password removed!", SUCCESS_COLOR);
            } else {
                showTemporaryStatus("No matching entry found.", WARNING_COLOR);
            }
        } catch (SQLException ex) {
            showError("Error removing password: " + ex.getMessage());
        }
    }

    private String generatePassword() {
        try {
            int upperCase = Integer.parseInt(upperCaseField.getText());
            int lowerCase = Integer.parseInt(lowerCaseField.getText());
            int numbers = Integer.parseInt(numbersField.getText());
            int symbols = Integer.parseInt(symbolsField.getText());

            if (upperCase < 0 || lowerCase < 0 || numbers < 0 || symbols < 0) {
                showError("Please enter non-negative numbers");
                return null;
            }

            return passwordManager.generatePassword(
                websiteNameField.getText(),
                upperCase,
                lowerCase,
                numbers,
                symbols
            );
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for password generation");
            return null;
        }
    }

    // Replace displayFileContents with displayPasswords
    private void displayPasswords() throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        String header = String.format("%-20s %-30s %-15s %-20s %-10s %-20s%n", 
            "Website", "Link", "Username", "Password", "Length", "Date/Time");
        displayArea.setText(header);
        displayArea.setForeground(PRIMARY_COLOR);
        
        sb.append("─".repeat(120) + "\n");
        
        for (PasswordManager.PasswordEntry entry : passwordManager.readFile(null)) {
            sb.append(String.format("%-20s %-30s %-15s %-20s %-10d %-20s%n",
                truncateString(entry.getAppName(), 18),
                truncateString(entry.getLink(), 28),
                truncateString(entry.getUsername(), 13),
                "••••••••",
                entry.getLength(),
                entry.getDateTime()));
        }
        sb.append("─".repeat(120) + "\n");

        displayArea.append(sb.toString());
        displayArea.setCaretPosition(0);
    }

    private String truncateString(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 2) + "..";
    }

    private void displayMessage(String message) {
        displayArea.append(message + "\n");
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    private void clearDisplay() {
        displayArea.setText("");
        websiteNameField.setText("");
        websiteLinkField.setText("");
        usernameField.setText("");
        upperCaseField.setText("");
        lowerCaseField.setText("");
        numbersField.setText("");
        symbolsField.setText("");
        searchField.setText("");
        generatedPasswordField.setText("");
        showPasswordCheckBox.setSelected(false);
    }

    private void showError(String message) {
        UIManager.put("OptionPane.background", PANEL_BACKGROUND);
        UIManager.put("Panel.background", PANEL_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        searchField = createStyledTextField(30);
        searchField.setPreferredSize(new Dimension(300, 35));
        
        JButton searchButton = createStyledButton("Search", e -> searchPassword());
        
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);
        
        return panel;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleTextField(field);
        return field;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = PRIMARY_COLOR;
                this.trackColor = FIELD_BACKGROUND;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = PRIMARY_COLOR;
                this.trackColor = FIELD_BACKGROUND;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    private Border createModernBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker(), 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(),
                    title,
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 16),
                    ACCENT_COLOR
                )
            )
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PasswordGeneratorGUI().setVisible(true);
        });
    }
}