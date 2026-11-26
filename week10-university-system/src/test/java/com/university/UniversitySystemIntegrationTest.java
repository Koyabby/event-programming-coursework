package com.university;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class UniversitySystemIntegrationTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException e) {
            started.countDown();
        }
        assertTrue(started.await(15, TimeUnit.SECONDS), "JavaFX toolkit did not start");
        onFx(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void performsStudentWorkflowAndHandlesDatabaseFailures() throws Exception {
        String registrationNumber = "W10" + System.currentTimeMillis();
        String foreignKeyRegistrationNumber = registrationNumber + "F";

        try (Connection connection = DatabaseUtil.getConnection()) {
            deleteStudent(connection, registrationNumber);
            deleteStudent(connection, foreignKeyRegistrationNumber);
            try (Statement statement = connection.createStatement();
                 ResultSet programs = statement.executeQuery("SELECT COUNT(*) FROM programs")) {
                assertTrue(programs.next());
                assertTrue(programs.getInt(1) >= 2, "The two sample programs should exist");
            }
        } catch (SQLException e) {
            assumeTrue(false, "Live MySQL integration tests require university_db and configured credentials: "
                    + e.getMessage());
        }

        try {
            SQLException foreignKeyError = assertThrows(SQLException.class, () -> {
                try (Connection connection = DatabaseUtil.getConnection();
                     PreparedStatement statement = connection.prepareStatement("""
                             INSERT INTO students
                                 (registration_number, first_name, last_name, email, program_id)
                             VALUES (?, 'Week', 'Ten', ?, ?)
                             """)) {
                    statement.setString(1, foreignKeyRegistrationNumber);
                    statement.setString(2, foreignKeyRegistrationNumber + "@university.edu");
                    statement.setInt(3, Integer.MAX_VALUE);
                    statement.executeUpdate();
                }
            });
            assertEquals("23000", foreignKeyError.getSQLState());

            Parent mainRoot = onFx(() -> FXMLLoader.load(
                    UniversitySystemIntegrationTest.class.getResource("/com/university/MainView.fxml")));
            Button studentsButton = onFx(() -> findButton(mainRoot, "Students"));
            onFx(() -> {
                studentsButton.fire();
                return null;
            });

            TextField regField = onFx(() -> find(mainRoot, "#regField", TextField.class));
            TextField firstNameField = onFx(() -> find(mainRoot, "#firstNameField", TextField.class));
            TextField lastNameField = onFx(() -> find(mainRoot, "#lastNameField", TextField.class));
            Button saveButton = onFx(() -> find(mainRoot, "#saveButton", Button.class));
            Label statusLabel = onFx(() -> find(mainRoot, "#statusLabel", Label.class));
            TableView<Student> studentTable = onFx(() -> find(mainRoot, "#studentTable", TableView.class));

            assertTrue(onFx(saveButton::isDisabled));
            onFx(() -> {
                regField.setText("   ");
                firstNameField.setText("Ada");
                lastNameField.setText("Lovelace");
                return null;
            });
            assertTrue(onFx(saveButton::isDisabled));

            onFx(() -> {
                regField.setText("  " + registrationNumber + "  ");
                firstNameField.setText("  Ada  ");
                lastNameField.setText("  Lovelace  ");
                return null;
            });
            assertFalse(onFx(saveButton::isDisabled));
            onFx(() -> {
                saveButton.fire();
                return null;
            });

            waitUntil(() -> studentTable.getItems().stream()
                    .anyMatch(student -> registrationNumber.equals(student.getRegNumber()))
                    && statusLabel.getText().contains("Loaded"));

            onFx(() -> {
                Student saved = studentTable.getItems().stream()
                        .filter(student -> registrationNumber.equals(student.getRegNumber()))
                        .findFirst().orElseThrow();
                assertEquals("Ada", saved.getFirstName());
                assertEquals("Lovelace", saved.getLastName());
                assertEquals("", regField.getText());
                assertEquals("", firstNameField.getText());
                assertEquals("", lastNameField.getText());
                return null;
            });

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT email, program_id FROM students WHERE registration_number = ?
                         """)) {
                statement.setString(1, registrationNumber);
                try (ResultSet savedRow = statement.executeQuery()) {
                    assertTrue(savedRow.next());
                    assertEquals(registrationNumber + "@university.edu", savedRow.getString("email"));
                    savedRow.getInt("program_id");
                    assertTrue(savedRow.wasNull(), "The lecturer's form does not assign a program");
                }
            }

            onFx(() -> {
                regField.setText(registrationNumber);
                firstNameField.setText("Ada");
                lastNameField.setText("Lovelace");
                return null;
            });
            assertFalse(onFx(saveButton::isDisabled));
            onFx(() -> {
                saveButton.fire();
                return null;
            });
            waitUntil(() -> statusLabel.getText().contains("already exists"));
            assertEquals(registrationNumber, onFx(regField::getText));

            String originalUrl = System.getProperty("university.db.url");
            System.setProperty("university.db.url",
                    "jdbc:mysql://127.0.0.1:1/university_db?connectTimeout=1000&socketTimeout=1000");
            try {
                Parent failingRoot = onFx(() -> FXMLLoader.load(
                        UniversitySystemIntegrationTest.class.getResource("/com/university/StudentView.fxml")));
                Label failingStatus = onFx(() -> find(failingRoot, "#statusLabel", Label.class));
                waitUntil(() -> failingStatus.getText().startsWith("Could not load students:"));

                TextField failingReg = onFx(() -> find(failingRoot, "#regField", TextField.class));
                TextField failingFirst = onFx(() -> find(failingRoot, "#firstNameField", TextField.class));
                TextField failingLast = onFx(() -> find(failingRoot, "#lastNameField", TextField.class));
                Button failingSave = onFx(() -> find(failingRoot, "#saveButton", Button.class));
                onFx(() -> {
                    failingReg.setText("OFFLINE1");
                    failingFirst.setText("Offline");
                    failingLast.setText("Test");
                    return null;
                });
                assertFalse(onFx(failingSave::isDisabled));
                onFx(() -> {
                    failingSave.fire();
                    return null;
                });
                waitUntil(() -> failingStatus.getText().startsWith("Could not save student:"));
                assertEquals("OFFLINE1", onFx(failingReg::getText));
            } finally {
                if (originalUrl == null) {
                    System.clearProperty("university.db.url");
                } else {
                    System.setProperty("university.db.url", originalUrl);
                }
            }

            Button dashboardButton = onFx(() -> findButton(mainRoot, "Dashboard"));
            onFx(() -> {
                dashboardButton.fire();
                return null;
            });
            Label dashboardStatus = onFx(() -> find(mainRoot, "#dashboardStatus", Label.class));
            waitUntil(() -> dashboardStatus.getText().startsWith("Students in database:"));
        } finally {
            try (Connection connection = DatabaseUtil.getConnection()) {
                deleteStudent(connection, registrationNumber);
                deleteStudent(connection, foreignKeyRegistrationNumber);
            }
        }
    }

    private static void deleteStudent(Connection connection, String registrationNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM students WHERE registration_number = ?")) {
            statement.setString(1, registrationNumber);
            statement.executeUpdate();
        }
    }

    private static Button findButton(Parent root, String text) {
        return root.lookupAll(".button").stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> text.equals(button.getText()))
                .findFirst().orElseThrow();
    }

    private static <T extends Node> T find(Parent root, String selector, Class<T> type) {
        Node node = root.lookup(selector);
        assertNotNull(node, "Missing UI component " + selector);
        return type.cast(node);
    }

    private static void waitUntil(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            if (onFx(condition::getAsBoolean)) {
                return;
            }
            Thread.sleep(25);
        }
        assertTrue(false, "Timed out waiting for the expected UI state");
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> future = new FutureTask<>(action);
        Platform.runLater(future);
        return future.get(15, TimeUnit.SECONDS);
    }
}
