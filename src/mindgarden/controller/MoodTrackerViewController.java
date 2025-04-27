package mindgarden.controller;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import mindgarden.MainApp;
import mindgarden.db.MoodEntryDAO;
import mindgarden.model.MoodEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MoodTrackerViewController {

    private static final Random RANDOM = new Random();
    private static final int MAX_ANIMATIONS = 120;
    private static final double AMBIENT_VOLUME = 0.2;
    private static final int SPARKLE_COUNT = 30;
    private static final int SHOOTING_STAR_COUNT = 20;

    @FXML private Pane auroraPane;
    @FXML private Pane sparklesPane;
    @FXML private Pane moodAnimationPane;
    @FXML private BorderPane mainPane;
    @FXML private Label selectedMoodLabel;
    @FXML private TextArea moodNotes;
    @FXML private PieChart moodPieChart;
    @FXML private ListView<String> moodHistoryList;
    @FXML private HBox moodButtonsBox;
    @FXML private LineChart<Number, Number> moodTrendChart;

    private final MoodEntryDAO moodEntryDAO = new MoodEntryDAO();
    private final ObservableList<String> moodHistoryDisplay = FXCollections.observableArrayList();
    private final Map<String, Color> moodColors = new HashMap<>();
    private final Map<String, Integer> moodValues = new HashMap<>();
    private final List<Animation> activeAnimations = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    private String selectedMood = "";

    @FXML
    public void initialize() {
        initializeMoodMaps();
        setupUIComponents();
        startVisualEffects();
        playAmbientMusic();
        loadMoodHistory();
    }

    private void initializeMoodMaps() {
        moodColors.put("😍 Excellent", Color.LIMEGREEN);
        moodColors.put("😊 Happy", Color.DODGERBLUE);
        moodColors.put("😐 Neutral", Color.GOLD);
        moodColors.put("😔 Sad", Color.GRAY);
        moodColors.put("😡 Angry", Color.RED);

        moodValues.put("😍 Excellent", 5);
        moodValues.put("😊 Happy", 4);
        moodValues.put("😐 Neutral", 3);
        moodValues.put("😔 Sad", 2);
        moodValues.put("😡 Angry", 1);
    }

    private void setupUIComponents() {
        moodHistoryList.setItems(moodHistoryDisplay);
        selectedMoodLabel.setText("Selected: None");
        setupMoodButtonActions();
    }

    private void startVisualEffects() {
        initializeAurora();
        initializeSparkles();
        launchShootingStars(SHOOTING_STAR_COUNT);
        startBreathingGlow();
    }

    private void loadMoodHistory() {
        moodHistoryDisplay.clear();
        List<MoodEntry> entries = moodEntryDAO.getAllMoodEntries();

        entries.stream()
                .map(entry -> String.format("%s - %s (%s)",
                        entry.getTimestamp().toLocalDate(),
                        entry.getMoodType(),
                        Optional.ofNullable(entry.getNotes()).orElse("")))
                .forEach(moodHistoryDisplay::add);

        updatePieChart(entries);
        updateMoodTrendChart(entries);
    }

    private void updatePieChart(List<MoodEntry> entries) {
        Map<String, Long> moodCounts = entries.stream()
                .collect(Collectors.groupingBy(MoodEntry::getMoodType, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                moodCounts.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );

        moodPieChart.setData(pieChartData);
    }

    private void updateMoodTrendChart(List<MoodEntry> entries) {
        moodTrendChart.getData().clear();

        NumberAxis yAxis = (NumberAxis) moodTrendChart.getYAxis();
        yAxis.setLabel("");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(6);
        yAxis.setTickUnit(1);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return switch (object.intValue()) {
                    case 5 -> "😍 Excellent";
                    case 4 -> "😊 Happy";
                    case 3 -> "😐 Neutral";
                    case 2 -> "😔 Sad";
                    case 1 -> "😡 Angry";
                    default -> "";
                };
            }
        });

        NumberAxis xAxis = (NumberAxis) moodTrendChart.getXAxis();
        xAxis.setLabel("Entries (Oldest to Recent)");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(entries.size() + 1);
        xAxis.setTickUnit(1);
        xAxis.setForceZeroInRange(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        List<MoodEntry> reversedEntries = new ArrayList<>(entries);
        Collections.reverse(reversedEntries);

        int index = 1;
        for (MoodEntry entry : reversedEntries) {
            int moodValue = moodValues.getOrDefault(entry.getMoodType(), 3);
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(index++, moodValue);

            String color = toHexString(moodColors.getOrDefault(entry.getMoodType(), Color.GRAY));
            Circle circle = new Circle(7, Color.web(color));
            dataPoint.setNode(circle);

            series.getData().add(dataPoint);
        }

        moodTrendChart.getData().add(series);

        moodTrendChart.applyCss();
        Set<Node> lines = moodTrendChart.lookupAll(".chart-series-line");
        for (Node line : lines) {
            line.setStyle("-fx-stroke: transparent;");
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @FXML
    private void saveMoodEntry() {
        if (selectedMood.isEmpty()) {
            showAlert("No Mood Selected", "Please select a mood before saving.");
            return;
        }

        String notes = moodNotes.getText().trim();
        if (moodEntryDAO.addMoodEntryWithNotes(selectedMood, notes)) {
            showAlert("Success", "Mood entry saved!");
            moodNotes.clear();
            loadMoodHistory();
        } else {
            showAlert("Error", "Failed to save mood entry.");
        }
    }

    private void setupMoodButtonActions() {
        moodButtonsBox.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .forEach(moodButton -> {
                    moodButton.setOnAction(e -> {
                        selectedMood = moodButton.getText();
                        selectedMoodLabel.setText("Selected: " + selectedMood);
                        playButtonAnimation(moodButton);
                    });
                });
    }

    private void initializeAurora() {
        auroraPane.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #a18cd1, #fbc2eb);");
    }

    private void initializeSparkles() {
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            Circle sparkle = createSparkle();
            sparklesPane.getChildren().add(sparkle);
            setupSparkleAnimations(sparkle);
        }
    }

    private Circle createSparkle() {
        double width = sparklesPane.getWidth() > 0 ? sparklesPane.getWidth() : 900;
        double height = sparklesPane.getHeight() > 0 ? sparklesPane.getHeight() : 700;

        Circle sparkle = new Circle(
                RANDOM.nextDouble(1, 3),
                Color.rgb(255, 255, 255, RANDOM.nextDouble(0.5, 1.0))
        );
        sparkle.setCenterX(RANDOM.nextDouble(width));
        sparkle.setCenterY(RANDOM.nextDouble(height));
        return sparkle;
    }

    private void setupSparkleAnimations(Circle sparkle) {
        TranslateTransition floatUp = new TranslateTransition(
                Duration.seconds(20 + RANDOM.nextDouble() * 20), sparkle);
        floatUp.setByY(-200);
        floatUp.setCycleCount(Animation.INDEFINITE);
        addManagedAnimation(floatUp);
        floatUp.play();

        FadeTransition twinkle = new FadeTransition(
                Duration.seconds(2 + RANDOM.nextDouble() * 2), sparkle);
        twinkle.setFromValue(0.3);
        twinkle.setToValue(1.0);
        twinkle.setAutoReverse(true);
        twinkle.setCycleCount(Animation.INDEFINITE);
        addManagedAnimation(twinkle);
        twinkle.play();
    }

    private void launchShootingStars(int count) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(5 + RANDOM.nextDouble() * 5),
                        e -> createShootingStar()));
        timeline.setCycleCount(count);
        addManagedAnimation(timeline);
        timeline.play();
    }

    private void createShootingStar() {
        double width = sparklesPane.getWidth() > 0 ? sparklesPane.getWidth() : 900;
        double height = sparklesPane.getHeight() > 0 ? sparklesPane.getHeight() : 700;

        Line shootingStar = new Line(0, 0, 50, 0);
        shootingStar.setStroke(Color.WHITE);
        shootingStar.setStrokeWidth(2);
        shootingStar.setOpacity(0.8);

        sparklesPane.getChildren().add(shootingStar);
        shootingStar.setStartX(RANDOM.nextDouble(width));
        shootingStar.setStartY(RANDOM.nextDouble(height / 2));
        shootingStar.setEndX(shootingStar.getStartX() + 50);
        shootingStar.setEndY(shootingStar.getStartY());

        TranslateTransition move = new TranslateTransition(
                Duration.seconds(1 + RANDOM.nextDouble()), shootingStar);
        move.setByX(300);
        move.setByY(150);
        move.setOnFinished(e -> sparklesPane.getChildren().remove(shootingStar));
        addManagedAnimation(move);
        move.play();
    }

    private void startBreathingGlow() {
        ScaleTransition breathe = new ScaleTransition(Duration.seconds(6), auroraPane);
        breathe.setFromX(1.0);
        breathe.setFromY(1.0);
        breathe.setToX(1.05);
        breathe.setToY(1.05);
        breathe.setCycleCount(Animation.INDEFINITE);
        breathe.setAutoReverse(true);
        addManagedAnimation(breathe);
        breathe.play();
    }

    private void playAmbientMusic() {
        try {
            String[] possiblePaths = {
                    "src/main/resources/sounds/ambient.mp3",
                    "resources/sounds/ambient.mp3",
                    "sounds/ambient.mp3"
            };

            Optional<File> soundFile = Arrays.stream(possiblePaths)
                    .map(File::new)
                    .filter(File::exists)
                    .findFirst();

            if (soundFile.isPresent()) {
                Media sound = new Media(soundFile.get().toURI().toString());
                mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(AMBIENT_VOLUME);
                mediaPlayer.play();
            } else {
                System.out.println("Silent fallback audio active.");
            }
        } catch (Exception e) {
            System.err.println("Error playing ambient music: " + e.getMessage());
        }
    }

    private void addManagedAnimation(Animation animation) {
        activeAnimations.add(animation);
        if (activeAnimations.size() > MAX_ANIMATIONS) {
            Animation oldest = activeAnimations.remove(0);
            oldest.stop();
        }
    }

    private void playButtonAnimation(Button moodButton) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), moodButton);
        st.setByX(0.1);
        st.setByY(0.1);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            cleanupResources();
            MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            System.err.println("Error changing scene: " + e.getMessage());
        }
    }

    private void cleanupResources() {
        activeAnimations.forEach(Animation::stop);
        activeAnimations.clear();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}