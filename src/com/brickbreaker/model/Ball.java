package com.brickbreaker.model;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * Represents the ball bouncing on paddle and destroying bricks.
 * Supports multi-ball, fireball penetration, and trail visuals.
 */
public class Ball {
    private double x;
    private double y;
    private double radius = 7.0;
    private double dx;
    private double dy;
    private double baseSpeed;
    private double currentSpeed;

    private boolean fireball = false;
    private int fireballTimer = 0;
    private final int MAX_FIREBALL_TIME = 60 * 8; // ~8 seconds

    private LinkedList<Point2D.Double> trail = new LinkedList<>();
    private final int MAX_TRAIL_LENGTH = 6;

    public Ball(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.baseSpeed = speed;
        this.currentSpeed = speed;
        // Default launch upwards with a slight angle
        setAngle(-Math.PI / 2 + 0.2, speed);
    }

    public void setAngle(double angleRad, double speed) {
        this.currentSpeed = speed;
        this.dx = Math.cos(angleRad) * speed;
        this.dy = Math.sin(angleRad) * speed;
    }

    public void move() {
        // Record trail
        trail.addFirst(new Point2D.Double(x, y));
        if (trail.size() > MAX_TRAIL_LENGTH) {
            trail.removeLast();
        }

        x += dx;
        y += dy;

        if (fireball) {
            fireballTimer--;
            if (fireballTimer <= 0) {
                fireball = false;
            }
        }
    }

    public void activateFireball() {
        this.fireball = true;
        this.fireballTimer = MAX_FIREBALL_TIME;
    }

    public void reverseX() {
        dx = -dx;
    }

    public void reverseY() {
        dy = -dy;
    }

    public Rectangle getBounds() {
        return new Rectangle(
                (int) (x - radius),
                (int) (y - radius),
                (int) (radius * 2),
                (int) (radius * 2)
        );
    }

    public void draw(Graphics2D g2) {
        int r = (int) radius;

        // Draw trail
        int trailIndex = 0;
        for (Point2D.Double pt : trail) {
            float alpha = 0.4f * (1.0f - (float) trailIndex / MAX_TRAIL_LENGTH);
            int tr = Math.max(2, (int) (r * (1.0f - (float) trailIndex / (MAX_TRAIL_LENGTH + 2))));
            Color trailColor = fireball ? new Color(249, 115, 22, (int) (alpha * 255))
                                        : new Color(148, 163, 184, (int) (alpha * 255));
            g2.setColor(trailColor);
            g2.fillOval((int) (pt.x - tr), (int) (pt.y - tr), tr * 2, tr * 2);
            trailIndex++;
        }

        // Draw outer glow if fireball
        if (fireball) {
            g2.setColor(new Color(251, 146, 60, 90));
            g2.fillOval((int) (x - r - 4), (int) (y - r - 4), (r + 4) * 2, (r + 4) * 2);
        }

        // Draw ball body
        Color mainColor = fireball ? new Color(249, 115, 22) : Color.WHITE;
        Color edgeColor = fireball ? new Color(194, 65, 12) : new Color(203, 213, 225);

        // 3D sphere gradient
        Point2D center = new Point2D.Float((float) (x - r * 0.3), (float) (y - r * 0.3));
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {mainColor, edgeColor};
        RadialGradientPaint p = new RadialGradientPaint(center, (float) (r * 1.4), dist, colors);
        g2.setPaint(p);
        g2.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        // Subtle specular highlight
        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillOval((int) (x - r * 0.5), (int) (y - r * 0.5), (int) (r * 0.6), (int) (r * 0.6));
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }
    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
    public double getRadius() { return radius; }
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double speed) { this.currentSpeed = speed; }
    public boolean isFireball() { return fireball; }
}
