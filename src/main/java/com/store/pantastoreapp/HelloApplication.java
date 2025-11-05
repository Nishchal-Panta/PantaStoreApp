package com.store.pantastoreapp;

import com.store.pantastoreapp.db.DbMigrations;
import com.store.pantastoreapp.db.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DbMigrations.ensureTables(DBUtil.getDataSource());
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/FXML/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Panta Store");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}