package mindgarden.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import mindgarden.MainApp;
import mindgarden.db.MoodEntryDAO; // Added DAO import
import mindgarden.model.MoodEntry; // Added Model import

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List; // Added List import
import java.util.Map;

public class MoodTrackerViewController {

    @FXML
    private TextArea moodNotes;

    @FXML
    private ListView<String> moodHistoryList;

    @FXML
    private PieChart moodPieChart;

    @FXML
    private Label selectedMoodLabel;
    private String selectedMood = "";
    private ObservableList<String> moodHistoryDisplay = FXCollections.observableArrayList(); // Renamed for clarity
    private Map<String, Integer> moodCounts = new HashMap<>();
    private MoodEntryDAO moodEntryDAO; // DAO instance

    // Formatter for display
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        moodEntryDAO = new MoodEntryDAO(); // Initialize DAO
        moodHistoryList.setItems(moodHistoryDisplay);
        selectedMoodLabel.setText("Selected: None");

        // Initialize mood counts map (can be dynamic based on buttons later if needed)
        moodCounts.put("😊 Happy", 0);
        moodCounts.put("😐 Neutral", 0);
        moodCounts.put("😔 Sad", 0);
        moodCounts.put("😡 Angry", 0);
        moodCounts.put("😴 Tired", 0);

        loadMoodHistory(); // Load data from DB on initialization
    }

    @FXML
    private void goBack() {
        try {
            MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectMood(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        selectedMood = clickedButton.getText();
        selectedMoodLabel.setText("Selected: " + selectedMood);
        System.out.println("Selected mood: " + selectedMood);
    }

    @FXML
    private void saveMoodEntry() {
        if (!selectedMood.isEmpty()) {
            // Save to database
            boolean success = moodEntryDAO.addMoodEntry(selectedMood);

            if (success) {
                System.out.println("Mood entry saved successfully.");
                // Reload history from DB to show the new entry
                loadMoodHistory();

                // Reset fields
                moodNotes.clear(); // Note: moodNotes is not currently saved to DB
                selectedMoodLabel.setText("Selected: None");
                selectedMood = "";
            } else {
                System.err.println("Failed to save mood entry.");
                // Optionally show an error message to the user
            }
        } else {
             System.out.println("No mood selected to save.");
             // Optionally show a message to the user
        }
    }

    /**
     * Loads mood history from the database and updates the UI components.
     */
    private void loadMoodHistory() {
        List<MoodEntry> entries = moodEntryDAO.getAllMoodEntries();
        moodHistoryDisplay.clear();
        moodCounts.replaceAll((k, v) -> 0); // Reset counts before recounting

        for (MoodEntry entry : entries) {
            String formattedDateTime = entry.getTimestamp().format(DISPLAY_FORMATTER);
            String displayEntry = formattedDateTime + " - " + entry.getMoodType();
            moodHistoryDisplay.add(displayEntry); // Add to the display list

            // Update counts for pie chart - need to handle potential new mood types if UI changes
            String moodKey = entry.getMoodType(); // Assuming moodType matches the keys like "😊 Happy"
             if (moodCounts.containsKey(moodKey)) {
                 moodCounts.put(moodKey, moodCounts.get(moodKey) + 1);
             } else {
                 // Handle case where a mood type from DB doesn't match predefined keys
                 System.err.println("Warning: Mood type '" + moodKey + "' from DB not found in predefined map.");
                 // Optionally add it dynamically: moodCounts.put(moodKey, 1);
             }
        }

        updatePieChart(); // Update chart with new counts
    }

    private void updatePieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > 0) {
                String moodName = entry.getKey().split(" ")[1]; // Get the mood name without emoji
                pieChartData.add(new PieChart.Data(moodName, entry.getValue()));
            }
        }

        moodPieChart.setData(pieChartData);
    }
}
