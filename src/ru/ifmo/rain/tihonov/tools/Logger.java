package ru.ifmo.rain.tihonov.tools;

public class Logger {
    private static boolean errorOccurred = false;

    public static void error(String message) {
        errorOccurred = true;
        System.err.println(message);
    }

    public static boolean errorOccurred() {
        return errorOccurred;
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
