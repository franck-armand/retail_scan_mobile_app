package com.example.mafscan;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanSessionViewHolder extends RecyclerView.ViewHolder {
    TextView sessionIdTextView;
    TextView sessionDateTextView;
    TextView scanCountTextView;
    ImageView expandCollapseImageView;
    RecyclerView scansRecyclerView;
    Button resendButton;
    Button deleteButton;
    View statusIndicatorView;
    ScanSession currentScanSession;
    private final ScanRecordAdapter scanRecordAdapter;
    private final String TAG = getClass().getSimpleName();
    private boolean isExpanded = false;
    private final FailedOrSavedScanRepository repository;
    private final Context context;
    private final ScanSessionAdapter adapter;

    public ScanSessionViewHolder(@NonNull View itemView, Context context, FailedOrSavedScanRepository repository, ScanSessionAdapter adapter) {
        super(itemView);
        this.context = context;
        this.repository = repository;
        this.adapter = adapter;
        sessionIdTextView = itemView.findViewById(R.id.sessionIdTextView);
        sessionDateTextView = itemView.findViewById(R.id.sessionDateTextView);
        scanCountTextView = itemView.findViewById(R.id.scanCountTextView);
        expandCollapseImageView = itemView.findViewById(R.id.expandCollapseImageView);
        scansRecyclerView = itemView.findViewById(R.id.scansRecyclerView);
        resendButton = itemView.findViewById(R.id.resendButton);
        deleteButton = itemView.findViewById(R.id.deleteButton);
        statusIndicatorView = itemView.findViewById(R.id.statusIndicatorView);
        scansRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        scanRecordAdapter = new ScanRecordAdapter();
        scansRecyclerView.setAdapter(scanRecordAdapter);
        itemView.setOnClickListener(v -> toggleExpandCollapse());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    public void bind(ScanSession scanSession, List<ScanRecord> scanRecords) {
        currentScanSession = scanSession;
        sessionIdTextView.setText(scanSession.sessionId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        try {
            Date date = dateFormat.parse(scanSession.sessionCreationDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a",
                    Locale.getDefault());
            sessionDateTextView.setText(outputFormat.format(date));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }
        scanCountTextView.setText(context.getString(R.string.scan_count, scanRecords.size()));
        scanRecordAdapter.submitList(scanRecords);
        boolean isFailed = false;
        boolean isSaved = false;
        for (ScanRecord scanRecord : scanRecords) {
            if (scanRecord.saveType == 0) {
                isFailed = true;
            } else if (scanRecord.saveType == 1) {
                isSaved = true;
            }
        }
        if (isFailed) {
            statusIndicatorView.setBackgroundColor(Color.RED);
        } else if (isSaved) {
            statusIndicatorView.setBackgroundColor(Color.BLUE);
        }
        updateUI();
    }

    private void toggleExpandCollapse() {
        isExpanded = !isExpanded;
        updateUI();
    }

    private void updateUI() {
        if (isExpanded) {
            scansRecyclerView.setVisibility(View.VISIBLE);
            expandCollapseImageView.setImageResource(R.drawable.ic_expand_less);
        } else {
            scansRecyclerView.setVisibility(View.GONE);
            expandCollapseImageView.setImageResource(R.drawable.ic_expand_more);
        }
    }

    private void showDeleteConfirmationDialog() {
        DialogUtils.showInvalidSelectionDialog(
            context,
            "Delete Scan Session",
            "Are you sure you want to delete this session and all its records?",
            "Yes",
            (dialog, which) -> deleteScanSession(),
            "No",
            (dialog, which) -> dialog.dismiss()
        );
    }

    private void deleteScanSession() {
        repository.deleteScanSessionWithRecordsCallBack(currentScanSession, () -> {
                ((FailedOrSavedScanActivity) itemView.getContext()).runOnUiThread(() -> {
                adapter.remove(currentScanSession);
                ((FailedOrSavedScanActivity) itemView.getContext()).updateEmptyState();
            });
        });
    }

}