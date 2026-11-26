package com.university;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentController {

    @FXML
    private TextField regField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TableView<Student> studentTable;
    @FXML
    private TableColumn<Student, String> colReg;
    @FXML
    private TableColumn<Student, String> colFirst;
    @FXML
    private TableColumn<Student, String> colLast;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final SimpleBooleanProperty saving = new SimpleBooleanProperty(false);
    private LoadStudentsTask loadTask;
    private boolean reloadAfterLoad;

    @FXML
    public void initialize() {
        colReg.setCellValueFactory(cellData -> cellData.getValue().regNumberProperty());
        colFirst.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        colLast.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        studentTable.setItems(studentList);

        saveButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> regField.getText().isBlank()
                        || firstNameField.getText().isBlank()
                        || lastNameField.getText().isBlank()
                        || saving.get(),
                regField.textProperty(), firstNameField.textProperty(), lastNameField.textProperty(), saving));

        loadStudents();
    }

    @FXML
    private void handleSaveStudent() {
        String registrationNumber = regField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String validationError = StudentValidator.validate(registrationNumber, firstName, lastName);

        if (validationError != null) {
            setStatus(validationError, Color.RED);
            return;
        }
        if (saving.get()) {
            return;
        }

        saving.set(true);
        setStatus("Saving student...", Color.BLUE);

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String sql = """
                        INSERT INTO students (registration_number, first_name, last_name, email)
                        VALUES (?, ?, ?, ?)
                        """;

                try (Connection connection = DatabaseUtil.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, registrationNumber);
                    statement.setString(2, firstName);
                    statement.setString(3, lastName);
                    statement.setString(4, registrationNumber + "@university.edu");
                    statement.executeUpdate();
                }
                return null;
            }
        };

        saveTask.setOnSucceeded(event -> {
            saving.set(false);
            regField.clear();
            firstNameField.clear();
            lastNameField.clear();
            setStatus("Student saved successfully!", Color.GREEN);
            loadStudents();
        });

        saveTask.setOnFailed(event -> {
            saving.set(false);
            setStatus("Could not save student: " + databaseErrorMessage(saveTask.getException()), Color.RED);
        });

        startTask(saveTask, "save-student");
    }

    private void loadStudents() {
        if (loadTask != null && loadTask.isRunning()) {
            reloadAfterLoad = true;
            return;
        }

        LoadStudentsTask task = new LoadStudentsTask();
        loadTask = task;
        setStatus("Loading students...", Color.BLUE);

        task.setOnSucceeded(event -> {
            studentList.setAll(task.getValue());
            setStatus("Loaded " + studentList.size() + " students from database.", Color.GREEN);
            if (reloadAfterLoad) {
                reloadAfterLoad = false;
                loadStudents();
            }
        });

        task.setOnFailed(event -> {
            setStatus("Could not load students: " + databaseErrorMessage(task.getException()), Color.RED);
            if (reloadAfterLoad) {
                reloadAfterLoad = false;
                loadStudents();
            }
        });

        startTask(task, "load-students");
    }

    private void startTask(Task<?> task, String name) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Database tasks must be started on the JavaFX Application Thread.");
        }
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    private String databaseErrorMessage(Throwable error) {
        if (error instanceof SQLException sqlException
                && "23000".equals(sqlException.getSQLState())
                && sqlException.getErrorCode() == 1062) {
            return "A student with this registration number or email already exists.";
        }
        if (error == null || error.getMessage() == null || error.getMessage().isBlank()) {
            return "Unknown database error.";
        }
        return error.getMessage();
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
}
