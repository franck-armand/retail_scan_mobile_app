package com.maf.mafscan;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ScanSession",
        indices = {@Index(value = {"sessionId"})} // To easily access the scannedSessionId
)
public class ScanSession {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "sessionId")
    public String sessionId;
    @ColumnInfo(name = "sessionType")
    public String sessionType;
    @ColumnInfo(name="sessionNumber")
    public String sessionNumber;
    @ColumnInfo(name="sessionCreationDate")
    public String sessionCreationDate;
    @ColumnInfo(name="sessionCreationBy")
    public String sessionCreationBy;
    @ColumnInfo(name="sessionUpdatedDate")
    public String sessionUpdatedDate;
    @ColumnInfo(name="sessionUpdatedBy")
    public String sessionUpdatedBy;


    public ScanSession() {
        sessionId = "";
    }
}
