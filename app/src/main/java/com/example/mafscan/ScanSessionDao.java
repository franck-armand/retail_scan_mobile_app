package com.example.mafscan;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ScanSessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertScanSession(ScanSession scanSession);

    @Query("SELECT * FROM ScanSession WHERE sessionId = :sessionId")
    ScanSession getScanSessionById(String sessionId);

    @Query("DELETE FROM ScanSession WHERE sessionId = :sessionId")
    void deleteSessionWithScanBySessionId(String sessionId);

    @Query("SELECT ss.* FROM ScanSession ss LEFT JOIN ScanRecord sr ON ss.sessionId = sr.sessionId WHERE sr.sessionId IS NULL")
    List<ScanSession> getScanSessionsWithoutRecords();

}
