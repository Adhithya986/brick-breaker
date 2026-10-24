package com.brickbreaker.ui;

import com.brickbreaker.audio.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Bottom control bar containing Start, Pause, Save Score, Exit buttons, and Sound toggle.
 */
public class ControlPanel extends JPanel {
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton saveScoreBtn;
    private JButton exitBtn;
    private JButton soundToggleBtn;

    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        setBackground(new Color(226, 232, 240)); // Matching sample template border tone
        setBorder(new EmptyBorder(6, 12, 6, 12));

        initComponents();
    }

    private void initComponents() {
        // Start Button (Blue #0284c7)
        startBtn = createStyledButton("Start", new Color(2, 132, 199), Color.WHITE);

        // Pause Button (Slate Blue #0369a1)
        pauseBtn = createStyledButton("Pause", new Color(3, 105, 161), Color.WHITE);

        // Save Score Button (Cyan Blue #0284c7)
        saveScoreBtn = createStyledButton("Save Score", new Color(2, 132, 199), Color.WHITE);

        // Exit Button (Red #dc2626)
        exitBtn = createStyledButton("Exit", new Color(220, 38, 38), Color.WHITE);

        // Sound Toggle Button
        soundToggleBtn = createStyledButton("🔊 Audio: ON", new Color(71, 85, 105), Color.WHITE);
        soundToggleBtn.addActionListener(e -> {
            boolean current = SoundManager.isSoundEnabled();
            SoundManager.setSoundEnabled(!current);
            soundToggleBtn.setText(!current ? "🔊 Audio: ON" : "🔇 Audio: OFF");
        });

        add(startBtn);
        add(pauseBtn);
        add(saveScoreBtn);
        add(exitBtn);
        add(soundToggleBtn);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void setStartAction(ActionListener al) {
        startBtn.addActionListener(al);
    }

    public void setPauseAction(ActionListener al) {
        pauseBtn.addActionListener(al);
    }

    public void setSaveScoreAction(ActionListener al) {
        saveScoreBtn.addActionListener(al);
    }

    public void setExitAction(ActionListener al) {
        exitBtn.addActionListener(al);
    }

    public void setStartButtonText(String text) {
        startBtn.setText(text);
    }

    public void setPauseButtonText(String text) {
        pauseBtn.setText(text);
    }
}
