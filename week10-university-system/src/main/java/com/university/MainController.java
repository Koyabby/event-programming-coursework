package com.university;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML
    private StackPane centerPane;

    private Node dashboardView;
    private Label dashboardStatus;
    private Task<Integer> dashboardTask;
    private boolean dashboardVisible;

    @FXML
    private void initialize() {
        dashboardView = createDashboardView();
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        dashboardVisible = true;
        if (centerPane.getChildren().isEmpty()
                || centerPane.getChildren().get(0) != dashboardView) {
            centerPane.getChildren().setAll(dashboardView);
        }
        refreshDashboard();
    }

    @FXML
    private void showStudents() {
        dashboardVisible = false;
        if (dashboardTask != null) {
            dashboardTask.cancel(true);
            dashboardTask = null;
        }

        try {
            Parent studentView = FXMLLoader.load(getClass().getResource("StudentView.fxml"));
            centerPane.getChildren().setAll(studentView);
        } catch (IOException e) {
            centerPane.getChildren().setAll(
                    new Label("Unable to load Student Management: " + e.getMessage()));
        }
    }

    void refreshDashboard() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Dashboard refresh must run on the JavaFX Application Thread.");
        }
        if (!dashboardVisible || centerPane.getChildren().isEmpty()
                || centerPane.getChildren().get(0) != dashboardView
                || (dashboardTask != null && dashboardTask.isRunning())) {
            return;
        }

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                try (Connection connection = DatabaseUtil.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "SELECT COUNT(*) FROM students");
                     ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        };
        dashboardTask = task;

        task.setOnSucceeded(event -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            dashboardStatus.setText("Students in database: " + task.getValue()
                    + "\nLast refreshed: " + time);
            dashboardStatus.setTextFill(Color.DARKGREEN);
        });

        task.setOnFailed(event -> {
            dashboardStatus.setText("Dashboard refresh failed: "
                    + dashboardErrorMessage(task.getException()));
            dashboardStatus.setTextFill(Color.RED);
        });

        Thread thread = new Thread(task, "dashboard-refresh");
        thread.setDaemon(true);
        thread.start();
    }

    void shutdown() {
        if (dashboardTask != null) {
            dashboardTask.cancel(true);
        }
    }

    private Parent createDashboardView() {
        Label welcome = new Label("Welcome to the University Dashboard!");
        welcome.setFont(Font.font(20));

        dashboardStatus = new Label("Refreshing dashboard...");
        dashboardStatus.setId("dashboardStatus");
        dashboardStatus.setTextFill(Color.BLUE);

        VBox layout = new VBox(20, welcome, dashboardStatus);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    private String dashboardErrorMessage(Throwable error) {
        if (error == null || error.getMessage() == null || error.getMessage().isBlank()) {
            return "Unknown database error.";
        }
        return error.getMessage();
    }
}
