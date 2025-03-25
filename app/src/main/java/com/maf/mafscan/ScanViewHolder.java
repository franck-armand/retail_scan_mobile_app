package com.maf.mafscan;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ScanViewHolder extends RecyclerView.ViewHolder {
    public ImageView iconCheck;
    public TextView textScannedData;
    public TextView textScanType;
    public TextView textScanDate;
    public TextView textQuantity;

    public ScanViewHolder(View itemView) {
        super(itemView);
        iconCheck = itemView.findViewById(R.id.icon_check);
        textScannedData = itemView.findViewById(R.id.text_scanned_data);
        textScanType = itemView.findViewById(R.id.text_scan_type);
        textScanDate = itemView.findViewById(R.id.text_scan_date);
        textQuantity = itemView.findViewById(R.id.text_quantity);
    }
}
