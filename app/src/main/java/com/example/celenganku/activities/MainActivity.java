package com.example.celenganku.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        chartTabungan = findViewById(R.id.chartTabungan);
        btnTambah = findViewById(R.id.btnTambah);
        btnTarik = findViewById(R.id.btnTarik);
        tvTotalSaldo = findViewById(R.id.tvTotalSaldo);

        setupChart();
        setupBottomNavigation();
        setupButtons();
        updateTotalSaldo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotalSaldo();
        setupChart();
    }

    private void setupChart() {
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi();
        List<Entry> entries = new ArrayList<>();

        float total = 0;
        for (int i = 0; i < transaksiList.size(); i++) {
            Transaksi t = transaksiList.get(i);
            if (t.getJenis().equals("masuk")) {
                total += t.getNominal();
            } else {
                total -= t.getNominal();
            }
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
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Already in MainActivity
                    return true;
                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(MainActivity.this, RiwayatActivity.class));
                    return true;
                } else if (id == R.id.nav_target) {
                    startActivity(new Intent(MainActivity.this, TargetActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupButtons() {
        btnTambah.setOnClickListener(v -> {
            new TransactionDialog(this, dbHelper, this).show("masuk");
        });

        btnTarik.setOnClickListener(v -> {
            new TransactionDialog(this, dbHelper, this).show("keluar");
        });
    }

    private void updateTotalSaldo() {
        int saldo = dbHelper.getTotalSaldo();
        tvTotalSaldo.setText(MoneyHelper.formatSimple(saldo));
    }

    @Override
    public void onTransactionAdded() {
        updateTotalSaldo();
        setupChart();
    }
}