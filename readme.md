# Password Manager Application

## Overview

This is a Java-based Password Manager application that allows users to generate, store, and manage passwords securely. The application provides both a command-line interface and a graphical user interface. It uses SQLite to store password data locally, ensuring your sensitive information never leaves your computer.

## Features

- Generate secure passwords with customizable criteria (uppercase, lowercase, numbers, symbols)
- Store passwords in a local SQLite database
- Retrieve and manage stored passwords
- Search for passwords by website or username
- Modern, user-friendly GUI with dark theme
- Copy passwords to clipboard with a single click
- Password strength indicator
- Secure storage with local database

## Requirements

- Java Development Kit (JDK) 11 or higher
- SQLite JDBC Driver (included in the download)

## Download and Setup

### 1. Install Java Development Kit (JDK)

If you don't have Java installed:

1. Download the latest JDK from [Oracle's website](https://www.oracle.com/java/technologies/javase-downloads.html) or use OpenJDK
2. Follow the installation instructions for your operating system
3. Verify installation by opening a command prompt and typing:
   ```bash
   java -version

## Compilation and Execution
```bash
# Compilation and Execution
javac -cp ".;sqlite-jdbc.jar" PasswordManager.java PasswordGeneratorApp.java PasswordGeneratorGUI.java
java -cp ".;sqlite-jdbc.jar" PasswordGeneratorGUI
# Run the Application
java --enable-native-access=ALL-UNNAMED -cp ".;sqlite-jdbc-3.42.0.0.jar" PasswordGeneratorGUI
```
