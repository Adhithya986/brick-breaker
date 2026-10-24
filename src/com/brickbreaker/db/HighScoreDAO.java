package com.brickbreaker.db;

import com.brickbreaker.model.HighScore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for high_scores table.
 */
public class HighScoreDAO {

    public List<HighScore> getTopScores(int limit) {
        List<HighScore> list = new ArrayList<>();
        String sql = "SELECT id, player, score, level_reached, played_on "
                   + "FROM high_scores "
                   + "ORDER BY score DESC, played_on ASC "
                   + "LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HighScore hs = new HighScore(
                            rs.getInt("id"),
                            rs.getString("player"),
                            rs.getInt("score"),
                            rs.getInt("level_reached"),
                            rs.getTimestamp("played_on")
                    );
                    list.add(hs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top scores: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertHighScore(HighScore hs) {
        String sql = "INSERT INTO high_scores (player, score, level_reached) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, hs.getPlayer());
            pstmt.setInt(2, hs.getScore());
            pstmt.setInt(3, hs.getLevelReached());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        hs.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving high score: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
