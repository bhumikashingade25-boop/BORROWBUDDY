package com.borrowbuddy.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date conversions and formatting used throughout BorrowBuddy.
 */
public class DateUtil {

    private static final String DATE_FORMAT      = "dd MMM yyyy";
    private static final String TIMESTAMP_FORMAT = "dd MMM yyyy HH:mm";

    private DateUtil() {}

    /** Converts a {@code java.util.Date} to a {@code java.sql.Date}. */
    public static Date toSqlDate(java.util.Date utilDate) {
        return (utilDate == null) ? null : new Date(utilDate.getTime());
    }

    /** Converts a {@code java.sql.Date} to a {@code java.util.Date}. */
    public static java.util.Date toUtilDate(Date sqlDate) {
        return (sqlDate == null) ? null : new java.util.Date(sqlDate.getTime());
    }

    /** Formats a {@code java.sql.Date} as "dd MMM yyyy", e.g. "15 Oct 2026". */
    public static String formatDate(Date sqlDate) {
        if (sqlDate == null) return "";
        return new SimpleDateFormat(DATE_FORMAT).format(sqlDate);
    }

    /** Formats a {@code Timestamp} as "dd MMM yyyy HH:mm". */
    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(ts);
    }

    /**
     * Returns true if {@code dueDate} is strictly before today's date.
     *
     * @param dueDate the date to check
     * @return true if overdue
     */
    public static boolean isOverdue(Date dueDate) {
        if (dueDate == null) return false;
        return dueDate.toLocalDate().isBefore(LocalDate.now());
    }

    /**
     * Returns the number of days between two SQL dates.
     *
     * @param start the earlier date
     * @param end   the later date
     * @return days between start and end (may be negative if end < start)
     */
    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    /** Returns today's date as a {@code java.sql.Date}. */
    public static Date today() {
        return Date.valueOf(LocalDate.now());
    }
}
