package com.example.celenganku.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.adapters.TransaksiAdapter;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import android.widget.Toast;

public class RiwayatActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView rvTransaksi;
    private TransaksiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        // Initialize Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        rvTransaksi = findViewById(R.id.rvTransaksi);
        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));

        loadTransaksi();
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
            // Handle back button click
            finish();
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
                    // Handle item click (optional)
                },
                transaksi -> {
                    // Handle delete click
                    dbHelper.deleteTransaksi(transaksi.getId());
                    Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                    loadTransaksi(); // Refresh list
                });
        rvTransaksi.setAdapter(adapter);
    }

    private void showDeleteAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus semua transaksi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dbHelper.deleteAllTransaksi();
                    Toast.makeText(this, "Semua transaksi dihapus", Toast.LENGTH_SHORT).show();
                    loadTransaksi();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}