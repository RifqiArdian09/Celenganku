package com.example.celenganku.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.models.Target;
import com.example.celenganku.utils.DateHelper;
import com.example.celenganku.utils.MoneyHelper;
import java.util.List;

public class TargetAdapter extends RecyclerView.Adapter<TargetAdapter.ViewHolder> {
    private final List<Target> targetList;
    private final OnTargetClickListener listener;

    public interface OnTargetClickListener {
        void onAddSavingsClick(Target target);
    }

    public TargetAdapter(List<Target> targetList, OnTargetClickListener listener) {
        this.targetList = targetList;
        this.listener = listener;
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
        holder.tvNamaTarget.setText(target.getNama());
        holder.progressBar.setProgress(target.getProgress());

        String progressText = target.getProgress() + "% (" +
                MoneyHelper.formatSimple(target.getTerkumpul()) + "/" +
                MoneyHelper.formatSimple(target.getTargetNominal()) + ")";
        holder.tvProgress.setText(progressText);
        holder.tvTargetDate.setText(DateHelper.formatForDisplay(target.getTargetDate()));

        holder.btnTambahTabungan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddSavingsClick(target);
            }
        });
    }

    @Override
    public int getItemCount() {
        return targetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvNamaTarget;
        public final ProgressBar progressBar;
        public final TextView tvProgress;
        public final TextView tvTargetDate;
        public final Button btnTambahTabungan;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNamaTarget = itemView.findViewById(R.id.tvNamaTarget);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
            btnTambahTabungan = itemView.findViewById(R.id.btnTambahTabungan);
        }
    }
}