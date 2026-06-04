package com.spendx.controller;

import com.spendx.dao.ExpenseDAO;
import com.spendx.dao.FriendDAO;
import com.spendx.dao.NotificationDAO;
import com.spendx.dao.SettlementDAO;
import com.spendx.model.Settlement;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SettlementController implements Initializable {

    @FXML private ListView<User> friendsList;
    @FXML private Label balanceLabel;
    @FXML private TextField amountField;
    @FXML private TextField noteField;
    @FXML private ListView<Settlement> historyList;
    @FXML private Label statusLabel;

    private final FriendDAO friendDAO = new FriendDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFriends();
        loadHistory();
        friendsList.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, selected) -> { if (selected != null) showBalance(selected); }
        );
        friendsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
        historyList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Settlement s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null :
                        s.getPayerName() + " → " + s.getPayeeName() + ": Rs. " + s.getAmount());
            }
        });
    }

    private void loadFriends() {
        try {
            friendsList.getItems().setAll(friendDAO.getFriends(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading friends."); }
    }

    private void loadHistory() {
        try {
            historyList.getItems().setAll(settlementDAO.getSettlementsForUser(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading history."); }
    }

    private void showBalance(User friend) {
        try {
            BigDecimal balance = expenseDAO.getBalanceBetween(currentUserId(), friend.getId());
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                balanceLabel.setText(friend.getFullName() + " owes you Rs. " + balance);
                balanceLabel.setStyle("-fx-text-fill: #27ae60;");
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balanceLabel.setText("You owe " + friend.getFullName() + " Rs. " + balance.abs());
                balanceLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else {
                balanceLabel.setText("All settled with " + friend.getFullName());
                balanceLabel.setStyle("-fx-text-fill: #7f8c8d;");
            }
        } catch (Exception e) { balanceLabel.setText("Error loading balance."); }
    }

    @FXML
    private void settle() {
        User friend = friendsList.getSelectionModel().getSelectedItem();
        if (friend == null) { statusLabel.setText("Select a friend."); return; }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter a valid amount.");
            return;
        }

        try {
            settlementDAO.settle(currentUserId(), friend.getId(), amount, noteField.getText().trim());
            notifDAO.create(friend.getId(), SessionManager.getCurrentUser().getFullName() +
                    " settled Rs. " + amount + " with you.");
            amountField.clear(); noteField.clear();
            loadHistory();
            showBalance(friend);
            statusLabel.setText("Settlement recorded.");
        } catch (Exception e) { statusLabel.setText("Error recording settlement."); }
    }

    @FXML private void goBack() throws Exception { SceneManager.switchTo("/com/spendx/view/dashboard.fxml"); }

    private int currentUserId() { return SessionManager.getCurrentUser().getId(); }
}
