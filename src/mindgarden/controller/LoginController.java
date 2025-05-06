package mindgarden.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import mindgarden.MainApp;
import mindgarden.db.UserDAO;
import mindgarden.model.User;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() throws Exception {
        String email = emailField.getText();
        String password = passwordField.getText();

        User user = userDAO.login(email, password);
        if (user != null) {
            MainApp.currentUser = user;
            MainApp.changeScene("HomeView.fxml");
        } else {
            errorLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    private void goToSignup() throws Exception {
        MainApp.changeScene("signup.fxml");
    }
}
