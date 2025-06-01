package com.tinyjvm.utils;

public class Logger {

    public enum LogLevel {
        DEBUG,
        INFO,
        ERROR,
        NONE
    }

    private static LogLevel currentLevel = LogLevel.INFO;

    public static void setLevel(LogLevel level) {
        currentLevel = level;
    }

    public static void debug(String message) {
        if (currentLevel.ordinal() <= LogLevel.DEBUG.ordinal()) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void info(String message) {
        if (currentLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            System.out.println("[INFO] " + message);
        }
    }

    public static void error(String message) {
        if (currentLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            System.err.println("[ERROR] " + message);
        }
    }
}
