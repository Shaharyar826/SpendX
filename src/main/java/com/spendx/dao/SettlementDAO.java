package com.spendx.dao;

import com.spendx.model.Settlement;
import com.spendx.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettlementDAO {

    public boolean settle(int payerId, int payeeId, BigDecimal amount, String note) throws SQLException {
        String sql = "INSERT INTO settlements (payer_id, payee_id, amount, note) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, payerId);
            ps.setInt(2, payeeId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, note);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Settlement> getSettlementsForUser(int userId) throws SQLException {
        String sql = """
                SELECT s.*, p.full_name AS payer_name, e.full_name AS payee_name
                FROM settlements s
                JOIN users p ON p.id = s.payer_id
                JOIN users e ON e.id = s.payee_id
                WHERE s.payer_id = ? OR s.payee_id = ?
                ORDER BY s.settled_at DESC
                """;
        List<Settlement> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Settlement s = new Settlement();
                s.setId(rs.getInt("id"));
                s.setPayerId(rs.getInt("payer_id"));
                s.setPayeeId(rs.getInt("payee_id"));
                s.setPayerName(rs.getString("payer_name"));
                s.setPayeeName(rs.getString("payee_name"));
                s.setAmount(rs.getBigDecimal("amount"));
                s.setNote(rs.getString("note"));
                Timestamp ts = rs.getTimestamp("settled_at");
                if (ts != null) s.setSettledAt(ts.toLocalDateTime());
                list.add(s);
            }
        }
        return list;
    }

    /** Total amount settled between two users */
    public BigDecimal getTotalSettled(int payerId, int payeeId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM settlements WHERE payer_id=? AND payee_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, payerId); ps.setInt(2, payeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }
}
