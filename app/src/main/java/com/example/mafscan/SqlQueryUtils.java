package com.example.mafscan;

public class SqlQueryUtils {
    public static final String INSERT_SCAN_SESSION = "INSERT INTO Scan_Session (" +
            "Session_Id, " +
            "Session_Type, " +
            "Loc_Id_From, " +
            "Loc_Id_To, " +
            "Session_CreationDate) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String INSERT_SCAN_READING = "INSERT INTO Scan_Reading (" +
            "Scan_Value, " +
            "Scan_Type, " +
            "Scan_Qty, " +
            "Scan_DateUTC, " +
            "Scan_DeviceSerialNumber, " +
            "Loc_Id_From, " +
            "Loc_Id_To, " +
            "Session_Id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_SCAN_SESSION = "UPDATE Scan_Session" +
            " SET Session_IsActive = 1 WHERE Session_Id = ?";
}
