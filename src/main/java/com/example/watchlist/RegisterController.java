package com.example.watchlist;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        btnRegister.setOnAction(this::register);
        skipToLogin.setOnAction(this::skip);
    }


    @FXML
    private void register(ActionEvent event) {
        String username = inputUser.getText();
        String password = inputPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            wrongLogin.setText("Details cannot be empty");
        }
        else if(username.length() < 6 ){
            wrongLogin.setText("Username too short");
        }
        else if(password.length() < 6){
            wrongLogin.setText("Password too short");
        }else {
            try (Connection c = Register.getConnection()) {

                try (PreparedStatement checkStatement = c.prepareStatement(
                        "SELECT COUNT(*) FROM tbluseraccount WHERE username = ?")) {
                    checkStatement.setString(1, username);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            wrongLogin.setText("Username already exists");
                            return;
                        }
                    }
                }


                try (PreparedStatement statement = c.prepareStatement(
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

                }
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
            stage.getIcons().add(new Image(Login.class.getResourceAsStream("/logo.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

            ((Stage) skipToLogin.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
