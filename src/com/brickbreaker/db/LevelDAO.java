package com.brickbreaker.db;

import com.brickbreaker.model.LevelConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for levels table.
 */
public class LevelDAO {

    public List<LevelConfig> getAllLevels() {
        List<LevelConfig> list = new ArrayList<>();
        String sql = "SELECT level_no, brick_rows, ball_speed, layout_name FROM levels ORDER BY level_no ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LevelConfig cfg = new LevelConfig(
                        rs.getInt("level_no"),
                        rs.getInt("brick_rows"),
                        rs.getDouble("ball_speed"),
                        rs.getString("layout_name")
                );
                list.add(cfg);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching levels: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public LevelConfig getLevel(int levelNo) {
        String sql = "SELECT level_no, brick_rows, ball_speed, layout_name FROM levels WHERE level_no = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, levelNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new LevelConfig(
                            rs.getInt("level_no"),
                            rs.getInt("brick_rows"),
                            rs.getDouble("ball_speed"),
                            rs.getString("layout_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching level " + levelNo + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Fallback default level configuration if not found
        return new LevelConfig(levelNo, 5, 5.0 + (levelNo - 1) * 0.8, "Level " + levelNo);
    }
}
