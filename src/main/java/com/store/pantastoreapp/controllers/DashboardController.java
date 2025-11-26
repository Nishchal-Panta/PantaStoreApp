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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

public class DashboardController {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardController.class.getName());
    public BorderPane dashboardRoot;
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
    @FXML
    private StackPane rootPane;   // Main dashboard root

    private GaussianBlur blur = new GaussianBlur(20);

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
    private void handleStockAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddItemPopup.fxml"));
            Parent popupRoot = loader.load();

            // Create blur effect
            ColorAdjust blur = new ColorAdjust();
            GaussianBlur gaussian = new GaussianBlur(20);
            blur.setInput(gaussian);

            // Create dimmed overlay
            Rectangle overlay = new Rectangle();
            overlay.setFill(Color.rgb(0, 0, 0, 0.35));
            overlay.widthProperty().bind(rootPane.widthProperty());
            overlay.heightProperty().bind(rootPane.heightProperty());

            // Layer them inside rootPane
            rootPane.getChildren().addAll(overlay, popupRoot);

            // Apply blur
            dashboardRoot.setEffect(blur);

            // Center popup
            StackPane.setAlignment(popupRoot, Pos.CENTER);

            // Close handler
            AddItemPopupController controller = loader.getController();
            controller.setOnClose(() -> {
                rootPane.getChildren().remove(popupRoot);
                rootPane.getChildren().remove(overlay);
                dashboardRoot.setEffect(null);
            });

        } catch (Exception e) {
            e.printStackTrace();
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
//    @FXML
//    private void handleStockAdd(ActionEvent event) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddItemPopup.fxml"));
//            Parent popupRoot = loader.load();
//
//            Rectangle overlay = new Rectangle();
//            overlay.setFill(Color.rgb(0, 0, 0, 0.4));
//            overlay.widthProperty().bind(rootPane.widthProperty());
//            overlay.heightProperty().bind(rootPane.heightProperty());
//
//            rootPane.getChildren().addAll(overlay, popupRoot);
//
//            // Blur background
//            dashboardRoot.setEffect(blur);
//
//            StackPane.setAlignment(popupRoot, Pos.CENTER);
//
//            // Close handler
//            AddItemPopupController controller = loader.getController();
//            controller.setOnClose(() -> {
//                rootPane.getChildren().remove(popupRoot);
//                rootPane.getChildren().remove(overlay);
//                dashboardRoot.setEffect(null);
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void initializeDashboard(User user) {
        welcomeMessage.setText("Welcome, " + user.getFirstName());
    }

}
