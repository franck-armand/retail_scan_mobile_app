package com.example.mafscan;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ScanRecord",
        foreignKeys = @ForeignKey(entity = ScanSession.class,
                parentColumns = "sessionId",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index("sessionId"),
                @Index(value = {"fromLocationId", "toLocationId"}),
                @Index("isSentToServer"),
                @Index("scanDate"),
                @Index("scannedData")
        }
)
public class ScanRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id; // Unique identifier for each scan record
    @NonNull
    @ColumnInfo(name = "scannedData")
    public String scannedData; // The scanned data (e.g., (TYP)ART|(ART)3000407|(ARD)CALE DE TRANSPORT)
    @NonNull
    @ColumnInfo(name = "codeType")
    public String codeType; // The type of code (e.g., barcode, QR code)
    @ColumnInfo(name = "scanCount")
    public float scanCount; // Number of scan quantity
    @ColumnInfo(name = "scanDate")
    public String scanDate; // Date and time of the scan
    @ColumnInfo(name = "deviceSerialNumber")
    public String deviceSerialNumber; // Serial number of the device
    @ColumnInfo(name = "fromLocationId")
    public String fromLocationId; // Source location ID
    @ColumnInfo(name = "toLocationId")
    public String toLocationId; // Destination location ID
    @ColumnInfo(name = "fromLocationName")
    public String fromLocationName; // Name or label of the source location
    @ColumnInfo(name = "toLocationName")
    public String toLocationName; // Name or label of the destination location
    @ColumnInfo(name = "fromLocationCode")
    public String fromLocationCode; // Abbreviated destination location name
    @ColumnInfo(name = "toLocationCode")
    public String toLocationCode; // Abbreviated provenance location name
    @ColumnInfo(name = "saveType")
    public int saveType; // Flag to indicate if the record has been saved to the database (0:auto-save, 1:manually save)
    @ColumnInfo(defaultValue = "0")
    public int isSentToServer; // Flag to indicate if the record has been sent to the server (0:not sent, 1:sent)
    @ColumnInfo(defaultValue = "0")
    public int sendAttemptCount; // Number of attempts to send the record to the server
    @ColumnInfo(name = "sessionId")
    public String sessionId; // Foreign key to the ScanSession table
    @ColumnInfo(name = "scanUser",defaultValue = "Not defined")
    public String scanUser; // User who performed the scan, will be defined in the future
    @ColumnInfo(name = "lastSendAttemptDate")
    public String lastSendAttemptDate; // TEXT (ISO 8601 format) - To store the date and time of the last send attempt.
    @ColumnInfo(name = "sendError")
    public String sendError; // Error message if the record failed to send to the server

    public ScanRecord() {
        scannedData = "";
        codeType = "";
    }
}
