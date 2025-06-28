// TransactionDialog.java
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

    public interface OnTransactionSuccessListener {
        void onTransactionAdded();
    }

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final OnTransactionSuccessListener listener;
    private AlertDialog dialog;
    private Date selectedDate = new Date();

    public TransactionDialog(Context context, DatabaseHelper dbHelper, OnTransactionSuccessListener listener) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void show(String defaultJenis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaksi, null);
        builder.setView(dialogView);

        // Initialize views
        RadioGroup rgJenis = dialogView.findViewById(R.id.rgJenis);
        EditText etNominal = dialogView.findViewById(R.id.etNominal);
        EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
        Button btnPilihTanggal = dialogView.findViewById(R.id.btnPilihTanggal);
        TextView tvTanggal = dialogView.findViewById(R.id.tvTanggal);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);

        // Set initial values
        tvTanggal.setText(DateHelper.formatForDisplay(selectedDate));
        rgJenis.check(defaultJenis.equals("masuk") ? R.id.rbMasuk : R.id.rbKeluar);

        // Date picker
        btnPilihTanggal.setOnClickListener(v -> showDatePicker(tvTanggal));

        // Cancel button
        btnBatal.setOnClickListener(v -> dismiss());

        // Save button
        btnSimpan.setOnClickListener(v -> saveTransaction(rgJenis, etNominal, etDeskripsi));

        dialog = builder.create();
        dialog.show();
    }

    private void showDatePicker(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        new DatePickerDialog(context, (view, year, month, day) -> {
            calendar.set(year, month, day);
            selectedDate = calendar.getTime();
            tvDate.setText(DateHelper.formatForDisplay(selectedDate));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction(RadioGroup rgJenis, EditText etNominal, EditText etDeskripsi) {
        try {
            String jenis = rgJenis.getCheckedRadioButtonId() == R.id.rbMasuk ? "masuk" : "keluar";
            int nominal = Integer.parseInt(etNominal.getText().toString());
            String deskripsi = etDeskripsi.getText().toString();

            if (validateInput(deskripsi, nominal)) {
                Transaksi transaksi = new Transaksi(0, jenis, nominal, deskripsi, selectedDate);
                if (dbHelper.addTransaksi(transaksi) != -1) {
                    if (listener != null) {
                        listener.onTransactionAdded();
                    }
                    dismiss();
                } else {
                    showError("Gagal menyimpan transaksi");
                }
            }
        } catch (NumberFormatException e) {
            showError("Nominal harus berupa angka");
        }
    }

    private boolean validateInput(String deskripsi, int nominal) {
        if (deskripsi.isEmpty()) {
            showError("Deskripsi tidak boleh kosong");
            return false;
        }
        if (nominal <= 0) {
            showError("Nominal harus lebih dari 0");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}