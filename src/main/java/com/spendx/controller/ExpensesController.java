package com.spendx.controller;

import com.spendx.dao.ExpenseDAO;
import com.spendx.dao.FriendDAO;
import com.spendx.dao.NotificationDAO;
import com.spendx.model.Expense;
import com.spendx.model.ExpenseParticipant;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExpensesController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField amountField;
    @FXML private ComboBox<Expense.Category> categoryCombo;
    @FXML private TextArea notesArea;
    @FXML private ListView<User> participantsList;
    @FXML private ListView<Expense> expensesList;
    @FXML private Label statusLabel;

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryCombo.getItems().setAll(Expense.Category.values());
        categoryCombo.setValue(Expense.Category.OTHER);
        loadFriendsAsParticipants();
        loadExpenses();
        participantsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        participantsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
        expensesList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Expense e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null :
                        "[" + e.getCategory() + "] " + e.getTitle() + " — Rs. " + e.getTotalAmount()
                        + " | Paid by: " + e.getPaidByName());
            }
        });
    }

    private void loadFriendsAsParticipants() {
        try {
            participantsList.getItems().setAll(friendDAO.getFriends(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading friends."); }
    }

    private void loadExpenses() {
        try {
            expensesList.getItems().setAll(expenseDAO.getExpensesForUser(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading expenses."); }
    }

    @FXML
    private void addExpense() {
        String title = titleField.getText().trim();
        String amountText = amountField.getText().trim();
        List<User> selected = new ArrayList<>(participantsList.getSelectionModel().getSelectedItems());

        if (title.isEmpty() || amountText.isEmpty()) {
            statusLabel.setText("Title and amount are required.");
            return;
        }

        BigDecimal total;
        try {
            total = new BigDecimal(amountText);
            if (total.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter a valid positive amount.");
            return;
        }

        // Include current user in participants
        User currentUser = SessionManager.getCurrentUser();
        boolean selfIncluded = selected.stream().anyMatch(u -> u.getId() == currentUserId());
        if (!selfIncluded) {
            User self = new User();
            self.setId(currentUser.getId());
            self.setFullName(currentUser.getFullName());
            selected.add(0, self);
        }

        BigDecimal share = total.divide(BigDecimal.valueOf(selected.size()), 2, RoundingMode.HALF_UP);

        try {
            Expense expense = new Expense();
            expense.setTitle(title);
            expense.setTotalAmount(total);
            expense.setPaidBy(currentUserId());
            expense.setCategory(categoryCombo.getValue());
            expense.setNotes(notesArea.getText());

            int expenseId = expenseDAO.addExpense(expense);

            List<ExpenseParticipant> participants = new ArrayList<>();
            for (User u : selected) {
                participants.add(new ExpenseParticipant(u.getId(), share));
            }
            expenseDAO.addParticipants(expenseId, participants);

            // Notify participants (excluding self)
            for (User u : selected) {
                if (u.getId() != currentUserId()) {
                    notifDAO.create(u.getId(), currentUser.getFullName() +
                            " added an expense '" + title + "'. Your share: Rs. " + share);
                }
            }

            titleField.clear(); amountField.clear(); notesArea.clear();
            participantsList.getSelectionModel().clearSelection();
            loadExpenses();
            statusLabel.setText("Expense added. Each share: Rs. " + share);
        } catch (Exception e) {
            statusLabel.setText("Error saving expense.");
        }
    }

    @FXML private void goBack() throws Exception { SceneManager.switchTo("/com/spendx/view/dashboard.fxml"); }

    private int currentUserId() { return SessionManager.getCurrentUser().getId(); }
}
