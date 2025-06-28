package com.example.celenganku.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TargetActivity extends AppCompatActivity implements TargetAdapter.OnTargetClickListener {
    private DatabaseHelper dbHelper;
    private RecyclerView rvTarget;
    private TargetAdapter adapter;
    private TextView tvEmpty;

    // Dialog references
    private AlertDialog currentDialog;
    private AlertDialog savingsDialog;
    private Date selectedTargetDate;
    private Target currentTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        dbHelper = new DatabaseHelper(this);
        rvTarget = findViewById(R.id.rvTarget);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAddTarget = findViewById(R.id.fabAddTarget);

        rvTarget.setLayoutManager(new LinearLayoutManager(this));

        loadTarget();
        setupAddButton();
    }

    private void loadTarget() {
        List<Target> targetList = dbHelper.getAllTarget();
        if (targetList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTarget.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTarget.setVisibility(View.VISIBLE);
            adapter = new TargetAdapter(targetList, this);
            rvTarget.setAdapter(adapter);
        }
    }

    private void setupAddButton() {
        FloatingActionButton fabAddTarget = findViewById(R.id.fabAddTarget);
        fabAddTarget.setOnClickListener(v -> showAddTargetDialog());
    }

    private void showAddTargetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_target, null);
        builder.setView(dialogView);

        EditText etNamaTarget = dialogView.findViewById(R.id.etNamaTarget);
        EditText etTargetNominal = dialogView.findViewById(R.id.etTargetAmount);
        TextView tvTanggalTarget = dialogView.findViewById(R.id.tvTanggalTarget);
        Button btnPilihTanggal = dialogView.findViewById(R.id.btnPilihTanggal);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        selectedTargetDate = calendar.getTime();
        tvTanggalTarget.setText(DateHelper.formatForDisplay(selectedTargetDate));

        btnPilihTanggal.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                selectedTargetDate = calendar.getTime();
                tvTanggalTarget.setText(DateHelper.formatForDisplay(selectedTargetDate));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        currentDialog = builder.create();
        currentDialog.show();
    }

    public void onSimpanTargetClick(View view) {
        View dialogView = (View) view.getParent().getParent();
        EditText etNamaTarget = dialogView.findViewById(R.id.etNamaTarget);
        EditText etTargetNominal = dialogView.findViewById(R.id.etTargetAmount);

        if (etNamaTarget.getText().toString().isEmpty()) {
            etNamaTarget.setError("Nama target tidak boleh kosong");
            return;
        }

        try {
            int targetNominal = Integer.parseInt(etTargetNominal.getText().toString());
            if (targetNominal <= 0) {
                etTargetNominal.setError("Nominal harus lebih dari 0");
                return;
            }

            Target target = new Target(0,
                    etNamaTarget.getText().toString(),
                    targetNominal,
                    0,
                    selectedTargetDate);

            dbHelper.addTarget(target);
            loadTarget();

            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }

            Toast.makeText(this, "Target berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            etTargetNominal.setError("Nominal tidak valid");
        }
    }

    @Override
    public void onAddSavingsClick(Target target) {
        currentTarget = target;
        showAddSavingsDialog();
    }

    private void showAddSavingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_savings, null);
        builder.setView(dialogView);

        TextView tvTargetName = dialogView.findViewById(R.id.tvTargetName);
        tvTargetName.setText(currentTarget.getNama());

        savingsDialog = builder.create();
        savingsDialog.show();
    }

    public void onSimpanTabunganClick(View view) {
        View dialogView = (View) view.getParent().getParent();
        EditText etNominal = dialogView.findViewById(R.id.etNominal);

        try {
            int nominal = Integer.parseInt(etNominal.getText().toString());
            if (nominal <= 0) {
                etNominal.setError("Nominal harus lebih dari 0");
                return;
            }

            int newTerkumpul = currentTarget.getTerkumpul() + nominal;
            dbHelper.updateTerkumpul(currentTarget.getId(), newTerkumpul);
            loadTarget();

            if (savingsDialog != null && savingsDialog.isShowing()) {
                savingsDialog.dismiss();
            }

            Toast.makeText(this, "Tabungan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            etNominal.setError("Nominal tidak valid");
        }
    }

    public void onBatalClick(View view) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        if (savingsDialog != null && savingsDialog.isShowing()) {
            savingsDialog.dismiss();
        }
    }
}