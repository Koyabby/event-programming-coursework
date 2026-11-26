package com.university;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    @Test
    void exposesStudentValuesThroughPropertiesAndGetters() {
        Student student = new Student(7, "S101", "Ada", "Lovelace");

        assertEquals(7, student.getId());
        assertEquals(7, student.idProperty().get());
        assertEquals("S101", student.getRegNumber());
        assertEquals("S101", student.regNumberProperty().get());
        assertEquals("Ada", student.getFirstName());
        assertEquals("Ada", student.firstNameProperty().get());
        assertEquals("Lovelace", student.getLastName());
        assertEquals("Lovelace", student.lastNameProperty().get());
    }

    @Test
    void observablePropertyChangesAreVisibleThroughGetters() {
        Student student = new Student(1, "S101", "Ada", "Lovelace");

        student.regNumberProperty().set("S102");
        student.firstNameProperty().set("Grace");
        student.lastNameProperty().set("Hopper");

        assertEquals("S102", student.getRegNumber());
        assertEquals("Grace", student.getFirstName());
        assertEquals("Hopper", student.getLastName());
    }
}
