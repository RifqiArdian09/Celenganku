package com.example.celenganku.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.celenganku.R;
import com.example.celenganku.adapters.TargetAdapter;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Target;
import com.example.celenganku.utils.DateHelper;
import com.example.celenganku.utils.MoneyHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TargetActivity extends AppCompatActivity implements TargetAdapter.OnTargetClickListener {
    private DatabaseHelper dbHelper;
    private RecyclerView rvTarget;
    private TargetAdapter adapter;
    private TextView tvEmpty;
    private MaterialToolbar toolbar;

    // Dialog references
    private AlertDialog currentDialog;
    private AlertDialog savingsDialog;
    private Date selectedTargetDate;
    private Target currentTarget;
    private EditText etNamaTarget;
    private EditText etTargetNominal;
    private TextView tvTanggalTarget;
    private EditText etNominal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        rvTarget = findViewById(R.id.rvTarget);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvTarget.setLayoutManager(new LinearLayoutManager(this));
        loadTarget();

        // Setup FAB click listener
        FloatingActionButton fabAddTarget = findViewById(R.id.fabAddTarget);
        fabAddTarget.setOnClickListener(v -> showAddTargetDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadTarget() {
        List<Target> targetList = dbHelper.getAllTarget();
        if (targetList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTarget.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTarget.setVisibility(View.VISIBLE);
            adapter = new TargetAdapter(targetList, this, this::showDeleteConfirmation);
            rvTarget.setAdapter(adapter);
        }
    }

    private void showDeleteConfirmation(Target target) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Target")
                .setMessage("Apakah Anda yakin ingin menghapus target ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dbHelper.deleteTarget(target.getId());
                    loadTarget();
                    Toast.makeText(this, "Target dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void showAddTargetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_target, null);
        builder.setView(dialogView);

        etNamaTarget = dialogView.findViewById(R.id.etNamaTarget);
        etTargetNominal = dialogView.findViewById(R.id.etTargetAmount);
        tvTanggalTarget = dialogView.findViewById(R.id.tvTanggalTarget);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        selectedTargetDate = calendar.getTime();
        tvTanggalTarget.setText(DateHelper.formatForDisplay(selectedTargetDate));

        // Date picker button handled in XML
        currentDialog = builder.create();
        currentDialog.show();
    }

    // Called from XML onClick
    public void onPilihTanggalClick(View view) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (datePicker, year, month, day) -> {
            calendar.set(year, month, day);
            selectedTargetDate = calendar.getTime();
            tvTanggalTarget.setText(DateHelper.formatForDisplay(selectedTargetDate));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Called from XML onClick
    public void onSimpanTargetClick(View view) {
        String nama = etNamaTarget.getText().toString();
        String nominalStr = etTargetNominal.getText().toString();

        if (nama.isEmpty()) {
            etNamaTarget.setError("Nama target tidak boleh kosong");
            return;
        }

        try {
            int targetNominal = Integer.parseInt(nominalStr);
            if (targetNominal <= 0) {
                etTargetNominal.setError("Nominal harus lebih dari 0");
                return;
            }

            Target target = new Target(0, nama, targetNominal, 0, selectedTargetDate);
            dbHelper.addTarget(target);
            loadTarget();
            currentDialog.dismiss();
            Toast.makeText(this, "Target berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            etTargetNominal.setError("Nominal tidak valid");
        }
    }

    // Called from XML onClick
    public void onBatalClick(View view) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    @Override
    public void onAddSavingsClick(Target target) {
        currentTarget = target;
        if (target.getProgress() >= 100) {
            Toast.makeText(this, "Target sudah tercapai!", Toast.LENGTH_SHORT).show();
            return;
        }
        showAddSavingsDialog();
    }

    private void showAddSavingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_savings, null);
        builder.setView(dialogView);

        TextView tvTargetName = dialogView.findViewById(R.id.tvTargetName);
        TextView tvCurrentProgress = dialogView.findViewById(R.id.tvCurrentProgress);
        etNominal = dialogView.findViewById(R.id.etNominal);

        tvTargetName.setText(currentTarget.getNama());
        String progressText = "Terkumpul: " + MoneyHelper.formatSimple(currentTarget.getTerkumpul()) +
                " dari " + MoneyHelper.formatSimple(currentTarget.getTargetNominal());
        tvCurrentProgress.setText(progressText);

        savingsDialog = builder.create();
        savingsDialog.show();
    }

    // Called from XML onClick
    public void onSimpanTabunganClick(View view) {
        String nominalStr = etNominal.getText().toString();

        if (nominalStr.isEmpty()) {
            etNominal.setError("Jumlah tidak boleh kosong");
            return;
        }

        try {
            int nominal = Integer.parseInt(nominalStr);
            if (nominal <= 0) {
                etNominal.setError("Jumlah harus lebih dari 0");
                return;
            }

            int newTerkumpul = currentTarget.getTerkumpul() + nominal;
            dbHelper.updateTerkumpul(currentTarget.getId(), newTerkumpul);

            Target updatedTarget = dbHelper.getTarget(currentTarget.getId());
            if (updatedTarget != null && updatedTarget.getProgress() >= 100) {
                showCompletionDialog(updatedTarget);
            }

            loadTarget();
            savingsDialog.dismiss();
            Toast.makeText(this, "Tabungan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            etNominal.setError("Jumlah tidak valid");
        }
    }

    // Called from XML onClick
    public void onBatalTabunganClick(View view) {
        if (savingsDialog != null && savingsDialog.isShowing()) {
            savingsDialog.dismiss();
        }
    }

    private void showCompletionDialog(Target target) {
        new AlertDialog.Builder(this)
                .setTitle("Selamat!")
                .setMessage("Target " + target.getNama() + " telah tercapai!")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        if (savingsDialog != null && savingsDialog.isShowing()) {
            savingsDialog.dismiss();
        }
        super.onDestroy();
    }
}