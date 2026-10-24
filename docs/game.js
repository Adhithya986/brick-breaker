/**
 * Brick Breaker - Web Engine (HTML5 Canvas + Web Audio API)
 * Mirrors the Java Swing + JDBC desktop implementation
 */

// --- Audio Engine (Web Audio API Synthesizer) ---
class WebSoundManager {
    constructor() {
        this.enabled = true;
        this.ctx = null;
    }

    initCtx() {
        if (!this.ctx) {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (AudioCtx) this.ctx = new AudioCtx();
        }
        if (this.ctx && this.ctx.state === 'suspended') {
            this.ctx.resume();
        }
    }

    playTone(freq, duration, type = 'sine', volume = 0.2) {
        if (!this.enabled) return;
        this.initCtx();
        if (!this.ctx) return;

        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = type;
            osc.frequency.setValueAtTime(freq, this.ctx.currentTime);

            gain.gain.setValueAtTime(volume, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start();
            osc.stop(this.ctx.currentTime + duration);
        } catch (e) {}
    }

    playSlide(startFreq, endFreq, duration, volume = 0.25) {
        if (!this.enabled) return;
        this.initCtx();
        if (!this.ctx) return;

        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(startFreq, this.ctx.currentTime);
            osc.frequency.linearRampToValueAtTime(endFreq, this.ctx.currentTime + duration);

            gain.gain.setValueAtTime(volume, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start();
            osc.stop(this.ctx.currentTime + duration);
        } catch (e) {}
    }

    paddleBounce() { this.playSlide(320, 240, 0.08, 0.25); }
    brickHit() { this.playSlide(540, 680, 0.06, 0.25); }
    brickBreak() { this.playSlide(440, 880, 0.10, 0.3); }
    powerUp() {
        this.playTone(523.25, 0.08, 'sine', 0.25);
        setTimeout(() => this.playTone(659.25, 0.08, 'sine', 0.25), 60);
        setTimeout(() => this.playTone(783.99, 0.14, 'sine', 0.3), 120);
    }
    lostLife() { this.playSlide(320, 110, 0.28, 0.35); }
    gameOver() {
        this.playSlide(392, 330, 0.18, 0.35);
        setTimeout(() => this.playSlide(330, 261, 0.22, 0.35), 180);
        setTimeout(() => this.playSlide(261, 146, 0.35, 0.4), 400);
    }
    levelWin() {
        this.playTone(440, 0.09, 'triangle', 0.3);
        setTimeout(() => this.playTone(554, 0.09, 'triangle', 0.3), 80);
        setTimeout(() => this.playTone(659, 0.09, 'triangle', 0.3), 160);
        setTimeout(() => this.playTone(880, 0.25, 'triangle', 0.35), 240);
    }
}

// --- High Score Persistence (Simulating Database) ---
class HighScoreStore {
    constructor() {
        this.storageKey = 'brick_breaker_high_scores';
        this.scores = this.loadScores();
    }

    loadScores() {
        const saved = localStorage.getItem(this.storageKey);
        if (saved) {
            try { return JSON.parse(saved); } catch (e) {}
        }
        // Initial sample scores as per specification sheet
        const initial = [
            { name: 'Rahul', score: 2400, level: 3 },
            { name: 'Sneha', score: 1900, level: 2 },
            { name: 'Amit', score: 1500, level: 2 },
            { name: 'Priya', score: 1150, level: 1 },
            { name: 'Vikram', score: 850, level: 1 }
        ];
        this.saveToStorage(initial);
        return initial;
    }

    saveToStorage(scores) {
        localStorage.setItem(this.storageKey, JSON.stringify(scores));
    }

    addScore(name, score, level) {
        this.scores.push({ name, score, level });
        this.scores.sort((a, b) => b.score - a.score);
        this.scores = this.scores.slice(0, 10);
        this.saveToStorage(this.scores);
    }

    getTopScores() {
        return this.scores;
    }
}

// --- Game Engine ---
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

const sounds = new WebSoundManager();
const scoreStore = new HighScoreStore();

// Game State Enum
const State = { READY: 0, RUNNING: 1, PAUSED: 2, LEVEL_CLEAR: 3, GAME_OVER: 4 };

let gameState = State.READY;
let score = 0;
let lives = 3;
let currentLevel = 1;

// Level configurations
const LEVEL_CONFIGS = {
    1: { name: 'Classic Rainbow', rows: 5, speed: 5.0 },
    2: { name: 'Pyramid', rows: 6, speed: 5.8 },
    3: { name: 'Checkerboard', rows: 6, speed: 6.5 },
    4: { name: 'Armored Diamond', rows: 7, speed: 7.2 },
    5: { name: 'Cosmic Chaos', rows: 8, speed: 8.0 }
};

// Power-up types
const POWER_TYPES = [
    { type: 'WIDE_PADDLE', label: 'WIDE PADDLE', color: '#3b82f6', icon: 'W' },
    { type: 'MULTI_BALL', label: 'MULTI BALL', color: '#ec4899', icon: '+2' },
    { type: 'EXTRA_LIFE', label: 'EXTRA LIFE', color: '#ef4444', icon: '♥' },
    { type: 'FIREBALL', label: 'FIREBALL', color: '#f97316', icon: '🔥' },
    { type: 'SLOW_BALL', label: 'SLOW MOTION', color: '#10b981', icon: 'S' },
    { type: 'SHIELD', label: 'BOTTOM SHIELD', color: '#a855f7', icon: '🛡' }
];

// Entities
let paddle = {
    x: 290,
    y: 495,
    width: 100,
    baseWidth: 100,
    height: 14,
    speed: 9,
    wide: false,
    wideTimer: 0
};

let balls = [];
let bricks = [];
let powerUps = [];
let particles = [];
let floatingTexts = [];
let shieldActive = false;
let shieldTimer = 0;

let keys = { left: false, right: false };

// --- Setup & Initialization ---
function initLevel(levelNo) {
    currentLevel = levelNo;
    const cfg = LEVEL_CONFIGS[levelNo] || LEVEL_CONFIGS[1];
    
    if (levelNo === 1) {
        score = 0;
        lives = 3;
    }
    
    shieldActive = false;
    shieldTimer = 0;

    paddle.width = paddle.baseWidth;
    paddle.wide = false;
    paddle.wideTimer = 0;
    paddle.x = (canvas.width - paddle.width) / 2;

    balls = [{
        x: canvas.width / 2,
        y: paddle.y - 8,
        radius: 7,
        dx: cfg.speed * 0.4,
        dy: -cfg.speed * 0.9,
        speed: cfg.speed,
        trail: [],
        fireball: false,
        fireballTimer: 0
    }];

    powerUps = [];
    particles = [];
    floatingTexts = [];

    buildBricks(cfg);
    gameState = State.READY;
    updateUI();
}

function buildBricks(cfg) {
    bricks = [];
    const rows = cfg.rows;
    const cols = 10;
    const brickW = 58;
    const brickH = 18;
    const startX = (canvas.width - (cols * (brickW + 4))) / 2;
    const startY = 50;

    const rowColors = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#3b82f6', '#a855f7', '#ec4899', '#0ea5e9'];

    for (let r = 0; r < rows; r++) {
        const color = rowColors[r % rowColors.length];
        const pts = (rows - r) * 10;

        for (let c = 0; c < cols; c++) {
            if (cfg.rows === 6 && (r + c) % 2 === 1 && r > 2) continue; // Pyramid
            if (currentLevel === 3 && (r + c) % 2 === 1) continue; // Checkerboard
            if (currentLevel === 4 && r + Math.abs(c - 4.5) > 8) continue; // Diamond

            let hp = 1;
            if (currentLevel >= 2 && r === 0) hp = 2;
            if (currentLevel >= 3 && r <= 1) hp = 2;
            if (currentLevel >= 4 && r === 0) hp = 3;

            let powerType = null;
            if (Math.random() < 0.22) {
                powerType = POWER_TYPES[Math.floor(Math.random() * POWER_TYPES.length)];
            }

            bricks.push({
                x: startX + c * (brickW + 4),
                y: startY + r * (brickH + 4),
                width: brickW,
                height: brickH,
                color: color,
                hp: hp,
                maxHp: hp,
                pts: pts,
                powerType: powerType,
                destroyed: false
            });
        }
    }
}

// --- Physics & Game Loop ---
function gameLoop() {
    update();
    render();
    requestAnimationFrame(gameLoop);
}

function update() {
    if (gameState !== State.RUNNING) {
        if (gameState === State.READY && balls.length > 0) {
            balls[0].x = paddle.x + paddle.width / 2;
            balls[0].y = paddle.y - balls[0].radius;
        }
        return;
    }

    // Keyboard paddle move
    if (keys.left) paddle.x = Math.max(0, paddle.x - paddle.speed);
    if (keys.right) paddle.x = Math.min(canvas.width - paddle.width, paddle.x + paddle.speed);

    // Paddle wide timer
    if (paddle.wide) {
        paddle.wideTimer--;
        if (paddle.wideTimer <= 0) {
            paddle.wide = false;
            paddle.width = paddle.baseWidth;
        }
    }

    // Shield timer
    if (shieldActive) {
        shieldTimer--;
        if (shieldTimer <= 0) shieldActive = false;
    }

    // Update Balls
    for (let i = balls.length - 1; i >= 0; i--) {
        const b = balls[i];

        // Motion trail
        b.trail.unshift({ x: b.x, y: b.y });
        if (b.trail.length > 6) b.trail.pop();

        b.x += b.dx;
        b.y += b.dy;

        if (b.fireball) {
            b.fireballTimer--;
            if (b.fireballTimer <= 0) b.fireball = false;
        }

        // Left/Right Walls
        if (b.x - b.radius <= 0) {
            b.x = b.radius;
            b.dx = Math.abs(b.dx);
            sounds.paddleBounce();
        } else if (b.x + b.radius >= canvas.width) {
            b.x = canvas.width - b.radius;
            b.dx = -Math.abs(b.dx);
            sounds.paddleBounce();
        }

        // Top Wall
        if (b.y - b.radius <= 0) {
            b.y = b.radius;
            b.dy = Math.abs(b.dy);
            sounds.paddleBounce();
        }

        // Bottom Shield
        if (shieldActive && b.y + b.radius >= canvas.height - 6) {
            b.y = canvas.height - 6 - b.radius;
            b.dy = -Math.abs(b.dy);
            sounds.paddleBounce();
            spawnSparks(b.x, canvas.height - 6, '#a855f7', 8);
        }

        // Paddle Collision
        if (b.dy > 0 &&
            b.y + b.radius >= paddle.y &&
            b.y - b.radius <= paddle.y + paddle.height &&
            b.x >= paddle.x &&
            b.x <= paddle.x + paddle.width) {
            
            const hitRatio = (b.x - (paddle.x + paddle.width / 2)) / (paddle.width / 2);
            const angle = -Math.PI / 2 + hitRatio * (Math.PI * 0.38);
            b.dx = Math.cos(angle) * b.speed;
            b.dy = Math.sin(angle) * b.speed;
            b.y = paddle.y - b.radius;
            sounds.paddleBounce();
            spawnSparks(b.x, paddle.y, '#ffffff', 6);
        }

        // Brick Collisions
        for (let br of bricks) {
            if (br.destroyed) continue;

            if (b.x + b.radius >= br.x &&
                b.x - b.radius <= br.x + br.width &&
                b.y + b.radius >= br.y &&
                b.y - b.radius <= br.y + br.height) {

                br.hp--;
                if (br.hp <= 0) {
                    br.destroyed = true;
                    score += br.pts * currentLevel;
                    sounds.brickBreak();
                    spawnSparks(br.x + br.width / 2, br.y + br.height / 2, br.color, 14);
                    floatingTexts.push({ text: `+${br.pts * currentLevel}`, x: br.x + 10, y: br.y, color: br.color, life: 40 });

                    if (br.powerType) {
                        powerUps.push({
                            x: br.x + br.width / 2 - 18,
                            y: br.y,
                            width: 36,
                            height: 20,
                            dy: 2.4,
                            info: br.powerType
                        });
                    }
                } else {
                    sounds.brickHit();
                    spawnSparks(b.x, b.y, br.color, 5);
                }

                if (!b.fireball) {
                    const prevX = b.x - b.dx;
                    const prevY = b.y - b.dy;
                    if (prevX + b.radius <= br.x || prevX - b.radius >= br.x + br.width) {
                        b.dx = -b.dx;
                    } else {
                        b.dy = -b.dy;
                    }
                }

                updateUI();
                break;
            }
        }

        // Lost below screen
        if (b.y - b.radius > canvas.height) {
            balls.splice(i, 1);
        }
    }

    // Check life lost
    if (balls.length === 0) {
        lives--;
        updateUI();

        if (lives > 0) {
            sounds.lostLife();
            const cfg = LEVEL_CONFIGS[currentLevel];
            balls.push({
                x: paddle.x + paddle.width / 2,
                y: paddle.y - 8,
                radius: 7,
                dx: cfg.speed * 0.4,
                dy: -cfg.speed * 0.9,
                speed: cfg.speed,
                trail: [],
                fireball: false,
                fireballTimer: 0
            });
            gameState = State.READY;
        } else {
            sounds.gameOver();
            gameState = State.GAME_OVER;
            document.getElementById('startBtn').innerText = 'Start';
        }
    }

    // Update Power-ups
    for (let i = powerUps.length - 1; i >= 0; i--) {
        const p = powerUps[i];
        p.y += p.dy;

        // Paddle catch
        if (p.y + p.height >= paddle.y &&
            p.y <= paddle.y + paddle.height &&
            p.x + p.width >= paddle.x &&
            p.x <= paddle.x + paddle.width) {
            
            applyPowerUp(p.info);
            powerUps.splice(i, 1);
        } else if (p.y > canvas.height) {
            powerUps.splice(i, 1);
        }
    }

    // Update Particles
    for (let i = particles.length - 1; i >= 0; i--) {
        const pt = particles[i];
        pt.x += pt.dx;
        pt.y += pt.dy;
        pt.dy += 0.14;
        pt.life--;
        if (pt.life <= 0) particles.splice(i, 1);
    }

    // Update Floating Text
    for (let i = floatingTexts.length - 1; i >= 0; i--) {
        const ft = floatingTexts[i];
        ft.y -= 1.2;
        ft.life--;
        if (ft.life <= 0) floatingTexts.splice(i, 1);
    }

    // Check Level Complete
    const allCleared = bricks.length > 0 && bricks.every(b => b.destroyed);
    if (allCleared) {
        sounds.levelWin();
        score += 500 * currentLevel;
        updateUI();
        gameState = State.LEVEL_CLEAR;
        document.getElementById('startBtn').innerText = 'Next Level';
    }
}

function applyPowerUp(info) {
    sounds.powerUp();
    floatingTexts.push({ text: `${info.label}!`, x: paddle.x + 10, y: paddle.y - 12, color: info.color, life: 55 });

    switch (info.type) {
        case 'WIDE_PADDLE':
            paddle.wide = true;
            paddle.width = paddle.baseWidth * 1.5;
            paddle.wideTimer = 60 * 12;
            break;
        case 'MULTI_BALL':
            if (balls.length > 0) {
                const ref = balls[0];
                balls.push({
                    x: ref.x, y: ref.y, radius: ref.radius,
                    dx: ref.speed * 0.8, dy: -ref.speed * 0.6,
                    speed: ref.speed, trail: [], fireball: false, fireballTimer: 0
                });
                balls.push({
                    x: ref.x, y: ref.y, radius: ref.radius,
                    dx: -ref.speed * 0.8, dy: -ref.speed * 0.6,
                    speed: ref.speed, trail: [], fireball: false, fireballTimer: 0
                });
            }
            break;
        case 'EXTRA_LIFE':
            lives = Math.min(5, lives + 1);
            updateUI();
            break;
        case 'FIREBALL':
            balls.forEach(b => { b.fireball = true; b.fireballTimer = 60 * 8; });
            break;
        case 'SLOW_BALL':
            balls.forEach(b => { b.speed = Math.max(3.5, b.speed * 0.75); });
            break;
        case 'SHIELD':
            shieldActive = true;
            shieldTimer = 60 * 12;
            break;
    }
}

function spawnSparks(x, y, color, count) {
    for (let i = 0; i < count; i++) {
        const angle = Math.random() * Math.PI * 2;
        const spd = 1.0 + Math.random() * 4.0;
        particles.push({
            x, y,
            dx: Math.cos(angle) * spd,
            dy: Math.sin(angle) * spd - 1,
            color,
            size: 2 + Math.floor(Math.random() * 4),
            life: 20 + Math.floor(Math.random() * 20),
            maxLife: 40
        });
    }
}

// --- Rendering ---
function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Subtle background grid
    ctx.strokeStyle = 'rgba(30, 41, 59, 0.4)';
    ctx.lineWidth = 1;
    for (let x = 0; x < canvas.width; x += 40) {
        ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke();
    }
    for (let y = 0; y < canvas.height; y += 40) {
        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke();
    }

    // Shield
    if (shieldActive) {
        ctx.strokeStyle = '#a855f7';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(0, canvas.height - 4);
        ctx.lineTo(canvas.width, canvas.height - 4);
        ctx.stroke();
    }

    // Bricks
    for (let br of bricks) {
        if (br.destroyed) continue;

        // Beveled brick gradient
        const grad = ctx.createLinearGradient(br.x, br.y, br.x, br.y + br.height);
        grad.addColorStop(0, br.color);
        grad.addColorStop(1, '#0f172a');

        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.roundRect(br.x, br.y, br.width, br.height, 4);
        ctx.fill();

        // 3D Bevel Highlight
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.35)';
        ctx.lineWidth = 1.5;
        ctx.stroke();

        // Power-up badge
        if (br.powerType) {
            ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
            ctx.beginPath();
            ctx.arc(br.x + br.width / 2, br.y + br.height / 2, 3, 0, Math.PI * 2);
            ctx.fill();
        }

        // Cracks
        if (br.maxHp > 1 && br.hp < br.maxHp) {
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.lineWidth = 1.2;
            ctx.beginPath();
            ctx.moveTo(br.x + 6, br.y + 4);
            ctx.lineTo(br.x + br.width / 2, br.y + br.height / 2);
            ctx.lineTo(br.x + br.width - 6, br.y + br.height - 4);
            ctx.stroke();
        }
    }

    // Power-ups
    for (let p of powerUps) {
        ctx.fillStyle = p.info.color;
        ctx.beginPath();
        ctx.roundRect(p.x, p.y, p.width, p.height, 8);
        ctx.fill();
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 1.2;
        ctx.stroke();

        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 11px Outfit, sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(p.info.icon, p.x + p.width / 2, p.y + p.height / 2);
    }

    // Paddle
    const padGrad = ctx.createLinearGradient(paddle.x, paddle.y, paddle.x, paddle.y + paddle.height);
    padGrad.addColorStop(0, paddle.wide ? '#93c5fd' : '#f1f5f9');
    padGrad.addColorStop(1, paddle.wide ? '#2563eb' : '#94a3b8');

    ctx.fillStyle = padGrad;
    ctx.beginPath();
    ctx.roundRect(paddle.x, paddle.y, paddle.width, paddle.height, 7);
    ctx.fill();
    ctx.strokeStyle = paddle.wide ? '#3b82f6' : '#cbd5e1';
    ctx.lineWidth = 1.2;
    ctx.stroke();

    // Paddle timer bar
    if (paddle.wide && paddle.wideTimer > 0) {
        const barW = paddle.width * (paddle.wideTimer / (60 * 12));
        ctx.fillStyle = '#3b82f6';
        ctx.fillRect(paddle.x + (paddle.width - barW) / 2, paddle.y - 4, barW, 2);
    }

    // Balls
    for (let b of balls) {
        // Trail
        b.trail.forEach((t, idx) => {
            const alpha = 0.4 * (1 - idx / b.trail.length);
            ctx.fillStyle = b.fireball ? `rgba(249, 115, 22, ${alpha})` : `rgba(148, 163, 184, ${alpha})`;
            ctx.beginPath();
            ctx.arc(t.x, t.y, Math.max(2, b.radius * (1 - idx / 8)), 0, Math.PI * 2);
            ctx.fill();
        });

        // Ball Glow & Body
        ctx.fillStyle = b.fireball ? '#f97316' : '#ffffff';
        ctx.beginPath();
        ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
        ctx.beginPath();
        ctx.arc(b.x - b.radius * 0.3, b.y - b.radius * 0.3, b.radius * 0.4, 0, Math.PI * 2);
        ctx.fill();
    }

    // Particles
    for (let pt of particles) {
        const alpha = Math.max(0, pt.life / pt.maxLife);
        ctx.fillStyle = pt.color;
        ctx.globalAlpha = alpha;
        ctx.fillRect(pt.x, pt.y, pt.size, pt.size);
    }
    ctx.globalAlpha = 1.0;

    // Floating text
    for (let ft of floatingTexts) {
        ctx.fillStyle = ft.color;
        ctx.globalAlpha = Math.max(0, ft.life / 40);
        ctx.font = 'bold 12px Outfit, sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(ft.text, ft.x, ft.y);
    }
    ctx.globalAlpha = 1.0;

    // Overlay Banners
    if (gameState === State.READY) {
        drawBanner('CLICK OR PRESS SPACE TO LAUNCH', 'Move Mouse or use Left/Right Arrows to steer', '#3b82f6');
    } else if (gameState === State.PAUSED) {
        drawBanner('PAUSED', "Press 'P' or Click Resume to Continue", '#f59e0b');
    } else if (gameState === State.LEVEL_CLEAR) {
        drawBanner('LEVEL COMPLETED! +500 BONUS', 'Click Start or Press SPACE for Next Level', '#22c55e');
    } else if (gameState === State.GAME_OVER) {
        drawBanner('GAME OVER', "Click 'Save Score' or 'Start' to Play Again", '#ef4444');
    }
}

function drawBanner(title, subtitle, accent) {
    const w = 460;
    const h = 100;
    const x = (canvas.width - w) / 2;
    const y = (canvas.height - h) / 2 + 30;

    ctx.fillStyle = 'rgba(15, 23, 42, 0.92)';
    ctx.beginPath();
    ctx.roundRect(x, y, w, h, 14);
    ctx.fill();

    ctx.strokeStyle = accent;
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 20px Outfit, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(title, x + w / 2, y + 42);

    ctx.fillStyle = '#cbd5e1';
    ctx.font = '13px Outfit, sans-serif';
    ctx.fillText(subtitle, x + w / 2, y + 72);
}

// --- UI Sync & Event Listeners ---
function updateUI() {
    document.getElementById('scoreDisplay').innerText = score;
    
    let hearts = '';
    for (let i = 0; i < lives && i < 5; i++) hearts += '♥';
    document.getElementById('livesDisplay').innerText = `${lives} ${hearts}`;

    // Update top scores table
    const tbody = document.getElementById('scoresTableBody');
    tbody.innerHTML = '';
    const topScores = scoreStore.getTopScores();
    topScores.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${s.name}</td><td>${s.score}</td>`;
        tbody.appendChild(tr);
    });
}

// Mouse Controls
canvas.addEventListener('mousemove', e => {
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const mouseX = (e.clientX - rect.left) * scaleX;
    
    paddle.x = Math.max(0, Math.min(canvas.width - paddle.width, mouseX - paddle.width / 2));
});

canvas.addEventListener('click', () => {
    if (gameState === State.READY) {
        gameState = State.RUNNING;
        document.getElementById('startBtn').innerText = 'Start';
    } else if (gameState === State.LEVEL_CLEAR) {
        initLevel(currentLevel >= 5 ? 1 : currentLevel + 1);
        document.getElementById('levelSelect').value = currentLevel;
        document.getElementById('startBtn').innerText = 'Start';
    }
});

// Keyboard Controls
window.addEventListener('keydown', e => {
    if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') keys.left = true;
    if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') keys.right = true;

    if (e.code === 'Space') {
        e.preventDefault();
        if (gameState === State.READY) {
            gameState = State.RUNNING;
        } else if (gameState === State.LEVEL_CLEAR) {
            initLevel(currentLevel >= 5 ? 1 : currentLevel + 1);
            document.getElementById('levelSelect').value = currentLevel;
        } else if (gameState === State.RUNNING) {
            gameState = State.PAUSED;
            document.getElementById('pauseBtn').innerText = 'Resume';
        } else if (gameState === State.PAUSED) {
            gameState = State.RUNNING;
            document.getElementById('pauseBtn').innerText = 'Pause';
        }
    } else if (e.key === 'p' || e.key === 'P') {
        togglePause();
    } else if (e.key === 'r' || e.key === 'R') {
        initLevel(currentLevel);
    }
});

window.addEventListener('keyup', e => {
    if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') keys.left = false;
    if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') keys.right = false;
});

function togglePause() {
    if (gameState === State.RUNNING) {
        gameState = State.PAUSED;
        document.getElementById('pauseBtn').innerText = 'Resume';
    } else if (gameState === State.PAUSED) {
        gameState = State.RUNNING;
        document.getElementById('pauseBtn').innerText = 'Pause';
    }
}

// Button actions
document.getElementById('startBtn').addEventListener('click', () => {
    if (gameState === State.READY) {
        gameState = State.RUNNING;
    } else if (gameState === State.PAUSED) {
        gameState = State.RUNNING;
        document.getElementById('pauseBtn').innerText = 'Pause';
    } else if (gameState === State.LEVEL_CLEAR) {
        initLevel(currentLevel >= 5 ? 1 : currentLevel + 1);
        document.getElementById('levelSelect').value = currentLevel;
        document.getElementById('startBtn').innerText = 'Start';
    } else if (gameState === State.GAME_OVER) {
        initLevel(currentLevel);
    }
});

document.getElementById('pauseBtn').addEventListener('click', togglePause);

document.getElementById('levelSelect').addEventListener('change', e => {
    initLevel(parseInt(e.target.value));
});

// Audio Toggle
document.getElementById('audioBtn').addEventListener('click', () => {
    sounds.enabled = !sounds.enabled;
    document.getElementById('audioBtn').innerText = sounds.enabled ? '🔊 Audio: ON' : '🔇 Audio: OFF';
});

// Save Score Modal
const modal = document.getElementById('saveModal');
const playerNameInput = document.getElementById('playerNameInput');
const modalSummary = document.getElementById('modalScoreSummary');

document.getElementById('saveScoreBtn').addEventListener('click', () => {
    modalSummary.innerText = `Final Score: ${score} | Level Reached: ${currentLevel}`;
    modal.classList.remove('hidden');
    playerNameInput.focus();
    playerNameInput.select();
});

document.getElementById('modalCancelBtn').addEventListener('click', () => {
    modal.classList.add('hidden');
});

document.getElementById('modalSaveBtn').addEventListener('click', () => {
    const name = playerNameInput.value.trim() || 'Player';
    scoreStore.addScore(name, score, currentLevel);
    updateUI();
    modal.classList.add('hidden');
});

document.getElementById('exitBtn').addEventListener('click', () => {
    if (confirm('Restart game from Level 1?')) {
        initLevel(1);
        document.getElementById('levelSelect').value = 1;
    }
});

// Start game on load
initLevel(1);
requestAnimationFrame(gameLoop);
