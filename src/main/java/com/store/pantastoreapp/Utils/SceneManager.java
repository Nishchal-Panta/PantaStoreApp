package com.store.pantastoreapp.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneManager {

    public static <T> T switchScene(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(SceneManager.class.getResource(fxmlPath))
        );

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // Return controller instance so caller can use it
        return loader.getController();
    }
}
