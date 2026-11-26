package com.university;

final class StudentValidator {

    private StudentValidator() {
    }

    static String validate(String registrationNumber, String firstName, String lastName) {
        if (registrationNumber == null || registrationNumber.isBlank()
                || firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()) {
            return "Registration number, first name and last name are required.";
        }
        if (registrationNumber.length() > 20) {
            return "Registration number must be 20 characters or fewer.";
        }
        if (firstName.length() > 50) {
            return "First name must be 50 characters or fewer.";
        }
        if (lastName.length() > 50) {
            return "Last name must be 50 characters or fewer.";
        }
        return null;
    }
}
