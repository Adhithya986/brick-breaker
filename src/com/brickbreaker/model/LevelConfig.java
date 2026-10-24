package com.brickbreaker.model;

/**
 * Model representing a level configuration from the database.
 * Table: levels(level_no PK, brick_rows, ball_speed)
 */
public class LevelConfig {
    private int levelNo;
    private int brickRows;
    private double ballSpeed;
    private String layoutName;

    public LevelConfig() {
    }

    public LevelConfig(int levelNo, int brickRows, double ballSpeed, String layoutName) {
        this.levelNo = levelNo;
        this.brickRows = brickRows;
        this.ballSpeed = ballSpeed;
        this.layoutName = layoutName;
    }

    public int getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(int levelNo) {
        this.levelNo = levelNo;
    }

    public int getBrickRows() {
        return brickRows;
    }

    public void setBrickRows(int brickRows) {
        this.brickRows = brickRows;
    }

    public double getBallSpeed() {
        return ballSpeed;
    }

    public void setBallSpeed(double ballSpeed) {
        this.ballSpeed = ballSpeed;
    }

    public String getLayoutName() {
        return layoutName != null ? layoutName : "Level " + levelNo;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    @Override
    public String toString() {
        return "Level " + levelNo + (layoutName != null ? " - " + layoutName : "");
    }
}
