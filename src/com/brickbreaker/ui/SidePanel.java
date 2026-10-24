package com.brickbreaker.ui;

import com.brickbreaker.db.HighScoreDAO;
import com.brickbreaker.db.LevelDAO;
import com.brickbreaker.model.HighScore;
import com.brickbreaker.model.LevelConfig;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sidebar panel displaying Level selection, Score, Lives, and Top Scores table from the database.
 */
public class SidePanel extends JPanel {
    private JComboBox<LevelConfig> levelCombo;
    private JLabel scoreValueLabel;
    private JLabel livesValueLabel;
    private JTable topScoresTable;
    private DefaultTableModel tableModel;

    private final HighScoreDAO highScoreDAO = new HighScoreDAO();
    private final LevelDAO levelDAO = new LevelDAO();
    private Consumer<Integer> onLevelSelected;

    public SidePanel() {
        setPreferredSize(new Dimension(240, 560));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setBackground(new Color(241, 245, 249)); // Soft modern slate-gray background

        initComponents();
        loadLevels();
        refreshHighScores();
    }

    private void initComponents() {
        // --- Level Selection ---
        JPanel levelPanel = new JPanel(new BorderLayout(5, 5));
        levelPanel.setOpaque(false);
        JLabel levelLabel = new JLabel("Level");
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        levelLabel.setForeground(new Color(30, 41, 59));

        levelCombo = new JComboBox<>();
        levelCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        levelCombo.addActionListener(e -> {
            LevelConfig selected = (LevelConfig) levelCombo.getSelectedItem();
            if (selected != null && onLevelSelected != null) {
                onLevelSelected.accept(selected.getLevelNo());
            }
        });

        levelPanel.add(levelLabel, BorderLayout.NORTH);
        levelPanel.add(levelCombo, BorderLayout.CENTER);
        add(levelPanel);
        add(Box.createVerticalStrut(18));

        // --- Score & Lives Stats Card ---
        JPanel statsCard = new JPanel(new GridLayout(2, 2, 8, 12));
        statsCard.setOpaque(true);
        statsCard.setBackground(Color.WHITE);
        statsCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel scoreTitle = new JLabel("Score");
        scoreTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        scoreTitle.setForeground(new Color(30, 41, 59));

        scoreValueLabel = new JLabel("0", SwingConstants.RIGHT);
        scoreValueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreValueLabel.setForeground(new Color(15, 23, 42));

        JLabel livesTitle = new JLabel("Lives");
        livesTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        livesTitle.setForeground(new Color(30, 41, 59));

        livesValueLabel = new JLabel("3", SwingConstants.RIGHT);
        livesValueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        livesValueLabel.setForeground(new Color(225, 29, 72)); // Crimson red

        statsCard.add(scoreTitle);
        statsCard.add(scoreValueLabel);
        statsCard.add(livesTitle);
        statsCard.add(livesValueLabel);

        add(statsCard);
        add(Box.createVerticalStrut(20));

        // --- Top Scores Table ---
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);

        JLabel topScoresTitle = new JLabel("Top Scores");
        topScoresTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        topScoresTitle.setForeground(new Color(30, 41, 59));
        topScoresTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        tableContainer.add(topScoresTitle, BorderLayout.NORTH);

        String[] columnNames = {"Name", "Score"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        topScoresTable = new JTable(tableModel);
        topScoresTable.setRowHeight(26);
        topScoresTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        topScoresTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        topScoresTable.getTableHeader().setBackground(new Color(219, 234, 254)); // Soft light blue header
        topScoresTable.getTableHeader().setForeground(new Color(30, 58, 138));
        topScoresTable.setShowGrid(true);
        topScoresTable.setGridColor(new Color(226, 232, 240));

        // Center align table contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        topScoresTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer scoreRenderer = new DefaultTableCellRenderer();
        scoreRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        topScoresTable.getColumnModel().getColumn(1).setCellRenderer(scoreRenderer);

        JScrollPane scrollPane = new JScrollPane(topScoresTable);
        scrollPane.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(200, 220));

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer);
    }

    private void loadLevels() {
        levelCombo.removeAllItems();
        List<LevelConfig> levels = levelDAO.getAllLevels();
        for (LevelConfig cfg : levels) {
            levelCombo.addItem(cfg);
        }
    }

    public void refreshHighScores() {
        tableModel.setRowCount(0);
        List<HighScore> scores = highScoreDAO.getTopScores(10);
        for (HighScore hs : scores) {
            tableModel.addRow(new Object[]{hs.getPlayer(), hs.getScore()});
        }
    }

    public void updateScore(int score) {
        scoreValueLabel.setText(String.valueOf(score));
    }

    public void updateLives(int lives) {
        StringBuilder sb = new StringBuilder();
        sb.append(lives).append(" ");
        for (int i = 0; i < lives && i < 5; i++) {
            sb.append("♥");
        }
        livesValueLabel.setText(sb.toString());
    }

    public void setLevelSelected(int levelNo) {
        for (int i = 0; i < levelCombo.getItemCount(); i++) {
            LevelConfig cfg = levelCombo.getItemAt(i);
            if (cfg.getLevelNo() == levelNo) {
                levelCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    public void setOnLevelSelected(Consumer<Integer> listener) {
        this.onLevelSelected = listener;
    }
}
