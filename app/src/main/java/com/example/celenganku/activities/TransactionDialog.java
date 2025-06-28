package com.example.celenganku.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.celenganku.R;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.DateHelper;

import java.util.Calendar;
import java.util.Date;

public class TransactionDialog {

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final OnTransactionSuccessListener listener;
    private AlertDialog dialog;
    private Date selectedDate = new Date();

    public interface OnTransactionSuccessListener {
        void onTransactionAdded();
    }

    public TransactionDialog(Context context, DatabaseHelper dbHelper, OnTransactionSuccessListener listener) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void show(String defaultJenis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaksi, null);
        builder.setView(dialogView);

        RadioGroup rgJenis = dialogView.findViewById(R.id.rgJenis);
        EditText etNominal = dialogView.findViewById(R.id.etNominal);
        EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
        Button btnPilihTanggal = dialogView.findViewById(R.id.btnPilihTanggal);
        TextView tvTanggal = dialogView.findViewById(R.id.tvTanggal);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);

        // Set default date
        tvTanggal.setText(DateHelper.formatForDisplay(selectedDate));

        // Set default jenis
        if (defaultJenis != null) {
            if (defaultJenis.equals("masuk")) {
                rgJenis.check(R.id.rbMasuk);
            } else {
                rgJenis.check(R.id.rbKeluar);
            }
        }

        // Date picker setup
        btnPilihTanggal.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);

            new DatePickerDialog(context, (view, year, month, day) -> {
                calendar.set(year, month, day);
                selectedDate = calendar.getTime();
                tvTanggal.setText(DateHelper.formatForDisplay(selectedDate));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Cancel button
        btnBatal.setOnClickListener(v -> dismiss());

        // Save button
        btnSimpan.setOnClickListener(v -> {
            try {
                String jenis = rgJenis.getCheckedRadioButtonId() == R.id.rbMasuk ? "masuk" : "keluar";
                int nominal = Integer.parseInt(etNominal.getText().toString());
                String deskripsi = etDeskripsi.getText().toString();

                if (deskripsi.isEmpty()) {
                    Toast.makeText(context, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (nominal <= 0) {
                    Toast.makeText(context, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Transaksi transaksi = new Transaksi(0, jenis, nominal, deskripsi, selectedDate);
                dbHelper.addTransaksi(transaksi);

                if (listener != null) {
                    listener.onTransactionAdded();
                }
                dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}