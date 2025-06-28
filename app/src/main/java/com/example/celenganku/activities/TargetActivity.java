package com.example.celenganku.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.celenganku.R;
import com.example.celenganku.adapters.TargetAdapter;
import com.example.celenganku.database.DatabaseHelper;
import com.example.celenganku.models.Target;
import com.example.celenganku.models.Transaksi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TargetActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvTarget;
    private TargetAdapter adapter;
    private TextView tvEmpty;
    private Calendar targetDateCalendar = Calendar.getInstance();
    private AlertDialog currentDialog;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        dbHelper = new DatabaseHelper(this);
        rvTarget = findViewById(R.id.rvTarget);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAddTarget = findViewById(R.id.fabAddTarget);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvTarget.setLayoutManager(new LinearLayoutManager(this));
        setupBottomNavigation();
        loadTargets();

        fabAddTarget.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
            lastClickTime = SystemClock.elapsedRealtime();
            showAddTargetDialog();
        });
    }

    private void loadTargets() {
        List<Target> targetList = dbHelper.getAllTarget();
        if (targetList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTarget.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTarget.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new TargetAdapter(targetList,
                        this::showAddSavingsDialog,
                        this::showDeleteConfirmation);
                rvTarget.setAdapter(adapter);
            } else {
                adapter.updateData(targetList);
            }
        }
    }

    private void showAddTargetDialog() {
        if (isDialogShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_target, null);
        builder.setView(dialogView);

        currentDialog = builder.create();
        currentDialog.setCanceledOnTouchOutside(false);

        EditText etNamaTarget = dialogView.findViewById(R.id.etNamaTarget);
        EditText etTargetAmount = dialogView.findViewById(R.id.etTargetAmount);
        Button btnPilihTanggal = dialogView.findViewById(R.id.btnPilihTanggal);
        TextView tvTanggalTarget = dialogView.findViewById(R.id.tvTanggalTarget);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);

        targetDateCalendar = Calendar.getInstance();
        targetDateCalendar.add(Calendar.MONTH, 1);
        updateDateText(tvTanggalTarget);

        btnPilihTanggal.setOnClickListener(v -> showDatePickerDialog(tvTanggalTarget));

        btnBatal.setOnClickListener(v -> dismissDialog());

        btnSimpan.setOnClickListener(v -> {
            String nama = etNamaTarget.getText().toString().trim();
            String nominalStr = etTargetAmount.getText().toString().trim();

            if (nama.isEmpty()) {
                etNamaTarget.setError("Nama target harus diisi");
                return;
            }

            if (nominalStr.isEmpty()) {
                etTargetAmount.setError("Target nominal harus diisi");
                return;
            }

            try {
                int targetNominal = Integer.parseInt(nominalStr);
                if (targetNominal <= 0) {
                    etTargetAmount.setError("Nominal harus lebih dari 0");
                    return;
                }

                Target target = new Target(
                        0, nama, targetNominal, 0, targetDateCalendar.getTime()
                );

                long result = dbHelper.addTarget(target);
                if (result != -1) {
                    Toast.makeText(this, "Target berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    loadTargets();
                    dismissDialog();
                } else {
                    Toast.makeText(this, "Gagal menambahkan target", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                etTargetAmount.setError("Masukkan angka yang valid");
            }
        });

        showDialog();
    }

    private void showAddSavingsDialog(Target target) {
        if (isDialogShowing() || target.getTerkumpul() >= target.getTargetNominal()) {
            Toast.makeText(this, "Target sudah terpenuhi", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_savings, null);
        builder.setView(dialogView);

        currentDialog = builder.create();
        currentDialog.setCanceledOnTouchOutside(false);

        TextView tvTargetName = dialogView.findViewById(R.id.tvTargetName);
        TextView tvCurrentProgress = dialogView.findViewById(R.id.tvCurrentProgress);
        EditText etNominal = dialogView.findViewById(R.id.etNominal);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);

        int remaining = target.getTargetNominal() - target.getTerkumpul();

        tvTargetName.setText(target.getNama());
        tvCurrentProgress.setText(String.format(Locale.getDefault(),
                "Progress: Rp%,d dari Rp%,d\nSisa: Rp%,d",
                target.getTerkumpul(), target.getTargetNominal(), remaining));

        btnBatal.setOnClickListener(v -> dismissDialog());

        btnSimpan.setOnClickListener(v -> {
            String nominalStr = etNominal.getText().toString().trim();

            if (nominalStr.isEmpty()) {
                etNominal.setError("Jumlah tabungan harus diisi");
                return;
            }

            try {
                int nominal = Integer.parseInt(nominalStr);
                if (nominal <= 0) {
                    etNominal.setError("Jumlah harus lebih dari 0");
                    return;
                }

                if (nominal > remaining) {
                    etNominal.setError("Jumlah melebihi sisa target");
                    return;
                }

                int newTerkumpul = target.getTerkumpul() + nominal;
                int updated = dbHelper.updateTerkumpul(target.getId(), newTerkumpul);

                if (updated > 0) {
                    Transaksi transaksi = new Transaksi(
                            0, "masuk", nominal,
                            "Tabungan untuk " + target.getNama(), new Date()
                    );
                    dbHelper.addTransaksi(transaksi);

                    Toast.makeText(this, "Tabungan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    loadTargets();
                    dismissDialog();
                } else {
                    Toast.makeText(this, "Gagal menambahkan tabungan", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                etNominal.setError("Masukkan angka yang valid");
            }
        });

        showDialog();
    }

    private void showDeleteConfirmation(Target target) {
        if (isDialogShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus Target")
                .setMessage("Apakah Anda yakin ingin menghapus target ini?")
                .setPositiveButton("Ya", (d, which) -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
                    lastClickTime = SystemClock.elapsedRealtime();

                    int deleted = dbHelper.deleteTarget(target.getId());
                    if (deleted > 0) {
                        Toast.makeText(this, "Target berhasil dihapus", Toast.LENGTH_SHORT).show();
                        loadTargets();
                    } else {
                        Toast.makeText(this, "Gagal menghapus target", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", (d, which) -> d.dismiss())
                .setCancelable(false);

        currentDialog = builder.create();
        currentDialog.setCanceledOnTouchOutside(false);
        showDialog();
    }

    private boolean isDialogShowing() {
        return currentDialog != null && currentDialog.isShowing();
    }

    private void showDialog() {
        if (!isFinishing() && !isDestroyed() && !isDialogShowing()) {
            currentDialog.show();
        }
    }

    private void dismissDialog() {
        if (isDialogShowing()) {
            currentDialog.dismiss();
        }
    }

    private void showDatePickerDialog(TextView tvDate) {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    targetDateCalendar.set(Calendar.YEAR, year);
                    targetDateCalendar.set(Calendar.MONTH, month);
                    targetDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateText(tvDate);
                },
                targetDateCalendar.get(Calendar.YEAR),
                targetDateCalendar.get(Calendar.MONTH),
                targetDateCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateText(TextView textView) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        textView.setText(sdf.format(targetDateCalendar.getTime()));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return false;
            }
            lastClickTime = SystemClock.elapsedRealtime();

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_target) {
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, RiwayatActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_target);
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}