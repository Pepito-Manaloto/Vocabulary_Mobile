package com.aaron.vocabulary.model;

import android.util.Log;

import java.util.Arrays;

/**
 * Handles the application logging(different from LogCat).
 */
public class LogsManager
{
    public static final String TAG = "Vocabulary";
    private static final StringBuilder logs = new StringBuilder();

    /**
     * Adds the given message to the string builder logs.
     * Logs to Logcat.
     *
     * @param text the message to add
     */
    public static void log(final String className, final String methodName, final String text)
    {
        logs.append(text).append("\n");
        Log.d(LogsManager.TAG, className + ": " + methodName + ". " + text);
    }

    /**
     * Adds the given message to the string builder logs.
     * Logs error to Logcat.
     *
     * @param text the message to add
     */
    public static void log(final String className, final String methodName, final String text, final Throwable t)
    {
        logs.append(text).append("\nError: ").append(t.getMessage()).append("\n");
        Log.e(LogsManager.TAG, className + ": " + methodName + ". " + text, t);
    }

    /**
     * Returns the StringBuilder object into a single String
     *
     * @return String
     */
    public String getLogs()
    {
        return logs.toString().substring(0, logs.length() - 1); // Removes trailing '\n' character
    }

    /**
     * Returns the filtered StringBuilder String
     *
     * @param keyWord the word used in filtering
     */
    public String getLogs(final String keyWord)
    {
        String lineSeparator = System.lineSeparator();
        String[] lines = logs.toString().split(lineSeparator);
        StringBuilder sb = new StringBuilder();

        Arrays.stream(lines).filter(line -> line.contains(keyWord)).forEach((line) -> sb.append(line).append(lineSeparator));

        return sb.toString();
    }

    /**
     * Saves the logs to the local disk.
     *
     * @return true on success, else false
     */
    public boolean saveToDisk()
    {
        // TODO: Implements functionalities
        return false;
    }
}
