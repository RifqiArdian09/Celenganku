package com.example.celenganku.navigation;

import android.content.Context;
import android.content.Intent;

import com.example.celenganku.activities.MainActivity;
import com.example.celenganku.activities.RiwayatActivity;
import com.example.celenganku.activities.TargetActivity;

public class NavigationHelper {
    public static void goToMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void goToRiwayat(Context context) {
        Intent intent = new Intent(context, RiwayatActivity.class);
        context.startActivity(intent);
    }

    public static void goToTarget(Context context) {
        Intent intent = new Intent(context, TargetActivity.class);
        context.startActivity(intent);
    }
}