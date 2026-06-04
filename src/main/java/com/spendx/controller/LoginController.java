package com.spendx.controller;

import com.spendx.dao.UserDAO;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                errorLabel.setText("Invalid username or password.");
                return;
            }
            SessionManager.setCurrentUser(user);
            SceneManager.switchTo("/com/spendx/view/dashboard.fxml");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToSignup() throws Exception {
        SceneManager.switchTo("/com/spendx/view/signup.fxml");
    }
}
