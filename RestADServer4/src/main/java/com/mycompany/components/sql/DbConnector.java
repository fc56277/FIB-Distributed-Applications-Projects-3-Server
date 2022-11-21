package com.mycompany.components.sql;

import java.sql.*;

public class DbConnector {

    private Connection conn;

    public void open() {
        try {
            String dbConnection = "jdbc:derby://localhost:1527/pr2;user=pr2;password=pr2";
            this.conn = DriverManager.getConnection(dbConnection);
        } catch (SQLException e) {
            System.out.println("Opening database-connection failed.");
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement prepareStatement(String query) {
        try {
            return conn.prepareStatement(query);
        } catch (SQLException e) {
            System.out.println("Error while creating PreparedStatement.");
            throw new RuntimeException(e);
        }
    }

    public ResultSet executeQuery(PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (SQLException e) {
            System.out.println("Error while executing query.");
            throw new RuntimeException(e);
        }
    }

    public void executeUpdate(PreparedStatement statement) {
        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while executing update.");
            throw new RuntimeException(e);
        }
    }

    public void closeResultSet(ResultSet rs) {
        try { rs.close(); } catch (Exception e) { /* Ignored */ }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        try { ps.close(); } catch (Exception e) { /* Ignored */ }
    }

    public void closeConnection() {
        try { conn.close(); } catch (Exception e) { /* Ignored */ }
    }

    public void closeAll(ResultSet rs, PreparedStatement ps) {
        closeResultSet(rs);
        closePreparedStatement(ps);
        closeConnection();
    }

    public boolean isValid(int timeout) throws SQLException {
        return conn.isValid(timeout);
    }
}
