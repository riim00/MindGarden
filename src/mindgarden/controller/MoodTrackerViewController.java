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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    private ObservableList<String> moodHistory = FXCollections.observableArrayList();
    private Map<String, Integer> moodCounts = new HashMap<>();

    @FXML
    public void initialize() {
        moodHistoryList.setItems(moodHistory);
        selectedMoodLabel.setText("Selected: None");

        // Initialize mood counts for pie chart
        moodCounts.put("😊 Happy", 0);
        moodCounts.put("😐 Neutral", 0);
        moodCounts.put("😔 Sad", 0);
        moodCounts.put("😡 Angry", 0);
        moodCounts.put("😴 Tired", 0);

        updatePieChart();
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
            String notes = moodNotes.getText();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDateTime = now.format(formatter);

            String entry = formattedDateTime + " - " + selectedMood;
            if (!notes.trim().isEmpty()) {
                entry += " - " + notes.trim();
            }

            moodHistory.add(0, entry);

            // Update mood counts for pie chart
            moodCounts.put(selectedMood, moodCounts.get(selectedMood) + 1);
            updatePieChart();

            // Reset fields
            moodNotes.clear();
            selectedMoodLabel.setText("Selected: None");
            selectedMood = "";
        }
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