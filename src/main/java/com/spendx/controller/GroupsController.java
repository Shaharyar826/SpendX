package com.spendx.controller;

import com.spendx.dao.FriendDAO;
import com.spendx.dao.GroupDAO;
import com.spendx.model.Group;
import com.spendx.model.User;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GroupsController implements Initializable {

    @FXML private TextField groupNameField;
    @FXML private ListView<Group> groupsList;
    @FXML private ListView<User> membersList;
    @FXML private ListView<User> friendsList;
    @FXML private Label statusLabel;

    private final GroupDAO groupDAO = new GroupDAO();
    private final FriendDAO friendDAO = new FriendDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadGroups();
        loadFriends();
        groupsList.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, selected) -> { if (selected != null) loadMembers(selected.getId()); }
        );
        setupCellFactories();
    }

    private void loadGroups() {
        try {
            groupsList.getItems().setAll(groupDAO.getGroupsForUser(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading groups."); }
    }

    private void loadFriends() {
        try {
            friendsList.getItems().setAll(friendDAO.getFriends(currentUserId()));
        } catch (Exception e) { statusLabel.setText("Error loading friends."); }
    }

    private void loadMembers(int groupId) {
        try {
            membersList.getItems().setAll(groupDAO.getMembers(groupId));
        } catch (Exception e) { statusLabel.setText("Error loading members."); }
    }

    private void setupCellFactories() {
        groupsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Group g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : g.getName());
            }
        });
        membersList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
        friendsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (@" + u.getUsername() + ")");
            }
        });
    }

    @FXML
    private void createGroup() {
        String name = groupNameField.getText().trim();
        if (name.isEmpty()) { statusLabel.setText("Enter a group name."); return; }
        try {
            int groupId = groupDAO.createGroup(name, currentUserId());
            groupDAO.addMember(groupId, currentUserId());
            groupNameField.clear();
            loadGroups();
            statusLabel.setText("Group '" + name + "' created.");
        } catch (Exception e) { statusLabel.setText("Error creating group."); }
    }

    @FXML
    private void addMemberToGroup() {
        Group group = groupsList.getSelectionModel().getSelectedItem();
        User friend = friendsList.getSelectionModel().getSelectedItem();
        if (group == null || friend == null) { statusLabel.setText("Select a group and a friend."); return; }
        try {
            groupDAO.addMember(group.getId(), friend.getId());
            loadMembers(group.getId());
            statusLabel.setText(friend.getFullName() + " added to " + group.getName());
        } catch (Exception e) { statusLabel.setText("Error adding member."); }
    }

    @FXML
    private void removeMemberFromGroup() {
        Group group = groupsList.getSelectionModel().getSelectedItem();
        User member = membersList.getSelectionModel().getSelectedItem();
        if (group == null || member == null) return;
        try {
            groupDAO.removeMember(group.getId(), member.getId());
            loadMembers(group.getId());
        } catch (Exception e) { statusLabel.setText("Error removing member."); }
    }

    @FXML private void goBack() throws Exception { SceneManager.switchTo("/com/spendx/view/dashboard.fxml"); }

    private int currentUserId() { return SessionManager.getCurrentUser().getId(); }
}
