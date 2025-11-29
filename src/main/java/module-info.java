module com.store.pantastoreapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires jdk.compiler;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires jbcrypt;
    requires javafx.graphics;


    opens com.store.pantastoreapp to javafx.fxml;
    opens com.store.pantastoreapp.controllers to javafx.fxml;
    opens com.store.pantastoreapp.Models to javafx.base;

    exports com.store.pantastoreapp;
    exports com.store.pantastoreapp.controllers;
    exports com.store.pantastoreapp.db;
    exports com.store.pantastoreapp.Models;
    exports com.store.pantastoreapp.Utils;
}