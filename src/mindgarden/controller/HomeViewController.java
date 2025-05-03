package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mindgarden.MainApp;

import java.time.LocalDateTime;

public class HomeViewController {

    @FXML
    private Label greetingLabel;

    @FXML
    private PieChart moodMiniChart;

    @FXML
    private BarChart<String, Number> weeklyProgressChart;

    @FXML
    private CheckBox meditationGoalCheck;

    @FXML
    private CheckBox journalGoalCheck;

    @FXML
    private void openSettings() {
        // À personnaliser : pour l’instant, on peut juste afficher un log
        System.out.println("Settings button clicked");
    }

    @FXML
    private void openHelp() {
        System.out.println("Help button clicked");
    }

    @FXML
    private void openAbout() {
        System.out.println("About clicked");
    }

    @FXML
    private void openPrivacy() {
        System.out.println("Privacy clicked");
    }

    @FXML
    private void openContact() {
        System.out.println("Contact clicked");
    }

    @FXML
    private void getNextTip() {
        System.out.println("Next tip requested");
    }

    @FXML
    private void openGoalSetting() {
        System.out.println("Opening goal setting");
    }

    /**
     * Initialize method, called after FXML fields are populated
     */
    @FXML
    public void initialize() {
        setupGreeting();
        setupMoodChart();
        setupWeeklyProgressChart();
    }

    /**
     * Sets up time-based greeting
     */
    private void setupGreeting() {
        // Only set greeting if the label exists in the FXML
        if (greetingLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            String greeting;

            if (hour < 12) {
                greeting = "Good morning";
            } else if (hour < 18) {
                greeting = "Good afternoon";
            } else {
                greeting = "Good evening";
            }

            // Assuming a user name could be loaded from user preferences
            // For demo purposes, we'll use a hardcoded name
            greetingLabel.setText(greeting + ", Jamie");
        }
    }

    /**
     * Sets up the mood distribution mini chart
     */
    private void setupMoodChart() {
        if (moodMiniChart != null) {
            // Sample data - in a real app, this would come from a database
            PieChart.Data happy = new PieChart.Data("Happy", 42);
            PieChart.Data calm = new PieChart.Data("Calm", 30);
            PieChart.Data neutral = new PieChart.Data("Neutral", 15);
            PieChart.Data stressed = new PieChart.Data("Stressed", 13);

            moodMiniChart.getData().addAll(happy, calm, neutral, stressed);

            // Apply custom colors
            applyCustomChartColors();
        }
    }

    /**
     * Applies custom colors to the mood chart
     */
    private void applyCustomChartColors() {
        String[] pieColors = {"#64B5F6", "#81C784", "#FFD54F", "#E57373"};
        int i = 0;

        for (PieChart.Data data : moodMiniChart.getData()) {
            String color = pieColors[i % pieColors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    /**
     * Sets up the weekly progress chart
     */
    private void setupWeeklyProgressChart() {
        if (weeklyProgressChart != null) {
            // Series for mood tracking
            XYChart.Series<String, Number> moodSeries = new XYChart.Series<>();
            moodSeries.setName("Mood");

            // Series for journal entries
            XYChart.Series<String, Number> journalSeries = new XYChart.Series<>();
            journalSeries.setName("Journal");

            // Series for meditation
            XYChart.Series<String, Number> meditationSeries = new XYChart.Series<>();
            meditationSeries.setName("Meditation");

            // Sample data - would come from database in real app
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            for (String day : days) {
                // Random data for demo purposes
                double moodVal = Math.random() < 0.7 ? 1 : 0;
                double journalVal = Math.random() < 0.6 ? 1 : 0;
                double meditationVal = Math.random() < 0.5 ? 1 : 0;

                moodSeries.getData().add(new XYChart.Data<>(day, moodVal));
                journalSeries.getData().add(new XYChart.Data<>(day, journalVal));
                meditationSeries.getData().add(new XYChart.Data<>(day, meditationVal));
            }

            weeklyProgressChart.getData().addAll(moodSeries, journalSeries, meditationSeries);

            // Apply custom colors to each series
            weeklyProgressChart.lookup(".series0").setStyle("-fx-bar-fill: #1976d2;");
            weeklyProgressChart.lookup(".series1").setStyle("-fx-bar-fill: #43a047;");
            weeklyProgressChart.lookup(".series2").setStyle("-fx-bar-fill: #ec407a;");
        }
    }

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
            MainApp.changeScene("MeditationView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}