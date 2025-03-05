package com.example.mafscan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.Collections;
import java.util.List;

public class FailedOrSavedScanViewModel extends AndroidViewModel {

    private FailedOrSavedScanRepository repository;
    private LiveData<List<ScanSession>> allScanSessions;

    public FailedOrSavedScanViewModel(@NonNull Application application) {
        super(application);
        repository = new FailedOrSavedScanRepository(application);
        allScanSessions = Transformations.map(repository.getAllScanSessions(), scanSessions -> {
            // Sort the list by sessionCreationDate in descending order (newest first)
            scanSessions.sort((s1, s2) -> s2.sessionCreationDate.compareTo(s1.sessionCreationDate));
            return scanSessions;
        });
    }

    public LiveData<List<ScanSession>> getAllScanSessions() {
        return allScanSessions;
    }
}