package com.university;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoadStudentsTask extends Task<ObservableList<Student>> {

    private static final String SQL = """
            SELECT student_id, registration_number, first_name, last_name
            FROM students
            ORDER BY student_id
            """;

    @Override
    protected ObservableList<Student> call() throws Exception {
        ObservableList<Student> students = FXCollections.observableArrayList();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add(new Student(
                        resultSet.getInt("student_id"),
                        resultSet.getString("registration_number"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name")));
            }
        }

        return students;
    }
}
