package com.example.mafscan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class ScanRecordAdapter extends ListAdapter<ScanRecord, ScanRecordViewHolder> {

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(ScanRecord scanRecord);
    }

    public ScanRecordAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ScanRecord> DIFF_CALLBACK = new DiffUtil.ItemCallback<ScanRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanRecord oldItem, @NonNull ScanRecord newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanRecord oldItem, @NonNull ScanRecord newItem) {
            return oldItem.isSentToServer == newItem.isSentToServer &&
                    oldItem.saveType == newItem.saveType &&
                    oldItem.scanCount == newItem.scanCount;
        }
    };

//    public void setOnItemClickListener(OnItemClickListener listener) {
//        mListener = listener;
//    }

    @NonNull
    @Override
    public ScanRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_record, parent, false);
        return new ScanRecordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanRecordViewHolder holder, int position) {
        ScanRecord currentScanRecord = getItem(position);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(currentScanRecord);
            }
        });
        holder.bind(currentScanRecord);
    }
}