package com.example.watchlist;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.sql.*;

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
    private TableView<Movie> movieTable;

    @FXML
    private ObservableList<Movie> movieData;
    private Connection connection;

    @FXML
    private Hyperlink logout;
    @FXML
    private ComboBox<Genre> genreComboBox;





    public enum Genre {
        ACTION,
        COMEDY,
        DRAMA,
        HORROR,
        ROMANCE,
        SCIENCE_FICTION,
        THRILLER,
        OTHER
    }
    @FXML
    private void initialize() {
        genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll(Genre.values());
        genreComboBox.setItems(FXCollections.observableArrayList(Genre.values()));
        connection = Register.getConnection();
    }

    public void setUsername(String username) {
        this.username = username;
        nameOfUser.setText(username + "'s Watchlist");
        this.userId = getUserId(username);
        fetchMovies();
    }

    @FXML
    private void addList(ActionEvent event) {
        String currentDate = java.time.LocalDateTime.now().toString();

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

        grid.add(new Label("Movie Title:"), 0, 0);
        grid.add(movieTitle, 1, 0);
        grid.add(new Label("Genre:"), 0, 1);
        grid.add(genreComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return movieTitle.getText() + "," + genreComboBox.getValue() + "," + currentDate;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String[] movieDetails = result.split(",");
            if (movieDetails.length >= 3) {
                String movieTitleText = movieDetails[0];
                String genreText = movieDetails[1];
                String dateAdded = movieDetails[2];
                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO tblwatchlist (id, movie_title, genre, dateAdded) VALUES (?, ?, ?, ?)");
                    statement.setInt(1, getUserId(username));
                    statement.setString(2, movieTitleText);
                    statement.setString(3, genreText);
                    statement.setString(4, currentDate);
                    statement.executeUpdate();
                    fetchMovies();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {

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
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete the selected movie?");

            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeYes) {
                    try {
                        PreparedStatement statement = connection.prepareStatement("DELETE FROM tblwatchlist WHERE movie_id = ?");
                        statement.setInt(1, selectedMovie.getId());
                        statement.executeUpdate();
                        fetchMovies();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Movie Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a movie to delete.");
            alert.showAndWait();
        }
    }

    @FXML
    private void updateList(ActionEvent event) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Update Movie Details");

            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField movieTitle = new TextField(selectedMovie.getMovieTitle());
            movieTitle.setPromptText("Movie Title");

            ComboBox<Genre> genreComboBox = new ComboBox<>();
            genreComboBox.getItems().addAll(Genre.values());
            genreComboBox.setValue(Genre.valueOf(selectedMovie.getGenre()));
            grid.add(new Label("Movie Title:"), 0, 0);
            grid.add(movieTitle, 1, 0);
            grid.add(new Label("Genre:"), 0, 1);
            grid.add(genreComboBox, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return movieTitle.getText() + "," + genreComboBox.getValue();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                String[] movieDetails = result.split(",");
                if (movieDetails.length >= 2) {
                    String movieTitleText = movieDetails[0];
                    Genre genre = genreComboBox.getValue();
                    String genreText = genre.toString();
                    try {
                        PreparedStatement statement = connection.prepareStatement("UPDATE tblwatchlist SET movie_title = ?, genre = ? WHERE movie_id = ?");
                        statement.setString(1, movieTitleText);
                        statement.setString(2, genreText);
                        statement.setInt(3, selectedMovie.getId());
                        statement.executeUpdate();
                        fetchMovies();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Movie Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a movie to update.");
            alert.showAndWait();
        }
    }




    @FXML
    private void fetchMovies() {
        try (Connection c = Register.getConnection();
             PreparedStatement statement = c.prepareStatement("SELECT movie_id, movie_title, genre, dateAdded FROM tblwatchlist WHERE id = ?")) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (movieData == null) {
                movieData = FXCollections.observableArrayList();
                movieTable.setItems(movieData);
            } else {
                movieData.clear();
            }

            while (resultSet.next()) {
                int movieID = resultSet.getInt("movie_id");
                String movieTitle = resultSet.getString("movie_title");
                String genre = resultSet.getString("genre");

                Timestamp timestamp = resultSet.getTimestamp("dateAdded");


                LocalDateTime localDateTime = timestamp.toLocalDateTime();


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
                String formattedDateTime = localDateTime.format(formatter);


                movieData.add(new Movie(movieID, movieTitle, genre, formattedDateTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.getIcons().add(new Image(Login.class.getResourceAsStream("/logo.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

            ((Stage) logout.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class Movie {
        private final SimpleStringProperty movieTitle;
        private final SimpleStringProperty genre;
        private final SimpleIntegerProperty movieID;
        private final SimpleStringProperty dateAdded;

        public Movie(int id, String movieTitle, String genre, String dateAdded) {
            this.movieID = new SimpleIntegerProperty(id);
            this.movieTitle = new SimpleStringProperty(movieTitle);
            this.genre = new SimpleStringProperty(genre);
            this.dateAdded = new SimpleStringProperty(dateAdded);
        }

        public String getMovieTitle() {
            return movieTitle.get();
        }

        public int getId() {
            return movieID.get();
        }

        public SimpleIntegerProperty idProperty() {
            return movieID;
        }

        public String getGenre() {
            return genre.get();
        }

        public String getDateAdded() {
            return dateAdded.get();
        }
    }


}
