// MainActivity.java
package com.example.celenganku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.celenganku.R;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.MoneyHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TransactionDialog.OnTransactionSuccessListener {

    private DatabaseHelper dbHelper;
    private LineChart chartTabungan;
    private Button btnTambah, btnTarik;
    private TextView tvTotalSaldo;
    private TransactionDialog transactionDialog;
    private long lastClickTime = 0;

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

        // Setup components
        setupChart();
        setupBottomNavigation();
        setupButtons();
        updateTotalSaldo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        updateTotalSaldo();
        setupChart();
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
            LineDataSet dataSet = new LineDataSet(entries, "Perkembangan Tabungan");
            LineData lineData = new LineData(dataSet);
            chartTabungan.setData(lineData);
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
        tvTotalSaldo.setText(MoneyHelper.formatSimple(dbHelper.getTotalSaldo()));
    }

    @Override
    public void onTransactionAdded() {
        refreshData();
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