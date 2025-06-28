package com.example.celenganku.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DISPLAY_FORMAT = "dd MMM yyyy";

    public static String dateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return format.format(date);
    }

    public static Date stringToDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // return current date if parsing fails
        }
    }

    public static String formatForDisplay(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault());
        return format.format(date);
    }
}