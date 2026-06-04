package com.spendx.model;

import java.math.BigDecimal;

public class ExpenseParticipant {
    private int id;
    private int expenseId;
    private int userId;
    private String username;
    private String fullName;
    private BigDecimal share;

    public ExpenseParticipant() {}

    public ExpenseParticipant(int userId, BigDecimal share) {
        this.userId = userId;
        this.share = share;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BigDecimal getShare() { return share; }
    public void setShare(BigDecimal share) { this.share = share; }
}
