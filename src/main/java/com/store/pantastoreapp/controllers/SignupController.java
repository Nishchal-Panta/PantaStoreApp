package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Utils.SceneManager;
import com.store.pantastoreapp.Utils.UserDAO;
import com.store.pantastoreapp.Models.User;
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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupController {
    private static final Logger logger = Logger.getLogger(SignupController.class.getName());

    @FXML
    private Button signup;
    @FXML
    private TextField firstname;
    @FXML
    private TextField lastname;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Label errorMessage;
    @FXML
    private Button back;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void passwordsMatch(ActionEvent event) {
        if (!isValidPassword(password.getText())) {
            errorMessage.setVisible(true);
            errorMessage.setText("Password must be 8–16 chars\n include an uppercase letter\n a number\n and a unique symbol.");
            logger.log(Level.WARNING, "Invalid password: {0}", password.getText());
            return;
        } else if (!password.getText().equals(confirmPassword.getText())) {
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
            String user = fname + "-" + lname;
            // check username exists
            if (userDAO.findByUsername(user) != null) {
                errorMessage.setVisible(true);
                errorMessage.setText("Username already exists");
                logger.log(Level.WARNING, "Username already exists: {0}", user);
                return;
            }
            User created = userDAO.createUser(user, plainPass, fname, lname);
            UserDAO.showAllUsers();
            // success: navigate back to login or dashboard
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // optionally set welcome message with created.getFirstName()
            SceneManager.switchScene(stage, "/FXML/Login.fxml");
        } catch (SQLException e) {
            e.printStackTrace();
            errorMessage.setVisible(true);
            errorMessage.setText("Error saving user (DB).");
            logger.log(Level.SEVERE, "Error saving user (DB): {0}", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setVisible(true);
            errorMessage.setText("Unexpected error");
            logger.log(Level.SEVERE, "Unexpected error: {0}", e.getMessage());
        }
    }

        private String hashPasswordSha256 (String password, String salt) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String toHash = salt + password;
            byte[] digest = md.digest(toHash.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        }

        private static String bytesToHex ( byte[] bytes){
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }
        private boolean isValidPassword (String password){
            // Must contain:
            // - at least one uppercase letter
            // - at least one lowercase letter
            // - at least one digit
            // - at least one special character
            // - 8 to 16 characters long
            String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
            return password.matches(regex);
        }
        public void handleBack (ActionEvent event){
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                SceneManager.switchScene(stage, "/FXML/Login.fxml");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error switching to Login scene", e);
                errorMessage.setText("Could not open Login page.");
                errorMessage.setVisible(true);
            }
        }

        public String getfn () {
            return firstname.getText();
        }
        public String getln () {
            return lastname.getText();
        }
        public String getpass () {
            return password.getText();
        }
    }
