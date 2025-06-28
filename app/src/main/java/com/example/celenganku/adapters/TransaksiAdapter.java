package com.example.celenganku.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.DateHelper;
import com.example.celenganku.utils.MoneyHelper;
import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {
    private List<Transaksi> transaksiList;
    private OnItemClickListener itemClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Transaksi transaksi);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Transaksi transaksi);
    }

    public TransaksiAdapter(List<Transaksi> transaksiList,
                            OnItemClickListener itemClickListener,
                            OnDeleteClickListener deleteClickListener) {
        this.transaksiList = transaksiList;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi transaksi = transaksiList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTanggal.setText(DateHelper.formatForDisplay(transaksi.getTanggal()));
        holder.tvDeskripsi.setText(transaksi.getDeskripsi());

        String nominal = MoneyHelper.formatSimple(transaksi.getNominal());
        if (transaksi.getJenis().equals("masuk")) {
            holder.tvNominal.setText("+" + nominal);
            holder.tvNominal.setTextColor(context.getColor(R.color.income));
        } else {
            holder.tvNominal.setText("-" + nominal);
            holder.tvNominal.setTextColor(context.getColor(R.color.expense));
        }

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation(context, transaksi);
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(transaksi);
            }
        });
    }

    private void showDeleteConfirmation(Context context, Transaksi transaksi) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(transaksi);
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public void updateData(List<Transaksi> newTransaksiList) {
        transaksiList = newTransaksiList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTanggal, tvNominal, tvDeskripsi;
        public ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDelete.setColorFilter(itemView.getContext().getColor(R.color.error));
        }
    }
}