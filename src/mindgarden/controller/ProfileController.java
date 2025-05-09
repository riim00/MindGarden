package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mindgarden.MainApp;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;

    @FXML
    public void initialize() {
        if (MainApp.currentUser != null) {
            usernameLabel.setText("Username: " + MainApp.currentUser.getUsername());
            emailLabel.setText("Email: " + MainApp.currentUser.getEmail());
        } else {
            usernameLabel.setText("No user logged in");
            emailLabel.setText("");
        }
    }

    @FXML
    private void handleLogout() throws Exception {
        MainApp.currentUser = null;
        MainApp.changeScene("login.fxml");
    }

    @FXML
    private void goBack() {
        try {
            mindgarden.MainApp.changeScene("HomeView.fxml"); // changez selon le nom réel de votre fichier FXML
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
