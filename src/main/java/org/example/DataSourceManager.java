package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.stream.Collectors;

public class DataSourceManager {
    private String dbURL = "jdbc:sqlite:library.db";
    private Connection conn = null;

    public DataSourceManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbURL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.out.println("Error connecting: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error work with DB: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Work whith DB ended");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void executeSqlScript(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
            Statement statement = conn.createStatement()) {
            String sql = reader.lines().collect(Collectors.joining("\n"));
            statement.executeUpdate(sql);
            System.out.println("SQL script executed successfully.");
        } catch (IOException e) {
            System.out.println("Error reading SQL script: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error executing SQL script: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in work with SQL script: " + e.getMessage());
        }
    }

    public void selectQueryResult(String sqlQuery) {
        try (Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sqlQuery)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    public void executeInsert(String insertQuery) {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(insertQuery);
            System.out.println("Insert query executed successfully.");
        } catch (SQLException e) {
            System.out.println("Error executing insert: " + e.getMessage());
        }
    }

    public void deleteTable(String tableName) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate("PRAGMA foreign_keys = OFF");
            statement.executeUpdate("DROP TABLE IF EXISTS `" + tableName + "`");
            statement.executeUpdate("PRAGMA foreign_keys = ON");
            System.out.println("Table '" + tableName + "' delete successfully.");
        } catch (SQLException e) {
            System.out.println("Error delete table '" + tableName + "': " + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.out.println("Error delete statement: " + e.getMessage());
                }
            }
        }
    }
}
