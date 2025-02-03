package com.example.mafscan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScanData {
    private final String scannedData;
    private final String codeType;
    private Date scanDate;
    private float scanCount;

    public ScanData(String scannedData, String codeType, Date scanDate) {
        this.scannedData = scannedData;
        this.codeType = codeType;
        this.scanDate = scanDate;
        this.scanCount = 1; // Default quantity to 1
    }

    public String getScannedData() {
        return scannedData;
    }

    public String getCodeType() {
        return codeType;
    }

    public String getFormattedScanDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(scanDate);
    }

    public float getScanCount() {
        return scanCount;
    }

    public void setScanCount(float scanCount) {
        this.scanCount = scanCount;
    }
    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }
}
