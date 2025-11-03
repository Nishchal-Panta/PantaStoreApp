package com.store.pantastoreapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    public void onSignup(){

    }

    @FXML
    public void onLogin(){
        if(username.getText().equals("admin") && password.getText().equals("<PASSWORD>")){
            JOptionPane.showMessageDialog(null, "Login Successful");
        }else{
            JOptionPane.showMessageDialog(null, "Invalid Username or Password");
        }
    }
}