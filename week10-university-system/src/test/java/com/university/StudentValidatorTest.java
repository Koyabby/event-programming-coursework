package com.university;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentValidatorTest {

    @Test
    void rejectsMissingOrBlankRequiredFields() {
        assertNotNull(StudentValidator.validate(null, "Ada", "Lovelace"));
        assertNotNull(StudentValidator.validate("", "Ada", "Lovelace"));
        assertNotNull(StudentValidator.validate("   ", "Ada", "Lovelace"));
        assertNotNull(StudentValidator.validate("S101", " ", "Lovelace"));
        assertNotNull(StudentValidator.validate("S101", "Ada", null));
    }

    @Test
    void acceptsValuesAtDatabaseLengthLimits() {
        assertNull(StudentValidator.validate("R".repeat(20), "F".repeat(50), "L".repeat(50)));
    }

    @Test
    void rejectsValuesThatExceedDatabaseLengthLimits() {
        assertNotNull(StudentValidator.validate("R".repeat(21), "Ada", "Lovelace"));
        assertNotNull(StudentValidator.validate("S101", "F".repeat(51), "Lovelace"));
        assertNotNull(StudentValidator.validate("S101", "Ada", "L".repeat(51)));
    }
}
