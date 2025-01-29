package com.example.mafscan;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ScanRecord",
        indices = {
                @Index(value = {"scannedData"}), // To easily access the scannedData
                @Index(value = {"scanDate"}),  // For faster queries on date-based filtering
                @Index(value = {"isSentToServer"}), // For faster queries on unsent records
                @Index(value = {"fromLocationId", "toLocationId"}) // For queries involving locations
        }
)
public class ScanRecord {
    @PrimaryKey(autoGenerate = true)
    public int id; // Unique identifier for each scan record
    @NonNull
    public String scannedData; // The scanned data (e.g., (TYP)ART|(ART)3000407|(ARD)CALE DE TRANSPORT)
    @NonNull
    public String codeType; // The type of code (e.g., barcode, QR code)
    public float scanCount; // Number of scan quantity
    public String scanDate; // Date and time of the scan
    public String deviceSerialNumber; // Serial number of the device
    public String fromLocationId; // Source location ID
    public String toLocationId; // Destination location ID
    public String fromLocationName; // Name or label of the source location
    public String toLocationName; // Name or label of the destination location
    public String fromLocationCode; // Abbreviated destination location name
    public String toLocationCode; // Abbreviated provenance location name
    @ColumnInfo(defaultValue = "Not defined")
    public String scanUser; // User who performed the scan, will be defined in the future
    public int saveType; // Flag to indicate if the record has been saved to the database (0:auto-save, 1:manually save)
    @ColumnInfo(defaultValue = "0")
    public int isSentToServer; // Flag to indicate if the record has been sent to the server (0:not sent, 1:sent)
    @ColumnInfo(defaultValue = "0")
    public int sendAttemptCount; // Number of attempts to send the record to the server
    public String lastSendAttemptDate; // TEXT (ISO 8601 format) - To store the date and time of the last send attempt.
    public String sendError; // Error message if the record failed to send to the server

    public ScanRecord() {
        scannedData = "";
        codeType = "";
    }
}
