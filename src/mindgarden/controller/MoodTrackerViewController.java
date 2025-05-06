    package mindgarden.controller;

    import javafx.animation.*;
    import javafx.application.Platform;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.scene.Node;
    import javafx.scene.chart.LineChart;
    import javafx.scene.chart.NumberAxis;
    import javafx.scene.chart.PieChart;
    import javafx.scene.chart.XYChart;
    import javafx.scene.control.*;
    import javafx.scene.effect.Glow;
    import javafx.scene.layout.*;
    import javafx.scene.media.Media;
    import javafx.scene.media.MediaPlayer;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Circle;
    import javafx.scene.shape.Line;
    import javafx.scene.text.Text;
    import javafx.util.Duration;
    import mindgarden.MainApp;
    import mindgarden.db.MoodEntryDAO;
    import mindgarden.model.MoodEntry;

    import java.io.File;
    import java.util.*;
    import java.util.stream.Collectors;

    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;


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
        @FXML private FlowPane moodFactorsPane;
        @FXML private Text mostCommonMoodEmoji;
        @FXML private Label mostCommonMoodLabel;
        @FXML private Slider moodSlider;
        @FXML private VBox moodEntrySection;
        @FXML private VBox analyticsSection;
        @FXML private ToggleButton moodEntryTab;
        @FXML private ToggleButton analyticsTab;
        @FXML private Label currentDateLabel;



        private final MoodEntryDAO moodEntryDAO = new MoodEntryDAO();
        private final ObservableList<String> moodHistoryDisplay = FXCollections.observableArrayList();
        private final Map<String, Color> moodColors = new HashMap<>();
        private final Map<String, Integer> moodValues = new HashMap<>();
        private final List<Animation> activeAnimations = new ArrayList<>();
        private final Set<String> selectedFactors = new HashSet<>();

        private MediaPlayer mediaPlayer;
        private String selectedMood = "";

        @FXML
        public void initialize() {
            updateCurrentDate();
            initializeMoodMaps();
            setupUIComponents();
            startVisualEffects();
            playAmbientMusic();
            loadMoodHistory();
            setupMoodSlider();
            setupTabSwitching();
        }

        private void updateCurrentDate() {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            currentDateLabel.setText("Today: " + today.format(formatter));
        }

        private void setupTabSwitching() {
            moodEntryTab.setOnAction(e -> {
                showMoodEntry();
                updateTabStyles();
            });
            analyticsTab.setOnAction(e -> {
                showAnalytics();
                updateTabStyles();
            });
        }

        private void updateTabStyles() {
            if (moodEntryTab.isSelected()) {
                moodEntryTab.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-text-fill: #3949ab; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10 25;");
                analyticsTab.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10 25;");
            } else {
                analyticsTab.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-text-fill: #3949ab; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10 25;");
                moodEntryTab.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10 25;");
            }
        }

        private void showMoodEntry() {
            moodEntryTab.setSelected(true);
            analyticsTab.setSelected(false);
            moodEntrySection.setVisible(true);
            moodEntrySection.setManaged(true);
            analyticsSection.setVisible(false);
            analyticsSection.setManaged(false);
        }

        private void showAnalytics() {
            analyticsTab.setSelected(true);
            moodEntryTab.setSelected(false);
            moodEntrySection.setVisible(false);
            moodEntrySection.setManaged(false);
            analyticsSection.setVisible(true);
            analyticsSection.setManaged(true);
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
            setupFactorToggleButtons();
        }

        private void setupMoodSlider() {
            moodSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int moodValue = newVal.intValue();
                selectedMood = switch(moodValue) {
                    case 1 -> "😡 Angry";
                    case 2 -> "😔 Sad";
                    case 3 -> "😐 Neutral";
                    case 4 -> "😊 Happy";
                    case 5 -> "😍 Excellent";
                    default -> "";
                };
                selectedMoodLabel.setText("Selected: " + selectedMood);
            });
        }

        private void setupFactorToggleButtons() {
            for (Node node : moodFactorsPane.getChildren()) {
                if (node instanceof ToggleButton toggleButton) {
                    toggleButton.setOnAction(event -> {
                        String factor = toggleButton.getText();
                        if (toggleButton.isSelected()) {
                            selectedFactors.add(factor);
                            toggleButton.setStyle(toggleButton.getStyle() + "; -fx-effect: dropshadow(gaussian, #9e9e9e, 5, 0.5, 0, 2);");
                        } else {
                            selectedFactors.remove(factor);
                            toggleButton.setStyle(toggleButton.getStyle().replaceAll(";?\\s*-fx-effect:.*?;", ""));
                        }
                    });
                }
            }
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
            // Filter out null/empty moods and count occurrences
            Map<String, Long> moodCounts = entries.stream()
                    .filter(entry -> entry.getMoodType() != null && !entry.getMoodType().isEmpty())
                    .collect(Collectors.groupingBy(
                            MoodEntry::getMoodType,
                            Collectors.counting()
                    ));

            // Debug output
            System.out.println("Mood counts for pie chart:");
            moodCounts.forEach((k, v) -> System.out.println(k + ": " + v));

            // Create pie chart data and apply colors
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            moodCounts.forEach((mood, count) -> {
                PieChart.Data slice = new PieChart.Data(mood, count);
                pieChartData.add(slice);
            });

            moodPieChart.setData(pieChartData);

            // Apply colors to pie chart slices after they're added to the chart
            for (PieChart.Data data : pieChartData) {
                String mood = data.getName();
                Color color = moodColors.getOrDefault(mood, Color.GRAY);

                // We need to apply the color after the chart has been laid out
                Platform.runLater(() -> {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-pie-color: " + toHexString(color) + ";");
                    }
                });
            }

            updateMostCommonMoodDisplay(pieChartData);
        }

        private void updateMostCommonMoodDisplay(ObservableList<PieChart.Data> dataList) {
            if (dataList == null || dataList.isEmpty()) {
                mostCommonMoodEmoji.setText("");
                mostCommonMoodLabel.setText("No data available");
                mostCommonMoodLabel.setStyle("-fx-font-size: 20; -fx-text-fill: gray;");
                return;
            }

            // Calculate total count
            double total = dataList.stream()
                    .mapToDouble(PieChart.Data::getPieValue)
                    .sum();

            if (total <= 0) {
                mostCommonMoodEmoji.setText("");
                mostCommonMoodLabel.setText("No valid data");
                return;
            }

            // Find mood with maximum count
            Optional<PieChart.Data> maxDataOpt = dataList.stream()
                    .max(Comparator.comparingDouble(PieChart.Data::getPieValue));

            if (maxDataOpt.isEmpty()) {
                mostCommonMoodEmoji.setText("");
                mostCommonMoodLabel.setText("Could not determine most common mood");
                return;
            }

            PieChart.Data maxData = maxDataOpt.get();
            String mood = maxData.getName();
            double count = maxData.getPieValue();
            double percentage = (count * 100.0) / total;

            // Debug output
            System.out.printf("Most common mood: %s (%.1f%%) - Count: %.0f, Total: %.0f%n",
                    mood, percentage, count, total);

            // Update display
            String[] moodParts = mood.split(" ", 2);
            String emoji = moodParts.length > 0 ? moodParts[0] : "";
            Color moodColor = moodColors.getOrDefault(mood, Color.GRAY);
            String colorHex = toHexString(moodColor);

            mostCommonMoodEmoji.setText(emoji);
            mostCommonMoodLabel.setText(String.format("%s (%.0f%%)", mood, percentage));
            mostCommonMoodLabel.setStyle("-fx-font-size: 20; -fx-text-fill: " + colorHex + ";");
        }

        private void updateMoodTrendChart(List<MoodEntry> entries) {
            moodTrendChart.getData().clear();

            NumberAxis yAxis = (NumberAxis) moodTrendChart.getYAxis();
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

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            List<MoodEntry> reversedEntries = new ArrayList<>(entries);
            Collections.reverse(reversedEntries);

            int index = 1;
            for (MoodEntry entry : reversedEntries) {
                if (entry.getMoodType() == null || entry.getMoodType().isEmpty()) continue;

                int moodValue = moodValues.getOrDefault(entry.getMoodType(), 3);
                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(index++, moodValue);

                String color = toHexString(moodColors.getOrDefault(entry.getMoodType(), Color.GRAY));
                Circle circle = new Circle(7, Color.web(color));
                dataPoint.setNode(circle);

                series.getData().add(dataPoint);
            }

            moodTrendChart.getData().add(series);
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
            if (notes.length() > 500) {
                showAlert("Note Too Long", "Please keep notes under 500 characters.");
                return;
            }

            String allNotes = notes;
            if (!selectedFactors.isEmpty()) {
                allNotes += "\nFactors: " + String.join(", ", selectedFactors);
            }

            if (moodEntryDAO.addMoodEntryWithNotes(selectedMood, allNotes)) {
                showAlert("Success", "Mood entry saved!");
                moodNotes.clear();
                selectedFactors.clear();
                resetFactorStyles();
                loadMoodHistory();
            } else {
                showAlert("Error", "Failed to save mood entry.");
            }
        }

        private void resetFactorStyles() {
            for (Node node : moodFactorsPane.getChildren()) {
                if (node instanceof ToggleButton toggleButton) {
                    toggleButton.setSelected(false);
                    toggleButton.setStyle(toggleButton.getStyle().replaceAll(";?\\s*-fx-effect:.*?;", ""));
                }
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
                            playButtonClickAnimation(moodButton);

                            int value = moodValues.getOrDefault(selectedMood, 3);
                            moodSlider.setValue(value);
                        });

                        // ➕ Animation de survol
                        moodButton.setOnMouseEntered(e -> {
                            ScaleTransition st = new ScaleTransition(Duration.millis(200), moodButton);
                            st.setToX(1.1);
                            st.setToY(1.1);
                            st.play();

                            Glow glow = new Glow(0.3);
                            moodButton.setEffect(glow);
                        });

                        moodButton.setOnMouseExited(e -> {
                            ScaleTransition st = new ScaleTransition(Duration.millis(200), moodButton);
                            st.setToX(1.0);
                            st.setToY(1.0);
                            st.play();
                            moodButton.setEffect(null);
                        });
                    });
        }

        private void playButtonClickAnimation(Button button) {
            ScaleTransition shrink = new ScaleTransition(Duration.millis(100), button);
            shrink.setToX(0.9);
            shrink.setToY(0.9);

            ScaleTransition grow = new ScaleTransition(Duration.millis(100), button);
            grow.setToX(1.0);
            grow.setToY(1.0);

            SequentialTransition bounce = new SequentialTransition(shrink, grow);
            bounce.play();
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
