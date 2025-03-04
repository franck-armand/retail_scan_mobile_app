package com.example.mafscan;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScanRecordDao {

    //////////////Insert queries////////////////

    // Insert a single scan record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertScanRecord(ScanRecord scanRecord);

    // Insert multiple scan records
    @Insert
    List<Long> insertScanRecords(List<ScanRecord> scanRecords);

    //////////////Retrieve queries////////////////

    // Retrieve scan count by session ID
    @Query("SELECT COUNT(*) FROM ScanRecord WHERE sessionId = :sessionId")
    int getScanRecordCountForSession(String sessionId);

    // Retrieve all scan records
    @Query("SELECT * FROM ScanRecord")
    List<ScanRecord> getAllScanRecords();

    // Retrieve unsent scan records
    @Query("SELECT * FROM ScanRecord WHERE isSentToServer = 0")
    List<ScanRecord> getUnsentScanRecords();

    // Retrieve scan records by a specific date
    @Query("SELECT * FROM ScanRecord WHERE scanDate = :date")
    List<ScanRecord> getScanRecordsByDate(String date);

    // Retrieve scan records within a date range
    @Query("SELECT * FROM ScanRecord WHERE scanDate BETWEEN :startDate AND :endDate")
    List<ScanRecord> getScanRecordsByDateRange(String startDate, String endDate);

    @Query("SELECT * FROM ScanRecord LIMIT :limit OFFSET :offset")
    List<ScanRecord> getScanRecordsPaginated(int limit, int offset);

    @Query("SELECT * FROM ScanRecord WHERE scannedData = :scannedData AND sessionId = :sessionId")
    ScanRecord getScanRecordByScannedData(String scannedData, String sessionId);
    //////////////Update queries////////////////

    // Update a specific scan record
    @Update
    void update(ScanRecord scanRecord);

    // Update the isSentToServer flag
    @Query("UPDATE ScanRecord SET isSentToServer = 1," +
            " lastSendAttemptDate = :lastSendDate WHERE id = :id")
    void markAsSent(int id, String lastSendDate);

    // Update sendAttemptCount and lastSendAttemptDate for a specific record
    @Query("UPDATE ScanRecord SET sendAttemptCount = sendAttemptCount + 1," +
            " lastSendAttemptDate = :lastSendDate WHERE id = :id")
    void updateSendAttempt(int id, String lastSendDate);

    //////////////Delete queries////////////////

    // Delete a specific scan record
    @Delete
    void deleteScanRecord(ScanRecord scanRecord);

    // Delete all scan records
    @Query("DELETE FROM ScanRecord")
    void deleteAllScanRecords();

    // Delete a scan record by its ID
    @Query("DELETE FROM ScanRecord WHERE id = :id")
    void deleteById(int id);

    // Delete all records marked as sent to the server
    @Query("DELETE FROM ScanRecord WHERE isSentToServer = 1")
    void deleteSentRecords();

    // Delete a record by its scanned data and date
    @Query("DELETE FROM ScanRecord WHERE scannedData = :scannedData AND scanDate = :scanDate")
    void deleteByScannedDataAndDate(String scannedData, String scanDate);
}
