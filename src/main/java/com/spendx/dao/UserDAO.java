package com.spendx.dao;

import com.spendx.model.User;
import com.spendx.util.DBConnection;
import com.spendx.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, username, email, password) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, PasswordUtil.hash(user.getPassword()));
            return ps.executeUpdate() == 1;
        }
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                return mapUser(rs);
            }
        }
        return null;
    }

    public List<User> searchByUsername(String query, int excludeUserId) throws SQLException {
        String sql = "SELECT id, full_name, username, email, avatar_path FROM users " +
                     "WHERE username LIKE ? AND id != ?";
        List<User> users = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setInt(2, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapUser(rs));
        }
        return users;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, full_name, username, email, avatar_path FROM users WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setAvatarPath(rs.getString("avatar_path"));
        return u;
    }
}
