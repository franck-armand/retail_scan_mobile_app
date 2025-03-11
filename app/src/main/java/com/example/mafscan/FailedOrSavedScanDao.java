package com.example.mafscan;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface FailedOrSavedScanDao {

    @Transaction
    @Query("SELECT * FROM ScanSession WHERE sessionId IN" +
            " (SELECT DISTINCT sessionId FROM ScanRecord WHERE saveType = 0 OR saveType = 1)")
    LiveData<List<ScanSession>> getAllFailedOrSavedSessions();

    @Query("SELECT * FROM ScanRecord WHERE sessionId = :sessionId")
    LiveData<List<ScanRecord>> getScansForSession(String sessionId);

//    @Transaction
//    @Query("DELETE FROM ScanSession WHERE sessionId = :sessionId")
//    void deleteSessionWithScans(String sessionId);

//    @Query("UPDATE ScanRecord SET isSentToServer = 1 WHERE sessionId = :sessionId")
//    void markSessionAsSent(String sessionId);

//    @Query("UPDATE ScanRecord SET isSentToServer = 1 WHERE id = :scanId")
//    void markScanAsSent(int scanId);

//    @Insert
//    void insertScanSession(ScanSession scanSession);

//    @Insert
//    void insertScanRecord(ScanRecord scanRecord);

//    @Query("SELECT COUNT(DISTINCT sessionId) FROM ScanRecord WHERE saveType = 0 OR saveType = 1")
//    int getFailedOrSavedScanSessionsCount();
}