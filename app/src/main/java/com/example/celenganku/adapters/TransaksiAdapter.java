package com.example.celenganku.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.celenganku.R;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.DateHelper;
import com.example.celenganku.utils.MoneyHelper;
import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {
    private List<Transaksi> transaksiList;

    public TransaksiAdapter(List<Transaksi> transaksiList) {
        this.transaksiList = transaksiList;
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

        holder.tvTanggal.setText(DateHelper.formatForDisplay(transaksi.getTanggal()));
        holder.tvDeskripsi.setText(transaksi.getDeskripsi());

        String nominal = MoneyHelper.formatSimple(transaksi.getNominal());
        if (transaksi.getJenis().equals("masuk")) {
            holder.tvNominal.setText("+" + nominal);
            holder.tvNominal.setTextColor(holder.itemView.getContext().getColor(R.color.income));
        } else {
            holder.tvNominal.setText("-" + nominal);
            holder.tvNominal.setTextColor(holder.itemView.getContext().getColor(R.color.expense));
        }
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTanggal, tvNominal, tvDeskripsi;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
        }
    }
}