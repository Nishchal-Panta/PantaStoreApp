package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Utils.ProductDAO;
import com.store.pantastoreapp.Models.Product;
import com.store.pantastoreapp.Utils.CurrentUser;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DashboardController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colExp;
    @FXML private TableColumn<Product, Integer> colQty;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, String> colCat_name;
    @FXML private TableColumn<Product, Timestamp> colCreatedAt;
    @FXML private Label welcomeMessage;

    private final ProductDAO dao = new ProductDAO();
    private ObservableList<Product> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        CurrentUser user= new CurrentUser();
        welcomeMessage.setText("Welcome,"+ CurrentUser.get().getFirstName());
        colCreatedAt.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTimestamp()));
        colCat_name.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCat_name()));
        colName.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
        colExp.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getExp().replace("/", "-").replace("T", " ").substring(0, 16) + " " + cell.getValue().getCost() + " USD"));
        colQty.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        colPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPrice()));
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<Product> list = dao.findAll();
            data.setAll(list);
            productTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            // show error
        }
    }
}
