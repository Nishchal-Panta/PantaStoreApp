package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Models.User;
import com.sun.tools.javac.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

public class LoginController {
    @FXML
    private Label errorMessage;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button login;

    @FXML
    private Hyperlink signup;

    private Main mainApp;

    @FXML
    private void initialize() {
        login.setOnAction(event -> handleLogin());
        signup.setOnAction(event -> handleSignUp());
    }

    @FXML
    private void handleLogin(){
        String pass = password.getText();
        String name = username.getText();
        if (name.isEmpty() || pass.isEmpty()) {
            errorMessage.setVisible(true);
            errorMessage.setText( "Please fill all required fields.");
            return;
        }
        try{
            BufferedReader br = new BufferedReader(new FileReader("UserInfo.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String usrname = data[0].trim();
                String pswrd = data[1].trim();
                if (name.equals(usrname) && pass.equals(pswrd)) {
                    errorMessage.setText("");
                } else {
                    errorMessage.setVisible(true);
                    errorMessage.setText("Invalid username or password");
                }
            }
            br.close();
        } catch (IOException e) {
            errorMessage.setText("Error reading file");
        }
    }
    @FXML
    private void handleSignUp(){
        System.out.println("Sign up");
    }
}