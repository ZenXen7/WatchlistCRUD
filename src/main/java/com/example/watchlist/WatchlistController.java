package com.example.watchlist;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WatchlistController {

    private int userId;
    private String username;

    @FXML
    private Label nameOfUser;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField movieTitleInput;

    @FXML
    private TextField genreInput;

    @FXML
    private TableView<Movie> movieTable;

    @FXML
    private ObservableList<Movie> movieData;
    private Connection connection;

    @FXML
    private Hyperlink logout;

    public void setUsername(String username) {
        this.username = username;
        nameOfUser.setText(username + "'s Watchlist");
        this.userId = getUserId(username);
        fetchMovies();
    }

    @FXML
    private void initialize() {
        connection = Register.getConnection();
    }

    @FXML
    private void addList(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Movie to Watchlist");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField movieTitle = new TextField();
        movieTitle.setPromptText("Movie Title");
        TextField genre = new TextField();
        genre.setPromptText("Genre");

        grid.add(new Label("Movie Title:"), 0, 0);
        grid.add(movieTitle, 1, 0);
        grid.add(new Label("Genre:"), 0, 1);
        grid.add(genre, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return movieTitle.getText() + "," + genre.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String[] movieDetails = result.split(",");
            String movieTitleText = movieDetails[0];
            String genreText = movieDetails[1];
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO tblwatchlist (id, movie_title, genre) VALUES (?, ?, ?)");
                statement.setInt(1, getUserId(username));
                statement.setString(2, movieTitleText);
                statement.setString(3, genreText);
                statement.executeUpdate();
                fetchMovies();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private int getUserId(String username) {
        try (Connection c = Register.getConnection();
             PreparedStatement statement = c.prepareStatement("SELECT id FROM tbluseraccount WHERE username = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @FXML
    private void deleteList(ActionEvent event) {

    }

    private void fetchMovies() {
        if (movieData != null) {
            movieData.clear();
        } else {
            movieData = FXCollections.observableArrayList();
            movieTable.setItems(movieData);
        }
        try (Connection c = Register.getConnection();
             PreparedStatement statement = c.prepareStatement("SELECT movie_title, genre FROM tblwatchlist WHERE id = ?")) {
            statement.setInt(1, userId); // Use the user's ID
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String movieTitle = resultSet.getString("movie_title");
                String genre = resultSet.getString("genre");
                movieData.add(new Movie(movieTitle, genre));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();

            ((Stage) logout.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Movie {
        private final SimpleStringProperty movieTitle;
        private final SimpleStringProperty genre;

        public Movie(String movieTitle, String genre) {
            this.movieTitle = new SimpleStringProperty(movieTitle);
            this.genre = new SimpleStringProperty(genre);
        }

        public String getMovieTitle() {
            return movieTitle.get();
        }

        public String getGenre() {
            return genre.get();
        }
    }
}
