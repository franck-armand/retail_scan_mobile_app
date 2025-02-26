package com.example.mafscan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class FailedOrSavedScanViewModel extends AndroidViewModel {

    private FailedOrSavedScanRepository repository;
    private LiveData<List<ScanSession>> allScanSessions;

    public FailedOrSavedScanViewModel(@NonNull Application application) {
        super(application);
        repository = new FailedOrSavedScanRepository(application);
        allScanSessions = repository.getAllScanSessions();
    }

    public LiveData<List<ScanSession>> getAllScanSessions() {
        return allScanSessions;
    }
}