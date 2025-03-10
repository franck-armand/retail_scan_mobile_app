package com.example.mafscan;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanSessionAdapter extends ListAdapter<ScanSession, ScanSessionViewHolder> {

    private final FailedOrSavedScanRepository repository;
    private final Map<String, Observer<List<ScanRecord>>> observers = new HashMap<>();

    public ScanSessionAdapter(Application application) {
        super(DIFF_CALLBACK);
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
        LiveData<List<ScanRecord>> currentScanRecords = repository.getScanRecordsBySessionId(currentScanSession.sessionId);

        Observer<List<ScanRecord>> observer = scanRecords -> {
            holder.bind(currentScanSession, scanRecords);
        };
        observers.put(currentScanSession.sessionId, observer);
        currentScanRecords.observe((LifecycleOwner) holder.itemView.getContext(), observer);
    }

    @Override
    public void onViewRecycled(@NonNull ScanSessionViewHolder holder) {
        super.onViewRecycled(holder);
        ScanSession currentScanSession = holder.currentScanSession;
        if (currentScanSession != null) {
            LiveData<List<ScanRecord>> currentScanRecords = repository.getScanRecordsBySessionId(currentScanSession.sessionId);
            Observer<List<ScanRecord>> observer = observers.remove(currentScanSession.sessionId);
            if (observer != null) {
                currentScanRecords.removeObserver(observer);
            }
        }
    }

    public void remove(ScanSession scanSession) {
        List<ScanSession> currentList = new ArrayList<>(getCurrentList());
        if (currentList.contains(scanSession)) {
            currentList.remove(scanSession);
            submitList(currentList);
        }
    }
}