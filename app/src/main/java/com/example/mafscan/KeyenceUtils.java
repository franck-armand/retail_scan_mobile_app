package com.example.mafscan;

import android.content.Context;
import android.util.Log;

import com.keyence.autoid.sdk.scan.DecodeResult;
import com.keyence.autoid.sdk.scan.ScanManager;

public class KeyenceUtils implements ScanManager.DataListener {
    private static ScanManager mScanManager;
    private static ScanListener mScanListener;
    private static final String TAG = "KeyenceUtils";
    public interface ScanListener {
        void onScan(String scannedData);
    }

    public static boolean initializeScanner(Context context) {
        if (mScanManager == null) {
            mScanManager = ScanManager.createScanManager(context);
            if (mScanManager != null) {
                mScanManager.addDataListener(new KeyenceUtils());
                Log.d(TAG, "Scanner initialized successfully");
                return true;
            } else {
                Log.e(TAG, "Failed to initialize scanner");
                return false;
            }
        }
        return true;
    }

    public static void setScanListener(ScanListener listener) {
        mScanListener = listener;
    }

    public static void stopScanning() {
        if (mScanManager != null) {
            Log.d(TAG, "Stopping scanning");
            mScanManager.stopRead();
        } else{
            Log.e(TAG, "Scanner not initialized");
        }
    }

    public static void releaseScanner() {
        if (mScanManager != null) {
            Log.d(TAG, "Releasing scanner");
            mScanManager.releaseScanManager();
            mScanManager = null;
        } else{
            Log.e(TAG, "Scanner not initialized");
        }
    }

    @Override
    public void onDataReceived(DecodeResult decodeResult) {
        String scannedData = decodeResult.getData();
        Log.d(TAG, "OnDataReceived: " + scannedData);
        if (mScanListener != null) {
            mScanListener.onScan(scannedData);
        }
    }
}