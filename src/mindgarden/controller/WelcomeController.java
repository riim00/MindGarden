package mindgarden.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mindgarden.MainApp;

import java.io.IOException;

public class WelcomeController {

    @FXML
    public void navigateToSignUp(ActionEvent event) {
        try {
            MainApp.changeScene("signup.fxml"); // ou une popup/modal si tu préfères
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            MainApp.changeScene("login.fxml"); // ou une popup/modal si tu préfères
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openAbout(ActionEvent event) {
        // Implement about page navigation or dialog
        System.out.println("About button clicked");
    }

    @FXML
    public void openPrivacy(ActionEvent event) {
        // Implement privacy page navigation or dialog
        System.out.println("Privacy button clicked");
    }

    @FXML
    public void openContact(ActionEvent event) {
        // Implement contact page navigation or dialog
        System.out.println("Contact button clicked");
    }
}