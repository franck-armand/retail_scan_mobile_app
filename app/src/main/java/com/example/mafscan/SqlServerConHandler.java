package com.example.mafscan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlServerConHandler {
    private static final String DB_URL = "jdbc:jtds:sqlserver://10.10.1.24:1433;" +
                                            "databaseName=ProductionProgress;" +
                                            "encrypt=true;" +
                                            "trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "sqlserveur2022maf";

    public static Connection establishSqlServCon() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to sql server successfully.");
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to connect to SQL Server: " + e.getMessage());
            throw e;
        }
    }
}
