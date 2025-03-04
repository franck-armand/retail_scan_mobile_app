package com.example.mafscan;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FailedOrSavedScanRepository {

    private ScanSessionDao scanSessionDao;
    private ScanRecordDao scanRecordDao;
    private FailedOrSavedScanDao failedOrSavedScanDao;
    private LiveData<List<ScanSession>> allFailedOrSavedSessions;

    public FailedOrSavedScanRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        scanSessionDao = db.scanSessionDao();
        scanRecordDao = db.scanRecordDao();
        failedOrSavedScanDao = db.failedOrSavedScanDao();
        allFailedOrSavedSessions = failedOrSavedScanDao.getAllFailedOrSavedSessions();
    }

    // Methods for ScanSession
    public LiveData<List<ScanSession>> getAllScanSessions() {
        return allFailedOrSavedSessions;
    }

    public ScanSession getScanSessionById(String sessionId) {
        return scanSessionDao.getScanSessionById(sessionId);
    }

    public void insertScanSession(ScanSession scanSession) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanSessionDao.insertScanSession(scanSession);
        });
    }

    public void deleteScanSessionWithRecords(ScanSession scanSession) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanSessionDao.deleteSessionWithScanBySessionId(scanSession.sessionId);
        });
    }

    public void deleteScanSessionWithRecordsCallBack(ScanSession scanSession, Runnable callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanSessionDao.deleteSessionWithScanBySessionId(scanSession.sessionId);
            if (callback != null) {
                callback.run();
            }
        });
    }

    // Methods for ScanRecord
    public LiveData<List<ScanRecord>> getScanRecordsBySessionId(String sessionId) {
        return failedOrSavedScanDao.getScansForSession(sessionId);
    }

    public List<ScanRecord> getAllScanRecords() {
        return scanRecordDao.getAllScanRecords();
    }

    public List<ScanRecord> getUnsentScanRecords() {
        return scanRecordDao.getUnsentScanRecords();
    }

    public List<ScanRecord> getScanRecordsByDate(String date) {
        return scanRecordDao.getScanRecordsByDate(date);
    }

    public List<ScanRecord> getScanRecordsByDateRange(String startDate, String endDate) {
        return scanRecordDao.getScanRecordsByDateRange(startDate, endDate);
    }

    public List<ScanRecord> getScanRecordsPaginated(int limit, int offset) {
        return scanRecordDao.getScanRecordsPaginated(limit, offset);
    }

    public ScanRecord getScanRecordByScannedData(String scannedData, String sessionId) {
        return scanRecordDao.getScanRecordByScannedData(scannedData, sessionId);
    }

    public void updateScanRecord(ScanRecord scanRecord) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.update(scanRecord);
        });
    }

    public void markAsSent(int id, String lastSendDate) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.markAsSent(id, lastSendDate);
        });
    }

    public void updateSendAttempt(int id, String lastSendDate) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.updateSendAttempt(id, lastSendDate);
        });
    }

    public void deleteScanRecord(ScanRecord scanRecord) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.deleteScanRecord(scanRecord);
        });
    }

    public void deleteAllScanRecords() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.deleteAllScanRecords();
        });
    }

    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.deleteById(id);
        });
    }

    public void deleteSentRecords() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.deleteSentRecords();
        });
    }

    public void deleteByScannedDataAndDate(String scannedData, String scanDate) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scanRecordDao.deleteByScannedDataAndDate(scannedData, scanDate);
        });
    }

    public long insertScanRecord(ScanRecord scanRecord) {
        return scanRecordDao.insertScanRecord(scanRecord);
    }

    public List<Long> insertScanRecords(List<ScanRecord> scanRecords) {
        return scanRecordDao.insertScanRecords(scanRecords);
    }

    // Methods for FailedOrSavedScan
    public void deleteSessionWithScans(String sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            failedOrSavedScanDao.deleteSessionWithScans(sessionId);
        });
    }

    public void markSessionAsSent(String sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            failedOrSavedScanDao.markSessionAsSent(sessionId);
        });
    }

    public void markScanAsSent(int scanId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            failedOrSavedScanDao.markScanAsSent(scanId);
        });
    }

    public int getFailedOrSavedScanSessionsCount() {
        return failedOrSavedScanDao.getFailedOrSavedScanSessionsCount();
    }
}