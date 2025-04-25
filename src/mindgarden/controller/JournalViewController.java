package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mindgarden.MainApp;

public class JournalViewController {

    @FXML
    private TextArea journalTextArea;

    @FXML
    private void saveEntry() {
        String entry = journalTextArea.getText().trim();
        if (!entry.isEmpty()) {
            System.out.println("Journal saved: " + entry); // Pour l’instant on affiche dans la console
            journalTextArea.clear();
        }
    }

    @FXML
    private void goBack() {
        try {
            MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
