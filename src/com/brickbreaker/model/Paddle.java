package com.brickbreaker.model;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents the player paddle controlled by mouse and keyboard.
 */
public class Paddle {
    private double x;
    private double y;
    private int width;
    private int baseWidth = 100;
    private int height = 14;
    private double speed = 9.0;

    // Power-up state
    private boolean wide = false;
    private int wideTimer = 0; // ticks remaining
    private final int MAX_WIDE_TIME = 60 * 12; // ~12 seconds at 60 FPS

    public Paddle(int startX, int startY) {
        this.width = baseWidth;
        this.x = startX;
        this.y = startY;
    }

    public void update() {
        if (wide) {
            wideTimer--;
            if (wideTimer <= 0) {
                wide = false;
                width = baseWidth;
            }
        }
    }

    public void activateWidePaddle() {
        this.wide = true;
        this.width = (int) (baseWidth * 1.5);
        this.wideTimer = MAX_WIDE_TIME;
    }

    public void moveLeft(int minX) {
        x = Math.max(minX, x - speed);
    }

    public void moveRight(int maxX) {
        x = Math.min(maxX - width, x + speed);
    }

    public void moveTo(int targetCenterX, int panelWidth) {
        double newX = targetCenterX - (width / 2.0);
        if (newX < 0) newX = 0;
        if (newX + width > panelWidth) newX = panelWidth - width;
        this.x = newX;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    /**
     * Calculates the deflection ratio from -1.0 (far left) to 1.0 (far right).
     */
    public double getHitRatio(double ballCenterX) {
        double paddleCenterX = x + (width / 2.0);
        double diff = ballCenterX - paddleCenterX;
        double ratio = diff / (width / 2.0);
        return Math.max(-0.92, Math.min(0.92, ratio));
    }

    public void draw(Graphics2D g2) {
        int drawX = (int) x;
        int drawY = (int) y;

        // Paddle shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(drawX, drawY + 3, width, height, 10, 10);

        // Body gradient (sleek metallic silver/cyan)
        Color topColor = wide ? new Color(147, 197, 253) : new Color(241, 245, 249);
        Color bottomColor = wide ? new Color(37, 99, 235) : new Color(148, 163, 184);

        GradientPaint gp = new GradientPaint(
                drawX, drawY, topColor,
                drawX, drawY + height, bottomColor
        );
        g2.setPaint(gp);
        RoundRectangle2D body = new RoundRectangle2D.Float(drawX, drawY, width, height, 8, 8);
        g2.fill(body);

        // Top glossy streak
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRoundRect(drawX + 4, drawY + 1, width - 8, 3, 4, 4);

        // Paddle border
        g2.setColor(wide ? new Color(59, 130, 246) : new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(body);

        // Power-up duration bar if active
        if (wide && wideTimer > 0) {
            float progress = (float) wideTimer / MAX_WIDE_TIME;
            int barW = (int) (width * progress);
            g2.setColor(new Color(59, 130, 246, 200));
            g2.fillRect(drawX + (width - barW) / 2, drawY - 4, barW, 2);
        }
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public boolean isWide() { return wide; }
}
