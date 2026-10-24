package com.brickbreaker;

import com.brickbreaker.db.DatabaseManager;
import com.brickbreaker.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point for Brick Breaker game application.
 */
public class Main {
    public static void main(String[] args) {
        // Set modern Look and Feel (FlatLaf if available, fallback to System)
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Database tables and seed initial data
        DatabaseManager.initializeDatabase();

        // Launch Game UI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
