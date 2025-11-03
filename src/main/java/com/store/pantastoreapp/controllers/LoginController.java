package com.store.pantastoreapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;

public class LoginController {
    @FXML
    private Label usernameErrorMessage;

    @FXML
    private Label passwordErrorMessage;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button login;

    @FXML
    private Hyperlink signup;

    @FXML
    private void initialize(){
    login.setOnAction(e -> {
        if(username.getText().isEmpty()){
            usernameErrorMessage.setText("Please enter your username");
        }else if(password.getText().isEmpty()){
            passwordErrorMessage.setText("Please enter your password");
        }else{
            JOptionPane.showMessageDialog(null, "Login Successful");
        }
    });
    }
}