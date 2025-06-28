// MoneyHelper.java
package com.example.celenganku.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyHelper {
    public static String formatSimple(int amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount).replace("Rp", "Rp ");
    }
}