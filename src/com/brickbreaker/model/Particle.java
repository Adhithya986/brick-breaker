package com.brickbreaker.model;

import java.awt.*;

/**
 * Visual particle for explosion effects when bricks break.
 */
public class Particle {
    private double x;
    private double y;
    private double dx;
    private double dy;
    private Color color;
    private int size;
    private int life;
    private int maxLife;

    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        // Random velocity spread
        double angle = Math.random() * Math.PI * 2;
        double speed = 1.0 + Math.random() * 4.5;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed - 1.0; // slight upward bias
        this.size = 3 + (int) (Math.random() * 4);
        this.maxLife = 20 + (int) (Math.random() * 25);
        this.life = maxLife;
    }

    public void update() {
        x += dx;
        y += dy;
        dy += 0.15; // gentle gravity
        dx *= 0.98; // air resistance
        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g2) {
        if (isDead()) return;
        float alpha = Math.max(0f, Math.min(1f, (float) life / maxLife));
        Color pColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (alpha * 255)
        );
        g2.setColor(pColor);
        g2.fillRect((int) x, (int) y, size, size);
    }
}
