package com.example.watchlist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Watchlist extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Watchlist.class.getResource("Watchlist.fxml"));
        Parent root = fxmlLoader.load();


        stage.getIcons().add(new Image(Watchlist.class.getResourceAsStream("/logo.png")));
        Scene scene = new Scene(root, 820, 620);
        stage.setTitle("Watchlist");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
