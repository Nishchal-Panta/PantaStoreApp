package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Models.Product;
import com.store.pantastoreapp.Utils.ProductDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddItemPopupController {

    @FXML private TextField itemNameField;
    @FXML private TextField itemPriceField;
    @FXML private TextField itemCostField;
    @FXML private TextField itemQtyField;
    @FXML private TextField itemCatField;
    @FXML private DatePicker itemExpField;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Runnable onClose;
    private Product editingProduct = null;

    private final ProductDAO dao = new ProductDAO();

    @FXML
    public void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> closePopup());
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    private void closePopup() {
        if (onClose != null) onClose.run();
    }

    @FXML
    private void handleSave() {
        try {
            if (editingProduct == null) {
                // INSERT
                Product p = new Product();
                p.setName(itemNameField.getText());
                p.setPrice(Double.parseDouble(itemPriceField.getText()));
                p.setCost(Double.parseDouble(itemCostField.getText()));
                p.setQuantity(Integer.parseInt(itemQtyField.getText()));
                p.setCat_name(itemCatField.getText());
                p.setExp(java.sql.Date.valueOf(itemExpField.getValue()));
                dao.insert(p);
            } else {
                // UPDATE
                editingProduct.setName(itemNameField.getText());
                editingProduct.setPrice(Double.parseDouble(itemPriceField.getText()));
                editingProduct.setCost(Double.parseDouble(itemCostField.getText()));
                editingProduct.setQuantity(Integer.parseInt(itemQtyField.getText()));
                editingProduct.setCat_name(itemCatField.getText());
                editingProduct.setExp(java.sql.Date.valueOf(itemExpField.getValue()));
                dao.update(editingProduct);
            }

            if (onClose != null) onClose.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setEditingProduct(Product p) {
        this.editingProduct = p;
        itemNameField.setText(p.getName());
        itemPriceField.setText(String.valueOf(p.getPrice()));
        itemCostField.setText(String.valueOf(p.getCost()));
        itemQtyField.setText(String.valueOf(p.getQuantity()));
        itemCatField.setText(p.getCat_name());
        itemExpField.setValue(p.getExp().toLocalDate());
    }
}
