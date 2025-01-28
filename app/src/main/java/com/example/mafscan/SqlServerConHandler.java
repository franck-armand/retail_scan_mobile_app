package com.example.mafscan;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlServerConHandler {
    private static final String DB_URL = "jdbc:jtds:sqlserver://10.10.1.24:1433;" +
                                            "databaseName=ProductionProgress;" +
                                            "encrypt=true;" +
                                            "trustServerCertificate=true;";
    private static final String USER = "ScanDataLogicLogin";
    private static final String PASS = "scandatalogic";
    private static final String TAG = SqlServerConHandler.class.getSimpleName();

    public static Connection establishSqlServCon() throws SQLException {
        Connection connection;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            Log.d(TAG, "Connected to sql server successfully.");
            return connection;
        } catch (SQLException e) {
            Log.e(TAG,"Failed to connect to SQL Server: " + e.getMessage());
            throw e;
        }
    }
}
