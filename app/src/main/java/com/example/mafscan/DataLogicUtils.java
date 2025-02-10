package com.example.mafscan;

import android.content.Context;
import android.util.Log;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.DeviceException;
import com.datalogic.device.info.SYSTEM;

public class DataLogicUtils {
    private static BarcodeManager mBarcodeManager;
    private static ScanListener mScanListener;
    private static final String TAG = "DataLogicUtils";

    /**
     * Interface to allow external handling of scanned data.
     */
    public interface ScanListener {
        void onScan(String scannedData, String codeType);
    }

    /**
     * Initializes the BarcodeManager and sets up a ReadListener for scan events.
     *
     * @param context The application context.
     * @return true if initialization succeeds, false otherwise.
     */
    public static boolean initializeScanner(Context context) {
        if (mBarcodeManager == null) {
            try {
                mBarcodeManager = new BarcodeManager();
                mBarcodeManager.addReadListener(new ReadListener() {
                    @Override
                    public void onRead(DecodeResult readResult) {
                        String scannedData = readResult.getText();
                        String codeType = readResult.getBarcodeID().toString();
                        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);

                        // Pass the scan result to the external listener if set
                        if (mScanListener != null) {
                            mScanListener.onScan(scannedData, codeType);
                        }
                    }
                });
                Log.d(TAG, "BarcodeManager initialized successfully");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize BarcodeManager", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Sets an external listener to handle scanned data.
     *
     * @param listener An implementation of the ScanListener interface.
     */
    public static void setScanListener(ScanListener listener) {
        mScanListener = listener;
    }

    /**
     * Starts the decoding process.
     */
    public static void startScanning() {
        if (mBarcodeManager != null) {
            try {
                mBarcodeManager.startDecode();
                Log.d(TAG, "Decoding started");
            } catch (Exception e) {
                Log.e(TAG, "Error starting decoding", e);
            }
        } else {
            Log.e(TAG, "BarcodeManager not initialized");
        }
    }

    /**
     * Stops the decoding process.
     */
    public static void stopScanning() {
        if (mBarcodeManager != null) {
            try {
                mBarcodeManager.stopDecode();
                Log.d(TAG, "Decoding stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping decoding", e);
            }
        } else {
            Log.e(TAG, "BarcodeManager not initialized");
        }
    }

    /**
     * Releases the BarcodeManager resources.
     */
    public static void releaseScanner() {
        if (mBarcodeManager != null) {
            try {
                mBarcodeManager.release();
                mBarcodeManager = null;
                Log.d(TAG, "BarcodeManager released");
            } catch (Exception e) {
                Log.e(TAG, "Error releasing BarcodeManager", e);
            }
        } else {
            Log.e(TAG, "BarcodeManager not initialized");
        }
    }

    /**
     * Retrieves the device name suffix with device serial number.
     *
     * @return The device name suffix as a string.
     */
    public static String getDeviceInfo() {
        try {
            String deviceSerialNumber = SYSTEM.SERIAL_NUMBER;
            Log.d(TAG, " Serial Number: " + deviceSerialNumber);
            return deviceSerialNumber;
        } catch (DeviceException e) {
            Log.e(TAG, "Failed to retrieve device information", e);
            return "Unknown Device";
        }
    }

}
