package mindgarden.controller;

import javafx.fxml.FXML;
import mindgarden.MainApp;

public class HomeViewController {

    @FXML
    private void openMoodTracker() {
        try {
            MainApp.changeScene("MoodTrackerView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openJournal() {
        try {
            MainApp.changeScene("JournalView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openMeditation() {
        try {
            MainApp.changeScene("MeditationView.fxml"); // ✅ This is now correct
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
