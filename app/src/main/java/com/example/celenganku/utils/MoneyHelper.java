package com.example.celenganku.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyHelper {
    public static String formatSimple(long amount) {  // Changed from int to long
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        return "Rp" + formatter.format(amount);
    }
}