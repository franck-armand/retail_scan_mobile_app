package com.example.mafscan;

import android.content.Context;
import android.util.Log;

import com.keyence.autoid.sdk.scan.DecodeResult;
import com.keyence.autoid.sdk.scan.ScanManager;

public class KeyenceUtils implements ScanManager.DataListener {
    private static ScanManager mScanManager;
    private static ScanListener mScanListener;

    public interface ScanListener {
        void onScan(String scannedData);
    }

    public static boolean initializeScanner(Context context) {
        if (mScanManager == null) {
            mScanManager = ScanManager.createScanManager(context);
            if (mScanManager != null) {
                mScanManager.addDataListener(new KeyenceUtils());
                return true;
            } else {
                Log.e("KeyenceUtils", "Failed to initialize scanner");
                return false;
            }
        }
        return true;
    }

    public static void setScanListener(Context context, ScanListener listener) {
        mScanListener = listener;
    }

    public static void startScanning(Context context) {
        if (mScanManager != null) {
            mScanManager.startRead();
        }
    }

    public static void stopScanning(Context context) {
        if (mScanManager != null) {
            mScanManager.stopRead();
        }
    }

    public static void releaseScanner() {
        if (mScanManager != null) {
            mScanManager.releaseScanManager();
            mScanManager = null;
        }
    }

    @Override
    public void onDataReceived(DecodeResult decodeResult) {
        String scannedData = decodeResult.getData();
        if (mScanListener != null) {
            mScanListener.onScan(scannedData);
        }
    }
}