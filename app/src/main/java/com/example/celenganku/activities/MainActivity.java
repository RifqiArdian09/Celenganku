package com.example.celenganku.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.celenganku.R;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.MoneyHelper;
import com.example.celenganku.utils.NotifHelper;
import com.example.celenganku.utils.NotificationScheduler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.animation.Easing;

public class MainActivity extends AppCompatActivity implements TransactionDialog.OnTransactionSuccessListener {

    private DatabaseHelper dbHelper;
    private LineChart chartTabungan;
    private Button btnTambah, btnTarik;
    private TextView tvTotalSaldo;
    private TransactionDialog transactionDialog;
    private long lastClickTime = 0;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final long LOW_BALANCE_THRESHOLD = 100000L; // Note the 'L' for long literal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        dbHelper = new DatabaseHelper(this);
        chartTabungan = findViewById(R.id.chartTabungan);
        btnTambah = findViewById(R.id.btnTambah);
        btnTarik = findViewById(R.id.btnTarik);
        tvTotalSaldo = findViewById(R.id.tvTotalSaldo);

        // Setup notification system
        setupNotifications();

        // Setup components
        setupChart();
        setupBottomNavigation();
        setupButtons();
        updateTotalSaldo();
    }

    private void setupNotifications() {
        NotifHelper.createNotificationChannel(this);
        NotificationScheduler.scheduleDailyNotification(this);
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            NotifHelper.showNotification(
                    this,
                    1001,
                    getString(R.string.welcome_notification_title),
                    getString(R.string.welcome_notification_message)
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        updateTotalSaldo();
        setupChart();
        checkLowBalanceNotification();
    }

    private void checkLowBalanceNotification() {
        long balance = dbHelper.getTotalSaldo();
        if (balance < LOW_BALANCE_THRESHOLD) {
            String formattedBalance = MoneyHelper.formatSimple(balance);
            NotifHelper.showNotification(
                    this,
                    1002,
                    getString(R.string.low_balance_title),
                    getString(R.string.low_balance_message, formattedBalance)
            );
        }
    }

    private void setupChart() {
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi();
        List<Entry> entries = new ArrayList<>();

        float total = 0;
        for (int i = 0; i < transaksiList.size(); i++) {
            Transaksi t = transaksiList.get(i);
            total += t.getJenis().equals("masuk") ? t.getNominal() : -t.getNominal();
            entries.add(new Entry(i, total));
        }

        if (!entries.isEmpty()) {
            // Create dataset with improved styling
            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.savings_progress));

            // Line styling
            dataSet.setColor(getColor(R.color.primary));
            dataSet.setLineWidth(2.5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curved line

            // Circle styling
            dataSet.setCircleColor(getColor(R.color.accent));
            dataSet.setCircleRadius(5f);
            dataSet.setCircleHoleRadius(3f);
            dataSet.setCircleHoleColor(getColor(R.color.white));

            // Value styling
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(getColor(R.color.text));
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return MoneyHelper.formatSimple((long) value);
                }
            });

            // Fill below line
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getColor(R.color.primary_light));
            dataSet.setFillAlpha(100);

            // Create line data
            LineData lineData = new LineData(dataSet);
            lineData.setDrawValues(false); // Hide values on points for cleaner look

            // Configure chart appearance
            chartTabungan.setData(lineData);
            chartTabungan.setBackgroundColor(getColor(R.color.background));
            chartTabungan.setDrawGridBackground(false);
            chartTabungan.setTouchEnabled(true);
            chartTabungan.setPinchZoom(true);

            // Description
            Description description = new Description();
            description.setText(getString(R.string.savings_progress));
            description.setTextSize(12f);
            description.setTextColor(getColor(R.color.text_secondary));
            chartTabungan.setDescription(description);

            // X-axis
            XAxis xAxis = chartTabungan.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(getColor(R.color.text_secondary));
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);

            // Left Y-axis
            YAxis leftAxis = chartTabungan.getAxisLeft();
            leftAxis.setTextColor(getColor(R.color.text_secondary));
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return MoneyHelper.formatSimple((long) value);
                }
            });
            leftAxis.setGranularity(10000f); // Show labels every 10,000
            leftAxis.setAxisMinimum(0f); // Start from 0

            // Right Y-axis
            chartTabungan.getAxisRight().setEnabled(false);

            // Legend
            Legend legend = chartTabungan.getLegend();
            legend.setTextColor(getColor(R.color.text));
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // Animation
            chartTabungan.animateY(1000, Easing.EaseInOutQuad);

            chartTabungan.invalidate();
        } else {
            // Clear chart if no data
            chartTabungan.clear();
            chartTabungan.invalidate();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return false;
            lastClickTime = SystemClock.elapsedRealtime();

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, RiwayatActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_target) {
                startActivity(new Intent(this, TargetActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        btnTambah.setOnClickListener(v -> showTransactionDialog("masuk"));
        btnTarik.setOnClickListener(v -> showTransactionDialog("keluar"));
    }

    private void showTransactionDialog(String jenis) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
        lastClickTime = SystemClock.elapsedRealtime();

        if (transactionDialog != null) {
            transactionDialog.dismiss();
        }

        transactionDialog = new TransactionDialog(this, dbHelper, this);
        transactionDialog.show(jenis);
    }

    private void updateTotalSaldo() {
        long balance = dbHelper.getTotalSaldo();
        tvTotalSaldo.setText(MoneyHelper.formatSimple(balance));
    }

    @Override
    public void onTransactionAdded() {
        refreshData();
        NotifHelper.showNotification(
                this,
                1003,
                getString(R.string.transaction_success_title),
                getString(R.string.transaction_success_message)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (transactionDialog != null) {
            transactionDialog.dismiss();
            transactionDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transactionDialog != null) {
            transactionDialog.dismiss();
        }
    }
}