package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Arrays;

import mindgarden.MainApp;
import mindgarden.db.JournalEntryDAO;
import mindgarden.model.JournalEntry;

/**
 * Controller for the journal entry editor
 * Handles both creating new entries and editing existing ones
 */
public class JournalEditorController {

    @FXML
    private Label headerLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label editModeLabel;

    @FXML
    private ComboBox<String> moodSelector;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentTextArea;

    @FXML
    private Label wordCountLabel;

    @FXML
    private Button boldButton;

    @FXML
    private Button italicButton;

    @FXML
    private Button underlineButton;

    @FXML
    private Button imageButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button saveDraftButton;

    @FXML
    private Button cancelButton;

    private JournalEntryDAO journalEntryDAO;
    private JournalEntry currentEntry;
    private boolean editMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        journalEntryDAO = new JournalEntryDAO();

        // Setup date label with current date
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
        dateLabel.setText(now.format(dateFormatter));

        // Set up mood selector
        setupMoodSelector();

        // Set up word count listener
        setupWordCountListener();

        // Initially set as new entry mode
        setNewEntryMode();
    }

    /**
     * Set up the mood selector with options
     */
    private void setupMoodSelector() {
        moodSelector.getItems().addAll(
                "Happy 😊",
                "Calm 😌",
                "Thoughtful 🤔",
                "Sad 😢",
                "Angry 😤",
                "Anxious 😰",
                "Tired 😴"
        );
    }

    /**
     * Setup a listener to count words in the content text area
     */
    private void setupWordCountListener() {
        contentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            String text = contentTextArea.getText().trim();
            int wordCount = 0;

            if (!text.isEmpty()) {
                // Split by whitespace and count non-empty words
                wordCount = (int) Arrays.stream(text.split("\\s+"))
                        .filter(word -> !word.isEmpty())
                        .count();
            }

            wordCountLabel.setText("Word count: " + wordCount);
        });
    }

    /**
     * Configure the UI for creating a new entry
     */
    private void setNewEntryMode() {
        editMode = false;
        headerLabel.setText("Create Journal Entry");
        editModeLabel.setText("Creating a new entry");
        titleField.setText("");
        contentTextArea.setText("");
        moodSelector.setValue(null);

        // Update button labels for new entry
        saveButton.setText("Save Entry");
    }

    /**
     * Configure the UI for editing an existing entry
     * @param entry The entry to edit
     */
    public void setEditMode(JournalEntry entry) {
        if (entry == null) {
            setNewEntryMode();
            return;
        }

        editMode = true;
        currentEntry = entry;

        // Set UI elements
        headerLabel.setText("Edit Journal Entry");
        editModeLabel.setText("Editing existing entry");

        // Load entry data into fields
        titleField.setText(entry.getTitle());
        contentTextArea.setText(entry.getContent());

        // Get entry date
        LocalDateTime entryDate = entry.getCreatedAt();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
        dateLabel.setText(entryDate.format(dateFormatter));

        // Set mood if available
        if (entry.getMood() != null && !entry.getMood().isEmpty()) {
            for (String mood : moodSelector.getItems()) {
                if (mood.startsWith(entry.getMood())) {
                    moodSelector.setValue(mood);
                    break;
                }
            }
        }

        // Update button labels for edit mode
        saveButton.setText("Update Entry");
    }

    /**
     * Saves the current entry to the database
     */
    @FXML
    private void saveEntry() {
        // Validate content
        if (contentTextArea.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Empty Entry",
                    "Your journal entry is empty",
                    "Please write something before saving.");
            return;
        }

        String title = titleField.getText().trim();
        String content = contentTextArea.getText().trim();
        String mood = moodSelector.getValue();

        // Extract mood text without emoji if selected
        if (mood != null && mood.contains(" ")) {
            mood = mood.substring(0, mood.indexOf(" "));
        }

        boolean success;

        if (editMode && currentEntry != null) {
            // Update existing entry
            success = journalEntryDAO.updateJournalEntry(
                    currentEntry.getId(),
                    content,
                    title,
                    mood
            );

            if (success) {
                showAlert(AlertType.INFORMATION, "Entry Updated",
                        "Your journal entry has been updated successfully.", null);
                navigateToEntryDetail(currentEntry.getId());
            } else {
                showAlert(AlertType.ERROR, "Update Failed",
                        "Failed to update your journal entry.",
                        "Please try again.");
            }
        } else {
            // Save new entry
            success = journalEntryDAO.addJournalEntry(content, title, mood);

            if (success) {
                showAlert(AlertType.INFORMATION, "Entry Saved",
                        "Your journal entry has been saved successfully.", null);
                goBack();
            } else {
                showAlert(AlertType.ERROR, "Save Failed",
                        "Failed to save your journal entry.",
                        "Please try again.");
            }
        }
    }

    /**
     * Saves the current entry as a draft
     */
    @FXML
    private void saveDraft() {
        String title = titleField.getText().trim();
        String content = contentTextArea.getText().trim();
        String mood = moodSelector.getValue();

        // Extract mood text without emoji if selected
        if (mood != null && mood.contains(" ")) {
            mood = mood.substring(0, mood.indexOf(" "));
        }

        boolean success;

        if (editMode && currentEntry != null) {
            // Not implemented - drafts for existing entries
            showAlert(AlertType.INFORMATION, "Not Implemented",
                    "Draft mode for existing entries is not currently supported.",
                    "Your entry has been saved normally.");
            saveEntry();
            return;
        } else {
            // Save new draft
            success = journalEntryDAO.addJournalEntryAsDraft(content, title, mood);

            if (success) {
                showAlert(AlertType.INFORMATION, "Draft Saved",
                        "Your draft has been saved.",
                        "You can find it in your drafts folder.");
                goBack();
            } else {
                showAlert(AlertType.ERROR, "Save Failed",
                        "Failed to save your draft.",
                        "Please try again.");
            }
        }
    }

    /**
     * Cancels the current edit and returns to the previous screen
     */
    @FXML
    private void cancelEdit() {
        // Check if there are unsaved changes
        if (!contentTextArea.getText().trim().isEmpty() || !titleField.getText().trim().isEmpty()) {
            Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
            confirmDialog.setTitle("Discard Changes");
            confirmDialog.setHeaderText("Are you sure you want to discard your changes?");
            confirmDialog.setContentText("Any unsaved changes will be lost.");

            // Style the dialog
            DialogPane dialogPane = confirmDialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #f5f9f5;");

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.setText("Discard");
            okButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 20;");

            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 20;");

            Optional<ButtonType> result = confirmDialog.showAndWait();

            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // User canceled the discard operation
            }
        }

        goBack();
    }

    /**
     * Shows the help dialog
     */
    @FXML
    private void showHelp() {
        Alert helpDialog = new Alert(AlertType.INFORMATION);
        helpDialog.setTitle("Journal Editor Help");
        helpDialog.setHeaderText("How to Use the Journal Editor");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        content.getChildren().addAll(
                createHelpItem("Title", "Add an optional title to identify your entry."),
                createHelpItem("Content", "Write freely about your thoughts, feelings, and experiences."),
                createHelpItem("Mood", "Select your current mood from the dropdown menu."),
                createHelpItem("Formatting", "Use the B, I, and U buttons to format your text."),
                createHelpItem("Images", "The camera button allows you to add images (coming soon)."),
                createHelpItem("Drafts", "Save as draft to continue working on your entry later.")
        );

        helpDialog.getDialogPane().setContent(content);
        helpDialog.getDialogPane().setStyle("-fx-background-color: #f5f9f5;");

        // Try to set icon if available
        try {
            Stage stage = (Stage) helpDialog.getDialogPane().getScene().getWindow();
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/mindgarden_icon.png"));
                if (icon != null && !icon.isError()) {
                    stage.getIcons().add(icon);
                }
            } catch (Exception e) {
                System.out.println("Icon not found, continuing without setting window icon");
            }
        } catch (Exception e) {
            System.out.println("Could not style help dialog: " + e.getMessage());
        }

        helpDialog.showAndWait();
    }

    /**
     * Creates a help item with title and description
     */
    private HBox createHelpItem(String title, String description) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title + ":");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        item.getChildren().addAll(titleLabel, descLabel);
        return item;
    }

    /**
     * Navigate back to the previous screen
     */
    @FXML
    private void goBack() {
        try {
            if (editMode && currentEntry != null) {
                // Return to entry detail view
                navigateToEntryDetail(currentEntry.getId());
            } else {
                // Return to journal view
                MainApp.changeScene("JournalView.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                    "Could not return to previous screen.",
                    e.getMessage());
        }
    }

    /**
     * Navigate to the entry detail view for a specific entry
     */
    private void navigateToEntryDetail(int entryId) {
        try {
            MainApp.changeScene("EntryDetailView.fxml");
            EntryDetailViewController controller = (EntryDetailViewController) MainApp.getController();
            controller.setEntryId(entryId);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                    "Could not display entry details.",
                    e.getMessage());
        }
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Add application styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f9f5;");

        // Try to set icon if available
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/mindgarden_icon.png"));
                if (icon != null && !icon.isError()) {
                    stage.getIcons().add(icon);
                }
            } catch (Exception e) {
                System.out.println("Icon not found, continuing without setting window icon");
            }
        } catch (Exception e) {
            System.out.println("Could not style alert dialog: " + e.getMessage());
        }

        alert.showAndWait();
    }
}