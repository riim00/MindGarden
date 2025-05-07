package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import mindgarden.MainApp;
import mindgarden.db.JournalEntryDAO;
import mindgarden.model.JournalEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AllEntriesViewController {

    @FXML
    private Button backButton;

    @FXML
    private Button settingsButton;

    @FXML
    private ComboBox<String> moodFilter;

    @FXML
    private DatePicker dateFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button clearFilterButton;

    @FXML
    private Label entriesCountLabel;

    @FXML
    private VBox entriesContainer;

    private JournalEntryDAO journalEntryDAO;
    private List<JournalEntry> allEntries;
    private List<JournalEntry> filteredEntries;

    @FXML
    public void initialize() {
        journalEntryDAO = new JournalEntryDAO();

        // Initialize mood filter options
        moodFilter.getItems().addAll(
                "All Moods",
                "😊 Happy",
                "😌 Calm",
                "🤔 Thoughtful",
                "😢 Sad",
                "😤 Angry",
                "😰 Anxious",
                "😴 Tired"
        );
        moodFilter.setValue("All Moods");

        // Set up event handlers
        searchButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        moodFilter.setOnAction(e -> applyFilters());
        dateFilter.setOnAction(e -> applyFilters());

        // Load all entries
        loadAllEntries();
    }

    /**
     * Loads all journal entries from the database and displays them
     */
    private void loadAllEntries() {
        // Clear the container first
        entriesContainer.getChildren().clear();

        // Get all entries from the database
        allEntries = journalEntryDAO.getAllEntries();
        filteredEntries = allEntries; // Initially, filtered entries are the same as all entries

        // Update the entries count label
        updateEntriesCountLabel(filteredEntries.size());

        // Display entries
        displayEntries(filteredEntries);
    }

    /**
     * Display the given list of entries in the UI
     * @param entries List of journal entries to display
     */
    private void displayEntries(List<JournalEntry> entries) {
        // Clear the container first
        entriesContainer.getChildren().clear();

        if (entries.isEmpty()) {
            Label noEntriesLabel = new Label("No journal entries found.");
            noEntriesLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-style: italic; -fx-font-size: 14;");
            noEntriesLabel.setPadding(new Insets(20));
            entriesContainer.getChildren().add(noEntriesLabel);
        } else {
            // Create an entry card for each entry
            for (JournalEntry entry : entries) {
                HBox entryCard = createEntryCard(entry);
                entriesContainer.getChildren().add(entryCard);
            }
        }
    }

    /**
     * Creates an entry card for display in the UI
     * @param entry The journal entry to display
     * @return An HBox containing the entry card
     */
    private HBox createEntryCard(JournalEntry entry) {
        // Create a card for the entry similar to the one in JournalViewController
        HBox entryCard = new HBox();
        entryCard.setAlignment(Pos.CENTER_LEFT);
        entryCard.setSpacing(15);
        entryCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Date and mood column
        VBox dateColumn = new VBox();
        dateColumn.setAlignment(Pos.CENTER);
        dateColumn.setMinWidth(80);

        // Format the date
        LocalDateTime entryDate = entry.getCreatedAt();
        String month = entryDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        String day = String.valueOf(entryDate.getDayOfMonth());
        String year = String.valueOf(entryDate.getYear());

        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #5f6368;");

        Label dayLabel = new Label(day);
        dayLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Label yearLabel = new Label(year);
        yearLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #5f6368;");

        // Emoji representing the mood
        Label moodLabel = new Label(getMoodEmoji(entry.getMood()));
        moodLabel.setStyle("-fx-font-size: 24;");

        dateColumn.getChildren().addAll(monthLabel, dayLabel, yearLabel, moodLabel);

        // Vertical separator
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        separator.setStyle("-fx-background-color: #e0e0e0;");

        // Entry content
        VBox contentColumn = new VBox();
        contentColumn.setSpacing(5);
        HBox.setHgrow(contentColumn, Priority.ALWAYS);

        Label titleLabel = new Label(entry.getTitle().isEmpty() ? "Untitled Entry" : entry.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #3c4043;");

        // Show first part of content (limited to 150 characters)
        String previewText = entry.getContent().length() > 150
                ? entry.getContent().substring(0, 150) + "..."
                : entry.getContent();

        Label previewLabel = new Label(previewText);
        previewLabel.setWrapText(true);
        previewLabel.setStyle("-fx-text-fill: #5f6368;");

        contentColumn.getChildren().addAll(titleLabel, previewLabel);

        // Add timestamp information
        Label timestampLabel = new Label(formatTimestamp(entry.getCreatedAt()));
        timestampLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #5f6368; -fx-font-style: italic;");
        contentColumn.getChildren().add(timestampLabel);

        // Button bar for actions
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_RIGHT);


// Utiliser du texte simple au lieu des émojis
        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 14px;");
        viewButton.setMinWidth(70); // Ajouter cette ligne pour définir une largeur minimale
        viewButton.setOnAction(e -> viewEntryDetails(entry.getId()));

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-border-color: #1976d2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 14px;");
        editButton.setMinWidth(70); // Ajouter cette ligne
        editButton.setOnAction(e -> editEntry(entry.getId()));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 14px;");
        deleteButton.setMinWidth(90); // Ajouter cette ligne
        deleteButton.setOnAction(e -> confirmDeleteEntry(entry.getId()));

        actionBar.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add a click event to the entire card to view the entry
        entryCard.setOnMouseClicked(e -> viewEntryDetails(entry.getId()));

        // Assemble all elements
        entryCard.getChildren().addAll(dateColumn, separator, contentColumn);

        // Add the action bar to a new VBox to ensure proper layout
        VBox actionColumn = new VBox(actionBar);
        actionColumn.setAlignment(Pos.CENTER);
        entryCard.getChildren().add(actionColumn);

        return entryCard;
    }

    /**
     * Apply filters based on mood, date, and search text
     */
    @FXML
    private void applyFilters() {
        // Start with all entries
        List<JournalEntry> result = allEntries;

        // Apply mood filter if not "All Moods"
        String selectedMood = moodFilter.getValue();
        if (selectedMood != null && !selectedMood.equals("All Moods")) {
            // Extract the mood name without emoji
            String moodName = selectedMood.substring(2);
            result = result.stream()
                    .filter(entry -> entry.getMood() != null && entry.getMood().contains(moodName))
                    .collect(Collectors.toList());
        }

        // Apply date filter if selected
        LocalDate selectedDate = dateFilter.getValue();
        if (selectedDate != null) {
            result = result.stream()
                    .filter(entry -> {
                        LocalDate entryDate = entry.getCreatedAt().toLocalDate();
                        return entryDate.equals(selectedDate);
                    })
                    .collect(Collectors.toList());
        }

        // Apply search filter if text is entered
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            String lowerCaseSearch = searchText.toLowerCase();
            result = result.stream()
                    .filter(entry ->
                            entry.getContent().toLowerCase().contains(lowerCaseSearch) ||
                                    (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(lowerCaseSearch)))
                    .collect(Collectors.toList());
        }

        // Update filtered entries and display them
        filteredEntries = result;
        updateEntriesCountLabel(filteredEntries.size());
        displayEntries(filteredEntries);
    }

    /**
     * Clear all filters and show all entries
     */
    @FXML
    private void clearFilters() {
        moodFilter.setValue("All Moods");
        dateFilter.setValue(null);
        searchField.clear();

        filteredEntries = allEntries;
        updateEntriesCountLabel(filteredEntries.size());
        displayEntries(filteredEntries);
    }

    /**
     * Update the label showing how many entries are being displayed
     */
    private void updateEntriesCountLabel(int count) {
        if (count == allEntries.size()) {
            entriesCountLabel.setText(String.format("Showing all entries (%d)", count));
        } else {
            entriesCountLabel.setText(String.format("Showing filtered entries (%d of %d)", count, allEntries.size()));
        }
    }

    /**
     * Format the timestamp for display
     */
    private String formatTimestamp(LocalDateTime timestamp) {
        return String.format("Created on %s at %02d:%02d",
                timestamp.toLocalDate(),
                timestamp.getHour(),
                timestamp.getMinute());
    }

    /**
     * Get the emoji for a mood string
     */
    private String getMoodEmoji(String mood) {
        if (mood == null || mood.isEmpty()) {
            return "📝"; // Default emoji
        }

        if (mood.contains("Happy")) return "😊";
        if (mood.contains("Calm")) return "😌";
        if (mood.contains("Thoughtful")) return "🤔";
        if (mood.contains("Sad")) return "😢";
        if (mood.contains("Angry")) return "😤";
        if (mood.contains("Anxious")) return "😰";
        if (mood.contains("Tired")) return "😴";

        return "📝"; // Default emoji
    }

    /**
     * Navigate to the EntryDetailView to view a specific entry
     */
    private void viewEntryDetails(int entryId) {
        try {
            // Get a reference to the current stage through an existing FXML element
            Stage primaryStage = (Stage) entriesContainer.getScene().getWindow();

            // Change to the EntryDetailView
            MainApp.changeScene("EntryDetailView.fxml");

            // Get the UserData of the scene which contains the loader
            FXMLLoader loader = (FXMLLoader) primaryStage.getScene().getUserData();
            EntryDetailViewController controller = (EntryDetailViewController) loader.getController();
            controller.setEntryId(entryId);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open entry details.");
        }
    }

    /**
     * Navigate to JournalEditorView to edit a specific entry
     * @param entryId The ID of the entry to edit
     */
    private void editEntry(int entryId) {
        try {
            // Change to the JournalEditorView for editing
            MainApp.changeScene("JournalEditorView.fxml");

            // Get the controller
            JournalEditorController controller = (JournalEditorController) MainApp.getController();

            // Load the entry from database
            JournalEntryDAO journalEntryDAO = new JournalEntryDAO();
            JournalEntry entry = journalEntryDAO.getJournalEntryById(entryId);

            // Set up controller for editing mode with the loaded entry
            controller.setEditMode(entry);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open entry for editing.");
        }
    }

    /**
     * Show a confirmation dialog before deleting an entry
     */
    private void confirmDeleteEntry(int entryId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Journal Entry");
        alert.setContentText("Are you sure you want to delete this journal entry? This action cannot be undone.");

        // Style the alert dialog
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

        // Show the dialog and wait for user's response
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
            deleteEntry(entryId);
        }
    }

    /**
     * Delete an entry from the database
     */
    private void deleteEntry(int entryId) {
        boolean success = journalEntryDAO.deleteJournalEntry(entryId);

        if (success) {
            // Show success message
            showAlert("Entry Deleted", "The journal entry was successfully deleted.", Alert.AlertType.INFORMATION);

            // Refresh the entries list
            loadAllEntries();
        } else {
            showErrorAlert("Deletion Error", "Could not delete the journal entry. Please try again.");
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an error alert dialog
     */
    private void showErrorAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    /**
     * Go back to the previous view
     */
    @FXML
    private void goBack() {
        try {
            MainApp.changeScene("JournalView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}