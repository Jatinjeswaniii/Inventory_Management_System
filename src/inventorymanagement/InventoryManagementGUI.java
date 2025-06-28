package inventorymanagement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;
import java.io.FileWriter;
import java.io.IOException;

public class InventoryManagementGUI extends JFrame {
    private Connection connection;
    private JTable table;
    private JTextField nameField, priceField, quantityField, idField;
    private JButton addButton, updateButton, deleteButton, viewButton, clearButton, exportButton;

    public InventoryManagementGUI(String role) {
        initializeDBConnection();
        initializeUI(role);
    }

    private void initializeDBConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = DBConfig.USER;
            String password = DBConfig.PASSWORD;
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void initializeUI(String role) {
        setTitle("Inventory Management System - Role: " + role);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Product ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Product Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        viewButton = new JButton("View All");
        clearButton = new JButton("Clear");
        exportButton = new JButton("Export CSV");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        if (!role.equalsIgnoreCase("admin")) {
            addButton.setEnabled(false);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tablePane = new JScrollPane(table);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(tablePane, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        viewButton.addActionListener(e -> viewProducts());
        clearButton.addActionListener(e -> clearFields());
        exportButton.addActionListener(e -> exportToCSV());

        table.getSelectionModel().addListSelectionListener(event -> fillFieldsFromSelectedRow());
    }

    private void addProduct() {
        String name = nameField.getText();
        String priceText = priceField.getText();
        String quantityText = quantityField.getText();

        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Products (name, price, quantity) VALUES (?, ?, ?)"
            );
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added successfully.");
            viewProducts();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage());
        }
    }

    private void updateProduct() {
        String idText = idField.getText();
        String name = nameField.getText();
        String priceText = priceField.getText();
        String quantityText = quantityField.getText();

        try {
            int id = Integer.parseInt(idText);
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE Products SET name=?, price=?, quantity=? WHERE id=?"
            );
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.setInt(4, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully.");
            viewProducts();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating product: " + e.getMessage());
        }
    }

    private void deleteProduct() {
        String idText = idField.getText();

        try {
            int id = Integer.parseInt(idText);

            PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM Products WHERE id=?"
            );
            stmt.setInt(1, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product deleted successfully.");
            viewProducts();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
        }
    }

    private void viewProducts() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Products");
            ResultSetMetaData metaData = rs.getMetaData();

            Vector<String> columnNames = new Vector<>();
            for (int column = 1; column <= metaData.getColumnCount(); column++) {
                columnNames.add(metaData.getColumnName(column));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<>();
                for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                    vector.add(rs.getObject(columnIndex));
                }
                data.add(vector);
            }

            table.setModel(new DefaultTableModel(data, columnNames));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error retrieving products: " + e.getMessage());
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
    }

    private void fillFieldsFromSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            idField.setText(table.getValueAt(selectedRow, 0).toString());
            nameField.setText(table.getValueAt(selectedRow, 1).toString());
            priceField.setText(table.getValueAt(selectedRow, 2).toString());
            quantityField.setText(table.getValueAt(selectedRow, 3).toString());
        }
    }

    private void exportToCSV() {
        try {
            FileWriter csvWriter = new FileWriter("inventory_export.csv");
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                csvWriter.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "," : ""));
            }
            csvWriter.write("\n");

            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    csvWriter.write(model.getValueAt(row, col).toString() + (col < model.getColumnCount() - 1 ? "," : ""));
                }
                csvWriter.write("\n");
            }
            csvWriter.close();
            JOptionPane.showMessageDialog(this, "Data exported to CSV successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting to CSV: " + e.getMessage());
        }
    }
}