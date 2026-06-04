package com.spendx.dao;

import com.spendx.model.Expense;
import com.spendx.model.ExpenseParticipant;
import com.spendx.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public int addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (title, total_amount, paid_by, group_id, category, notes) VALUES (?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expense.getTitle());
            ps.setBigDecimal(2, expense.getTotalAmount());
            ps.setInt(3, expense.getPaidBy());
            if (expense.getGroupId() != null) ps.setInt(4, expense.getGroupId());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, expense.getCategory().name());
            ps.setString(6, expense.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void addParticipants(int expenseId, List<ExpenseParticipant> participants) throws SQLException {
        String sql = "INSERT INTO expense_participants (expense_id, user_id, share) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (ExpenseParticipant p : participants) {
                ps.setInt(1, expenseId);
                ps.setInt(2, p.getUserId());
                ps.setBigDecimal(3, p.getShare());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Expense> getExpensesForUser(int userId) throws SQLException {
        String sql = """
                SELECT DISTINCT e.*, u.full_name AS paid_by_name
                FROM expenses e
                JOIN users u ON u.id = e.paid_by
                JOIN expense_participants ep ON ep.expense_id = e.id
                WHERE ep.user_id = ? OR e.paid_by = ?
                ORDER BY e.created_at DESC
                """;
        return queryExpenses(sql, userId, userId);
    }

    public List<Expense> getExpensesForGroup(int groupId) throws SQLException {
        String sql = """
                SELECT e.*, u.full_name AS paid_by_name
                FROM expenses e
                JOIN users u ON u.id = e.paid_by
                WHERE e.group_id = ?
                ORDER BY e.created_at DESC
                """;
        List<Expense> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapExpense(rs));
        }
        return list;
    }

    /** Returns net balance: positive = others owe you, negative = you owe others */
    public BigDecimal getNetBalance(int userId) throws SQLException {
        String sql = """
                SELECT
                  COALESCE(SUM(CASE WHEN e.paid_by = ? THEN ep.share ELSE 0 END), 0)
                  - COALESCE(SUM(CASE WHEN e.paid_by != ? THEN ep.share ELSE 0 END), 0) AS net
                FROM expense_participants ep
                JOIN expenses e ON e.id = ep.expense_id
                WHERE ep.user_id = ? AND e.paid_by != ep.user_id
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, userId); ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("net");
        }
        return BigDecimal.ZERO;
    }

    /** Returns how much userId owes friendId (positive) or is owed by friendId (negative) */
    public BigDecimal getBalanceBetween(int userId, int friendId) throws SQLException {
        String sql = """
                SELECT
                  COALESCE(SUM(CASE WHEN e.paid_by = ? THEN ep.share ELSE -ep.share END), 0) AS balance
                FROM expense_participants ep
                JOIN expenses e ON e.id = ep.expense_id
                WHERE ep.user_id = ? AND e.paid_by = ?
                   OR ep.user_id = ? AND e.paid_by = ?
                """;
        // Simpler two-query approach for clarity
        BigDecimal youOwe = getDirectionalBalance(friendId, userId);   // friend paid, you owe
        BigDecimal theyOwe = getDirectionalBalance(userId, friendId);  // you paid, they owe
        return theyOwe.subtract(youOwe);
    }

    private BigDecimal getDirectionalBalance(int payerId, int participantId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(ep.share), 0) AS total
                FROM expense_participants ep
                JOIN expenses e ON e.id = ep.expense_id
                WHERE e.paid_by = ? AND ep.user_id = ? AND ep.user_id != e.paid_by
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, payerId);
            ps.setInt(2, participantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("total");
        }
        return BigDecimal.ZERO;
    }

    private List<Expense> queryExpenses(String sql, int p1, int p2) throws SQLException {
        List<Expense> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p1); ps.setInt(2, p2);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapExpense(rs));
        }
        return list;
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setTotalAmount(rs.getBigDecimal("total_amount"));
        e.setPaidBy(rs.getInt("paid_by"));
        e.setPaidByName(rs.getString("paid_by_name"));
        e.setCategory(Expense.Category.valueOf(rs.getString("category")));
        e.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }
}
