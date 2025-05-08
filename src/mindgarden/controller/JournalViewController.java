package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import mindgarden.model.JournalEntry;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.geometry.Orientation;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ButtonBar;
import javafx.geometry.Side;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import mindgarden.MainApp;
import mindgarden.db.JournalEntryDAO; // Added DAO import
// Note: JournalEntry model is not directly used here unless loading/displaying entries

public class JournalViewController {

    @FXML
    private TextArea journalTextArea;

    @FXML
    private Label dateLabel;

    @FXML
    private Button settingsButton; // Reference to the settings button in FXML

    @FXML
    private TextField titleField; // Reference to the title field in FXML

    @FXML
    private ComboBox<String> moodSelector; // Reference to the mood selector in FXML

    private JournalEntryDAO journalEntryDAO; // DAO instance
    private ContextMenu settingsMenu; // Settings menu that will appear when button is clicked
    @FXML
    private Button saveButton;

    @FXML
    private Button saveDraftButton;

    private int currentUserId;



    @FXML
    public void initialize() {
        journalEntryDAO = new JournalEntryDAO();

        updateCurrentDate();

        // Initialiser les sélecteurs d'humeur
        if (moodSelector != null) {
            moodSelector.getItems().addAll(
                    "😊 Happy",
                    "😌 Calm",
                    "🤔 Thoughtful",
                    "😢 Sad",
                    "😤 Angry",
                    "😰 Anxious",
                    "😴 Tired"
            );
        }

        // Initialiser le menu des paramètres
        initializeSettingsMenu();

        // Ajouter les gestionnaires d'événements pour les boutons
        if (saveButton != null) {
            saveButton.setOnAction(e -> saveEntry());
        }

        if (saveDraftButton != null) {
            saveDraftButton.setOnAction(e -> saveDraft());
        }

        // Charger les entrées récentes uniquement pour l'utilisateur courant
        loadRecentEntries();
    }

    /**
     * Initialize the settings menu with menu items
     */
    private void initializeSettingsMenu() {
        settingsMenu = new ContextMenu();

        // Apply custom styling to match the app's design
        settingsMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Create menu items with styling to match app's design
        MenuItem fontSizeItem = new MenuItem("Font Size");
        fontSizeItem.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-text-fill: #3c4043;");
        fontSizeItem.setOnAction(e -> changeFontSize());

        MenuItem themesItem = new MenuItem("Themes");
        themesItem.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-text-fill: #3c4043;");
        themesItem.setOnAction(e -> changeTheme());

        MenuItem exportItem = new MenuItem("Export Journal");
        exportItem.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-text-fill: #3c4043;");
        exportItem.setOnAction(e -> exportJournal());

        MenuItem preferencesItem = new MenuItem("Preferences");
        preferencesItem.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-text-fill: #3c4043;");
        preferencesItem.setOnAction(e -> openPreferences());

        // Add menu items to the context menu
        settingsMenu.getItems().addAll(fontSizeItem, themesItem, exportItem, preferencesItem);

        // Add click handler to the settings button
        settingsButton.setOnAction(e -> showSettingsMenu());
    }

    /**
     * Display the settings menu next to the settings button
     */
    private void showSettingsMenu() {
        settingsMenu.show(settingsButton, Side.BOTTOM, 5, 5);
    }

    // Implementation of settings actions
    private void changeFontSize() {
        // Font size implementation with multiple options via submenu
        ContextMenu fontSizeMenu = new ContextMenu();
        fontSizeMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");

        MenuItem smallFont = new MenuItem("Small");
        smallFont.setStyle("-fx-font-size: 12px; -fx-padding: 5 15;");
        smallFont.setOnAction(e -> {
            journalTextArea.setStyle("-fx-font-size: 12px; -fx-background-color: #f5f9f5; -fx-background-radius: 10; -fx-padding: 10;");
        });

        MenuItem mediumFont = new MenuItem("Medium");
        mediumFont.setStyle("-fx-font-size: 14px; -fx-padding: 5 15;");
        mediumFont.setOnAction(e -> {
            journalTextArea.setStyle("-fx-font-size: 14px; -fx-background-color: #f5f9f5; -fx-background-radius: 10; -fx-padding: 10;");
        });

        MenuItem largeFont = new MenuItem("Large");
        largeFont.setStyle("-fx-font-size: 16px; -fx-padding: 5 15;");
        largeFont.setOnAction(e -> {
            journalTextArea.setStyle("-fx-font-size: 16px; -fx-background-color: #f5f9f5; -fx-background-radius: 10; -fx-padding: 10;");
        });

        fontSizeMenu.getItems().addAll(smallFont, mediumFont, largeFont);
        fontSizeMenu.show(settingsButton, Side.RIGHT, 180, 0);
    }

    private void changeTheme() {
        // Theme selection submenu
        ContextMenu themeMenu = new ContextMenu();
        themeMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");

        MenuItem greenTheme = new MenuItem("Mint Green");
        greenTheme.setStyle("-fx-padding: 5 15; -fx-text-fill: #2e7d32;");
        greenTheme.setOnAction(e -> {
            BorderPane rootNode = (BorderPane) settingsButton.getScene().getRoot();
            rootNode.setStyle("-fx-background-color: linear-gradient(to bottom, #e6f7e9, #ffffff);");

            // Update top navigation bar color
            VBox topContainer = (VBox) rootNode.getTop();
            if (topContainer != null && topContainer.getChildren().size() > 0) {
                Node navBar = topContainer.getChildren().get(0);
                navBar.setStyle("-fx-background-color: linear-gradient(to right, #a8e6cf, #dcedc1); -fx-padding: 15 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            }
        });

        MenuItem blueTheme = new MenuItem("Sky Blue");
        blueTheme.setStyle("-fx-padding: 5 15; -fx-text-fill: #1976d2;");
        blueTheme.setOnAction(e -> {
            BorderPane rootNode = (BorderPane) settingsButton.getScene().getRoot();
            rootNode.setStyle("-fx-background-color: linear-gradient(to bottom, #e7f7fe, #ffffff);");

            // Update top navigation bar color
            VBox topContainer = (VBox) rootNode.getTop();
            if (topContainer != null && topContainer.getChildren().size() > 0) {
                Node navBar = topContainer.getChildren().get(0);
                navBar.setStyle("-fx-background-color: linear-gradient(to right, #bbdefb, #e3f2fd); -fx-padding: 15 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            }
        });

        MenuItem purpleTheme = new MenuItem("Lavender");
        purpleTheme.setStyle("-fx-padding: 5 15; -fx-text-fill: #7b1fa2;");
        purpleTheme.setOnAction(e -> {
            BorderPane rootNode = (BorderPane) settingsButton.getScene().getRoot();
            rootNode.setStyle("-fx-background-color: linear-gradient(to bottom, #f3e5f5, #ffffff);");

            // Update top navigation bar color
            VBox topContainer = (VBox) rootNode.getTop();
            if (topContainer != null && topContainer.getChildren().size() > 0) {
                Node navBar = topContainer.getChildren().get(0);
                navBar.setStyle("-fx-background-color: linear-gradient(to right, #e1bee7, #f3e5f5); -fx-padding: 15 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            }
        });

        themeMenu.getItems().addAll(greenTheme, blueTheme, purpleTheme);
        themeMenu.show(settingsButton, Side.RIGHT, 180, 0);
    }

    /**
     * Exports the current journal entry to a text file.
     * First attempts to save to the desktop, with fallbacks if that fails.
     */
    @FXML
    private void exportJournal() {
        try {
            // First determine if Desktop directory exists and is accessible
            String desktopPath = null;

            try {
                desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
                File desktopDir = new File(desktopPath);

                if (!desktopDir.exists() || !desktopDir.isDirectory()) {
                    // Fall back to user home directory if Desktop is not available
                    desktopPath = System.getProperty("user.home");
                    desktopDir = new File(desktopPath);
                }

                // Ensure the directory is writable
                if (!desktopDir.canWrite()) {
                    throw new IOException("Cannot write to " + desktopPath + ". Please check permissions.");
                }
            } catch (Exception e) {
                // If all attempts fail, use user.home as a last resort
                desktopPath = System.getProperty("user.home");
                File homeDir = new File(desktopPath);
                if (!homeDir.exists() || !homeDir.canWrite()) {
                    throw new IOException("Could not determine a valid save location: " + e.getMessage());
                }
            }

            // Create a unique filename with timestamp
            String fileName = "MindGarden_Journal_Export_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            File exportFile = new File(desktopPath, fileName);

            // Ensure parent directory exists
            File parentDir = exportFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
                }
            }

            // Write the file
            try (FileWriter writer = new FileWriter(exportFile)) {
                // Write title if available
                if (titleField != null && titleField.getText() != null && !titleField.getText().isEmpty()) {
                    writer.write("TITLE: " + titleField.getText() + "\n\n");
                }

                // Write mood if selected
                if (moodSelector != null && moodSelector.getValue() != null) {
                    writer.write("MOOD: " + moodSelector.getValue() + "\n\n");
                }

                // Write journal content
                writer.write("ENTRY CONTENT:\n");
                writer.write(journalTextArea.getText());

                writer.write("\n\nExported from MindGarden on " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Journal Exported");
            alert.setHeaderText(null);
            alert.setContentText("Your journal entry has been exported to:\n" + exportFile.getAbsolutePath());

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

        } catch (IOException e) {
            // If export fails, show error with additional details and offer alternative location
            showExportErrorDialog(e);

            // Ask user if they want to try an alternative location
            tryAlternativeLocation();
        }
    }

    /**
     * Shows an error dialog with detailed information about the export failure.
     *
     * @param error The exception that caused the export to fail
     */
    private void showExportErrorDialog(IOException error) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Export Failed");
        errorAlert.setHeaderText("Could not export your journal entry");

        // Create a more detailed error message
        String errorDetails = "Error: " + error.getMessage() + "\n\n" +
                "Technical details:\n" +
                "- User home directory: " + System.getProperty("user.home") + "\n" +
                "- Operating system: " + System.getProperty("os.name") + "\n" +
                "- Java version: " + System.getProperty("java.version");

        errorAlert.setContentText(errorDetails);

        // Log the error for debugging
        System.err.println("Journal export failed: " + error.getMessage());
        error.printStackTrace();

        errorAlert.showAndWait();
    }

    /**
     * Shows a confirmation dialog asking if the user wants to try an alternative location.
     * If confirmed, tries to export to Documents folder.
     */
    private void tryAlternativeLocation() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Try Alternative Export");
        confirmDialog.setHeaderText("Would you like to try saving to a different location?");
        confirmDialog.setContentText("Press OK to try saving to your Documents folder instead.");

        ButtonType result = confirmDialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            exportToDocuments();
        }
    }

    /**
     * Attempts to export the journal to the Documents folder as a fallback.
     */
    private void exportToDocuments() {
        try {
            // Try to find Documents folder or another alternative location
            String docsPath = System.getProperty("user.home") + File.separator + "Documents";
            File docsDir = new File(docsPath);

            // If Documents doesn't exist, try My Documents (Windows) or just user home
            if (!docsDir.exists() || !docsDir.isDirectory()) {
                docsPath = System.getProperty("user.home") + File.separator + "My Documents";
                docsDir = new File(docsPath);

                if (!docsDir.exists() || !docsDir.isDirectory()) {
                    // Final fallback to user home
                    docsPath = System.getProperty("user.home");
                    docsDir = new File(docsPath);
                }
            }

            // Create a unique filename with timestamp
            String fileName = "MindGarden_Journal_Export_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            File exportFile = new File(docsPath, fileName);

            // Write the file
            try (FileWriter writer = new FileWriter(exportFile)) {
                // Write title if available
                if (titleField != null && titleField.getText() != null && !titleField.getText().isEmpty()) {
                    writer.write("TITLE: " + titleField.getText() + "\n\n");
                }

                // Write mood if selected
                if (moodSelector != null && moodSelector.getValue() != null) {
                    writer.write("MOOD: " + moodSelector.getValue() + "\n\n");
                }

                // Write journal content
                writer.write("ENTRY CONTENT:\n");
                writer.write(journalTextArea.getText());

                writer.write("\n\nExported from MindGarden on " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Journal Exported");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Your journal entry has been exported to:\n" + exportFile.getAbsolutePath());
            successAlert.showAndWait();

        } catch (IOException e) {
            // If this fallback also fails, show final error message
            Alert finalErrorAlert = new Alert(Alert.AlertType.ERROR);
            finalErrorAlert.setTitle("Export Failed");
            finalErrorAlert.setHeaderText("Could not export your journal");
            finalErrorAlert.setContentText("Export failed with error: " + e.getMessage() +
                    "\n\nPlease check your system permissions and try again later.");
            finalErrorAlert.showAndWait();
        }
    }

    private void openPreferences() {
        // Create preferences dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.setHeaderText("Journal Settings");

        // Create content for preferences
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f9f5;");

        // Add preference options
        CheckBox autoSaveCheckBox = new CheckBox("Auto-save entries");
        autoSaveCheckBox.setSelected(true);

        CheckBox reminderCheckBox = new CheckBox("Daily journal reminders");
        reminderCheckBox.setSelected(true);

        Label privacyLabel = new Label("Privacy:");
        ComboBox<String> privacyComboBox = new ComboBox<>();
        privacyComboBox.getItems().addAll("Private", "Share with therapist", "Public");
        privacyComboBox.setValue("Private");

        // Font settings
        Label fontLabel = new Label("Default Font:");
        ComboBox<String> fontComboBox = new ComboBox<>();
        fontComboBox.getItems().addAll("System Default", "Arial", "Verdana", "Times New Roman");
        fontComboBox.setValue("System Default");

        content.getChildren().addAll(
                autoSaveCheckBox,
                reminderCheckBox,
                privacyLabel,
                privacyComboBox,
                fontLabel,
                fontComboBox
        );

        // Create button types and buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Style the buttons to match app design
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");

        dialog.getDialogPane().setContent(content);

        // Show the dialog
        dialog.showAndWait();
    }

    /**
     * Updates the date label with the current date
     */
    private void updateCurrentDate() {
        if (dateLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
            dateLabel.setText(formatter.format(now));
        }
    }
    @FXML
    private void saveEntry() {
        // Vérifier si un utilisateur est connecté
        if (MainApp.currentUser == null) {
            showSaveError("Please log in to save journal entries.");
            return;
        }

        String entryContent = journalTextArea.getText().trim();
        String title = titleField != null ? titleField.getText().trim() : "";
        String mood = moodSelector != null ? moodSelector.getValue() : "";

        if (!entryContent.isEmpty()) {
            // Passer l'ID de l'utilisateur actuel au DAO
            boolean success = journalEntryDAO.addJournalEntry(entryContent, title, mood, MainApp.currentUser.getId());

            if (success) {
                showSaveConfirmation("Entry saved successfully!");
                journalTextArea.clear();
                if (titleField != null) titleField.clear();
                if (moodSelector != null) moodSelector.setValue(null);

                // Recharger les entrées pour cet utilisateur
                loadRecentEntries();
            } else {
                showSaveError("Failed to save journal entry.");
            }
        } else {
            showSaveError("Journal entry is empty. Please write something before saving.");
        }
    }

    @FXML
    private void saveDraft() {
        // Vérifier si un utilisateur est connecté
        if (MainApp.currentUser == null) {
            showSaveError("Please log in to save drafts.");
            return;
        }

        String entryContent = journalTextArea.getText().trim();
        String title = titleField != null ? titleField.getText().trim() : "";
        String mood = moodSelector != null ? moodSelector.getValue() : "";

        // Passer l'ID de l'utilisateur actuel au DAO
        boolean success = journalEntryDAO.addJournalEntryAsDraft(entryContent, title, mood, MainApp.currentUser.getId());

        if (success) {
            showSaveConfirmation("Draft saved successfully!");

            // Recharger les entrées pour cet utilisateur
            loadRecentEntries();
        } else {
            showSaveError("Failed to save draft.");
        }
    }

    private void showSaveConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

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

    private void showSaveError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private VBox recentEntriesContainer;

    private void loadRecentEntries() {
        // Effacer les entrées existantes
        if (recentEntriesContainer != null) {
            recentEntriesContainer.getChildren().clear();

            if (MainApp.currentUser == null) {
                // Si aucun utilisateur n'est connecté, afficher un message
                Label noUserLabel = new Label("Please log in to view your journal entries.");
                noUserLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-style: italic;");
                recentEntriesContainer.getChildren().add(noUserLabel);
                return;
            }

            // Récupérer les 3 dernières entrées POUR L'UTILISATEUR ACTUEL
            List<JournalEntry> recentEntries = journalEntryDAO.getRecentEntriesForUser(3, MainApp.currentUser.getId());

            if (recentEntries.isEmpty()) {
                Label noEntriesLabel = new Label("You haven't written any journal entries yet. Start writing today!");
                noEntriesLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-style: italic;");
                recentEntriesContainer.getChildren().add(noEntriesLabel);
            } else {
                // Ajouter chaque entrée récente à l'interface
                for (JournalEntry entry : recentEntries) {
                    HBox entryCard = createEntryCard(entry);
                    recentEntriesContainer.getChildren().add(entryCard);
                }
            }
        }
    }

    // Méthode pour créer une carte d'entrée de journal
    private HBox createEntryCard(JournalEntry entry) {
        // Créer une carte d'entrée de journal similaire à celle dans le FXML
        HBox entryCard = new HBox();
        entryCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        entryCard.setSpacing(15);
        entryCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Colonne date et humeur
        VBox dateColumn = new VBox();
        dateColumn.setAlignment(javafx.geometry.Pos.CENTER);
        dateColumn.setMinWidth(80);

        // Formatter la date
        LocalDateTime entryDate = entry.getCreatedAt();
        String month = entryDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        String day = String.valueOf(entryDate.getDayOfMonth());

        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #5f6368;");

        Label dayLabel = new Label(day);
        dayLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Emoji représentant l'humeur
        Label moodLabel = new Label(getMoodEmoji(entry.getMood()));
        moodLabel.setStyle("-fx-font-size: 24;");

        dateColumn.getChildren().addAll(monthLabel, dayLabel, moodLabel);

        // Séparateur vertical
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        separator.setStyle("-fx-background-color: #e0e0e0;");

        // Contenu de l'entrée
        VBox contentColumn = new VBox();
        contentColumn.setSpacing(5);
        HBox.setHgrow(contentColumn, Priority.ALWAYS);

        Label titleLabel = new Label(entry.getTitle().isEmpty() ? "Untitled Entry" : entry.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #3c4043;");

        // Limiter le texte d'aperçu à 100 caractères
        String previewText = entry.getContent().length() > 100
                ? entry.getContent().substring(0, 100) + "..."
                : entry.getContent();

        Label previewLabel = new Label(previewText);
        previewLabel.setWrapText(true);
        previewLabel.setStyle("-fx-text-fill: #5f6368;");

        contentColumn.getChildren().addAll(titleLabel, previewLabel);

        // Bouton pour voir l'entrée complète
        Button viewButton = new Button("View");
        viewButton.setMinWidth(70);
        viewButton.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 20; -fx-padding: 8 20;");
        viewButton.setOnAction(e -> viewEntryDetails(entry.getId()));

        // Assembler tous les éléments
        entryCard.getChildren().addAll(dateColumn, separator, contentColumn, viewButton);

        return entryCard;
    }

    // Méthode pour obtenir l'emoji d'humeur basé sur la valeur de l'humeur
    private String getMoodEmoji(String mood) {
        if (mood == null || mood.isEmpty()) {
            return "📝"; // Emoji par défaut
        }

        if (mood.contains("Happy")) return "😊";
        if (mood.contains("Calm")) return "😌";
        if (mood.contains("Thoughtful")) return "🤔";
        if (mood.contains("Sad")) return "😢";
        if (mood.contains("Angry")) return "😤";
        if (mood.contains("Anxious")) return "😰";
        if (mood.contains("Tired")) return "😴";

        return "📝"; // Emoji par défaut
    }

    @FXML
    private void viewEntryDetails(int entryId) {
        try {
            // Vérifier si un utilisateur est connecté
            if (MainApp.currentUser == null) {
                showErrorAlert("Authentication Error", "Please log in to view entry details.");
                return;
            }

            // Obtenir une référence à la scène actuelle
            Stage primaryStage = (Stage) journalTextArea.getScene().getWindow();

            // Changer vers la vue EntryDetailView.fxml
            MainApp.changeScene("EntryDetailView.fxml");

            // Récupérer le UserData de la scène qui contient le loader
            FXMLLoader loader = (FXMLLoader) primaryStage.getScene().getUserData();
            EntryDetailViewController controller = (EntryDetailViewController) loader.getController();

            // Définir l'ID de l'entrée à visualiser
            controller.setEntryId(entryId,currentUserId);

            // Pas besoin de passer l'ID utilisateur car EntryDetailViewController
            // peut accéder à MainApp.currentUser directement
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open entry details.");
        }
    }

    // Méthode pour voir toutes les entrées
    @FXML
    private void viewAllEntries() {
        try {
            MainApp.changeScene("AllEntriesView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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