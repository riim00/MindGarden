package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import mindgarden.MainApp;
import mindgarden.db.GoalDAO;
import mindgarden.db.TipDAO;
import mindgarden.model.Goal;
import mindgarden.model.Tip;

import java.time.LocalDateTime;
import java.util.List;

import javafx.animation.ScaleTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.animation.ScaleTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.layout.Pane;

public class HomeViewController {





    @FXML
    private Label greetingLabel;

    @FXML
    private PieChart moodMiniChart;

    @FXML
    private BarChart<String, Number> weeklyProgressChart;

    @FXML private Label goal1Label;
    @FXML private Label goal2Label;
    @FXML private Label tipContentLabel;


    public void loadWeeklyGoals() {
        int userId = MainApp.currentUser.getId(); // supposé que MainApp.currentUser existe
        List<Goal> latestGoals = GoalDAO.getLatestGoalsByUser(userId, 2);

        if (latestGoals.size() > 0) {
            goal1Label.setText(latestGoals.get(0).getTitle());
        }
        if (latestGoals.size() > 1) {
            goal2Label.setText(latestGoals.get(1).getTitle());
        }
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

    private List<Tip> tips;
    private int currentTipIndex = 0;

    @FXML
    private void getNextTip() {
        if (tips != null && !tips.isEmpty()) {
            currentTipIndex = (currentTipIndex + 1) % tips.size();
            tipContentLabel.setText(tips.get(currentTipIndex).getContent());
        }
    }


    @FXML
    private void openGoalSetting() {
        try {
            MainApp.changeScene("GoalSettingView.fxml"); // ou une popup/modal si tu préfères
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProfile() {
        try {
            MainApp.changeScene("profile.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private StackPane profileIcon;
    @FXML private Text profileInitials;
    @FXML private Circle profileCircle;
    @FXML private Button setGoalButton;
    @FXML private Button SessionBT;
    @FXML private Button JournalBT;
    @FXML private Button MoodBT;
    @FXML private Button TipBT;
    @FXML private Button Settings;








    /**
     * Initialize method, called after FXML fields are populated
     */
    @FXML
    public void initialize() {
        setupGreeting();
        setupMoodChart();
        setupWeeklyProgressChart();
        updateUserInitials();
        addProfileTooltip();
        loadWeeklyGoals();
        tips = TipDAO.getAllTips();
        if (!tips.isEmpty()) {
            tipContentLabel.setText(tips.get(currentTipIndex).getContent());
        }

        applyHoverAnimation(setGoalButton);
        applyHoverAnimation(SessionBT);
        applyHoverAnimation(MoodBT);
        applyHoverAnimation(JournalBT);
        applyHoverAnimation(TipBT);
        applyHoverAnimation(Settings);

    }

    private void applyHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            button.setEffect(new DropShadow(15, Color.web("#b39ddb")));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.setOnMouseExited(e -> {
            button.setEffect(new DropShadow(5, Color.web("#d1c4e9")));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }




    private void updateUserInitials() {
        if (MainApp.currentUser != null) {
            String[] names = MainApp.currentUser.getUsername().split(" ");
            String initials = names.length >= 2
                    ? ("" + names[0].charAt(0) + names[1].charAt(0)).toUpperCase()
                    : MainApp.currentUser.getUsername().substring(0, 1).toUpperCase();
            profileInitials.setText(initials);
        } else {
            profileInitials.setText("??");
        }
    }

    private void addProfileTooltip() {
        Tooltip tooltip = new Tooltip("View Profile");
        Tooltip.install(profileIcon, tooltip);
    }

    @FXML
    private void onHoverProfile() {
        profileCircle.setScaleX(1.1);
        profileCircle.setScaleY(1.1);
    }

    @FXML
    private void onExitProfile() {
        profileCircle.setScaleX(1.0);
        profileCircle.setScaleY(1.0);
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
            String name = (MainApp.currentUser != null) ? MainApp.currentUser.getUsername() : "Guest";
            greetingLabel.setText(greeting + ", " + name);
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