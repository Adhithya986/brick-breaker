package com.brickbreaker.model;

import java.awt.*;

/**
 * Represents a falling power-up item dropped from a destroyed brick.
 */
public class PowerUp {
    public enum Type {
        WIDE_PADDLE("WIDE PADDLE", new Color(59, 130, 246), "W"),       // Blue
        MULTI_BALL("MULTI BALL", new Color(236, 72, 153), "+2"),        // Pink
        EXTRA_LIFE("EXTRA LIFE", new Color(239, 68, 68), "♥"),          // Red
        FIREBALL("FIREBALL", new Color(249, 115, 22), "🔥"),             // Orange
        SLOW_BALL("SLOW MOTION", new Color(16, 185, 129), "S"),         // Green
        SHIELD("BOTTOM SHIELD", new Color(168, 85, 247), "🛡");          // Purple

        public final String label;
        public final Color color;
        public final String icon;

        Type(String label, Color color, String icon) {
            this.label = label;
            this.color = color;
            this.icon = icon;
        }
    }

    private double x;
    private double y;
    private int width = 36;
    private int height = 20;
    private double dy = 2.4;
    private Type type;
    private boolean collected = false;

    public PowerUp(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        y += dy;
    }

    public boolean checkCollision(Paddle paddle) {
        Rectangle itemRect = new Rectangle((int) x, (int) y, width, height);
        return itemRect.intersects(paddle.getBounds());
    }

    public void draw(Graphics2D g2) {
        if (collected) return;

        int drawX = (int) x;
        int drawY = (int) y;

        // Outer glow
        g2.setColor(new Color(type.color.getRed(), type.color.getGreen(), type.color.getBlue(), 60));
        g2.fillRoundRect(drawX - 2, drawY - 2, width + 4, height + 4, 10, 10);

        // Gradient capsule body
        GradientPaint gp = new GradientPaint(
                drawX, drawY, type.color.brighter(),
                drawX, drawY + height, type.color.darker()
        );
        g2.setPaint(gp);
        g2.fillRoundRect(drawX, drawY, width, height, 8, 8);

        // Highlight border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(drawX, drawY, width, height, 8, 8);

        // Text / Icon
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int strWidth = fm.stringWidth(type.icon);
        int strHeight = fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(type.icon, drawX + (width - strWidth) / 2, drawY + (height + strHeight) / 2 - 2);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Type getType() { return type; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}
