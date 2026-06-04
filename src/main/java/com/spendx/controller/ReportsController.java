package com.spendx.controller;

import com.spendx.dao.ExpenseDAO;
import com.spendx.model.Expense;
import com.spendx.util.SceneManager;
import com.spendx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private ListView<String> categoryReportList;
    @FXML private ListView<String> monthlyReportList;
    @FXML private Label totalSpentLabel;

    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<Expense> expenses = expenseDAO.getExpensesForUser(SessionManager.getCurrentUser().getId());
            showCategoryReport(expenses);
            showMonthlyReport(expenses);
            showTotal(expenses);
        } catch (Exception e) {
            totalSpentLabel.setText("Error loading reports.");
        }
    }

    private void showCategoryReport(List<Expense> expenses) {
        Map<Expense.Category, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Expense e : expenses) {
            byCategory.merge(e.getCategory(), e.getTotalAmount(), BigDecimal::add);
        }
        List<String> lines = byCategory.entrySet().stream()
                .sorted(Map.Entry.<Expense.Category, BigDecimal>comparingByValue().reversed())
                .map(en -> en.getKey() + ": Rs. " + en.getValue())
                .collect(Collectors.toList());
        categoryReportList.getItems().setAll(lines);
    }

    private void showMonthlyReport(List<Expense> expenses) {
        Map<String, BigDecimal> byMonth = new LinkedHashMap<>();
        for (Expense e : expenses) {
            if (e.getCreatedAt() == null) continue;
            String key = e.getCreatedAt().getYear() + " - " + e.getCreatedAt().getMonth().getDisplayName(
                    java.time.format.TextStyle.FULL, Locale.ENGLISH);
            byMonth.merge(key, e.getTotalAmount(), BigDecimal::add);
        }
        List<String> lines = byMonth.entrySet().stream()
                .map(en -> en.getKey() + ": Rs. " + en.getValue())
                .collect(Collectors.toList());
        monthlyReportList.getItems().setAll(lines);
    }

    private void showTotal(List<Expense> expenses) {
        BigDecimal total = expenses.stream()
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalSpentLabel.setText("Total Expenses Recorded: Rs. " + total);
    }

    @FXML private void goBack() throws Exception { SceneManager.switchTo("/com/spendx/view/dashboard.fxml"); }
}
