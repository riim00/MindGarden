package mindgarden.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import mindgarden.MainApp;
import mindgarden.db.GoalDAO;
import mindgarden.model.Goal;

import java.time.LocalDate;
import java.util.List;

public class GoalSettingController {

    @FXML private TextField goalTitleField;
    @FXML private ComboBox<String> goalTypeComboBox;
    @FXML private DatePicker goalStartDatePicker;
    @FXML private DatePicker goalDeadlinePicker;
    @FXML private TextArea goalDescriptionArea;
    @FXML private CheckBox reminderCheck;
    @FXML private CheckBox trackProgressCheck;
    @FXML private CheckBox notifyCompletionCheck;

    @FXML private TableView<Goal> goalTableView;
    @FXML private TableColumn<Goal, String> titleColumn;
    @FXML private TableColumn<Goal, String> typeColumn;
    @FXML private TableColumn<Goal, LocalDate> deadlineColumn;
    @FXML private TableColumn<Goal, LocalDate> startDateColumn;
    @FXML private TableColumn<Goal, String> descriptionColumn;
    @FXML private TableColumn<Goal, Void> actionsColumn;

    private int goalIdToUpdate = -1;

    @FXML
    public void initialize() {
        goalTypeComboBox.getItems().addAll("Meditation", "Journaling", "Mood Tracking", "Exercise", "Sleep");
        setupTable();
        loadGoals();
    }

    @FXML
    private void onBack() {
        try {
            MainApp.changeScene("HomeView.fxml");
        } catch (Exception e) {
            System.err.println("Error changing scene: " + e.getMessage());
        }
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox hbox = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
                deleteBtn.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");

                editBtn.setOnAction(e -> {
                    Goal selected = getTableView().getItems().get(getIndex());
                    populateForm(selected);
                });

                deleteBtn.setOnAction(e -> {
                    Goal selected = getTableView().getItems().get(getIndex());
                    GoalDAO.deleteGoal(selected.getId());
                    loadGoals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void loadGoals() {
        if (MainApp.currentUser == null) return;

        List<Goal> goals = GoalDAO.getGoalsByUser(MainApp.currentUser.getId());
        ObservableList<Goal> data = FXCollections.observableArrayList(goals);
        goalTableView.setItems(data);
    }

    @FXML
    private void onSaveGoal() {
        String title = goalTitleField.getText();
        String type = goalTypeComboBox.getValue();
        LocalDate startDate = goalStartDatePicker.getValue();
        LocalDate deadline = goalDeadlinePicker.getValue();
        String description = goalDescriptionArea.getText();
        boolean wantsReminder = reminderCheck.isSelected();
        boolean wantsTracking = trackProgressCheck.isSelected();
        boolean notifyOnComplete = notifyCompletionCheck.isSelected();

        if (title == null || title.isEmpty() || type == null || startDate == null || deadline == null) {
            showAlert("Please fill in all required fields.", Alert.AlertType.WARNING);
            return;
        }

        if (deadline.isBefore(startDate)) {
            showAlert("Deadline must be after start date.", Alert.AlertType.WARNING);
            return;
        }

        Goal goal = new Goal(title, type, description, startDate, deadline, wantsReminder, wantsTracking, notifyOnComplete);
        goal.setUserId(MainApp.currentUser.getId());

        if (goalIdToUpdate != -1) {
            goal.setId(goalIdToUpdate);
            GoalDAO.updateGoal(goal);
            goalIdToUpdate = -1;
            showAlert("Goal updated successfully!", Alert.AlertType.INFORMATION);
        } else {
            GoalDAO.saveGoal(goal);
            showAlert("Goal saved successfully!", Alert.AlertType.INFORMATION);
        }

        loadGoals();
        clearForm();
    }

    private void populateForm(Goal goal) {
        goalTitleField.setText(goal.getTitle());
        goalTypeComboBox.setValue(goal.getType());
        goalStartDatePicker.setValue(goal.getStartDate());
        goalDeadlinePicker.setValue(goal.getDeadline());
        goalDescriptionArea.setText(goal.getDescription());
        reminderCheck.setSelected(goal.isReminder());
        trackProgressCheck.setSelected(goal.isTrackProgress());
        notifyCompletionCheck.setSelected(goal.isNotifyOnCompletion());
        goalIdToUpdate = goal.getId();
    }

    private void clearForm() {
        goalTitleField.clear();
        goalTypeComboBox.getSelectionModel().clearSelection();
        goalStartDatePicker.setValue(null);
        goalDeadlinePicker.setValue(null);
        goalDescriptionArea.clear();
        reminderCheck.setSelected(false);
        trackProgressCheck.setSelected(false);
        notifyCompletionCheck.setSelected(false);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Goal Manager");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
