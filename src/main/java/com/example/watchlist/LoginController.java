package com.example.watchlist;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField loginPassInput;

    @FXML
    private TextField loginUserInput;

    @FXML
    private Label noAccount;

    @FXML
    private void login() {
        btnLogin.setOnAction((ActionEvent event) -> {
            String username = loginUserInput.getText();
            String password = loginPassInput.getText();
            boolean authenticated = authenticate(username, password);
            if (authenticated) {
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
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
