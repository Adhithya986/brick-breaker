package com.brickbreaker.ui;

import com.brickbreaker.db.HighScoreDAO;
import com.brickbreaker.model.HighScore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog prompting player to save their score into the database.
 */
public class SaveScoreDialog extends JDialog {
    private JTextField nameField;
    private final int score;
    private final int level;
    private final Runnable onScoreSaved;

    public SaveScoreDialog(JFrame parent, int score, int level, Runnable onScoreSaved) {
        super(parent, "Save High Score", true);
        this.score = score;
        this.level = level;
        this.onScoreSaved = onScoreSaved;

        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Save Your Score");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Final Score: " + score + "   |   Level Reached: " + level);
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(100, 116, 139));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(scoreLabel);

        // Input field
        JPanel formPanel = new JPanel(new BorderLayout(8, 8));
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        nameField = new JTextField("Player", 15);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameField.selectAll();

        formPanel.add(nameLabel, BorderLayout.NORTH);
        formPanel.add(nameField, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save Score");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> handleSave());
        nameField.addActionListener(e -> handleSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        contentPane.add(infoPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(saveBtn);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid player name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HighScore hs = new HighScore(name, score, level);
        HighScoreDAO dao = new HighScoreDAO();
        boolean success = dao.insertHighScore(hs);

        if (success) {
            JOptionPane.showMessageDialog(this, "Score saved successfully to database!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            if (onScoreSaved != null) {
                onScoreSaved.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save score. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
