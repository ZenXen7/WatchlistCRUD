package com.example.watchlist;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private Button btnRegister;

    @FXML
    private PasswordField inputPass;

    @FXML
    private Label wrongLogin;

    @FXML
    private TextField inputUser;

    @FXML
    private Hyperlink skipToLogin;

    private void initialize() {
        skipToLogin.setOnAction(this::skip);
    }

    @FXML
    private void register() {
        String username = inputUser.getText();
        String password = inputPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            wrongLogin.setText("Details cannot be empty");
        } else {
            try (Connection c = Register.getConnection();
                 PreparedStatement statement = c.prepareStatement(
                         "INSERT INTO tbluseraccount(username, password) VALUES (?,?)"
                 )) {
                statement.setString(1, username);
                statement.setString(2, password);
                int rows = statement.executeUpdate();
                System.out.println("Rows inserted: " + rows);
                System.out.println("Data Inserted Successfully");
                wrongLogin.setText("Account Created");

                inputUser.clear();
                inputPass.clear();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void skip(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

            ((Stage) skipToLogin.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
