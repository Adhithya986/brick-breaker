package com.brickbreaker.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Handles database connection and schema initialization using JDBC.
 * Supports SQLite (default, zero configuration) and MySQL.
 */
public class DatabaseManager {
    private static String dbType = "sqlite";
    private static String url = "jdbc:sqlite:brickbreaker.db";
    private static String user = "";
    private static String password = "";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(input);
                dbType = props.getProperty("db.type", "sqlite").trim().toLowerCase();
                if ("mysql".equals(dbType)) {
                    String host = props.getProperty("mysql.host", "localhost");
                    String port = props.getProperty("mysql.port", "3306");
                    String db = props.getProperty("mysql.database", "brick_breaker_db");
                    user = props.getProperty("mysql.user", "root");
                    password = props.getProperty("mysql.password", "");
                    url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                } else {
                    String dbPath = props.getProperty("sqlite.path", "brickbreaker.db");
                    url = "jdbc:sqlite:" + dbPath;
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not read db.properties, using default SQLite. Error: " + e.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            if ("mysql".equals(dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(url, user, password);
            } else {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection(url);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage(), e);
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Table: high_scores(id PK, player, score, level_reached, played_on)
            String createHighScoresTable;
            if ("mysql".equals(dbType)) {
                createHighScoresTable = "CREATE TABLE IF NOT EXISTS high_scores ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "player VARCHAR(100) NOT NULL, "
                        + "score INT NOT NULL, "
                        + "level_reached INT NOT NULL, "
                        + "played_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ");";
            } else {
                createHighScoresTable = "CREATE TABLE IF NOT EXISTS high_scores ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "player TEXT NOT NULL, "
                        + "score INTEGER NOT NULL, "
                        + "level_reached INTEGER NOT NULL, "
                        + "played_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ");";
            }
            stmt.execute(createHighScoresTable);

            // 2. Table: levels(level_no PK, brick_rows, ball_speed)
            String createLevelsTable;
            if ("mysql".equals(dbType)) {
                createLevelsTable = "CREATE TABLE IF NOT EXISTS levels ("
                        + "level_no INT PRIMARY KEY, "
                        + "brick_rows INT NOT NULL, "
                        + "ball_speed DOUBLE NOT NULL, "
                        + "layout_name VARCHAR(100) DEFAULT 'Standard'"
                        + ");";
            } else {
                createLevelsTable = "CREATE TABLE IF NOT EXISTS levels ("
                        + "level_no INTEGER PRIMARY KEY, "
                        + "brick_rows INTEGER NOT NULL, "
                        + "ball_speed REAL NOT NULL, "
                        + "layout_name TEXT DEFAULT 'Standard'"
                        + ");";
            }
            stmt.execute(createLevelsTable);

            // Seed default levels if empty
            seedLevelsIfEmpty(conn);

            // Seed sample high scores if empty (matches image prompt: Rahul, Sneha, Amit)
            seedHighScoresIfEmpty(conn);

            System.out.println("Database initialized successfully (" + dbType + ").");
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedLevelsIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM levels")) {
            if (rs.next() && rs.getInt("total") == 0) {
                String insertSql = "INSERT INTO levels (level_no, brick_rows, ball_speed, layout_name) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    // Level 1: Classic Rainbow (5 rows, speed 5.0)
                    pstmt.setInt(1, 1);
                    pstmt.setInt(2, 5);
                    pstmt.setDouble(3, 5.0);
                    pstmt.setString(4, "Classic Rainbow");
                    pstmt.executeUpdate();

                    // Level 2: Pyramid Challenge (6 rows, speed 5.8)
                    pstmt.setInt(1, 2);
                    pstmt.setInt(2, 6);
                    pstmt.setDouble(3, 5.8);
                    pstmt.setString(4, "Pyramid");
                    pstmt.executeUpdate();

                    // Level 3: Checkerboard Fortress (6 rows, speed 6.5)
                    pstmt.setInt(1, 3);
                    pstmt.setInt(2, 6);
                    pstmt.setDouble(3, 6.5);
                    pstmt.setString(4, "Checkerboard");
                    pstmt.executeUpdate();

                    // Level 4: Armored Diamond (7 rows, speed 7.2)
                    pstmt.setInt(1, 4);
                    pstmt.setInt(2, 7);
                    pstmt.setDouble(3, 7.2);
                    pstmt.setString(4, "Armored Diamond");
                    pstmt.executeUpdate();

                    // Level 5: Cosmic Chaos (8 rows, speed 8.0)
                    pstmt.setInt(1, 5);
                    pstmt.setInt(2, 8);
                    pstmt.setDouble(3, 8.0);
                    pstmt.setString(4, "Cosmic Chaos");
                    pstmt.executeUpdate();
                }
            }
        }
    }

    private static void seedHighScoresIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM high_scores")) {
            if (rs.next() && rs.getInt("total") == 0) {
                // Seed values shown in the prompt image: Rahul 2400, Sneha 1900, Amit 1500
                String insertSql = "INSERT INTO high_scores (player, score, level_reached) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "Rahul");
                    pstmt.setInt(2, 2400);
                    pstmt.setInt(3, 3);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Sneha");
                    pstmt.setInt(2, 1900);
                    pstmt.setInt(3, 2);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Amit");
                    pstmt.setInt(2, 1500);
                    pstmt.setInt(3, 2);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Priya");
                    pstmt.setInt(2, 1150);
                    pstmt.setInt(3, 1);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Vikram");
                    pstmt.setInt(2, 850);
                    pstmt.setInt(3, 1);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    public static String getDbType() {
        return dbType;
    }
}
