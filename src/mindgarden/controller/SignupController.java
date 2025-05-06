package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import mindgarden.MainApp;
import mindgarden.db.UserDAO;
import mindgarden.model.User;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleSignup() throws Exception {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        User user = new User(0, username, email, password);
        if (userDAO.register(user)) {
            MainApp.currentUser = user;
            MainApp.changeScene("HomeView.fxml");
        } else {
            messageLabel.setText("Signup failed.");
        }
    }

    @FXML
    private void goToLogin() throws Exception {
        MainApp.changeScene("login.fxml");
    }
}
