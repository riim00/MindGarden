package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


import java.net.URL;
import java.util.Map;

public class MeditationViewController {

    private MediaPlayer mediaPlayer;
    @FXML private Button threeMinButton;
    @FXML private Button fiveMinButton;
    @FXML private Button tenMinButton;
    @FXML private Button fifteenMinButton;
    @FXML private Button stopButton;
    @FXML private Circle progressCircle;
    @FXML private VBox moreMeditationsBox;
    @FXML private Button exploreButton;
    private String currentMeditation = null;
    private boolean isPlaying = false;
    private Thread autoStopThread = null;


    private final Map<String, String> meditationSoundMap = Map.of(
            "Ocean Waves", "/audio/sea-waves.mp3",
            "Night Rest", "/audio/night-rest.mp3",
            "Inner Fire", "/audio/Fire.mp3",
            "Mountain Strength", "/audio/Wind.mp3",
            "Mind Focus", "/audio/birds.mp3",
            "New Beginnings", "/audio/new-beginnings.mp3"
    );


    @FXML
    private ComboBox<String> backgroundSoundCombo;

    private final Map<String, String> soundMap = Map.of(
            "Forest Serenity", "/audio/spring-forest-nature.mp3",
            "️City Ambience", "/audio/city-ambience.mp3",
            "Sea Waves", "/audio/sea-waves.mp3",
            "️Rain Sound", "/audio/rain-sound.mp3",
            "Birds Chirping", "/audio/birds.mp3"
    );

    @FXML
    private MediaView mediaView;

    @FXML
    private Label timerLabel;

    private Timeline sessionTimeline;
    private int sessionSecondsRemaining = 5 * 60; // Valeur par défaut : 5 min
    private boolean isPaused = false;
    private double totalDurationInSeconds;



    private void playMeditationAudio(String title) {
        // Si on clique sur le même titre → pause/reprise
        if (currentMeditation != null && currentMeditation.equals(title)) {
            if (mediaPlayer != null) {
                if (isPlaying) {
                    mediaPlayer.pause();
                    isPlaying = false;
                    System.out.println("⏸ Paused: " + title);
                } else {
                    mediaPlayer.play();
                    isPlaying = true;
                    System.out.println("▶ Resumed: " + title);
                }
            }
            return;
        }

        // Sinon → nouvelle méditation
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (autoStopThread != null && autoStopThread.isAlive()) {
            autoStopThread.interrupt();
        }

        String path = meditationSoundMap.get(title);
        if (path != null) {
            URL audioURL = getClass().getResource(path);
            if (audioURL != null) {
                Media media = new Media(audioURL.toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();

                currentMeditation = title;
                isPlaying = true;

                System.out.println("🎧 Playing: " + title);

                // Auto-stop après 5 minutes
                autoStopThread = new Thread(() -> {
                    try {
                        Thread.sleep(300000); // 5 minutes
                        javafx.application.Platform.runLater(() -> {
                            if (mediaPlayer != null && title.equals(currentMeditation)) {
                                mediaPlayer.stop();
                                currentMeditation = null;
                                isPlaying = false;
                                System.out.println("⏹ Auto-stopped after 5 minutes: " + title);
                            }
                        });
                    } catch (InterruptedException e) {
                        System.out.println("🛑 Auto-stop cancelled for: " + title);
                    }
                });
                autoStopThread.start();
            } else {
                System.err.println("❌ Audio not found: " + title);
            }
        }
    }


    @FXML
    private void playOceanWaves() {
        playMeditationAudio("Ocean Waves");
    }

    @FXML
    private void playNightRest() {
        playMeditationAudio("Night Rest");
    }

    @FXML
    private void playInnerFire() {
        playMeditationAudio("Inner Fire");
    }

    @FXML
    private void playMountainStrength() {
        playMeditationAudio("Mountain Strength");
    }

    @FXML
    private void playMindFocus() {
        playMeditationAudio("Mind Focus");
    }

    @FXML
    private void playNewBeginnings() {
        playMeditationAudio("New Beginnings");
    }


    private void setupHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            button.setCursor(Cursor.HAND); // Curseur main
            if (!button.getStyle().contains("#43a047")) { // si non sélectionné
                button.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 20; -fx-padding: 8 20;");
            }
        });

        button.setOnMouseExited(e -> {
            button.setCursor(Cursor.DEFAULT); // Curseur normal
            if (!button.getStyle().contains("#43a047")) {
                button.setStyle("-fx-background-color: white; -fx-text-fill: #558b2f; -fx-background-radius: 20; -fx-padding: 8 20;");
            }
        });
    }


    @FXML
    private void initialize() {
        setupHoverEffect(threeMinButton);
        setupHoverEffect(fiveMinButton);
        setupHoverEffect(tenMinButton);
        setupHoverEffect(fifteenMinButton);
        setupHoverEffect(pauseResumeButton);
        setupHoverEffect(stopButton);

        backgroundSoundCombo.getItems().addAll(soundMap.keySet());
        backgroundSoundCombo.getSelectionModel().selectFirst(); // valeur par défaut

    }
    private boolean isExpanded = false;

    @FXML
    private void showMoreMeditations() {
        isExpanded = !isExpanded;
        moreMeditationsBox.setVisible(isExpanded);
        moreMeditationsBox.setManaged(isExpanded);
        exploreButton.setText(isExpanded ? "↑ Show Less" : "↓ Explore More");
    }



    @FXML
    private void goBack() {
        try {
            mindgarden.MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setTimerTo3() {
        sessionSecondsRemaining = 3 * 60;
        updateTimerDisplay();
        updateDurationButtonStyles(threeMinButton);
    }

    @FXML
    private void setTimerTo5() {
        sessionSecondsRemaining = 5 * 60;
        updateTimerDisplay();
        updateDurationButtonStyles(fiveMinButton);
    }

    @FXML
    private void setTimerTo10() {
        sessionSecondsRemaining = 10 * 60;
        updateTimerDisplay();
        updateDurationButtonStyles(tenMinButton);
    }

    @FXML
    private void setTimerTo15() {
        sessionSecondsRemaining = 15 * 60;
        updateTimerDisplay();
        updateDurationButtonStyles(fifteenMinButton);
    }


    private void updateTimerDisplay() {
        int minutes = sessionSecondsRemaining / 60;
        int seconds = sessionSecondsRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // Lancement du timer
    @FXML
    private void startMeditationTimer() {
        // Stop previous timer/audio
        if (sessionTimeline != null) sessionTimeline.stop();
        if (mediaPlayer != null) mediaPlayer.stop();

        isPaused = false;
        totalDurationInSeconds = sessionSecondsRemaining;

        // Choix du son
        String selectedSound = backgroundSoundCombo.getValue();
        String audioPath = soundMap.get(selectedSound);
        if (audioPath != null) {
            URL audioURL = getClass().getResource(audioPath);
            if (audioURL != null) {
                Media media = new Media(audioURL.toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // boucle infinie
                mediaPlayer.play();
            }
        }

        sessionTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (!isPaused) {
                        sessionSecondsRemaining--;
                        updateTimerDisplay();

                        if (sessionSecondsRemaining <= 0) {
                            sessionTimeline.stop();
                            timerLabel.setText("Session done");
                            if (mediaPlayer != null) mediaPlayer.stop();

                            playEndSound();
                            showEndNotification();
                        }
                    }
                })
        );

        sessionTimeline.setCycleCount(Timeline.INDEFINITE);
        sessionTimeline.play();
    }



    @FXML
    private void handleBeginSession() {
        // Durée de la session en secondes (ex : 10 minutes = 600s)
        sessionSecondsRemaining = 10 * 60;

        if (sessionTimeline != null) {
            sessionTimeline.stop();
        }

        sessionTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    int minutes = sessionSecondsRemaining / 60;
                    int seconds = sessionSecondsRemaining % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

                    sessionSecondsRemaining--;

                    if (sessionSecondsRemaining < 0) {
                        sessionTimeline.stop();
                        timerLabel.setText("Session done 🧘‍♀️");
                        // Optionnel : jouer un son de fin ou afficher une alerte
                    }
                })
        );

        sessionTimeline.setCycleCount(Timeline.INDEFINITE);
        sessionTimeline.play();

        System.out.println("🧘 Session started with 10 min timer");
    }




    @FXML
    private void handlePreview() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop(); // Stop previous audio if playing
            }

            // Charger le fichier audio
            URL audioURL = getClass().getResource("/audio/spring-forest-nature.mp3");
            if (audioURL == null) {
                System.err.println("⚠️ Audio file not found!");
                return;
            }

            Media media = new Media(audioURL.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer); // Facultatif
            mediaPlayer.play();

            System.out.println("🎧 Playing preview of 'Forest Serenity'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button pauseResumeButton;

    @FXML
    private void pauseOrResumeTimer() {
        if (sessionTimeline != null) {
            isPaused = !isPaused;
            pauseResumeButton.setText(isPaused ? "▶ Resume" : "⏸ Pause");
            System.out.println(isPaused ? "⏸ Timer paused" : "▶ Timer resumed");

            if (mediaPlayer != null) {
                if (isPaused) {
                    mediaPlayer.pause(); // ✅ Pause du son
                } else {
                    mediaPlayer.play();  // ✅ Reprise du son
                }
            }
        }
    }

    @FXML
    private void stopTimer() {
        if (sessionTimeline != null) {
            sessionTimeline.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // ✅ Arrête le son de fond
        }
        sessionSecondsRemaining = 0;
        updateTimerDisplay();
        pauseResumeButton.setText("⏸ Pause/Resume");
        System.out.println("⏹ Timer stopped");
    }


    @FXML
    private void handlePausePreview() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                System.out.println("⏸️ Preview paused.");
            } else if (status == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
                System.out.println("▶️ Resuming preview.");
            }
        }
    }


    private void updateDurationButtonStyles(Button selectedButton) {
        Button[] allButtons = { threeMinButton, fiveMinButton, tenMinButton, fifteenMinButton };
        for (Button btn : allButtons) {
            if (btn == selectedButton) {
                btn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20;");
            } else {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #558b2f; -fx-background-radius: 20; -fx-padding: 8 20;");
            }
        }
    }

    private void playEndSound() {
        try {
            URL soundURL = getClass().getResource("/audio/Ding.mp3");
            if (soundURL != null) {
                Media sound = new Media(soundURL.toString());
                MediaPlayer endPlayer = new MediaPlayer(sound);
                endPlayer.play();
            } else {
                System.err.println("🔔 End sound file not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEndNotification() {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Meditation Finished!");
            alert.setHeaderText("Session Complete !");
            alert.setContentText("Take a deep breath and enjoy your calm!");
            alert.showAndWait();
        });
    }



}
