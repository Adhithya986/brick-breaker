package com.brickbreaker.model;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents an individual brick in the grid.
 */
public class Brick {
    private int x;
    private int y;
    private int width;
    private int height;
    private Color baseColor;
    private int hitPoints;
    private int maxHitPoints;
    private int points;
    private PowerUp.Type powerUpType;
    private boolean destroyed = false;

    public Brick(int x, int y, int width, int height, Color baseColor, int hitPoints, int points, PowerUp.Type powerUpType) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baseColor = baseColor;
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.points = points;
        this.powerUpType = powerUpType;
    }

    /**
     * Deals 1 damage to brick.
     * @return true if brick is destroyed by this hit.
     */
    public boolean takeDamage() {
        if (destroyed) return false;
        hitPoints--;
        if (hitPoints <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2) {
        if (destroyed) return;

        // Compute color adjusted for health
        Color drawColor = baseColor;
        if (maxHitPoints > 1 && hitPoints < maxHitPoints) {
            float ratio = (float) hitPoints / maxHitPoints;
            int r = Math.min(255, (int) (baseColor.getRed() * ratio + 80 * (1 - ratio)));
            int g = Math.min(255, (int) (baseColor.getGreen() * ratio + 80 * (1 - ratio)));
            int b = Math.min(255, (int) (baseColor.getBlue() * ratio + 80 * (1 - ratio)));
            drawColor = new Color(r, g, b);
        }

        // 3D Beveled styling with rounded corners
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, 4, 4);

        // Gradient top-to-bottom
        GradientPaint gp = new GradientPaint(
                x, y, drawColor.brighter(),
                x, y + height, drawColor.darker()
        );
        g2.setPaint(gp);
        g2.fill(rect);

        // Top/Left highlight for 3D bevel
        g2.setColor(new Color(255, 255, 255, 90));
        g2.drawLine(x + 2, y + 1, x + width - 3, y + 1);
        g2.drawLine(x + 1, y + 2, x + 1, y + height - 3);

        // Bottom/Right shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawLine(x + 2, y + height - 1, x + width - 2, y + height - 1);
        g2.drawLine(x + width - 1, y + 2, x + width - 1, y + height - 2);

        // Dark border
        g2.setColor(new Color(20, 20, 25, 120));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(rect);

        // Power-up indicator badge (subtle star or glow in center)
        if (powerUpType != null) {
            g2.setColor(new Color(255, 255, 255, 110));
            g2.fillOval(x + width / 2 - 3, y + height / 2 - 3, 6, 6);
        }

        // Draw cracks if damaged
        if (maxHitPoints > 1 && hitPoints < maxHitPoints) {
            g2.setColor(new Color(255, 255, 255, 160));
            g2.setStroke(new BasicStroke(1.5f));
            int midX = x + width / 2;
            int midY = y + height / 2;
            g2.drawLine(x + 4, y + 3, midX, midY);
            g2.drawLine(midX, midY, x + width - 6, y + height - 4);
            g2.drawLine(midX, midY, x + width / 3, y + height - 2);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Color getBaseColor() { return baseColor; }
    public int getHitPoints() { return hitPoints; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public int getPoints() { return points; }
    public PowerUp.Type getPowerUpType() { return powerUpType; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
}
