package com.example.mafscan;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlServerConHandler {
    private static final String DB_URL = BuildConfig.DB_URL;
    private static final String USER = BuildConfig.DB_USER;
    private static final String PASS = BuildConfig.DB_PASS;
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
