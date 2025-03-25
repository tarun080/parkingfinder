package com.example.parkingfinder.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat ISO_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
    private static final SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMM", Locale.getDefault());

    /**
     * Format date to string (e.g., "Jan 01, 2023")
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    /**
     * Format time to string (e.g., "10:30 AM")
     */
    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }

    /**
     * Format date and time to string (e.g., "Jan 01, 2023 10:30 AM")
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }

    /**
     * Format date to ISO 8601 format for API calls
     */
    public static String formatIsoDateTime(Date date) {
        if (date == null) return "";
        return ISO_DATE_TIME_FORMAT.format(date);
    }

    /**
     * Format date to short format (e.g., "01/23/23")
     */
    public static String formatShortDate(Date date) {
        if (date == null) return "";
        return SHORT_DATE_FORMAT.format(date);
    }

    /**
     * Format time to short format (e.g., "14:30")
     */
    public static String formatShortTime(Date date) {
        if (date == null) return "";
        return SHORT_TIME_FORMAT.format(date);
    }

    /**
     * Format date to day of week (e.g., "Monday")
     */
    public static String formatDayOfWeek(Date date) {
        if (date == null) return "";
        return DAY_OF_WEEK_FORMAT.format(date);
    }

    /**
     * Format date to month name (e.g., "January")
     */
    public static String formatMonth(Date date) {
        if (date == null) return "";
        return MONTH_FORMAT.format(date);
    }

    /**
     * Convert milliseconds to Date object
     */
    public static Date fromMillis(long millis) {
        return new Date(millis);
    }

    /**
     * Get current date and time
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Check if a date is today
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);

        return today.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendarDate.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if a date is tomorrow
     */
    public static boolean isTomorrow(Date date) {
        if (date == null) return false;

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);

        return tomorrow.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == calendarDate.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if a date is in the future
     */
    public static boolean isFuture(Date date) {
        if (date == null) return false;
        return date.after(new Date());
    }

    /**
     * Check if a date is in the past
     */
    public static boolean isPast(Date date) {
        if (date == null) return false;
        return date.before(new Date());
    }

    /**
     * Check if a date is within the given number of days from now
     */
    public static boolean isWithinDays(Date date, int days) {
        if (date == null) return false;

        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, days);

        return date.after(new Date()) && date.before(future.getTime());
    }

    /**
     * Get a relative time string (e.g., "3 hours ago", "in 5 minutes")
     */
    public static String getRelativeTimeSpan(Date date) {
        if (date == null) return "";

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = Math.abs(now - time);

        boolean isPast = now > time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return isPast ? "just now" : "in a moment";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return isPast ? minutes + " minutes ago" : "in " + minutes + " minutes";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return isPast ? hours + " hours ago" : "in " + hours + " hours";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return isPast ? days + " days ago" : "in " + days + " days";
        } else {
            return formatDate(date);
        }
    }

    /**
     * Get a relative date string suitable for displaying booking dates
     */
    public static String getRelativeDateString(Date date) {
        if (date == null) return "";

        if (isToday(date)) {
            return "Today";
        } else if (isTomorrow(date)) {
            return "Tomorrow";
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // If it's within the next 7 days, show the day name
            Calendar nextWeek = Calendar.getInstance();
            nextWeek.add(Calendar.DAY_OF_YEAR, 7);

            if (date.before(nextWeek.getTime())) {
                return formatDayOfWeek(date);
            } else {
                return formatDate(date);
            }
        }
    }

    /**
     * Calculate duration between two dates in hours
     */
    public static double calculateDurationInHours(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;

        long durationMillis = endDate.getTime() - startDate.getTime();
        return durationMillis / (1000.0 * 60 * 60);
    }

    /**
     * Format duration in milliseconds to readable string (e.g., "2 hours 30 minutes")
     */
    public static String formatDuration(long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) -
                TimeUnit.HOURS.toMinutes(hours);

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" hour");
            if (hours > 1) sb.append("s");
        }

        if (minutes > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(minutes).append(" minute");
            if (minutes > 1) sb.append("s");
        }

        if (sb.length() == 0) {
            return "0 minutes";
        }

        return sb.toString();
    }

    /**
     * Format duration between two dates to readable string
     */
    public static String formatDuration(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return "";

        long durationMillis = endDate.getTime() - startDate.getTime();
        return formatDuration(durationMillis);
    }

    /**
     * Add specified number of hours to a date
     */
    public static Date addHours(Date date, int hours) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);

        return calendar.getTime();
    }

    /**
     * Add specified number of minutes to a date
     */
    public static Date addMinutes(Date date, int minutes) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);

        return calendar.getTime();
    }

    /**
     * Add specified number of days to a date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);

        return calendar.getTime();
    }

    /**
     * Get start of day for a given date
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Get end of day for a given date
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * Get difference in days between two dates
     */
    public static int getDaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;

        long startMillis = getStartOfDay(startDate).getTime();
        long endMillis = getStartOfDay(endDate).getTime();

        return (int) (TimeUnit.MILLISECONDS.toDays(endMillis - startMillis));
    }

    /**
     * Check if two dates are on the same day
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Round a date to the nearest minutes (e.g., nearest 15 minutes)
     */
    public static Date roundToNearestMinutes(Date date, int minutes) {
        if (date == null || minutes <= 0) return date;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % minutes;

        // Round up or down
        if (mod < minutes / 2) {
            // Round down
            calendar.add(Calendar.MINUTE, -mod);
        } else {
            // Round up
            calendar.add(Calendar.MINUTE, minutes - mod);
        }

        // Set seconds and milliseconds to zero
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}