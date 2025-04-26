package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mindgarden.MainApp;
import mindgarden.db.JournalEntryDAO; // Added DAO import
// Note: JournalEntry model is not directly used here unless loading/displaying entries

public class JournalViewController {

    @FXML
    private TextArea journalTextArea;

    private JournalEntryDAO journalEntryDAO; // DAO instance

    @FXML
    public void initialize() {
        journalEntryDAO = new JournalEntryDAO(); // Initialize DAO
        // TODO: Optionally load the latest or all entries here if needed
        // List<JournalEntry> entries = journalEntryDAO.getAllJournalEntries();
        // Populate a ListView or set text area with latest entry etc.
    }

    @FXML
    private void saveEntry() {
        String entryContent = journalTextArea.getText().trim();
        if (!entryContent.isEmpty()) {
            boolean success = journalEntryDAO.addJournalEntry(entryContent);
            if (success) {
                System.out.println("Journal entry saved successfully.");
                journalTextArea.clear();
                // Optionally, provide user feedback (e.g., a temporary label)
            } else {
                System.err.println("Failed to save journal entry.");
                // Optionally show an error message to the user
            }
        } else {
             System.out.println("Journal entry is empty, not saving.");
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
