package com.example.mafscan;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import java.util.List;

public class ScanSessionAdapter extends ListAdapter<ScanSession, ScanSessionViewHolder> {

    private final FailedOrSavedScanRepository repository;
    private Application application;

    public ScanSessionAdapter(Application application) {
        super(DIFF_CALLBACK);
        this.application = application;
        repository = new FailedOrSavedScanRepository(application);
    }

    private static final DiffUtil.ItemCallback<ScanSession>
            DIFF_CALLBACK = new DiffUtil.ItemCallback<ScanSession>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanSession oldItem, @NonNull ScanSession newItem) {
            return oldItem.sessionId.equals(newItem.sessionId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanSession oldItem, @NonNull ScanSession newItem) {
            return oldItem.sessionCreationDate.equals(newItem.sessionCreationDate);
        }
    };

    @NonNull
    @Override
    public ScanSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_session, parent, false);
        return new ScanSessionViewHolder(itemView, parent.getContext(), repository, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanSessionViewHolder holder, int position) {
        ScanSession currentScanSession = getItem(position);
        LiveData<List<ScanRecord>> currentScanRecords =
                repository.getScanRecordsBySessionId(currentScanSession.sessionId);
        currentScanRecords.observeForever(scanRecords -> {
            holder.bind(currentScanSession, scanRecords);
        });
    }
    public void removeAt(int position) {
        submitList(getCurrentList().subList(0, position));
        notifyItemRemoved(position);
    }
    public int getPosition(ScanSession scanSession) {
        return getCurrentList().indexOf(scanSession);
    }
}