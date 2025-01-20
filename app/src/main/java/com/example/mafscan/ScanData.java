package com.example.mafscan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanData {
    private final String scannedData;
    private final String codeType;
    private final Date scanDate;
    private int quantity;

    public ScanData(String scannedData, String codeType, Date scanDate) {
        this.scannedData = scannedData;
        this.codeType = codeType;
        this.scanDate = scanDate;
        this.quantity = 1; // Default quantity to 1
    }

    public String getScannedData() {
        return scannedData;
    }

    public String getCodeType() {
        return codeType;
    }

    public String getFormattedScanDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.FRANCE);
        return dateFormat.format(scanDate);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
