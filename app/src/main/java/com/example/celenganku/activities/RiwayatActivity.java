package com.example.celenganku.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.adapters.TransaksiAdapter;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import java.util.List;

public class RiwayatActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView rvTransaksi;
    private TransaksiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        dbHelper = new DatabaseHelper(this);
        rvTransaksi = findViewById(R.id.rvTransaksi);
        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));

        loadTransaksi();
    }

    private void loadTransaksi() {
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi();
        adapter = new TransaksiAdapter(transaksiList);
        rvTransaksi.setAdapter(adapter);
    }
}