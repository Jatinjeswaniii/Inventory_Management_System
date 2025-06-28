package inventorymanagement;

import javax.swing.*;
import java.sql.*;

public class Main {

    public static void main(String[] args) {
        showLoginOrRegister();
    }

    private static void showLoginOrRegister() {
        String[] options = {"Login", "Register"};
        int choice = JOptionPane.showOptionDialog(null, "Welcome! Choose an option:", "Inventory Login",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 1) {
            registerUser();
        } else {
            loginUser();
        }
    }

    private static void loginUser() {
        String username = JOptionPane.showInputDialog(null, "Enter username:");
        String password = JOptionPane.showInputDialog(null, "Enter password:");

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/InventoryDB",
                    DBConfig.USER,
                    DBConfig.PASSWORD
            );
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT role FROM Users WHERE username=? AND password=?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                SwingUtilities.invokeLater(() -> new InventoryManagementGUI(role).setVisible(true));
            } else {
                JOptionPane.showMessageDialog(null, "Invalid login credentials.");
                showLoginOrRegister();
            }

            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Login failed: " + e.getMessage());
        }
    }

    private static void registerUser() {
        String username = JOptionPane.showInputDialog(null, "Enter new username:");
        String password = JOptionPane.showInputDialog(null, "Enter new password:");
        String[] roles = {"admin", "user"};
        String role = (String) JOptionPane.showInputDialog(null, "Select role:", "Role Selection",
                JOptionPane.QUESTION_MESSAGE, null, roles, roles[1]);

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/InventoryDB",
                    DBConfig.USER,
                    DBConfig.PASSWORD
            );
            PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM Users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Username already exists.");
                rs.close();
                checkStmt.close();
                connection.close();
                showLoginOrRegister();
                return;
            }
            rs.close();
            checkStmt.close();

            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Registration successful. Please login.");

            stmt.close();
            connection.close();

            loginUser();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Registration failed: " + e.getMessage());
        }
    }
}
