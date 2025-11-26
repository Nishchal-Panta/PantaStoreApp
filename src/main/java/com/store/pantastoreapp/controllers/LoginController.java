package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Models.User;
import com.store.pantastoreapp.Utils.CurrentUser;
import com.store.pantastoreapp.Utils.SceneManager;
import com.store.pantastoreapp.Utils.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    @FXML private Label errorMessage;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button login;
    @FXML private Hyperlink signup;
    private final UserDAO userDAO = new UserDAO();
    @FXML
    private void handleLogin(ActionEvent event) {
        errorMessage.setVisible(false);
        String enteredUser = username.getText().trim();
        String enteredPass = password.getText();

        if (enteredUser.isEmpty() || enteredPass.isEmpty()) {
            errorMessage.setText("Please fill all required fields.");
            errorMessage.setVisible(true);
            return;
        }
        try{
            boolean ok = userDAO.authenticate(enteredUser, enteredPass);
            if (ok) {
                User u = userDAO.findByUsername(enteredUser);
                if (u != null) {
                    CurrentUser.set(u);
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                DashboardController controller =
                        SceneManager.switchScene(stage, "/FXML/Dashboard.fxml");

                if (controller != null) {
                    controller.initializeDashboard(CurrentUser.get());
                }
            } else {
                errorMessage.setText("Invalid username or password.");
                errorMessage.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorMessage.setText("Login failed (DB error).");
            errorMessage.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        Path csvPath = Paths.get("src", "main", "resources", "data", "UserInfo.csv");
//        if (!Files.exists(csvPath)) {
//            logger.log(Level.WARNING, "User data file not found: {0}", csvPath.toAbsolutePath().toString());
//            errorMessage.setText("No users found. Please sign up first.");
//            errorMessage.setVisible(true);
//            return;
//        }
//
//        boolean authenticated = false;
//
//        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (line.trim().isEmpty()) continue;
//                String[] parts = line.split(",", 3);
//                if (parts.length < 3) continue;
//                String storedFname = unescapeCsv(parts[0]);
//                String storedLname = unescapeCsv(parts[1]);
//                String storedHash = parts[2].trim();
//                String combinedUsername = (storedFname +"-"+ storedLname).trim();
//                if (combinedUsername.equalsIgnoreCase(enteredUser)) {
//                    // recompute hash using same salt (firstname+lastname)
//                    String recomputedHash = hashPasswordSha256(enteredPass, storedFname + storedLname);
//                    if (recomputedHash.equalsIgnoreCase(storedHash)) {
//                        authenticated = true;
//                    }
//                    break; // user found; stop searching
//                }
//            }
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "I/O error reading user data", e);
//            errorMessage.setText("Login failed (I/O error).");
//            errorMessage.setVisible(true);
//            return;
//        } catch (NoSuchAlgorithmException e) {
//            logger.log(Level.SEVERE, "Hash algorithm unavailable", e);
//            errorMessage.setText("Internal error (hashing).");
//            errorMessage.setVisible(true);
//            return;
//        }
//
//        if (authenticated) {
//            try {
//                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//                SceneManager.switchScene(stage, "/FXML/Dashboard.fxml");
//            } catch (IOException e) {
//                logger.log(Level.SEVERE, "Error switching scene", e);
//                errorMessage.setText("Error loading dashboard.");
//                errorMessage.setVisible(true);
//            }
//        } else {
//            errorMessage.setText("Username and password do not match.");
//            errorMessage.setVisible(true);
//        }
}
    @FXML
    public void handleSignUp() {
        try {
            Stage stage = (Stage) signup.getScene().getWindow();
            SceneManager.switchScene(stage, "/FXML/Signup.fxml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error switching to SignUp scene", e);
            errorMessage.setText("Could not open SignUp page.");
            errorMessage.setVisible(true);
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

    private String unescapeCsv(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}
