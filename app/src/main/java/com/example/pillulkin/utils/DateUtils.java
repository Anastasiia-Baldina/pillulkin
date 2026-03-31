package com.example.pillulkin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isExpired(String dateStr) {
        Date date = parseDate(dateStr);
        if (date == null) return false;
        return date.before(new Date());
    }

    public static boolean isExpiringSoon(String dateStr, int daysThreshold) {
        Date date = parseDate(dateStr);
        if (date == null) return false;
        long diff = date.getTime() - System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        return days >= 0 && days <= daysThreshold;
    }

    public static String getExpirationStatus(String dateStr) {
        if (isExpired(dateStr)) {
            return "EXPIRED";
        } else if (isExpiringSoon(dateStr, 30)) {
            return "EXPIRING_SOON";
        }
        return "VALID";
    }
}
