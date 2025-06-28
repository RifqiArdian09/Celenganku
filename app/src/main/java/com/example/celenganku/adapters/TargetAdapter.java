package com.example.celenganku.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.models.Target;
import com.example.celenganku.utils.DateHelper;
import com.example.celenganku.utils.MoneyHelper;
import java.util.List;

public class TargetAdapter extends RecyclerView.Adapter<TargetAdapter.ViewHolder> {
    private final List<Target> targetList;
    private final OnTargetClickListener listener;
    private final OnDeleteClickListener deleteListener;

    public interface OnTargetClickListener {
        void onAddSavingsClick(Target target);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Target target);
    }

    public TargetAdapter(List<Target> targetList,
                         OnTargetClickListener listener,
                         OnDeleteClickListener deleteListener) {
        this.targetList = targetList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_target, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Target target = targetList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvNamaTarget.setText(target.getNama());
        holder.progressBar.setProgress(target.getProgress());

        String progressText = target.getProgress() + "% (" +
                MoneyHelper.formatSimple(target.getTerkumpul()) + "/" +
                MoneyHelper.formatSimple(target.getTargetNominal()) + ")";
        holder.tvProgress.setText(progressText);
        holder.tvTargetDate.setText(DateHelper.formatForDisplay(target.getTargetDate()));

        // Change button color if target is completed
        if (target.getProgress() >= 100) {
            holder.btnTambahTabungan.setBackgroundColor(
                    context.getColor(R.color.success));
            holder.btnTambahTabungan.setText("Target Selesai");
        } else {
            holder.btnTambahTabungan.setBackgroundColor(
                    context.getColor(R.color.accent));
            holder.btnTambahTabungan.setText("Tambah Tabungan");
        }

        holder.btnTambahTabungan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddSavingsClick(target);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation(context, target);
        });
    }

    private void showDeleteConfirmation(Context context, Target target) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Target")
                .setMessage("Apakah Anda yakin ingin menghapus target ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(target);
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return targetList.size();
    }

    public void updateData(List<Target> newTargetList) {
        targetList.clear();
        targetList.addAll(newTargetList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvNamaTarget;
        public final ProgressBar progressBar;
        public final TextView tvProgress;
        public final TextView tvTargetDate;
        public final Button btnTambahTabungan;
        public final ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNamaTarget = itemView.findViewById(R.id.tvNamaTarget);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
            btnTambahTabungan = itemView.findViewById(R.id.btnTambahTabungan);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}