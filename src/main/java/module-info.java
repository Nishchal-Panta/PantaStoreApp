module com.store.pantastoreapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires jdk.compiler;
    requires com.zaxxer.hikari;
    requires java.sql;


    opens com.store.pantastoreapp to javafx.fxml;
    exports com.store.pantastoreapp;
    exports com.store.pantastoreapp.controllers;
    opens com.store.pantastoreapp.controllers to javafx.fxml;
}