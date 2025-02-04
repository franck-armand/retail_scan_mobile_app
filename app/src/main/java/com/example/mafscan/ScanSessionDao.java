package com.example.mafscan;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ScanSessionDao {
    @Insert
    long insertScanSession(ScanSession scanSession);

    @Query("SELECT * FROM ScanSession WHERE sessionId = :sessionId")
    ScanSession getScanSessionById(String sessionId);
}
