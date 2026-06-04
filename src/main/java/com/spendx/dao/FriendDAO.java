package com.spendx.dao;

import com.spendx.model.FriendRequest;
import com.spendx.model.User;
import com.spendx.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public boolean sendRequest(int senderId, int receiverId) throws SQLException {
        String sql = "INSERT IGNORE INTO friends (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int requestId, FriendRequest.Status status) throws SQLException {
        String sql = "UPDATE friends SET status = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean removeFriend(int userId, int friendId) throws SQLException {
        String sql = "DELETE FROM friends WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, friendId);
            ps.setInt(3, friendId); ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<User> getFriends(int userId) throws SQLException {
        String sql = """
                SELECT u.id, u.full_name, u.username, u.email, u.avatar_path
                FROM friends f
                JOIN users u ON (u.id = CASE WHEN f.sender_id = ? THEN f.receiver_id ELSE f.sender_id END)
                WHERE (f.sender_id = ? OR f.receiver_id = ?) AND f.status = 'ACCEPTED'
                """;
        List<User> friends = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, userId); ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setFullName(rs.getString("full_name"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setAvatarPath(rs.getString("avatar_path"));
                friends.add(u);
            }
        }
        return friends;
    }

    public List<FriendRequest> getPendingRequests(int userId) throws SQLException {
        String sql = """
                SELECT f.id, f.sender_id, u.full_name, u.username, f.created_at
                FROM friends f
                JOIN users u ON u.id = f.sender_id
                WHERE f.receiver_id = ? AND f.status = 'PENDING'
                """;
        List<FriendRequest> requests = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FriendRequest fr = new FriendRequest();
                fr.setId(rs.getInt("id"));
                fr.setSenderId(rs.getInt("sender_id"));
                fr.setSenderName(rs.getString("full_name"));
                fr.setSenderUsername(rs.getString("username"));
                fr.setStatus(FriendRequest.Status.PENDING);
                requests.add(fr);
            }
        }
        return requests;
    }

    public boolean areFriends(int userId, int otherId) throws SQLException {
        String sql = "SELECT 1 FROM friends WHERE ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)) AND status='ACCEPTED'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, otherId);
            ps.setInt(3, otherId); ps.setInt(4, userId);
            return ps.executeQuery().next();
        }
    }
}
