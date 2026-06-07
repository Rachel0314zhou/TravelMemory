package com.example.travelmemory.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static long getDaysBetween(String start, String end) {
        if (start == null || end == null || start.isEmpty() || end.isEmpty()) {
            return 1;
        }
        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            if (startDate != null && endDate != null) {
                long diff = endDate.getTime() - startDate.getTime();
                long days = diff / (1000 * 60 * 60 * 24) + 1; // inclusive
                return Math.max(1, days);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
