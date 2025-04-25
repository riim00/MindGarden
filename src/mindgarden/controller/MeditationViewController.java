package mindgarden.controller;

import javafx.fxml.FXML;
import mindgarden.MainApp;

public class MeditationViewController {

    @FXML
    private void goBack() {
        try {
            MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startMeditationTimer() {
        System.out.println("🧘 Meditation Timer Started!");
        // TODO: You can add countdown logic here later
    }
}
