module com.store.pantastoreapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.store.pantastoreapp to javafx.fxml;
    exports com.store.pantastoreapp;
    exports com.store.pantastoreapp.controllers;
    opens com.store.pantastoreapp.controllers to javafx.fxml;
}