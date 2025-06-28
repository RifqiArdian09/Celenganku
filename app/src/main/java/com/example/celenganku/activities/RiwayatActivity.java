package com.example.celenganku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.celenganku.R;
import com.example.celenganku.adapters.TransaksiAdapter;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class RiwayatActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvTransaksi;
    private TransaksiAdapter adapter;
    private MaterialToolbar toolbar;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        // Initialize views
        dbHelper = new DatabaseHelper(this);
        rvTransaksi = findViewById(R.id.rvTransaksi);
        toolbar = findViewById(R.id.toolbar);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Setup RecyclerView
        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));
        loadTransaksi();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.riwayat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_delete_all) {
            showDeleteAllConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTransaksi() {
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi();
        adapter = new TransaksiAdapter(transaksiList,
                transaksi -> {
                    // Handle item click if needed
                },
                transaksi -> {
                    // Handle delete click
                    showDeleteConfirmation(transaksi);
                });
        rvTransaksi.setAdapter(adapter);
    }

    private void showDeleteConfirmation(Transaksi transaksi) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dbHelper.deleteTransaksi(transaksi.getId());
                    loadTransaksi();
                    Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void showDeleteAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus semua transaksi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dbHelper.deleteAllTransaksi();
                    loadTransaksi();
                    Toast.makeText(this, "Semua transaksi dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return false;
            }
            lastClickTime = SystemClock.elapsedRealtime();

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_target) {
                startActivity(new Intent(this, TargetActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                // Already in history
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_history);
    }
}