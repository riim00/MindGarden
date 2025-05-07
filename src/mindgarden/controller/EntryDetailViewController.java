package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.util.Optional;

import mindgarden.MainApp;
import mindgarden.db.JournalEntryDAO;
import mindgarden.model.JournalEntry;

public class EntryDetailViewController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label moodLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private VBox relatedEntriesContainer;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button shareButton;

    private JournalEntryDAO journalEntryDAO;
    private JournalEntry currentEntry;
    private int entryId;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        journalEntryDAO = new JournalEntryDAO();
    }

    /**
     * Sets the entry ID and loads the entry details
     * This method is called after the controller is initialized
     * @param id The ID of the journal entry to display
     */
    public void setEntryId(int id) {
        this.entryId = id;
        loadEntryDetails();
        loadRelatedEntries();
    }

    /**
     * Loads the entry details from the database
     */
    private void loadEntryDetails() {
        currentEntry = journalEntryDAO.getJournalEntryById(entryId);

        if (currentEntry != null) {
            // Set title (or use "Untitled Entry" if empty)
            titleLabel.setText(currentEntry.getTitle().isEmpty() ?
                    "Untitled Entry" : currentEntry.getTitle());

            // Format and set date and time
            LocalDateTime entryDateTime = currentEntry.getCreatedAt();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

            dateLabel.setText(entryDateTime.format(dateFormatter));
            timeLabel.setText(entryDateTime.format(timeFormatter));

            // Set mood emoji
            moodLabel.setText(getMoodEmoji(currentEntry.getMood()));

            // Set content
            contentLabel.setText(currentEntry.getContent());
        } else {
            // Handle case where entry couldn't be loaded
            titleLabel.setText("Entry Not Found");
            contentLabel.setText("The requested journal entry could not be found.");

            // Disable buttons that require a valid entry
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            exportButton.setDisable(true);
            shareButton.setDisable(true);
        }
    }

    /**
     * Loads related entries from around the same time period
     */
    private void loadRelatedEntries() {
        // Clear existing related entries
        relatedEntriesContainer.getChildren().clear();

        if (currentEntry == null) {
            return;
        }

        // Get a few recent entries excluding the current one
        List<JournalEntry> recentEntries = journalEntryDAO.getRecentEntries(4);

        // Filter out current entry and keep only up to 3 entries
        int count = 0;
        for (JournalEntry entry : recentEntries) {
            if (entry.getId() != entryId && count < 3) {
                HBox entryCard = createRelatedEntryCard(entry);
                relatedEntriesContainer.getChildren().add(entryCard);
                count++;
            }
        }

        // Show a message if no related entries were found
        if (relatedEntriesContainer.getChildren().isEmpty()) {
            Label noEntriesLabel = new Label("No related entries found.");
            noEntriesLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-style: italic;");
            relatedEntriesContainer.getChildren().add(noEntriesLabel);
        }
    }

    /**
     * Creates a card for a related entry
     * @param entry The related journal entry
     * @return An HBox containing the entry card
     */
    private HBox createRelatedEntryCard(JournalEntry entry) {
        HBox entryCard = new HBox();
        entryCard.setAlignment(Pos.CENTER_LEFT);
        entryCard.setSpacing(15);
        entryCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Date column
        VBox dateColumn = new VBox();
        dateColumn.setAlignment(Pos.CENTER);
        dateColumn.setMinWidth(70);

        // Format date
        LocalDateTime entryDate = entry.getCreatedAt();
        String month = entryDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String day = String.valueOf(entryDate.getDayOfMonth());

        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #5f6368;");

        Label dayLabel = new Label(day);
        dayLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Mood emoji
        Label moodLabel = new Label(getMoodEmoji(entry.getMood()));
        moodLabel.setStyle("-fx-font-size: 16;");

        dateColumn.getChildren().addAll(monthLabel, dayLabel, moodLabel);

        // Vertical separator
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        separator.setStyle("-fx-background-color: #e0e0e0;");

        // Content column
        VBox contentColumn = new VBox();
        contentColumn.setSpacing(5);
        HBox.setHgrow(contentColumn, Priority.ALWAYS);

        Label titleLabel = new Label(entry.getTitle().isEmpty() ? "Untitled Entry" : entry.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #3c4043;");

        // Limit preview text to 60 characters
        String previewText = entry.getContent().length() > 60
                ? entry.getContent().substring(0, 60) + "..."
                : entry.getContent();

        Label previewLabel = new Label(previewText);
        previewLabel.setWrapText(true);
        previewLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12;");

        contentColumn.getChildren().addAll(titleLabel, previewLabel);

        // View button
        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 20; -fx-padding: 5 15;");
        viewButton.setOnAction(e -> viewEntryDetails(entry.getId()));

        // Assemble all elements
        entryCard.getChildren().addAll(dateColumn, separator, contentColumn, viewButton);

        return entryCard;
    }

    /**
     * Returns the mood emoji based on the mood value
     * @param mood The mood string from the database
     * @return An emoji character representing the mood
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
     * Navigates back to the previous screen
     */
    @FXML
    private void goBack() {
        try {
            MainApp.changeScene("JournalView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not return to journal view.");
        }
    }

    /**
     * Opens the editor to modify the current entry
     */
    @FXML
    private void editEntry() {
        try {
            // Navigate to the journal editor view
            MainApp.changeScene("JournalEditorView.fxml");

            // Get the controller of the editor view
            JournalEditorController editorController = (JournalEditorController) MainApp.getController();

            // Set the controller to edit mode with the current entry
            editorController.setEditMode(currentEntry);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not open editor");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Deletes the current entry after confirmation
     */
    @FXML
    private void deleteEntry() {
        if (currentEntry == null) {
            return;
        }

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Entry");
        confirmDialog.setHeaderText("Delete Journal Entry");
        confirmDialog.setContentText("Are you sure you want to delete this journal entry? This action cannot be undone.");

        // Apply styling to match app's design
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f9f5;");

        // Add styling to buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 20;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 20;");

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = journalEntryDAO.deleteJournalEntry(entryId);

            if (success) {
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Entry Deleted");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Journal entry deleted successfully.");
                successAlert.showAndWait();

                // Return to journal view
                goBack();
            } else {
                // Show error message
                showErrorAlert("Delete Failed", "Failed to delete journal entry. Please try again.");
            }
        }
    }

    /**
     * Exports the current entry to a file
     */
    @FXML
    private void exportEntry() {
        if (currentEntry == null) {
            return;
        }

        try {
            // Determine desktop path with fallbacks
            String exportPath = System.getProperty("user.home") + File.separator + "Desktop";
            File exportDir = new File(exportPath);

            if (!exportDir.exists() || !exportDir.isDirectory() || !exportDir.canWrite()) {
                // Fall back to user home directory
                exportPath = System.getProperty("user.home");
                exportDir = new File(exportPath);
            }

            // Create filename with timestamp
            String entryTitle = currentEntry.getTitle().isEmpty() ? "Untitled_Entry" :
                    currentEntry.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_");

            String fileName = "MindGarden_" + entryTitle + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

            File exportFile = new File(exportDir, fileName);

            // Write file content
            try (FileWriter writer = new FileWriter(exportFile)) {
                // Write title
                writer.write("TITLE: " + currentEntry.getTitle() + "\n\n");

                // Write date and time
                writer.write("DATE: " + currentEntry.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.ENGLISH)) + "\n\n");

                // Write mood if available
                if (currentEntry.getMood() != null && !currentEntry.getMood().isEmpty()) {
                    writer.write("MOOD: " + currentEntry.getMood() + "\n\n");
                }

                // Write content
                writer.write("ENTRY CONTENT:\n");
                writer.write(currentEntry.getContent());

                writer.write("\n\nExported from MindGarden on " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Entry Exported");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Your journal entry has been exported to:\n" + exportFile.getAbsolutePath());

            // Try to set icon and style the alert
            try {
                Stage stage = (Stage) successAlert.getDialogPane().getScene().getWindow();
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

            successAlert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Export Error", "Could not export journal entry: " + e.getMessage());
        }
    }

    /**
     * Implements sharing functionality
     */
    @FXML
    private void shareEntry() {
        if (currentEntry == null) {
            return;
        }

        try {
            // Create a temporary file with the entry content
            File tempFile = File.createTempFile("mindgarden_share_", ".txt");
            tempFile.deleteOnExit();

            try (FileWriter writer = new FileWriter(tempFile)) {
                // Write title
                writer.write("TITLE: " + currentEntry.getTitle() + "\n\n");

                // Write date and time
                writer.write("DATE: " + currentEntry.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.ENGLISH)) + "\n\n");

                // Write mood if available
                if (currentEntry.getMood() != null && !currentEntry.getMood().isEmpty()) {
                    writer.write("MOOD: " + currentEntry.getMood() + "\n\n");
                }

                // Write content
                writer.write("ENTRY CONTENT:\n");
                writer.write(currentEntry.getContent());
            }

            // Create options for sharing
            ChoiceDialog<String> shareDialog = new ChoiceDialog<>("Email",
                    List.of("Email", "Text File", "Copy to Clipboard"));
            shareDialog.setTitle("Share Entry");
            shareDialog.setHeaderText("How would you like to share this entry?");
            shareDialog.setContentText("Choose a sharing method:");

            // Style the dialog
            DialogPane dialogPane = shareDialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #f5f9f5;");

            Optional<String> result = shareDialog.showAndWait();
            result.ifPresent(choice -> {
                switch (choice) {
                    case "Email":
                        try {
                            // Create email with default mail client
                            Desktop desktop = Desktop.getDesktop();
                            if (desktop.isSupported(Desktop.Action.MAIL)) {
                                String subject = "MindGarden Journal: " +
                                        (currentEntry.getTitle().isEmpty() ? "Untitled Entry" : currentEntry.getTitle());
                                String mailtoUrl = "mailto:?subject=" +
                                        URLEncoder.encode(subject, StandardCharsets.UTF_8) +
                                        "&attachment=" + tempFile.toURI().toString();
                                desktop.mail(URI.create(mailtoUrl));
                            } else {
                                throw new UnsupportedOperationException("Mail not supported on this platform");
                            }
                        } catch (Exception e) {
                            showErrorAlert("Share Error", "Could not open email client: " + e.getMessage());
                        }
                        break;

                    case "Text File":
                        try {
                            // Open file explorer with file selected
                            Desktop desktop = Desktop.getDesktop();
                            if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                                desktop.browseFileDirectory(tempFile);
                            } else if (desktop.isSupported(Desktop.Action.OPEN)) {
                                desktop.open(tempFile.getParentFile());
                            } else {
                                throw new UnsupportedOperationException("File browsing not supported on this platform");
                            }
                        } catch (Exception e) {
                            showErrorAlert("Share Error", "Could not open file location: " + e.getMessage());
                        }
                        break;

                    case "Copy to Clipboard":
                        try {
                            // Read the file content
                            String content = Files.readString(tempFile.toPath());

                            // Copy to clipboard
                            final Clipboard clipboard = Clipboard.getSystemClipboard();
                            final ClipboardContent clipboardContent = new ClipboardContent();
                            clipboardContent.putString(content);
                            clipboard.setContent(clipboardContent);

                            // Show confirmation
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Copied to Clipboard");
                            alert.setHeaderText(null);
                            alert.setContentText("Journal entry has been copied to clipboard.");
                            alert.showAndWait();
                        } catch (Exception e) {
                            showErrorAlert("Share Error", "Could not copy to clipboard: " + e.getMessage());
                        }
                        break;
                }
            });

        } catch (IOException e) {
            showErrorAlert("Share Error", "Could not share journal entry: " + e.getMessage());
        }
    }

    /**
     * Shows an error alert with the given title and message
     * @param title The alert title
     * @param message The alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigates to view a specific entry
     * @param entryId The ID of the entry to view
     */
    private void viewEntryDetails(int entryId) {
        try {
            // First change to the EntryDetailView scene
            MainApp.changeScene("EntryDetailView.fxml");

            // Then get the controller and set the entry ID
            EntryDetailViewController controller = (EntryDetailViewController) MainApp.getController();
            controller.setEntryId(entryId);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open entry details.");
        }
    }

    /**
     * Navigates to the all entries view
     */
    @FXML
    private void viewAllEntries() {
        try {
            MainApp.changeScene("AllEntriesView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not navigate to all entries view.");
        }
    }
}