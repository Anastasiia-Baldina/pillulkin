package com.example.pillulkin.utils;

public class ValidationUtils {
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isValidAge(int age) {
        return age > 0 && age < 150;
    }

    public static boolean isValidDosage(String dosage) {
        return dosage != null && !dosage.trim().isEmpty();
    }

    public static boolean isValidQuantity(int quantity) {
        return quantity >= 0;
    }

    public static boolean isValidExpirationDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        return DateUtils.parseDate(dateStr) != null;
    }

    public static boolean isValidForm(String form) {
        return form != null && !form.trim().isEmpty();
    }
}
