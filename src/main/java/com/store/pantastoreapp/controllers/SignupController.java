package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupController {
    private static final Logger logger = Logger.getLogger(SignupController.class.getName());

    @FXML private Button signup;
    @FXML private TextField firstname;
    @FXML private TextField lastname;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label errorMessage;
    @FXML private Button back;

    @FXML
    public void passwordsMatch(ActionEvent event) {
        errorMessage.setVisible(false);
        if (!isValidPassword(password.getText())) {
            errorMessage.setVisible(true);
            errorMessage.setText("Password must be 8–16 chars\n include an uppercase letter\n a number\n and a unique symbol.");
            return;
        }
        else if (!password.getText().equals(confirmPassword.getText())) {
            errorMessage.setVisible(true);
            errorMessage.setText("Passwords do not match");
            return;
        }

        String fname = firstname.getText().trim();
        String lname = lastname.getText().trim();
        String plainPass = password.getText();

        if (fname.isEmpty() || lname.isEmpty() || plainPass.isEmpty()) {
            errorMessage.setVisible(true);
            errorMessage.setText("All fields are required");
            return;
        }
        try {
            Path csvPath = Paths.get("src", "main", "resources", "data", "UserInfo.csv");

            Path parent = csvPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            logger.log(Level.INFO, "Writing user data to: {0}", csvPath.toAbsolutePath().toString());

            String hashedPassword = hashPasswordSha256(plainPass, fname + lname);
            String line = String.join(",", escapeCsv(fname), escapeCsv(lname), hashedPassword);

            try (BufferedWriter bw = Files.newBufferedWriter(
                    csvPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                bw.write(line);
                bw.newLine();
            }

            logger.log(Level.INFO, "User data saved to: {0}", csvPath.toAbsolutePath().toString());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchScene(stage, "/FXML/Dashboard.fxml");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to file", e);
            errorMessage.setVisible(true);
            errorMessage.setText("Could not save user data (I/O error)");
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Hash algorithm not available", e);
            errorMessage.setVisible(true);
            errorMessage.setText("Internal error (hash unavailable)");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            errorMessage.setVisible(true);
            errorMessage.setText("Unexpected error");
        }
    }

    private String hashPasswordSha256(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String toHash = salt + password;
        byte[] digest = md.digest(toHash.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
    private boolean isValidPassword(String password) {
        // Must contain:
        // - at least one uppercase letter
        // - at least one lowercase letter
        // - at least one digit
        // - at least one special character
        // - 8 to 16 characters long
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        return password.matches(regex);
    }
    public void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchScene(stage, "/FXML/Login.fxml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error switching to Login scene", e);
            errorMessage.setText("Could not open Login page.");
            errorMessage.setVisible(true);
        }
    }
    public String getfn() { return firstname.getText(); }
    public String getln() { return lastname.getText(); }
    public String getpass() { return password.getText(); }
}
