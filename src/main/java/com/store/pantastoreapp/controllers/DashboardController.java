package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Models.User;
import com.store.pantastoreapp.Utils.ProductDAO;
import com.store.pantastoreapp.Models.Product;
import com.store.pantastoreapp.Utils.CurrentUser;
import com.store.pantastoreapp.Utils.SceneManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

public class DashboardController {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardController.class.getName());
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colExp;
    @FXML
    private TableColumn<Product, Integer> colQty;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, String> colCat_name;
    @FXML
    private TableColumn<Product, Timestamp> colCreatedAt;
    @FXML
    private Label welcomeMessage;
    @FXML
    private Button logout;

    private final ProductDAO dao = new ProductDAO();
    private ObservableList<Product> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (welcomeMessage == null) {
            System.err.println("WARNING: welcomeLabel was not injected — check fx:id in FXML and fx:controller.");
            return;
        }
        User user = CurrentUser.get();
        if (user != null) {
            String displayName = (user.getFirstName() != null && !user.getFirstName().isBlank())
                    ? user.getFirstName()
                    : (user.getUsername() != null ? user.getUsername() : "User");
            welcomeMessage.setText("Welcome, " + displayName);
        } else {
            // fallback to prevent NPE
            welcomeMessage.setText("Welcome, Guest");
            // optionally set a default avatar
            // avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));
        }
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

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchScene(stage, "/FXML/Login.fxml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error switching to Login scene", e);
        }
    }
}
