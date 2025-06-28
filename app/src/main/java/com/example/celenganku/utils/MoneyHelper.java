package com.example.celenganku.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyHelper {
    public static String formatRupiah(int amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    public static String formatSimple(int amount) {
        return "Rp " + NumberFormat.getNumberInstance(Locale.getDefault()).format(amount);
    }
}