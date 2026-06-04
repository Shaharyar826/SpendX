package com.spendx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Expense {
    public enum Category {
        BREAKFAST, LUNCH, DINNER, SNACKS, GROCERY, TRANSPORT, UTILITIES, ENTERTAINMENT, OTHER
    }

    private int id;
    private String title;
    private BigDecimal totalAmount;
    private int paidBy;
    private String paidByName;
    private Integer groupId;
    private Category category;
    private String notes;
    private LocalDateTime createdAt;
    private List<ExpenseParticipant> participants = new ArrayList<>();

    public Expense() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ExpenseParticipant> getParticipants() { return participants; }
    public void setParticipants(List<ExpenseParticipant> participants) { this.participants = participants; }
}
