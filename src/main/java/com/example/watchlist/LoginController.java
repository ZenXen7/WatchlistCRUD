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
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private int userId;

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField loginPassInput;

    @FXML
    private TextField loginUserInput;

    @FXML
    private Label noAccount;

    @FXML
    private Hyperlink backToRegister;


    private void initialize() {
        backToRegister.setOnAction(this::backRegister);
    }

    @FXML
    private void backRegister(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();

            ((Stage) backToRegister.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void login() {
        btnLogin.setOnAction((ActionEvent event) -> {
            String username = loginUserInput.getText();
            String password = loginPassInput.getText();
            boolean authenticated = authenticate(username, password);
            if (authenticated) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Watchlist.fxml"));
                    Parent root = loader.load();
                    WatchlistController watchlistController = loader.getController();
                    watchlistController.setUsername(username);
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Watchlist");
                    stage.show();

                    Stage loginStage = (Stage) btnLogin.getScene().getWindow();
                    loginStage.close();

                }
                catch (IOException e){
                    e.printStackTrace();
                }
                System.out.println("Login successful");
            } else if(loginPassInput.getText().isEmpty() || loginPassInput.getText().isEmpty()){
                noAccount.setText("Details Empty");
            }
            else {
                noAccount.setText("Wrong username or password");
            }
        });

    }

    private boolean authenticate(String username, String password) {
        try (Connection c = Register.getConnection();
             PreparedStatement statement = c.prepareStatement("SELECT * FROM tbluseraccount WHERE username = ? AND password = ?")) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("id");
                return true;
            }
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
