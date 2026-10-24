package com.brickbreaker.ui;

import com.brickbreaker.audio.SoundManager;
import com.brickbreaker.db.LevelDAO;
import com.brickbreaker.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main game canvas handling rendering, collision physics, and input events.
 * Implements MouseMotionListener, KeyListener, and Timer-based 60 FPS loop.
 */
public class GamePanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener {
    public enum GameState {
        READY,
        RUNNING,
        PAUSED,
        LEVEL_CLEAR,
        GAME_OVER
    }

    private GameState state = GameState.READY;
    private final Timer gameTimer;
    private final Random random = new Random();

    // Canvas Dimensions
    private final int PANEL_WIDTH = 680;
    private final int PANEL_HEIGHT = 540;

    // Game Entities
    private Paddle paddle;
    private final List<Ball> balls = new ArrayList<>();
    private final List<Brick> bricks = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // Shield power-up state
    private boolean shieldActive = false;
    private int shieldTimer = 0;

    // Game Statistics
    private int score = 0;
    private int lives = 3;
    private int currentLevel = 1;
    private double currentBallSpeed = 5.0;

    // Database DAO
    private final LevelDAO levelDAO = new LevelDAO();

    // UI Listeners / Callbacks
    private Runnable onStatsChanged;
    private Runnable onGameOver;
    private Runnable onLevelClear;

    // Keyboard state
    private boolean keyLeft = false;
    private boolean keyRight = false;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(15, 23, 42)); // Modern deep navy/slate background
        setFocusable(true);

        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);

        // 60 FPS Game Loop Timer
        gameTimer = new Timer(16, e -> gameTick());

        initGame(1);
    }

    public void initGame(int levelNo) {
        this.currentLevel = levelNo;
        this.score = (levelNo == 1) ? 0 : this.score;
        this.lives = (levelNo == 1) ? 3 : this.lives;
        this.shieldActive = false;
        this.shieldTimer = 0;

        LevelConfig cfg = levelDAO.getLevel(levelNo);
        this.currentBallSpeed = cfg.getBallSpeed();

        // Paddle setup
        int paddleY = PANEL_HEIGHT - 45;
        paddle = new Paddle(PANEL_WIDTH / 2 - 50, paddleY);

        // Reset entities
        balls.clear();
        Ball mainBall = new Ball(PANEL_WIDTH / 2.0, paddleY - 10, currentBallSpeed);
        balls.add(mainBall);

        powerUps.clear();
        particles.clear();
        floatingTexts.clear();

        // Generate brick layout based on database level configuration
        buildBricksForLevel(cfg);

        state = GameState.READY;
        gameTimer.start();
        notifyStatsChanged();
        repaint();
    }

    private void buildBricksForLevel(LevelConfig cfg) {
        bricks.clear();
        int rows = cfg.getBrickRows();
        int cols = 10;
        int brickWidth = 58;
        int brickHeight = 18;
        int startX = (PANEL_WIDTH - (cols * (brickWidth + 4))) / 2;
        int startY = 50;

        Color[] rowColors = {
                new Color(239, 68, 68),   // Red
                new Color(249, 115, 22),  // Orange
                new Color(234, 179, 8),   // Yellow
                new Color(34, 197, 94),   // Green
                new Color(59, 130, 246),  // Blue
                new Color(168, 85, 247),  // Purple
                new Color(236, 72, 153),  // Pink
                new Color(14, 165, 233)   // Cyan
        };

        for (int r = 0; r < rows; r++) {
            Color color = rowColors[r % rowColors.length];
            int points = (rows - r) * 10; // Top rows award higher points

            for (int c = 0; c < cols; c++) {
                // Layout pattern filters
                if (cfg.getLevelNo() == 2) {
                    // Pyramid / Staggered pattern
                    if ((r + c) % 2 == 1 && r > 2) continue;
                } else if (cfg.getLevelNo() == 3) {
                    // Checkerboard
                    if ((r + c) % 2 == 1) continue;
                } else if (cfg.getLevelNo() == 4) {
                    // Diamond hazard
                    int distCenter = Math.abs(c - cols / 2);
                    if (r + distCenter > 8) continue;
                }

                int bx = startX + c * (brickWidth + 4);
                int by = startY + r * (brickHeight + 4);

                // Multi-hit durability for upper rows in higher levels
                int hp = 1;
                if (cfg.getLevelNo() >= 2 && r == 0) hp = 2;
                if (cfg.getLevelNo() >= 3 && r <= 1) hp = 2;
                if (cfg.getLevelNo() >= 4 && r == 0) hp = 3;

                // Power-up assignment (approx 22% chance)
                PowerUp.Type powerType = null;
                if (random.nextDouble() < 0.22) {
                    PowerUp.Type[] types = PowerUp.Type.values();
                    powerType = types[random.nextInt(types.length)];
                }

                bricks.add(new Brick(bx, by, brickWidth, brickHeight, color, hp, points, powerType));
            }
        }
    }

    private void gameTick() {
        if (state != GameState.RUNNING) {
            repaint();
            return;
        }

        // Handle keyboard paddle movement
        if (keyLeft) paddle.moveLeft(0);
        if (keyRight) paddle.moveRight(PANEL_WIDTH);

        paddle.update();

        // Update Shield
        if (shieldActive) {
            shieldTimer--;
            if (shieldTimer <= 0) shieldActive = false;
        }

        // Update Balls
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            ball.move();

            // Wall collisions
            if (ball.getX() - ball.getRadius() <= 0) {
                ball.setX(ball.getRadius());
                ball.reverseX();
                SoundManager.playPaddleBounce();
            } else if (ball.getX() + ball.getRadius() >= PANEL_WIDTH) {
                ball.setX(PANEL_WIDTH - ball.getRadius());
                ball.reverseX();
                SoundManager.playPaddleBounce();
            }

            if (ball.getY() - ball.getRadius() <= 0) {
                ball.setY(ball.getRadius());
                ball.reverseY();
                SoundManager.playPaddleBounce();
            }

            // Bottom Shield bounce
            if (shieldActive && ball.getY() + ball.getRadius() >= PANEL_HEIGHT - 6) {
                ball.setY(PANEL_HEIGHT - 6 - ball.getRadius());
                ball.reverseY();
                SoundManager.playPaddleBounce();
                spawnSparks(ball.getX(), PANEL_HEIGHT - 6, new Color(168, 85, 247), 8);
            }

            // Paddle collision
            if (ball.getDy() > 0 && ball.getBounds().intersects(paddle.getBounds())) {
                double hitRatio = paddle.getHitRatio(ball.getX());
                // Deflect angle between -75 deg to -105 deg
                double angle = -Math.PI / 2 + (hitRatio * (Math.PI * 0.38));
                ball.setAngle(angle, ball.getCurrentSpeed());
                ball.setY(paddle.getY() - ball.getRadius());
                SoundManager.playPaddleBounce();
                spawnSparks(ball.getX(), paddle.getY(), Color.WHITE, 6);
            }

            // Brick collisions
            checkBrickCollisions(ball);

            // Ball lost (below screen)
            if (ball.getY() - ball.getRadius() > PANEL_HEIGHT) {
                ballIterator.remove();
            }
        }

        // Check if all balls are lost
        if (balls.isEmpty()) {
            handleLifeLost();
        }

        // Update Power-ups
        Iterator<PowerUp> pIt = powerUps.iterator();
        while (pIt.hasNext()) {
            PowerUp p = pIt.next();
            p.update();

            // Collision with paddle
            if (p.checkCollision(paddle)) {
                applyPowerUp(p);
                pIt.remove();
            } else if (p.getY() > PANEL_HEIGHT) {
                pIt.remove();
            }
        }

        // Update Particles
        Iterator<Particle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            Particle pt = partIt.next();
            pt.update();
            if (pt.isDead()) partIt.remove();
        }

        // Update Floating Texts
        Iterator<FloatingText> ftIt = floatingTexts.iterator();
        while (ftIt.hasNext()) {
            FloatingText ft = ftIt.next();
            ft.update();
            if (ft.isDead()) ftIt.remove();
        }

        // Check for level complete
        boolean allCleared = true;
        for (Brick b : bricks) {
            if (!b.isDestroyed()) {
                allCleared = false;
                break;
            }
        }

        if (allCleared && !bricks.isEmpty()) {
            handleLevelComplete();
        }

        repaint();
    }

    private void checkBrickCollisions(Ball ball) {
        Rectangle ballRect = ball.getBounds();

        for (Brick brick : bricks) {
            if (brick.isDestroyed()) continue;

            if (ballRect.intersects(brick.getBounds())) {
                boolean destroyed = brick.takeDamage();

                if (destroyed) {
                    score += brick.getPoints() * currentLevel;
                    SoundManager.playBrickBreak();
                    spawnSparks(brick.getX() + brick.getWidth() / 2.0, brick.getY() + brick.getHeight() / 2.0, brick.getBaseColor(), 14);
                    floatingTexts.add(new FloatingText("+" + (brick.getPoints() * currentLevel), brick.getX() + 10, brick.getY(), brick.getBaseColor()));

                    // Drop power-up if assigned
                    if (brick.getPowerUpType() != null) {
                        powerUps.add(new PowerUp(brick.getX() + brick.getWidth() / 2.0 - 18, brick.getY(), brick.getPowerUpType()));
                    }
                } else {
                    SoundManager.playBrickHit();
                    spawnSparks(ball.getX(), ball.getY(), brick.getBaseColor(), 5);
                }

                // If not in fireball mode, reflect ball
                if (!ball.isFireball()) {
                    resolveBallBrickReflection(ball, brick);
                }

                notifyStatsChanged();
                break; // Handle one brick hit per tick to prevent clipping
            }
        }
    }

    private void resolveBallBrickReflection(Ball ball, Brick brick) {
        Rectangle bRect = brick.getBounds();
        double prevX = ball.getX() - ball.getDx();
        double prevY = ball.getY() - ball.getDy();

        boolean fromHorizontal = (prevX + ball.getRadius() <= bRect.x) || (prevX - ball.getRadius() >= bRect.x + bRect.width);
        boolean fromVertical = (prevY + ball.getRadius() <= bRect.y) || (prevY - ball.getRadius() >= bRect.y + bRect.height);

        if (fromHorizontal) {
            ball.reverseX();
        } else if (fromVertical) {
            ball.reverseY();
        } else {
            ball.reverseY();
        }
    }

    private void applyPowerUp(PowerUp p) {
        SoundManager.playPowerUp();
        floatingTexts.add(new FloatingText(p.getType().label + "!", (int) p.getX(), paddle.getY() - 10, p.getType().color));

        switch (p.getType()) {
            case WIDE_PADDLE:
                paddle.activateWidePaddle();
                break;
            case MULTI_BALL:
                if (!balls.isEmpty()) {
                    Ball ref = balls.get(0);
                    Ball b1 = new Ball(ref.getX(), ref.getY(), ref.getCurrentSpeed());
                    b1.setAngle(-Math.PI * 0.35, ref.getCurrentSpeed());
                    Ball b2 = new Ball(ref.getX(), ref.getY(), ref.getCurrentSpeed());
                    b2.setAngle(-Math.PI * 0.65, ref.getCurrentSpeed());
                    balls.add(b1);
                    balls.add(b2);
                }
                break;
            case EXTRA_LIFE:
                lives = Math.min(5, lives + 1);
                notifyStatsChanged();
                break;
            case FIREBALL:
                for (Ball b : balls) b.activateFireball();
                break;
            case SLOW_BALL:
                for (Ball b : balls) b.setCurrentSpeed(Math.max(3.5, b.getCurrentSpeed() * 0.75));
                break;
            case SHIELD:
                shieldActive = true;
                shieldTimer = 60 * 12; // 12 seconds
                break;
        }
    }

    private void handleLifeLost() {
        lives--;
        notifyStatsChanged();

        if (lives > 0) {
            SoundManager.playLostLife();
            // Reset ball on paddle
            balls.clear();
            Ball newBall = new Ball(paddle.getX() + paddle.getWidth() / 2.0, paddle.getY() - 10, currentBallSpeed);
            balls.add(newBall);
            state = GameState.READY;
        } else {
            SoundManager.playGameOver();
            state = GameState.GAME_OVER;
            if (onGameOver != null) onGameOver.run();
        }
    }

    private void handleLevelComplete() {
        SoundManager.playLevelWin();
        score += 500 * currentLevel; // Level clear bonus!
        notifyStatsChanged();
        state = GameState.LEVEL_CLEAR;
        if (onLevelClear != null) onLevelClear.run();
    }

    public void advanceToNextLevel() {
        int next = currentLevel + 1;
        if (next > 5) next = 1; // loop or victory
        initGame(next);
    }

    private void spawnSparks(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    public void launchBall() {
        if (state == GameState.READY) {
            state = GameState.RUNNING;
        }
    }

    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
        repaint();
    }

    public void restartGame() {
        initGame(currentLevel);
    }

    private void notifyStatsChanged() {
        if (onStatsChanged != null) onStatsChanged.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Anti-aliasing for buttery smooth graphics
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw background grid lines (retro subtle grid)
        g2.setColor(new Color(30, 41, 59, 90));
        for (int x = 0; x < PANEL_WIDTH; x += 40) g2.drawLine(x, 0, x, PANEL_HEIGHT);
        for (int y = 0; y < PANEL_HEIGHT; y += 40) g2.drawLine(0, y, PANEL_WIDTH, y);

        // Draw Bottom Shield if active
        if (shieldActive) {
            g2.setColor(new Color(168, 85, 247, 180));
            g2.setStroke(new BasicStroke(4.0f));
            g2.drawLine(0, PANEL_HEIGHT - 4, PANEL_WIDTH, PANEL_HEIGHT - 4);
        }

        // Draw Bricks
        for (Brick brick : bricks) {
            brick.draw(g2);
        }

        // Draw Power-ups
        for (PowerUp p : powerUps) {
            p.draw(g2);
        }

        // Draw Paddle
        paddle.draw(g2);

        // Draw Balls
        for (Ball ball : balls) {
            ball.draw(g2);
        }

        // Draw Particles
        for (Particle pt : particles) {
            pt.draw(g2);
        }

        // Draw Floating Texts
        for (FloatingText ft : floatingTexts) {
            ft.draw(g2);
        }

        // State Overlays
        drawOverlays(g2);
    }

    private void drawOverlays(Graphics2D g2) {
        if (state == GameState.READY) {
            drawCenteredBanner(g2, "CLICK OR PRESS SPACE TO LAUNCH", "Use Mouse or Arrow Keys to Move Paddle", new Color(59, 130, 246));
        } else if (state == GameState.PAUSED) {
            drawCenteredBanner(g2, "PAUSED", "Press 'P' or Click Pause Button to Resume", new Color(245, 158, 11));
        } else if (state == GameState.LEVEL_CLEAR) {
            drawCenteredBanner(g2, "LEVEL COMPLETED! +500 BONUS", "Click Start or Press SPACE for Next Level", new Color(34, 197, 94));
        } else if (state == GameState.GAME_OVER) {
            drawCenteredBanner(g2, "GAME OVER", "Click 'Save Score' or 'Start' to Play Again", new Color(239, 68, 68));
        }
    }

    private void drawCenteredBanner(Graphics2D g2, String title, String subtitle, Color accentColor) {
        int bannerW = 460;
        int bannerH = 100;
        int bx = (PANEL_WIDTH - bannerW) / 2;
        int by = (PANEL_HEIGHT - bannerH) / 2 + 30;

        // Semi-transparent card
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(bx, by, bannerW, bannerH, 16, 16);

        // Glowing border
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(bx, by, bannerW, bannerH, 16, 16);

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm1 = g2.getFontMetrics();
        int tX = bx + (bannerW - fm1.stringWidth(title)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(title, tX, by + 42);

        // Subtitle
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm2 = g2.getFontMetrics();
        int sX = bx + (bannerW - fm2.stringWidth(subtitle)) / 2;
        g2.setColor(new Color(203, 213, 225));
        g2.drawString(subtitle, sX, by + 74);
    }

    // --- Mouse Listeners ---
    @Override
    public void mouseMoved(MouseEvent e) {
        paddle.moveTo(e.getX(), PANEL_WIDTH);

        // Keep ball pinned to paddle in READY state
        if (state == GameState.READY && !balls.isEmpty()) {
            Ball b = balls.get(0);
            b.setX(paddle.getX() + paddle.getWidth() / 2.0);
            b.setY(paddle.getY() - b.getRadius());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (state == GameState.READY) {
            launchBall();
        } else if (state == GameState.LEVEL_CLEAR) {
            advanceToNextLevel();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- Key Listeners ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) keyLeft = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) keyRight = true;

        if (code == KeyEvent.VK_SPACE) {
            if (state == GameState.READY) {
                launchBall();
            } else if (state == GameState.LEVEL_CLEAR) {
                advanceToNextLevel();
            } else if (state == GameState.RUNNING || state == GameState.PAUSED) {
                togglePause();
            }
        } else if (code == KeyEvent.VK_P) {
            togglePause();
        } else if (code == KeyEvent.VK_R) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) keyLeft = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) keyRight = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    // Getters / Setters
    public GameState getGameState() { return state; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getCurrentLevel() { return currentLevel; }
    public void setOnStatsChanged(Runnable r) { this.onStatsChanged = r; }
    public void setOnGameOver(Runnable r) { this.onGameOver = r; }
    public void setOnLevelClear(Runnable r) { this.onLevelClear = r; }

    /**
     * Inner helper for floating damage/bonus text.
     */
    private static class FloatingText {
        String text;
        double x, y;
        Color color;
        int life = 45;

        FloatingText(String text, double x, double y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        void update() {
            y -= 1.2;
            life--;
        }

        boolean isDead() { return life <= 0; }

        void draw(Graphics2D g2) {
            if (isDead()) return;
            float alpha = Math.max(0f, Math.min(1f, life / 45.0f));
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(text, (int) x, (int) y);
        }
    }
}
