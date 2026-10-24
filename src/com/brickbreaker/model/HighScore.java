package com.brickbreaker.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Model representing a high score record from the database.
 * Table: high_scores(id PK, player, score, level_reached, played_on)
 */
public class HighScore {
    private int id;
    private String player;
    private int score;
    private int levelReached;
    private Timestamp playedOn;

    public HighScore() {
    }

    public HighScore(int id, String player, int score, int levelReached, Timestamp playedOn) {
        this.id = id;
        this.player = player;
        this.score = score;
        this.levelReached = levelReached;
        this.playedOn = playedOn;
    }

    public HighScore(String player, int score, int levelReached) {
        this(0, player, score, levelReached, new Timestamp(System.currentTimeMillis()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public void setLevelReached(int levelReached) {
        this.levelReached = levelReached;
    }

    public Timestamp getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(Timestamp playedOn) {
        this.playedOn = playedOn;
    }

    public String getFormattedDate() {
        if (playedOn == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(playedOn);
    }

    @Override
    public String toString() {
        return "HighScore{" +
                "id=" + id +
                ", player='" + player + '\'' +
                ", score=" + score +
                ", levelReached=" + levelReached +
                ", playedOn=" + playedOn +
                '}';
    }
}
