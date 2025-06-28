package com.example.celenganku.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.celenganku.R;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.DateHelper;
import java.util.Calendar;
import java.util.Date;

public class TambahActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RadioGroup rgJenis;
    private EditText etNominal, etDeskripsi;
    private TextView tvTanggal;
    private Date selectedDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi);

        dbHelper = new DatabaseHelper(this);
        rgJenis = findViewById(R.id.rgJenis);
        etNominal = findViewById(R.id.etNominal);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        tvTanggal = findViewById(R.id.tvTanggal);

        // Set default jenis based on intent
        String jenis = getIntent().getStringExtra("jenis");
        if (jenis != null) {
            if (jenis.equals("masuk")) {
                rgJenis.check(R.id.rbMasuk);
            } else {
                rgJenis.check(R.id.rbKeluar);
            }
        }

        tvTanggal.setText(DateHelper.formatForDisplay(selectedDate));

        setupDatePicker();
        setupSaveButton();
    }

    private void setupDatePicker() {
        Button btnPilihTanggal = findViewById(R.id.btnPilihTanggal);
        btnPilihTanggal.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);

            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                selectedDate = calendar.getTime();
                tvTanggal.setText(DateHelper.formatForDisplay(selectedDate));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupSaveButton() {
        Button btnSimpan = findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(v -> {
            try {
                String jenis = rgJenis.getCheckedRadioButtonId() == R.id.rbMasuk ? "masuk" : "keluar";
                int nominal = Integer.parseInt(etNominal.getText().toString());
                String deskripsi = etDeskripsi.getText().toString();

                if (deskripsi.isEmpty()) {
                    Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                Transaksi transaksi = new Transaksi(0, jenis, nominal, deskripsi, selectedDate);
                dbHelper.addTransaksi(transaksi);
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
            }
        });
    }
}