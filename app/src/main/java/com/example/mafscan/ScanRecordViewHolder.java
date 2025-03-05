package com.example.mafscan;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanRecordViewHolder extends RecyclerView.ViewHolder {

    TextView scannedDataTextView;
    TextView codeTypeTextView;
    TextView scanDateTextView;
    TextView scanCountTextView;
    TextView scanStatusTextView;
    private final String TAG = getClass().getSimpleName();

    public ScanRecordViewHolder(@NonNull View itemView) {
        super(itemView);
        scannedDataTextView = itemView.findViewById(R.id.scannedDataTextView);
        codeTypeTextView = itemView.findViewById(R.id.codeTypeTextView);
        scanDateTextView = itemView.findViewById(R.id.scanDateTextView);
        scanCountTextView = itemView.findViewById(R.id.scanCountTextView);
        scanStatusTextView = itemView.findViewById(R.id.scanStatusTextView);
    }

    public void bind(ScanRecord scanRecord) {
        scannedDataTextView.setText(scanRecord.scannedData);
        codeTypeTextView.setText(scanRecord.codeType);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS",
                Locale.getDefault());
        try {
            Date date = dateFormat.parse(scanRecord.scanDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS",
                    Locale.getDefault());
            assert date != null;
            scanDateTextView.setText(outputFormat.format(date));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }
        scanCountTextView.setText(String.valueOf(scanRecord.scanCount));
        if (scanRecord.isSentToServer == 1) {
            scanStatusTextView.setText("Sent");
        } else {
            scanStatusTextView.setText("Not Sent");
        }
    }
}