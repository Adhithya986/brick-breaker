package com.brickbreaker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window for Brick Breaker.
 * Integrates Top Header, GamePanel, SidePanel, and ControlPanel.
 */
public class MainFrame extends JFrame {
    private final GamePanel gamePanel;
    private final SidePanel sidePanel;
    private final ControlPanel controlPanel;

    public MainFrame() {
        super("Brick Breaker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Initialize Child Components
        gamePanel = new GamePanel();
        sidePanel = new SidePanel();
        controlPanel = new ControlPanel();

        initLayout();
        hookListeners();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout(8, 8));
        rootPanel.setBackground(new Color(226, 232, 240)); // Slate blue border tone
        rootPanel.setBorder(new EmptyBorder(8, 12, 10, 12));

        // --- Top Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        headerPanel.setBackground(new Color(241, 245, 249));
        JLabel titleLabel = new JLabel("Brick Breaker");
        titleLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 28));
        titleLabel.setForeground(new Color(30, 41, 59));
        headerPanel.add(titleLabel);

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(gamePanel, BorderLayout.CENTER);
        rootPanel.add(sidePanel, BorderLayout.EAST);
        rootPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void hookListeners() {
        // Update SidePanel when game stats update
        gamePanel.setOnStatsChanged(() -> {
            sidePanel.updateScore(gamePanel.getScore());
            sidePanel.updateLives(gamePanel.getLives());
        });

        // Prompt Save Score on Game Over
        gamePanel.setOnGameOver(() -> {
            controlPanel.setStartButtonText("Start");
            controlPanel.setPauseButtonText("Pause");
            if (gamePanel.getScore() > 0) {
                SwingUtilities.invokeLater(() -> {
                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            "Game Over! Your final score is " + gamePanel.getScore() + ".\nWould you like to save your score?",
                            "Game Over",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        openSaveScoreDialog();
                    }
                });
            }
        });

        // Handle Level Clear
        gamePanel.setOnLevelClear(() -> {
            controlPanel.setStartButtonText("Next Level");
        });

        // SidePanel Level Combo box selection
        sidePanel.setOnLevelSelected(levelNo -> {
            if (levelNo != gamePanel.getCurrentLevel()) {
                gamePanel.initGame(levelNo);
                gamePanel.requestFocusInWindow();
            }
        });

        // Bottom Control Buttons
        controlPanel.setStartAction(e -> {
            if (gamePanel.getGameState() == GamePanel.GameState.READY) {
                gamePanel.launchBall();
            } else if (gamePanel.getGameState() == GamePanel.GameState.PAUSED) {
                gamePanel.togglePause();
            } else if (gamePanel.getGameState() == GamePanel.GameState.LEVEL_CLEAR) {
                gamePanel.advanceToNextLevel();
                sidePanel.setLevelSelected(gamePanel.getCurrentLevel());
            } else if (gamePanel.getGameState() == GamePanel.GameState.GAME_OVER) {
                gamePanel.restartGame();
            }
            controlPanel.setStartButtonText("Start");
            controlPanel.setPauseButtonText("Pause");
            gamePanel.requestFocusInWindow();
        });

        controlPanel.setPauseAction(e -> {
            gamePanel.togglePause();
            if (gamePanel.getGameState() == GamePanel.GameState.PAUSED) {
                controlPanel.setPauseButtonText("Resume");
            } else {
                controlPanel.setPauseButtonText("Pause");
            }
            gamePanel.requestFocusInWindow();
        });

        controlPanel.setSaveScoreAction(e -> openSaveScoreDialog());

        controlPanel.setExitAction(e -> confirmExit());
    }

    private void openSaveScoreDialog() {
        SaveScoreDialog dialog = new SaveScoreDialog(
                this,
                gamePanel.getScore(),
                gamePanel.getCurrentLevel(),
                sidePanel::refreshHighScores
        );
        dialog.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit Brick Breaker?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
