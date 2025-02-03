package com.example.mafscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public class ScanDataAdapter extends RecyclerView.Adapter<ScanViewHolder> {
    private final List<ScanData> dataList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(ScanData scanData);
    }
    public ScanDataAdapter(LinkedList<ScanData> dataList, Context context) {
        this.dataList = dataList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan, parent,
                false);
        return new ScanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanData scanData = dataList.get(position);
        holder.textScannedData.setText(scanData.getScannedData());
        holder.textScanType.setText(scanData.getCodeType());
        holder.textScanDate.setText(scanData.getFormattedScanDate());
        holder.textQuantity.setText(String.valueOf(scanData.getScanCount()));
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(scanData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void updateItem(int position, ScanData scanData) {
        dataList.set(position, scanData);
        notifyItemChanged(position);
    }
}
