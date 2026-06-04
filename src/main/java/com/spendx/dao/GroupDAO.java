package com.spendx.dao;

import com.spendx.model.Group;
import com.spendx.model.User;
import com.spendx.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public int createGroup(String name, int createdBy) throws SQLException {
        String sql = "INSERT INTO groups_table (name, created_by) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public boolean addMember(int groupId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean removeMember(int groupId, int userId) throws SQLException {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Group> getGroupsForUser(int userId) throws SQLException {
        String sql = """
                SELECT g.id, g.name, g.created_by, g.created_at
                FROM groups_table g
                JOIN group_members gm ON gm.group_id = g.id
                WHERE gm.user_id = ?
                ORDER BY g.created_at DESC
                """;
        List<Group> groups = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) groups.add(mapGroup(rs));
        }
        return groups;
    }

    public List<User> getMembers(int groupId) throws SQLException {
        String sql = """
                SELECT u.id, u.full_name, u.username, u.email, u.avatar_path
                FROM group_members gm
                JOIN users u ON u.id = gm.user_id
                WHERE gm.group_id = ?
                """;
        List<User> members = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setFullName(rs.getString("full_name"));
                u.setUsername(rs.getString("username"));
                members.add(u);
            }
        }
        return members;
    }

    private Group mapGroup(ResultSet rs) throws SQLException {
        Group g = new Group();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setCreatedBy(rs.getInt("created_by"));
        return g;
    }
}
