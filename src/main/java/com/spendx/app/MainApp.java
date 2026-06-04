package com.spendx.app;

import com.spendx.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Spendx");
        primaryStage.setResizable(false);
        SceneManager.switchTo("/com/spendx/view/login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
