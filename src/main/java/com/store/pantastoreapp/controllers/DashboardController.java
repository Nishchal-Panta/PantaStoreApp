package com.store.pantastoreapp.controllers;

import com.store.pantastoreapp.Models.Product;
import com.store.pantastoreapp.Models.User;
import com.store.pantastoreapp.Utils.CurrentUser;
import com.store.pantastoreapp.Utils.ProductDAO;
import com.store.pantastoreapp.Utils.SceneManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;

public class DashboardController {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardController.class.getName());

    // main UI
    @FXML public BorderPane dashboardRoot;
    @FXML private StackPane rootPane;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProduct_ID;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCat_name;
    @FXML private TableColumn<Product, java.util.Date> colExp;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colCost;
    @FXML private TableColumn<Product, Integer> colQty;
    @FXML private TableColumn<Product, Timestamp> colCreatedAt;
    @FXML private TableColumn<Product, Void> colActions;

    @FXML private Label welcomeMessage;
    @FXML private Button logout;
    @FXML private Button addItemBtn;
    @FXML private Button refreshBtn;

    // Embedded add/edit pane controls
    @FXML private AnchorPane addItemPane;
    @FXML private VBox addCard;
    @FXML private TextField itemNameField;
    @FXML private TextField itemPriceField;
    @FXML private TextField itemCostField;
    @FXML private TextField itemQtyField;
    @FXML private TextField itemCatField;
    @FXML private DatePicker itemExpField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final ProductDAO dao = new ProductDAO();
    private ObservableList<Product> data = FXCollections.observableArrayList();
    private final GaussianBlur blur = new GaussianBlur(16);

    // for edit flow
    private Product editingProduct = null;

    @FXML
    public void initialize() {
        // welcome message injection check
        if (welcomeMessage == null) {
            System.err.println("WARNING: welcomeLabel was not injected — check fx:id in FXML and fx:controller.");
            return;
        }

        // set welcome text
        User user = CurrentUser.get();
        if (user != null) {
            String displayName = (user.getFirstName() != null && !user.getFirstName().isBlank())
                    ? user.getFirstName()
                    : (user.getUsername() != null ? user.getUsername() : "User");
            welcomeMessage.setText("Welcome, " + displayName);
        } else {
            welcomeMessage.setText("Welcome, Guest");
        }

        // basic table column mapping
        setupTableColumns();

        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setPlaceholder(new Label("No products yet"));
        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addActionButtons();
        loadProducts();

        // wire save/cancel (they are also defined in FXML onAction)
        saveBtn.setOnAction(e -> onSaveAddItem());
        cancelBtn.setOnAction(e -> onCancelAddItem());
    }

    private void setupTableColumns() {
        colProduct_ID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colCat_name.setCellValueFactory(new PropertyValueFactory<>("cat_name"));
        colExp.setCellValueFactory(new PropertyValueFactory<>("exp")); // expect java.sql.Date -> Date
    }

    private void loadProducts() {
        try {
            List<Product> list = dao.findAll();
            data.setAll(list);
            productTable.setItems(data);
            productTable.refresh();
            System.out.println("Products loaded: " + list.size());
        } catch (SQLException e) {
            e.printStackTrace();
            // optionally show an alert
            showAlert(Alert.AlertType.ERROR, "Database error", "Could not load products: " + e.getMessage());
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-cursor: hand;");

                btnEdit.setOnAction(e -> {
                    Product selected = getTableView().getItems().get(getIndex());
                    openEditInline(selected);
                });

                btnDelete.setOnAction(e -> {
                    Product selected = getTableView().getItems().get(getIndex());
                    deleteProduct(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, btnEdit, btnDelete);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void deleteProduct(Product p) {
        if (p == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete product '" + p.getName() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    dao.delete(p.getId());
                    loadProducts();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Delete failed", e.getMessage());
                }
            }
        });
    }

    // ---------- Inline Add/Edit UI handlers ----------
    @FXML
    private void handleStockAdd(ActionEvent event) {
        // prepare form for Add
        editingProduct = null;
        clearAddForm();
        showAddPane();
    }

    private void openEditInline(Product product) {
        if (product == null) return;
        this.editingProduct = product;
        // fill fields
        itemNameField.setText(product.getName());
        itemPriceField.setText(String.valueOf(product.getPrice()));
        itemCostField.setText(String.valueOf(product.getCost()));
        itemQtyField.setText(String.valueOf(product.getQuantity()));
        itemCatField.setText(product.getCat_name());
        if (product.getExp() != null) itemExpField.setValue(((java.sql.Date) product.getExp()).toLocalDate());
        else itemExpField.setValue(null);

        showAddPane();
    }

    private void showAddPane() {
        // dim background with a semi-transparent rectangle and blur
        if (!rootPane.getChildren().contains(addItemPane)) {
            // ensure addItemPane is already in rootPane (it is declared in FXML)
        }
        // make it visible and managed so CSS/layout applies
        addItemPane.setManaged(true);
        addItemPane.setVisible(true);

        // center the card inside the rootPane (VBox / AnchorPane auto-centers visually)
        AnchorPane.setTopAnchor(addCard, 80.0);
        AnchorPane.setLeftAnchor(addCard, 80.0);
        AnchorPane.setRightAnchor(addCard, 80.0);

        // apply blur
        dashboardRoot.setEffect(blur);

        // also add a dimming Rectangle behind card so controls are not clickable (we'll add it if not present)
        // create or reuse overlay stored as userData
        if (rootPane.lookup("#__dimOverlay") == null) {
            Rectangle overlay = new Rectangle();
            overlay.setId("__dimOverlay");
            overlay.setFill(Color.rgb(0, 0, 0, 0.35));
            overlay.widthProperty().bind(rootPane.widthProperty());
            overlay.heightProperty().bind(rootPane.heightProperty());
            rootPane.getChildren().add(0, overlay); // behind addItemPane which is later in children list
        }

        // ensure addItemPane sits on top
        if (!rootPane.getChildren().contains(addItemPane)) {
            rootPane.getChildren().add(addItemPane);
        } else {
            // move to top
            rootPane.getChildren().remove(addItemPane);
            rootPane.getChildren().add(addItemPane);
        }
    }

    @FXML
    private void onCancelAddItem() {
        hideAddPane();
    }

    @FXML
    private void onSaveAddItem() {
        // Validate inputs
        String name = itemNameField.getText().trim();
        String cat = itemCatField.getText().trim();
        String priceText = itemPriceField.getText().trim();
        String costText = itemCostField.getText().trim();
        String qtyText = itemQtyField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Name is required.");
            return;
        }

        double price = parseDoubleOr(0.0, priceText);
        double cost = parseDoubleOr(0.0, costText);
        int qty = parseIntOr(0, qtyText);

        java.sql.Date expDate = null;
        LocalDate ld = itemExpField.getValue();
        if (ld != null) {
            expDate = java.sql.Date.valueOf(ld);
        }

        try {
            if (editingProduct == null) {
                // Insert
                Product p = new Product();
                p.setName(name);
                p.setCat_name(cat.isEmpty() ? null : cat);
                p.setPrice(price);
                p.setCost(cost);
                p.setQuantity(qty);
                p.setExp(expDate);
                dao.insert(p);
            } else {
                // Update
                editingProduct.setName(name);
                editingProduct.setCat_name(cat.isEmpty() ? null : cat);
                editingProduct.setPrice(price);
                editingProduct.setCost(cost);
                editingProduct.setQuantity(qty);
                editingProduct.setExp(expDate);
                dao.update(editingProduct);
            }
            hideAddPane();
            loadProducts();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save error", ex.getMessage());
        }
    }

    private void hideAddPane() {
        // remove dim overlay if present
        Node overlay = rootPane.lookup("#__dimOverlay");
        if (overlay != null) rootPane.getChildren().remove(overlay);

        addItemPane.setVisible(false);
        addItemPane.setManaged(false);
        dashboardRoot.setEffect(null);
        clearAddForm();
        editingProduct = null;
    }

    private void clearAddForm() {
        itemNameField.clear();
        itemPriceField.clear();
        itemCostField.clear();
        itemQtyField.clear();
        itemCatField.clear();
        itemExpField.setValue(null);
    }

    private double parseDoubleOr(double def, String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }

    private int parseIntOr(int def, String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    // Refresh button handler
    @FXML
    private void onRefresh(ActionEvent event) {
        loadProducts();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
    public void initializeDashboard(User user) {
        welcomeMessage.setText("Welcome, " + user.getFirstName());
    }
}
//package com.store.pantastoreapp.controllers;
//
//import com.store.pantastoreapp.Models.Product;
//import com.store.pantastoreapp.Models.User;
//import com.store.pantastoreapp.Utils.CurrentUser;
//import com.store.pantastoreapp.Utils.ProductDAO;
//import com.store.pantastoreapp.Utils.SceneManager;
//import javafx.beans.property.ReadOnlyObjectWrapper;
//import javafx.beans.property.ReadOnlyStringWrapper;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.effect.GaussianBlur;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.sql.Date;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.logging.Level;
//
//public class DashboardController {
//    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardController.class.getName());
//
//    // main UI
//    @FXML public BorderPane dashboardRoot;
//    @FXML private StackPane rootPane;
//
//    @FXML private TableView<Product> productTable;
//    @FXML private TableColumn<Product, Integer> colProduct_ID;
//    @FXML private TableColumn<Product, String> colName;
//    @FXML private TableColumn<Product, String> colCat_name;
//    @FXML private TableColumn<Product, java.util.Date> colExp;
//    @FXML private TableColumn<Product, Double> colPrice;
//    @FXML private TableColumn<Product, Double> colCost;
//    @FXML private TableColumn<Product, Integer> colQty;
//    @FXML private TableColumn<Product, Timestamp> colCreatedAt;
//    @FXML private TableColumn<Product, Void> colActions;
//
//    @FXML private Label welcomeMessage;
//    @FXML private Button logout;
//    @FXML private Button addItemBtn;
//    @FXML private Button refreshBtn;
//    @FXML private VBox logContainer;
//    @FXML private TabPane logTabPane;
//
//    // Embedded add/edit pane controls
//    @FXML private AnchorPane addItemPane;
//    @FXML private VBox addCard;
//    @FXML private TextField itemNameField;
//    @FXML private TextField itemPriceField;
//    @FXML private TextField itemCostField;
//    @FXML private TextField itemQtyField;
//    @FXML private TextField itemCatField;
//    @FXML private DatePicker itemExpField;
//    @FXML private Button saveBtn;
//    @FXML private Button cancelBtn;
//    @FXML private Button btnLogs;
//
//    private final ProductDAO dao = new ProductDAO();
//    private ObservableList<Product> data = FXCollections.observableArrayList();
//    private final GaussianBlur blur = new GaussianBlur(16);
//    // for edit flow
//    private Product editingProduct = null;
//    // for logging
//    private Product selectedProduct;
//
//    @FXML
//    public void initialize() {
//        // welcome message injection check
//        if (welcomeMessage == null) {
//            System.err.println("WARNING: welcomeLabel was not injected — check fx:id in FXML and fx:controller.");
//            return;
//        }
//
//        // set welcome text
//        User user = CurrentUser.get();
//        if (user != null) {
//            String displayName = (user.getFirstName() != null && !user.getFirstName().isBlank())
//                    ? user.getFirstName()
//                    : (user.getUsername() != null ? user.getUsername() : "User");
//            welcomeMessage.setText("Welcome, " + displayName);
//        } else {
//            welcomeMessage.setText("Welcome, Guest");
//        }
//
//        // basic table column mapping
//        setupTableColumns();
//
//        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        productTable.setPlaceholder(new Label("No products yet"));
//        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        addActionButtons();
//        loadProducts();
//
//        // wire save/cancel (they are also defined in FXML onAction)
//        saveBtn.setOnAction(e -> onSaveAddItem());
//        cancelBtn.setOnAction(e -> onCancelAddItem());
//    }
//
//    private void setupTableColumns() {
//        colProduct_ID.setCellValueFactory(new PropertyValueFactory<>("id"));
//        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
//        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
//        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
//        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
//        colCat_name.setCellValueFactory(new PropertyValueFactory<>("cat_name"));
//        colExp.setCellValueFactory(new PropertyValueFactory<>("exp")); // expect java.sql.Date -> Date
//    }
//
//    private void loadProducts() {
//        try {
//            List<Product> list = dao.findAll();
//            data.setAll(list);
//            productTable.setItems(data);
//            productTable.refresh();
//            System.out.println("Products loaded: " + list.size());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // optionally show an alert
//            showAlert(Alert.AlertType.ERROR, "Database error", "Could not load products: " + e.getMessage());
//        }
//    }
//
//    private void addActionButtons() {
//        colActions.setCellFactory(col -> new TableCell<>() {
//            private final Button btnLogs = new Button("Logs");
//            private final Button btnEdit = new Button("Edit");
//            private final Button btnDelete = new Button("Delete");
//
//            {
//                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
//                btnDelete.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-cursor: hand;");
//                btnLogs.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
//
//                btnLogs.setOnAction(e -> {
//                    Product p = getTableView().getItems().get(getIndex());
//                    toggleLogsRow(p, getIndex());
//                });
//
//                btnEdit.setOnAction(e -> {
//                    Product selected = getTableView().getItems().get(getIndex());
//                    openEditInline(selected);
//                });
//
//                btnDelete.setOnAction(e -> {
//                    Product selected = getTableView().getItems().get(getIndex());
//                    deleteProduct(selected);
//                });
//            }
//            private void toggleLogsRow(Product product, int rowIndex) {
//                int logsRowIndex = rowIndex + 1;
//
//                // If the logs row already exists, remove it
//                if (logsRowIndex < productTable.getItems().size()
//                        && productTable.getItems().get(logsRowIndex) == null) {
//                    productTable.getItems().remove(logsRowIndex);
//                    return;
//                }
//
//                // Insert a dummy row BELOW the product
//                productTable.getItems().add(logsRowIndex, null);
//
//                productTable.setRowFactory(tv -> new TableRow<>() {
//                    @Override
//                    protected void updateItem(Product item, boolean empty) {
//                        super.updateItem(item, empty);
//
//                        if (item == null && getIndex() == logsRowIndex) {
//                            setPrefHeight(180);
//                        } else {
//                            setGraphic(null);
//                            setPrefHeight(30);
//                        }
//                    }
//                });
//            }
//        });
//    }
//
//    private void deleteProduct(Product p) {
//        if (p == null) return;
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete product '" + p.getName() + "'?", ButtonType.YES, ButtonType.NO);
//        confirm.setHeaderText(null);
//        confirm.showAndWait().ifPresent(resp -> {
//            if (resp == ButtonType.YES) {
//                try {
//                    dao.delete(p.getId());
//                    loadProducts();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showAlert(Alert.AlertType.ERROR, "Delete failed", e.getMessage());
//                }
//            }
//        });
//    }
//
//    // ---------- Inline Add/Edit UI handlers ----------
//    @FXML
//    private void handleStockAdd(ActionEvent event) {
//        // prepare form for Add
//        editingProduct = null;
//        clearAddForm();
//        showAddPane();
//    }
//
//    private void openEditInline(Product product) {
//        if (product == null) return;
//        this.editingProduct = product;
//        // fill fields
//        itemNameField.setText(product.getName());
//        itemPriceField.setText(String.valueOf(product.getPrice()));
//        itemCostField.setText(String.valueOf(product.getCost()));
//        itemQtyField.setText(String.valueOf(product.getQuantity()));
//        itemCatField.setText(product.getCat_name());
//        if (product.getExp() != null) itemExpField.setValue(((java.sql.Date) product.getExp()).toLocalDate());
//        else itemExpField.setValue(null);
//
//        showAddPane();
//    }
//
//    private void showAddPane() {
//        // dim background with a semi-transparent rectangle and blur
//        if (!rootPane.getChildren().contains(addItemPane)) {
//            // ensure addItemPane is already in rootPane (it is declared in FXML)
//        }
//        // make it visible and managed so CSS/layout applies
//        addItemPane.setManaged(true);
//        addItemPane.setVisible(true);
//
//        // center the card inside the rootPane (VBox / AnchorPane auto-centers visually)
//        AnchorPane.setTopAnchor(addCard, 80.0);
//        AnchorPane.setLeftAnchor(addCard, 80.0);
//        AnchorPane.setRightAnchor(addCard, 80.0);
//
//        // apply blur
//        dashboardRoot.setEffect(blur);
//
//        // also add a dimming Rectangle behind card so controls are not clickable (we'll add it if not present)
//        // create or reuse overlay stored as userData
//        if (rootPane.lookup("#__dimOverlay") == null) {
//            Rectangle overlay = new Rectangle();
//            overlay.setId("__dimOverlay");
//            overlay.setFill(Color.rgb(0, 0, 0, 0.35));
//            overlay.widthProperty().bind(rootPane.widthProperty());
//            overlay.heightProperty().bind(rootPane.heightProperty());
//            rootPane.getChildren().add(0, overlay); // behind addItemPane which is later in children list
//        }
//
//        // ensure addItemPane sits on top
//        if (!rootPane.getChildren().contains(addItemPane)) {
//            rootPane.getChildren().add(addItemPane);
//        } else {
//            // move to top
//            rootPane.getChildren().remove(addItemPane);
//            rootPane.getChildren().add(addItemPane);
//        }
//    }
//
//    @FXML
//    private void onCancelAddItem() {
//        hideAddPane();
//    }
//
//    @FXML
//    private void onSaveAddItem() {
//        // Validate inputs
//        String name = itemNameField.getText().trim();
//        String cat = itemCatField.getText().trim();
//        String priceText = itemPriceField.getText().trim();
//        String costText = itemCostField.getText().trim();
//        String qtyText = itemQtyField.getText().trim();
//        if (isNameInvalid(name)) {
//            showError("Invalid Product Name",
//                    "Product name cannot consist of numbers only.");
//            return;
//        }
//
//        if (isCategoryInvalid(cat)) {
//            showError("Invalid Category",
//                    "Category cannot contain any numbers.");
//            return;
//        }
//
//        if (name.isEmpty()) {
//            showAlert(Alert.AlertType.WARNING, "Validation", "Name is required.");
//            return;
//        }
//
//        double price = parseDoubleOr(0.0, priceText);
//        double cost = parseDoubleOr(0.0, costText);
//        int qty = parseIntOr(0, qtyText);
//
//        java.sql.Date expDate = null;
//        LocalDate ld = itemExpField.getValue();
//        if (ld != null) {
//            expDate = java.sql.Date.valueOf(ld);
//        }
//        if (isExpiryInvalid(expDate)) {
//            showError("Invalid Expiry Date",
//                    "Expiry date must be a future date.\n"
//                            + "Today or past dates are not allowed.");
//            return;
//        }
//        try {
//            if (editingProduct == null) {
//                // Insert
//                Product p = new Product();
//                p.setName(name);
//                p.setCat_name(cat.isEmpty() ? null : cat);
//                p.setPrice(price);
//                p.setCost(cost);
//                p.setQuantity(qty);
//                p.setExp(expDate);
//                dao.insert(p);
//            } else {
//                // Update
//                editingProduct.setName(name);
//                editingProduct.setCat_name(cat.isEmpty() ? null : cat);
//                editingProduct.setPrice(price);
//                editingProduct.setCost(cost);
//                editingProduct.setQuantity(qty);
//                editingProduct.setExp(expDate);
//                dao.update(editingProduct);
//            }
//            hideAddPane();
//            loadProducts();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Save error", ex.getMessage());
//        }
//    }
//    @FXML
//    private void showError(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    private boolean isNameInvalid(String name) {
//        if (name == null || name.isBlank()) return true;
//        return name.matches("\\d+"); // true if ONLY digits
//    }
//
//    // Category must NOT contain any digit at all
//    private boolean isCategoryInvalid(String cat) {
//        if (cat == null || cat.isBlank()) return true;
//        return cat.matches(".*\\d.*"); // true if it contains a digit
//    }
//    private boolean isExpiryInvalid(java.util.Date exp) {
//        if (exp == null) return true;
//
//        java.util.Date today = new java.util.Date();
//
//        // Remove time (set all to midnight) to compare only by date
//        long expDay = exp.getTime() / (24 * 60 * 60 * 1000);
//        long todayDay = today.getTime() / (24 * 60 * 60 * 1000);
//
//        return expDay <= todayDay; // invalid if exp ≤ today
//    }
//
//
//    private void hideAddPane() {
//        // remove dim overlay if present
//        Node overlay = rootPane.lookup("#__dimOverlay");
//        if (overlay != null) rootPane.getChildren().remove(overlay);
//
//        addItemPane.setVisible(false);
//        addItemPane.setManaged(false);
//        dashboardRoot.setEffect(null);
//        clearAddForm();
//        editingProduct = null;
//    }
//
//    private void clearAddForm() {
//        itemNameField.clear();
//        itemPriceField.clear();
//        itemCostField.clear();
//        itemQtyField.clear();
//        itemCatField.clear();
//        itemExpField.setValue(null);
//    }
//
//    private double parseDoubleOr(double def, String s) {
//        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
//    }
//
//    private int parseIntOr(int def, String s) {
//        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
//    }
//
//    // Refresh button handler
//    @FXML
//    private void onRefresh(ActionEvent event) {
//        loadProducts();
//    }
//
//    @FXML
//    private void handleLogout(ActionEvent event) throws IOException {
//        try {
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            SceneManager.switchScene(stage, "/FXML/Login.fxml");
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Error switching to Login scene", e);
//        }
//    }
//
//    private void showAlert(Alert.AlertType type, String title, String message) {
//        Alert a = new Alert(type, message, ButtonType.OK);
//        a.setHeaderText(null);
//        a.setTitle(title);
//        a.showAndWait();
//    }
//    public void initializeDashboard(User user) {
//        welcomeMessage.setText("Welcome, " + user.getFirstName());
//    }
//}


//package com.store.pantastoreapp.controllers;
//
//import com.store.pantastoreapp.Models.User;
//import com.store.pantastoreapp.Utils.ProductDAO;
//import com.store.pantastoreapp.Models.Product;
//import com.store.pantastoreapp.Utils.CurrentUser;
//import com.store.pantastoreapp.Utils.SceneManager;
//import javafx.beans.property.ReadOnlyObjectWrapper;
//import javafx.beans.property.ReadOnlyStringWrapper;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.effect.ColorAdjust;
//import javafx.scene.effect.Effect;
//import javafx.scene.effect.GaussianBlur;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.util.Date;
//import java.util.List;
//import java.util.logging.Level;
//
//public class DashboardController {
//    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardController.class.getName());
//    public BorderPane dashboardRoot;
//    @FXML
//    private TableView<Product> productTable;
//    @FXML
//    private TableColumn<Product, String> colName;
//    @FXML
//    private TableColumn<Product, Date> colExp;
//    @FXML
//    private TableColumn<Product, Integer> colQty;
//    @FXML
//    private TableColumn<Product, Double> colPrice;
//    @FXML
//    private TableColumn<Product, Double> colCost;
//    @FXML
//    private TableColumn<Product, String> colCat_name;
//    @FXML
//    private TableColumn<Product, Integer> colProduct_ID;
//    @FXML
//    private TableColumn<Product, Timestamp> colCreatedAt;
//    @FXML
//    private Label welcomeMessage;
//    @FXML
//    private Button logout;
//    @FXML
//    private StackPane rootPane;
//    private final Effect blur = new GaussianBlur(20);
//    @FXML
//    private TableColumn<Product, Void> colActions;// Main dashboard root
//
//    private final ProductDAO dao = new ProductDAO();
//    private ObservableList<Product> data = FXCollections.observableArrayList();
//
//    @FXML
//    public void initialize() {
//        if (welcomeMessage == null) {
//            System.err.println("WARNING: welcomeLabel was not injected — check fx:id in FXML and fx:controller.");
//            return;
//        }
//        User user = CurrentUser.get();
//        if (user != null) {
//            String displayName = (user.getFirstName() != null && !user.getFirstName().isBlank())
//                    ? user.getFirstName()
//                    : (user.getUsername() != null ? user.getUsername() : "User");
//            welcomeMessage.setText("Welcome, " + displayName);
//        } else {
//            // fallback to prevent NPE
//            welcomeMessage.setText("Welcome, Guest");
//            // optionally set a default avatar
//            // avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));
//        }
//        colCreatedAt.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTimestamp()));
//        colCat_name.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCat_name()));
//        colName.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
//        colExp.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getExp()));
//        colQty.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
//        colPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPrice()));
//        colProduct_ID.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
//        colCost.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getCost()));
//        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        productTable.setEditable(true);
//        productTable.setPlaceholder(new Label("No products yet"));
//        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        setupTableColumns();
//        loadProducts();
//        addActionButtons();
//    }
//
//    private void loadProducts() {
//        try {
//            List<Product> list = dao.findAll();
//            data.setAll(list);
//            productTable.setItems(data);
//            productTable.refresh();
//            System.out.println("Products loaded: " + dao.findAll().size());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // show error
//        }
//    }
//    private void setupTableColumns() {
//        colProduct_ID.setCellValueFactory(new PropertyValueFactory<>("id"));
//        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
//        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
//        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
//        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
//        colCat_name.setCellValueFactory(new PropertyValueFactory<>("cat_name"));
//        colExp.setCellValueFactory(new PropertyValueFactory<>("exp"));
//    }
//
//    @FXML
//    private void handleStockAdd(ActionEvent event) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddItemPopup.fxml"));
//            Parent popupRoot = loader.load();
//
//            // Create blur effect
//            ColorAdjust blur = new ColorAdjust();
//            GaussianBlur gaussian = new GaussianBlur(20);
//            blur.setInput(gaussian);
//
//            // Create dimmed overlay
//            Rectangle overlay = new Rectangle();
//            overlay.setFill(Color.rgb(0, 0, 0, 0.35));
//            overlay.widthProperty().bind(rootPane.widthProperty());
//            overlay.heightProperty().bind(rootPane.heightProperty());
//
//            // Layer them inside rootPane
//            rootPane.getChildren().addAll(overlay, popupRoot);
//
//            // Apply blur
//            dashboardRoot.setEffect(blur);
//
//            // Center popup
//            StackPane.setAlignment(popupRoot, Pos.CENTER);
//
//            // Close handler
//            AddItemPopupController controller = loader.getController();
//            controller.setOnClose(() -> {
//                rootPane.getChildren().remove(popupRoot);
//                rootPane.getChildren().remove(overlay);
//                dashboardRoot.setEffect(null);
//                loadProducts();
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    @FXML
//    private void handleLogout(ActionEvent event) throws IOException {
//        try {
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            SceneManager.switchScene(stage, "/FXML/Login.fxml");
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Error switching to Login scene", e);
//        }
//    }
//    private void addActionButtons() {
//        colActions.setCellFactory(col -> new TableCell<>() {
//
//            private final Button btnEdit = new Button("Edit");
//            private final Button btnDelete = new Button("Delete");
//
//            {
//                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
//                btnDelete.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-cursor: hand;");
//
//                btnEdit.setOnAction(e -> {
//                    Product selected = getTableView().getItems().get(getIndex());
//                    openEditPopup(selected);
//                });
//
//                btnDelete.setOnAction(e -> {
//                    Product selected = getTableView().getItems().get(getIndex());
//                    deleteProduct(selected);
//                });
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setGraphic(null);
//                } else {
//                    HBox box = new HBox(10, btnEdit, btnDelete);
//                    box.setAlignment(Pos.CENTER);
//                    setGraphic(box);
//                }
//            }
//        });
//    }
//    private void deleteProduct(Product p) {
//        try {
//            dao.delete(p.getId());
//            loadProducts();  // Refresh table
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    private void openEditPopup(Product product) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddItemPopup.fxml"));
//            Parent popupRoot = loader.load();
//
//            AddItemPopupController controller = loader.getController();
//            controller.setEditingProduct(product);  // Prefill fields
//
//            showPopup(popupRoot, controller);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    private void showPopup(Parent popupRoot, AddItemPopupController controller) {
//        Rectangle overlay = new Rectangle(rootPane.getWidth(), rootPane.getHeight());
//        overlay.setFill(Color.rgb(0, 0, 0, 0.4));
//
//        StackPane stack = (StackPane) rootPane.getParent();
//        stack.getChildren().addAll(overlay, popupRoot);
//
//        rootPane.setEffect(blur);
//        StackPane.setAlignment(popupRoot, Pos.CENTER);
//
//        controller.setOnClose(() -> {
//            stack.getChildren().remove(popupRoot);
//            stack.getChildren().remove(overlay);
//            rootPane.setEffect(null);
//            loadProducts(); // Refresh
//        });
//
//        overlay.widthProperty().bind(stack.widthProperty());
//        overlay.heightProperty().bind(stack.heightProperty());
//    }
//
//
//
//
//    public void initializeDashboard(User user) {
//        welcomeMessage.setText("Welcome, " + user.getFirstName());
//    }
//
//}
