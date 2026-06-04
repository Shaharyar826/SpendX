package com.spendx.controller;

import com.spendx.dao.ExpenseDAO;
import com.spendx.dao.NotificationDAO;
import com.spendx.model.Expense;
import com.spendx.model.Notification;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private ListView<Expense> recentExpensesList;
    @FXML private ListView<Notification> notificationsList;

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
        loadBalance(user.getId());
        loadRecentExpenses(user.getId());
        loadNotifications(user.getId());
    }

    private void loadBalance(int userId) {
        try {
            BigDecimal net = expenseDAO.getNetBalance(userId);
            if (net.compareTo(BigDecimal.ZERO) >= 0) {
                balanceLabel.setText("You are owed Rs. " + net);
                balanceLabel.setStyle("-fx-text-fill: #27ae60;");
            } else {
                balanceLabel.setText("You owe Rs. " + net.abs());
                balanceLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            balanceLabel.setText("Balance unavailable");
        }
    }

    private void loadRecentExpenses(int userId) {
        try {
            List<Expense> expenses = expenseDAO.getExpensesForUser(userId);
            recentExpensesList.getItems().setAll(expenses.subList(0, Math.min(5, expenses.size())));
            recentExpensesList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Expense e, boolean empty) {
                    super.updateItem(e, empty);
                    setText(empty || e == null ? null :
                            e.getTitle() + " — Rs. " + e.getTotalAmount() + " (paid by " + e.getPaidByName() + ")");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications(int userId) {
        try {
            List<Notification> notifs = notifDAO.getUnread(userId);
            notificationsList.getItems().setAll(notifs);
            notificationsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Notification n, boolean empty) {
                    super.updateItem(n, empty);
                    setText(empty || n == null ? null : n.getMessage());
                }
            });
            if (!notifs.isEmpty()) notifDAO.markAllRead(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void goToFriends()    throws Exception { SceneManager.switchTo("/com/spendx/view/friends.fxml"); }
    @FXML private void goToGroups()     throws Exception { SceneManager.switchTo("/com/spendx/view/groups.fxml"); }
    @FXML private void goToExpenses()   throws Exception { SceneManager.switchTo("/com/spendx/view/expenses.fxml"); }
    @FXML private void goToReports()    throws Exception { SceneManager.switchTo("/com/spendx/view/reports.fxml"); }
    @FXML private void handleLogout()   throws Exception {
        SessionManager.clear();
        SceneManager.switchTo("/com/spendx/view/login.fxml");
    }
}
