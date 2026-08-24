package com.example.guide.util;

/** Small pure-function class used to demonstrate parameterized tests. */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isPalindrome(String input) {
        if (input == null) {
            return false;
        }
        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
