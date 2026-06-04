package com.spendx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Settlement {
    private int id;
    private int payerId;
    private int payeeId;
    private String payerName;
    private String payeeName;
    private BigDecimal amount;
    private String note;
    private LocalDateTime settledAt;

    public Settlement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPayerId() { return payerId; }
    public void setPayerId(int payerId) { this.payerId = payerId; }

    public int getPayeeId() { return payeeId; }
    public void setPayeeId(int payeeId) { this.payeeId = payeeId; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
}
