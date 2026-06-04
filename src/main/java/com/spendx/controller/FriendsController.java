package com.spendx.controller;

import com.spendx.dao.FriendDAO;
import com.spendx.dao.NotificationDAO;
import com.spendx.model.FriendRequest;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FriendsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ListView<User> searchResultsList;
    @FXML private ListView<User> friendsList;
    @FXML private ListView<FriendRequest> requestsList;
    @FXML private Label statusLabel;

    private final FriendDAO friendDAO = new FriendDAO();
    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFriends();
        loadRequests();
        setupCellFactories();
    }

    private void loadFriends() {
        try {
            List<User> friends = friendDAO.getFriends(currentUserId());
            friendsList.getItems().setAll(friends);
        } catch (Exception e) { statusLabel.setText("Error loading friends."); }
    }

    private void loadRequests() {
        try {
            List<FriendRequest> requests = friendDAO.getPendingRequests(currentUserId());
            requestsList.getItems().setAll(requests);
        } catch (Exception e) { statusLabel.setText("Error loading requests."); }
    }

    private void setupCellFactories() {
        friendsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
        requestsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(FriendRequest fr, boolean empty) {
                super.updateItem(fr, empty);
                setText(empty || fr == null ? null : fr.getSenderName() + " (@" + fr.getSenderUsername() + ")");
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;
        try {
            List<User> results = new com.spendx.dao.UserDAO().searchByUsername(query, currentUserId());
            searchResultsList.getItems().setAll(results);
        } catch (Exception e) { statusLabel.setText("Search failed."); }
    }

    @FXML
    private void sendRequest() {
        User selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a user first."); return; }
        try {
            if (friendDAO.areFriends(currentUserId(), selected.getId())) {
                statusLabel.setText("Already friends.");
                return;
            }
            friendDAO.sendRequest(currentUserId(), selected.getId());
            notifDAO.create(selected.getId(), SessionManager.getCurrentUser().getFullName() + " sent you a friend request.");
            statusLabel.setText("Request sent to " + selected.getUsername());
        } catch (Exception e) { statusLabel.setText("Could not send request."); }
    }

    @FXML
    private void acceptRequest() {
        FriendRequest fr = requestsList.getSelectionModel().getSelectedItem();
        if (fr == null) return;
        try {
            friendDAO.updateStatus(fr.getId(), FriendRequest.Status.ACCEPTED);
            notifDAO.create(fr.getSenderId(), SessionManager.getCurrentUser().getFullName() + " accepted your friend request.");
            loadFriends();
            loadRequests();
        } catch (Exception e) { statusLabel.setText("Error accepting request."); }
    }

    @FXML
    private void rejectRequest() {
        FriendRequest fr = requestsList.getSelectionModel().getSelectedItem();
        if (fr == null) return;
        try {
            friendDAO.updateStatus(fr.getId(), FriendRequest.Status.REJECTED);
            loadRequests();
        } catch (Exception e) { statusLabel.setText("Error rejecting request."); }
    }

    @FXML
    private void removeFriend() {
        User selected = friendsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            friendDAO.removeFriend(currentUserId(), selected.getId());
            loadFriends();
        } catch (Exception e) { statusLabel.setText("Error removing friend."); }
    }

    @FXML private void goBack() throws Exception { SceneManager.switchTo("/com/spendx/view/dashboard.fxml"); }

    private int currentUserId() { return SessionManager.getCurrentUser().getId(); }
}
