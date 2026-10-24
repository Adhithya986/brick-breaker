# 🎮 Brick Breaker (Java Swing + JDBC & Live Web Deployment)

[![Java CI](https://github.com/Adhithya986/brick-breaker/actions/workflows/ci.yml/badge.svg)](https://github.com/Adhithya986/brick-breaker/actions)
[![Live Demo](https://img.shields.io/badge/Play_Live-GitHub_Pages-brightgreen?style=flat&logo=github)](https://adhithya986.github.io/brick-breaker/)

> 🕹️ **Live Playable Web Version:** [https://adhithya986.github.io/brick-breaker/](https://adhithya986.github.io/brick-breaker/)
>
> 💻 **GitHub Repository:** [https://github.com/Adhithya986/brick-breaker](https://github.com/Adhithya986/brick-breaker)

A classic arcade **Brick Breaker** game built using **Java Swing** and **JDBC**, featuring a companion **HTML5 Web Version** deployed directly to GitHub Pages. The game features smooth 60 FPS physics, multiple level layouts loaded dynamically from a database, high-score tracking with database persistence, a responsive mouse/keyboard control system, particle explosion animations, power-ups, and synthesized sound effects.

---

## 📸 Overview & Features

### 1. Core Features (as specified in assignment)
- **Multiple Levels with Dynamic Brick Layouts**:
  - **Level 1 (Classic Rainbow)**: 5 colorful brick rows (Red, Orange, Yellow, Green, Blue) at base speed.
  - **Level 2 (Pyramid Challenge)**: 6 rows in a pyramid structure with fortified 2-hit bricks and increased ball speed.
  - **Level 3 (Checkerboard Fortress)**: Staggered checkerboard layout with armored bricks.
  - **Level 4 (Armored Diamond)**: Diamond hazard formation with multi-hit bricks.
  - **Level 5 (Cosmic Chaos)**: Dense multi-layer wall with power-ups and maximum ball velocity.
- **Accurate Collision Physics**:
  - Ball rebounds off borders and the paddle with variable angle deflection based on hit location.
  - Directional AABB collision detection against bricks (top, bottom, left, right).
  - Multi-ball support (multiple active balls handled simultaneously).
- **Score & Lives System**:
  - Real-time score counter with level bonuses and brick point values.
  - 3 lives starting count with heart icons; ball resets on paddle when a life is lost.
  - Level completion bonus (+500 pts * Level).
- **JDBC Database Integration**:
  - High scores loaded from and saved to the database.
  - Pre-seeded with sample scores (**Rahul: 2400**, **Sneha: 1900**, **Amit: 1500**).
  - Level configurations dynamically queried from the `levels` table.
- **Event Handling & Swing Components**:
  - **MouseMotionListener**: Paddle follows mouse X coordinate smoothly.
  - **KeyListener**: Arrow keys or `A`/`D` to move paddle, `SPACE` to launch ball or pause, `P` to pause/resume, `R` to restart.
  - **ActionListener**: Menu buttons for **Start**, **Pause**, **Save Score**, **Exit**, and audio toggle.
  - **Timer-based Game Loop**: `javax.swing.Timer` executing at ~60 FPS (16ms interval).

### 2. Extension Features
- 🌟 **Power-Ups System**:
  - **Wide Paddle**: Expands paddle width by 50% with an active duration timer.
  - **Multi-Ball**: Spawns 2 extra balls at divergent angles.
  - **Extra Life**: Grants +1 life (up to 5 hearts).
  - **Fireball**: Ball cuts straight through bricks without bouncing.
  - **Slow Motion**: Temporarily decreases ball speed for high-precision clearing.
  - **Bottom Shield**: Creates a safety energy floor preventing balls from falling.
- 💥 **Visual Particle System**: Shattered bricks emit bursting colored debris with gravity and fading.
- 🔊 **Synthesized Retro Sound Effects**: Generated procedurally using Java Sound API (`javax.sound.sampled`) — zero external MP3/WAV files required! Includes an Audio Mute/Unmute toggle.

---

## 🗄️ Database Architecture (JDBC)

The project includes an embedded SQLite database (`brickbreaker.db`) that works with zero setup, and also supports MySQL via `db.properties`.

### Database Tables:

#### 1. `high_scores`
```sql
CREATE TABLE high_scores (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    player         TEXT NOT NULL,
    score          INTEGER NOT NULL,
    level_reached  INTEGER NOT NULL,
    played_on      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. `levels`
```sql
CREATE TABLE levels (
    level_no    INTEGER PRIMARY KEY,
    brick_rows  INTEGER NOT NULL,
    ball_speed  REAL NOT NULL,
    layout_name TEXT DEFAULT 'Standard'
);
```

---

## 📂 Project Structure

```
├── lib/
│   ├── flatlaf.jar             # Modern Look & Feel library
│   ├── sqlite-jdbc.jar         # SQLite JDBC Driver (default zero-config DB)
│   ├── mysql-connector-j.jar   # MySQL JDBC Driver (optional)
│   ├── slf4j-api.jar           # Logging interface
│   └── slf4j-simple.jar        # Logging implementation
├── src/
│   └── com/brickbreaker/
│       ├── Main.java           # Application entry point & L&F initialization
│       ├── audio/
│       │   └── SoundManager.java    # Procedural synthesized audio engine
│       ├── db/
│       │   ├── DatabaseManager.java # JDBC connection & table initialization
│       │   ├── HighScoreDAO.java    # CRUD operations for high_scores
│       │   └── LevelDAO.java        # Level querying from levels table
│       ├── model/
│       │   ├── Ball.java            # Ball physics, fireball mode, motion trail
│       │   ├── Brick.java           # Durability, point values, 3D render
│       │   ├── HighScore.java       # HighScore data entity
│       │   ├── LevelConfig.java     # Level configuration entity
│       │   ├── Paddle.java          # Paddle movement & wide power-up
│       │   ├── Particle.java        # Explosion physics & alpha fading
│       │   └── PowerUp.java         # Falling bonus capsules
│       └── ui/
│           ├── ControlPanel.java    # Start, Pause, Save Score, Exit buttons
│           ├── GamePanel.java       # Canvas, event listeners, 60 FPS game loop
│           ├── MainFrame.java       # Main JFrame layout & wiring
│           ├── SaveScoreDialog.java # Modal dialog for saving score
│           └── SidePanel.java       # Level selector, Score, Lives, Top Scores JTable
├── db.properties               # Database configuration file (SQLite/MySQL toggle)
├── build.bat                   # Compilation batch script
├── run.bat                     # Windows batch launch script
├── run.ps1                     # PowerShell launch script
└── README.md                   # Documentation
```

---

## 🚀 How to Build & Run

### Prerequisites
- **Java Development Kit (JDK 21 or higher)**. Verify by typing:
  ```powershell
  java -version
  javac -version
  ```

### Quick Start (Windows)
1. **Compile**: Double-click `build.bat` or run:
   ```cmd
   .\build.bat
   ```
2. **Launch**: Double-click `run.bat` or run:
   ```cmd
   .\run.bat
   ```

### Running via Terminal
```powershell
# Compile
javac -cp "lib/*;src" -d bin src\com\brickbreaker\Main.java src\com\brickbreaker\*\*.java

# Run
java --enable-native-access=ALL-UNNAMED -cp "lib/*;bin" com.brickbreaker.Main
```

---

## 🎮 Controls

| Action | Control |
|---|---|
| **Move Paddle** | Move Mouse horizontally OR Left/Right Arrow Keys (`A`/`D`) |
| **Launch Ball** | Left Mouse Click OR `SPACE` |
| **Pause / Resume** | Click **Pause** button OR Press `P` / `SPACE` |
| **Restart Game** | Press `R` OR Click **Start** |
| **Save Score** | Click **Save Score** button |
| **Toggle Audio** | Click **🔊 Audio** button |
| **Select Level** | Use the **Level** dropdown on the right panel |
| **Exit** | Click **Exit** button |
