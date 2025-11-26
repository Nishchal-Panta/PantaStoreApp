package com.store.pantastoreapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddItemPopupController {

    @FXML private TextField itemNameField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Runnable closeCallback;

    @FXML
    public void initialize() {
        cancelButton.setOnAction(e -> close());
        saveButton.setOnAction(e -> handleSave());
    }

    private void handleSave() {
        String name = itemNameField.getText();
        String price = priceField.getText();
        String qty = quantityField.getText();

        // TODO: Insert item into database here

        close(); // close after saving
    }

    public void setOnClose(Runnable callback) {
        this.closeCallback = callback;
    }

    private void close() {
        if (closeCallback != null) closeCallback.run();
    }
}
