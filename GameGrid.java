package Bomberman;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter; // เพิ่ม import นี้
import java.awt.event.MouseEvent; // เพิ่ม import นี้
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections; // สำหรับ Collections.synchronizedList
import java.util.List;
import java.util.Random;

public class GameGrid extends JPanel {
    private int rows;
    private int cols;
    private int cellSize;
    private Color[][] cellColors;
    private Player player; // สร้าง instance ของ Player แทน

    public List<PowerUp> activePowerUps; // รายการ Power-up ที่อยู่บนแผนที่
    private List<Enemy> enemies; // รายการศัตรู

    // *** เพิ่มส่วนสำหรับ Socket Leaderboard ***
    private static final String SERVER_IP = "localhost"; // IP Address ของ Server (ถ้าอยู่เครื่องเดียวกันใช้ localhost)
    private static final int SERVER_PORT = 5000; // Port เดียวกับที่ Leaderboard Server ใช้

    // *** เพิ่มตัวแปรสำหรับชื่อผู้เล่น (อาจจะรับจาก Input หรือกำหนดค่าเริ่มต้น) ***
    private String playerName = "Player1"; // กำหนดชื่อผู้เล่นเริ่มต้น หรือให้ผู้ใช้ป้อน
    private int score; // คะแนนของผู้เล่น
    // *** เพิ่ม Game State Enum ***

    public enum GameState {
        MENU,
        PLAYING,
        GAME_OVER
    }

    private GameState currentGameState; // ตัวแปรเก็บสถานะปัจจุบัน

    private Timer gameTimer;
    private final int FRAME_RATE = 60; // 60 เฟรมต่อวินาที
    private final int MAX_ENEMIES = 4; // จำนวนศัตรูสูงสุดที่ต้องการ

    // *** UI-related constants ***
    private final int UI_HEIGHT = 60; // ความสูงสำหรับ UI ด้านบน
    private final int MAP_OFFSET_Y = UI_HEIGHT; // Map จะเริ่มวาดหลังจาก UI

    private Image playerImage;
    private Image bombImage;
    private Image enemyImage;

    // *** เพิ่ม Image สำหรับ Power-ups ***
    private Image bombPowerUpImage;
    private Image explosionPowerUpImage; // ใช้สำหรับ EXPLOSION_RANGE (fire.png)
    private Image speedPowerUpImage;

    public char[][] map;

    public GameGrid(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.cellColors = new Color[rows][cols];

        playerImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/player.png").getImage();
        bombImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/bomb.gif").getImage();
        enemyImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/enemy.png").getImage();

        try {
            bombPowerUpImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/bomb_powerup.png")
                    .getImage();
            explosionPowerUpImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/fire_powerup.png")
                    .getImage();
            speedPowerUpImage = new ImageIcon("C:/Users/san_p/working/cn311/proj/Bomberman/images/speed_powerup.png")
                    .getImage();
        } catch (Exception e) {
            System.err.println("Error loading power-up images: " + e.getMessage());
            // ตั้งค่าเป็น null หรือใช้รูปภาพ placeholder หากโหลดไม่ได้
            bombPowerUpImage = null;
            explosionPowerUpImage = null;
            speedPowerUpImage = null;
        }

        // Initialize Lists (สำคัญ: ต้องสร้างก่อนนำไปใช้)
        activePowerUps = Collections.synchronizedList(new ArrayList<>()); // ทำให้ thread-safe
        enemies = Collections.synchronizedList(new ArrayList<>()); // ทำให้ thread-safe

        // *** กำหนดสถานะเริ่มต้นเป็น MENU ***
        currentGameState = GameState.MENU;
        // setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        initializeMap();
        player = new Player(1, 1);
        score = 0; // คะแนนเริ่มต้น

        // เคลียร์พื้นที่รอบผู้เล่นหลังจากสร้างแผนที่
        clearPlayerSpawnArea(player.getRow(), player.getCol(), 1); // เคลียร์ 3x3 รอบผู้เล่น
        generateRandomBoxes(30);
        spawnEnemies(3);

        setFocusable(true); // เพิ่มเพื่อให้รับ KeyEvent ได้
        // ใช้ KeyListener สำหรับการจัดการสถานะการกด/ปล่อยปุ่ม
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> player.setMovingUp(true);
                    case KeyEvent.VK_DOWN -> player.setMovingDown(true);
                    case KeyEvent.VK_LEFT -> player.setMovingLeft(true);
                    case KeyEvent.VK_RIGHT -> player.setMovingRight(true);
                    case KeyEvent.VK_SPACE -> placeBomb();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> player.setMovingUp(false);
                    case KeyEvent.VK_DOWN -> player.setMovingDown(false);
                    case KeyEvent.VK_LEFT -> player.setMovingLeft(false);
                    case KeyEvent.VK_RIGHT -> player.setMovingRight(false);
                }
            }
        });
        // *** เพิ่ม MouseListener สำหรับปุ่ม ***
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                if (currentGameState == GameState.MENU) {
                    // ตำแหน่งปุ่ม Start
                    int buttonWidth = 150;
                    int buttonHeight = 50;
                    int x = (getWidth() - buttonWidth) / 2;
                    int y = (getHeight() - buttonHeight) / 2;

                    if (mouseX >= x && mouseX <= x + buttonWidth &&
                            mouseY >= y && mouseY <= y + buttonHeight) {
                        startGame();
                    }
                } else if (currentGameState == GameState.GAME_OVER) {
                    // ตำแหน่งปุ่ม Retry
                    int retryButtonWidth = 150;
                    int retryButtonHeight = 50;
                    int retryX = (getWidth() - retryButtonWidth) / 2;
                    int retryY = (getHeight() - retryButtonHeight) / 2 + 60; // ใต้ข้อความ Game Over

                    if (mouseX >= retryX && mouseX <= retryX + retryButtonWidth &&
                            mouseY >= retryY && mouseY <= retryY + retryButtonHeight) {
                        startGame(); // Retry คือการเริ่มเกมใหม่
                    }
                }
            }
        });
        // กำหนดขนาดของ JPanel ให้เหมาะสมกับ UI และ Map
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize + UI_HEIGHT)); // เพิ่มความสูงสำหรับ UI
        // *** สร้างและเริ่ม Game Timer ***
        gameTimer = new Timer(1000 / FRAME_RATE, e -> updateGame()); // Timer จะเรียก updateGame() ทุกๆ 1/FRAME_RATE //
                                                                     // วินาที
        gameTimer.start();

    }

    // *** เมธอดใหม่: เริ่มเกม ***
    private void startGame() {
        // Reset เกม
        initializeMap();
        player = new Player(1, 1); // สร้างผู้เล่นใหม่
        score = 0;
        activePowerUps.clear(); // ล้าง Power-up เก่า

        // หยุดและลบศัตรูเก่าทั้งหมด
        synchronized (enemies) {
            for (Enemy enemy : enemies) {
                enemy.stopEnemy();
            }
            enemies.clear(); // ล้างรายการศัตรู
        }

        clearPlayerSpawnArea(player.getRow(), player.getCol(), 1);
        generateRandomBoxes(30);

        // --- ส่วนที่เพิ่มเข้ามาเพื่อรับชื่อผู้เล่น ---
        playerName = JOptionPane.showInputDialog(this, "Enter your name for the leaderboard:", "Player Name",
                JOptionPane.QUESTION_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player1"; // กำหนดชื่อเริ่มต้นถ้าผู้เล่นยกเลิกหรือป้อนค่าว่าง
        }
        // หากคลาส Player ของคุณมีเมธอด setName() คุณสามารถเรียกใช้ได้
        // player.setName(playerName);
        // --- สิ้นสุดส่วนที่เพิ่มเข้ามา ---

        currentGameState = GameState.PLAYING; // เปลี่ยนสถานะเป็นกำลังเล่น
        System.out.println("Game Started!");
        System.out.println("Player Name: " + playerName); // แสดงชื่อผู้เล่นเมื่อเกมเริ่ม

        // *** เพิ่มบรรทัดนี้: เริ่ม Timer เกมอีกครั้ง ***
        gameTimer.start(); // สั่งให้ Game Timer เริ่มทำงาน (เรียก updateGame() ซ้ำๆ)
    }

    private void updateGame() {
        if (currentGameState != GameState.PLAYING) { // เฉพาะเมื่ออยู่ในโหมดเล่นเท่านั้น
            repaint(); // ยังคง repaint เพื่อแสดง UI
            return;
        }

        updatePlayerMovement();
        checkEnemyCollision();
        updatePlayerInvulnerability();
        manageEnemySpawning();
        checkAndCollectPowerUp(); // ตรวจสอบและเก็บ Power-up

        if (!player.isAlive()) {
            setGameOver();
        }

        repaint();
    }

    // *** เมธอดสำหรับจัดการการเกิดของศัตรูใหม่ ***
    private void manageEnemySpawning() {
        synchronized (enemies) {
            if (enemies.size() < MAX_ENEMIES) {
                spawnSingleEnemy(); // สร้างศัตรูเพิ่มทีละตัว
            }
        }
    }

    // เมธอดสำหรับสร้างศัตรู 1 ตัว
    private void spawnSingleEnemy() {
        Random rand = new Random();
        int attempts = 0;
        final int MAX_ATTEMPTS = 50; // จำกัดการลองสุ่ม เพื่อป้องกัน infinite loop

        while (attempts < MAX_ATTEMPTS) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            // ตรวจสอบว่าตำแหน่งนั้นเป็นช่องว่าง และไม่ได้อยู่ใกล้ผู้เล่นมากเกินไป
            if (map[r][c] == ' ' &&
                    !(r >= player.getRow() - 2 && r <= player.getRow() + 2 && // ไม่ใกล้ผู้เล่น 5x5
                            c >= player.getCol() - 2 && c <= player.getCol() + 2)) {

                boolean positionOccupied = false;
                synchronized (enemies) { // ตรวจสอบว่ามีศัตรูตัวอื่นอยู่ที่นั่นหรือไม่
                    for (Enemy existingEnemy : enemies) {
                        if (existingEnemy.getRow() == r && existingEnemy.getCol() == c) {
                            positionOccupied = true;
                            break;
                        }
                    }
                }

                // ตรวจสอบ Power-up
                synchronized (activePowerUps) {
                    for (PowerUp pu : activePowerUps) {
                        if (pu.getRow() == r && pu.getCol() == c) {
                            positionOccupied = true;
                            break;
                        }
                    }
                }

                if (!positionOccupied) {
                    Enemy newEnemy = new Enemy(map, r, c, this);
                    enemies.add(newEnemy);
                    newEnemy.start(); // เริ่ม thread ของศัตรู
                    System.out.println("Spawned new enemy at (" + r + "," + c + ")");
                    return; // ออกจากเมธอดเมื่อวางศัตรูได้แล้ว
                }
            }
            attempts++;
        }
        System.out.println("Could not find a valid spawn spot for enemy after " + MAX_ATTEMPTS + " attempts.");
    }

    // เมธอดสำหรับสร้างศัตรู
    private void spawnEnemies(int numEnemies) {
        Random rand = new Random();
        int enemiesPlaced = 0;
        while (enemiesPlaced < numEnemies) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            // ตรวจสอบว่าตำแหน่งนั้นไม่ใช่กำแพง, ไม่ใช่กล่อง, ไม่ใช่ตำแหน่งผู้เล่น,
            // และไม่ใช่ตำแหน่งที่ระเบิดจะลง
            if (map[r][c] == ' ' && !(r == player.getRow() && c == player.getCol())) {
                boolean validSpawn = true;
                // ตรวจสอบรอบๆ จุดเกิดผู้เล่นด้วย
                if (r >= player.getRow() - 1 && r <= player.getRow() + 1 &&
                        c >= player.getCol() - 1 && c <= player.getCol() + 1) {
                    validSpawn = false;
                }
                // ตรวจสอบว่าไม่มีศัตรูตัวอื่นอยู่ที่นั่น
                for (Enemy existingEnemy : enemies) {
                    if (existingEnemy.getRow() == r && existingEnemy.getCol() == c) {
                        validSpawn = false;
                        break;
                    }
                }

                if (validSpawn) {
                    Enemy newEnemy = new Enemy(map, r, c, this);
                    enemies.add(newEnemy);
                    newEnemy.start(); // เริ่ม thread ของศัตรู
                    enemiesPlaced++;
                }
            }
        }
    }

    // เมธอดสำหรับเข้าถึง List ของศัตรู (ให้ Bomb เข้าถึงได้)
    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setGameOver() {
        if (currentGameState != GameState.GAME_OVER) { // ป้องกันการเรียกซ้ำ
            currentGameState = GameState.GAME_OVER;
            gameTimer.stop(); // หยุด game loop
            System.out.println("GAME OVER! Final Score: " + score);

            // *** ส่งคะแนนไปที่ Leaderboard Server ***
            sendScoreToLeaderboard(playerName, score);

            // *** ดึง Leaderboard มาแสดง ***
            requestLeaderboard();

            repaint(); // สั่งให้ JPanel วาดใหม่เพื่อแสดงหน้าจอ Game Over และ Leaderboard ที่อัปเดต
            System.out.println("Game Over! Final Score: " + score);
        }
    }

    private void checkEnemyCollision() {
        if (player.isInvulnerable()) {
            return;
        }
        synchronized (enemies) {
            for (Enemy enemy : enemies) {
                if (player.getRow() == enemy.getRow() && player.getCol() == enemy.getCol()) {
                    player.takeDamage();
                    return;
                }
            }
        }
    }

    private void updatePlayerInvulnerability() {
        if (player.isInvulnerable()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - player.getInvulnerableStartTime() >= player.getInvulnerabilityDuration()) {
                player.setInvulnerable(false);
                System.out.println("Player no longer invulnerable.");
            }
        }
    }

    public void setCellColor(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            cellColors[row][col] = color;
            repaint();
        }
    }

    private void generateRandomBoxes(int numBoxes) {
        Random rand = new Random();
        int boxesPlaced = 0;

        while (boxesPlaced < numBoxes) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            // เงื่อนไขเพิ่มเติม: ไม่วางกล่องในพื้นที่เริ่มต้นของผู้เล่น
            // ตรวจสอบว่าไม่อยู่ในบล็อก 3x3 รอบ (playerRow, playerCol) หรือไม่
            if (r >= player.getRow() - 1 && r <= player.getRow() + 1 &&
                    c >= player.getCol() - 1 && c <= player.getCol() + 1) {
                continue; // ข้ามการสุ่มนี้ ถ้าอยู่ในพื้นที่เริ่มต้น
            }

            if (map[r][c] == ' ') { // วางเฉพาะในช่องว่าง
                map[r][c] = 'X';
                boxesPlaced++;
            }
        }
    }

    // *** ปรับปรุง paintComponent เพื่อวาด UI และจัดการ Player กระพริบ ***
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // --- วาด UI ด้านบน ---
        g.setColor(Color.BLACK); // พื้นหลัง UI
        g.fillRect(0, 0, getWidth(), UI_HEIGHT); // วาดพื้นหลัง UI

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18)); // ใช้ Font ที่ใหญ่ขึ้นสำหรับ UI

        // วาด Lives
        g.drawString("Lives: " + player.getLives(), 20, 30); // x=20, y=30 (กลาง UI_HEIGHT)

        // วาด Score
        String scoreText = "Score: " + score;
        FontMetrics fm = g.getFontMetrics();
        int scoreTextWidth = fm.stringWidth(scoreText);
        g.drawString(scoreText, (getWidth() - scoreTextWidth) / 2, 30); // กลางหน้าจอ

        // วาด Power-ups (Bombs, Range, Speed)
        g.drawString("B: " + player.getBombCount(), getWidth() - 150, 30); // ด้านขวา
        g.drawString("R: " + player.getExplosionRange(), getWidth() - 100, 30);
        g.drawString("S: " + player.getSpeedLevel(), getWidth() - 50, 30);
        // --- สิ้นสุดการวาด UI ---

        // --- วาดตาม GameState ---
        if (currentGameState == GameState.MENU) {
            drawMenu(g);
        } else if (currentGameState == GameState.PLAYING || currentGameState == GameState.GAME_OVER) {
            // วาดแผนที่และวัตถุ (เฉพาะเมื่อเล่นอยู่หรือ Game Over แล้ว)
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    switch (map[r][c]) {
                        case '#':
                            g.setColor(Color.DARK_GRAY);
                            break;
                        case 'X':
                            g.setColor(new Color(160, 82, 45)); // สีกล่อง
                            break;
                        case 'B':
                            // ภาพระเบิดจะถูกวาดทับทีหลัง ไม่ต้องใส่ g.setColor ตรงนี้
                            // g.setColor(Color.BLACK); // ไม่ต้องวาดสีพื้นหลัง ถ้าจะใช้รูปภาพ
                            break;
                        case '*':
                            g.setColor(Color.RED); // ไฟระเบิด
                            break;
                        case 'P': // สำหรับ Power-up ที่ยังไม่ถูกเก็บ
                            g.setColor(new Color(255, 200, 0)); // สีเหลืองส้มสำหรับ Power-up
                            break;
                        default:
                            g.setColor(Color.LIGHT_GRAY); // ช่องว่าง
                    }

                    // วาดพื้นหลังของช่อง (ยกเว้น Bomb เพราะจะวาดรูปทับไปเลย)
                    if (map[r][c] != 'B') {
                        g.fillRect(c * cellSize, r * cellSize + MAP_OFFSET_Y, cellSize, cellSize);
                        g.setColor(Color.BLACK); // ขอบช่อง
                        g.drawRect(c * cellSize, r * cellSize + MAP_OFFSET_Y, cellSize, cellSize);
                    }
                }
            }

            // *** วาด Power-ups ด้วยรูปภาพ ***
            synchronized (activePowerUps) {
                for (PowerUp pu : activePowerUps) {
                    Image powerUpImage = null;
                    switch (pu.getType()) {
                        case BOMB_COUNT -> powerUpImage = bombPowerUpImage;
                        case EXPLOSION_RANGE -> powerUpImage = explosionPowerUpImage;
                        case SPEED -> powerUpImage = speedPowerUpImage;
                    }
                    if (powerUpImage != null) {
                        g.drawImage(powerUpImage, pu.getCol() * cellSize, pu.getRow() * cellSize + MAP_OFFSET_Y,
                                cellSize, cellSize, null);
                    } else {
                        // ถ้าโหลดรูปภาพไม่สำเร็จ ให้กลับไปวาดเป็นวงกลมสีเดิม
                        g.setColor(new Color(255, 200, 0));
                        g.fillOval(pu.getCol() * cellSize + cellSize / 4,
                                pu.getRow() * cellSize + cellSize / 4 + MAP_OFFSET_Y,
                                cellSize / 2, cellSize / 2);
                    }
                }
            }

            // วาด Bomb (ใน Map) - วาดทับหลังจากวาดพื้นหลังของช่องแล้ว
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c] == 'B') {
                        g.drawImage(bombImage, c * cellSize, r * cellSize + MAP_OFFSET_Y, cellSize, cellSize, null);
                    }
                }
            }

            // วาดศัตรู (ใน Map)
            synchronized (enemies) {
                for (Enemy enemy : enemies) {
                    if (enemyImage != null) {
                        g.drawImage(enemyImage, enemy.getCol() * cellSize, enemy.getRow() * cellSize + MAP_OFFSET_Y,
                                cellSize, cellSize, null); // + MAP_OFFSET_Y
                    } else {
                        g.setColor(Color.RED);
                        g.fillRect(enemy.getCol() * cellSize, enemy.getRow() * cellSize + MAP_OFFSET_Y, cellSize,
                                cellSize); // + MAP_OFFSET_Y
                    }
                }
            }

            // วาด Player (ใน Map, พร้อมสถานะกระพริบเมื่ออมตะ)
            // ต้องเช็ค player != null เพราะตอน GameState.MENU player อาจจะยังไม่ได้สร้าง
            if (player != null) {
                if (playerImage != null) {
                    if (player.isInvulnerable()) {
                        long currentTime = System.currentTimeMillis();
                        // ทำให้กระพริบทุก 100ms
                        if ((currentTime / 100) % 2 == 0) {
                            g.drawImage(playerImage, player.getCol() * cellSize,
                                    player.getRow() * cellSize + MAP_OFFSET_Y,
                                    cellSize, cellSize, null); // + MAP_OFFSET_Y
                        }
                    } else {
                        g.drawImage(playerImage, player.getCol() * cellSize, player.getRow() * cellSize + MAP_OFFSET_Y,
                                cellSize, cellSize, null); // + MAP_OFFSET_Y
                    }
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(player.getCol() * cellSize, player.getRow() * cellSize + MAP_OFFSET_Y, cellSize,
                            cellSize); // + MAP_OFFSET_Y
                }
                g.setColor(Color.BLACK);
                g.drawRect(player.getCol() * cellSize, player.getRow() * cellSize + MAP_OFFSET_Y, cellSize, cellSize); // +
                                                                                                                       // MAP_OFFSET_Y
            }
        }

        // ถ้า Game Over ให้แสดงข้อความและปุ่ม Retry/Continue
        if (currentGameState == GameState.GAME_OVER) {
            drawGameOverScreen(g);
        }
    }

    public void placeBomb() {
        // ดึงจำนวนระเบิดที่ผู้เล่นมีจาก Player object
        int playerMaxBombs = player.getBombCount();

        int currentBombsOnMap = 0;
        synchronized (map) { // synchronized map เพื่อตรวจสอบระเบิดอย่างถูกต้อง
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c] == 'B') {
                        currentBombsOnMap++;
                    }
                }
            }
        }

        if (map[player.getRow()][player.getCol()] == ' ' && currentBombsOnMap < playerMaxBombs) {
            map[player.getRow()][player.getCol()] = 'B';
            // สร้าง Bomb object ด้วยรัศมีระเบิดของผู้เล่น
            new Bomb(map, player.getRow(), player.getCol(), this, player.getExplosionRange()).start();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    // เมธอดสำหรับอัปเดตการเคลื่อนที่ของผู้เล่น
    private void updatePlayerMovement() {
        long currentTime = System.currentTimeMillis();
        int dRow = 0;
        int dCol = 0;

        if (player.isMovingUp())
            dRow = -1;
        else if (player.isMovingDown())
            dRow = 1;
        else if (player.isMovingLeft())
            dCol = -1;
        else if (player.isMovingRight())
            dCol = 1;

        // ตรวจสอบว่าผู้เล่นกำลังพยายามเคลื่อนที่หรือไม่ และครบกำหนดคูลดาวน์แล้วหรือยัง
        if ((dRow != 0 || dCol != 0) && (currentTime - player.getLastMoveTime() >= player.getCurrentMoveDelay())) {
            int newRow = player.getRow() + dRow;
            int newCol = player.getCol() + dCol;

            // ตรวจสอบขอบเขต
            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                // ชนขอบแผนที่
                return;
            }

            char targetTile = map[newRow][newCol];

            // ตรวจสอบเงื่อนไขการเคลื่อนที่: ช่องว่าง, Power-up, หรือระเบิด
            // (ถ้าอนุญาตให้เดินทับ)
            if (targetTile == ' ' || isPowerUpAt(newRow, newCol) || targetTile == 'B') {
                player.setRow(newRow); // อัปเดตตำแหน่งผู้เล่น
                player.setCol(newCol);
                player.setLastMoveTime(currentTime); // รีเซ็ตเวลาเคลื่อนที่ล่าสุด
                checkAndCollectPowerUp(); // ตรวจสอบและเก็บ Power-up
            }
        }
    }

    private boolean isPowerUpAt(int r, int c) {
        for (PowerUp pu : activePowerUps) {
            if (pu.getRow() == r && pu.getCol() == c) {
                return true;
            }
        }
        return false;
    }

    private void checkAndCollectPowerUp() {
        PowerUp collectedPowerUp = null;
        synchronized (activePowerUps) {
            for (PowerUp pu : activePowerUps) {
                if (pu.getRow() == player.getRow() && pu.getCol() == player.getCol()) {
                    collectedPowerUp = pu;
                    break;
                }
            }

            if (collectedPowerUp != null) {
                activePowerUps.remove(collectedPowerUp);
                switch (collectedPowerUp.getType()) {
                    case BOMB_COUNT -> player.increaseBombCount(); // อัปเดตที่ Player object
                    case EXPLOSION_RANGE -> player.increaseExplosionRange(); // อัปเดตที่ Player object
                    case SPEED -> player.decreaseMoveDelay(20); // ลดคูลดาวน์
                }
                System.out.println("Collected Power-up: " + collectedPowerUp.getType() +
                        ", Bomb Count: " + player.getBombCount() +
                        ", Explosion Range: " + player.getExplosionRange() +
                        ", Current Move Delay: " + player.getCurrentMoveDelay() + "ms (Speed Level: "
                        + player.getSpeedLevel() + ")");
            }
        }
    }

    // *** เมธอดใหม่สำหรับสร้างแผนที่เปล่าและใส่กำแพงหลัก ***
    private void initializeMap() {
        map = new char[rows][cols]; // สร้าง map array ขึ้นมา

        // เติมทุกช่องด้วยช่องว่าง (' ') ก่อน
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = ' ';
            }
        }

        // ใส่กำแพงรอบนอก
        for (int c = 0; c < cols; c++) {
            map[0][c] = '#'; // แถวบนสุด
            map[rows - 1][c] = '#'; // แถวล่างสุด
        }
        for (int r = 0; r < rows; r++) {
            map[r][0] = '#'; // คอลัมน์ซ้ายสุด
            map[r][cols - 1] = '#'; // คอลัมน์ขวาสุด
        }

        // ใส่กำแพงทึบที่อยู่สลับกัน (ถ้ามี)
        // ตำแหน่งของกำแพงทึบ (Block walls) มักจะอยู่ที่ (คี่, คี่)
        for (int r = 2; r < rows - 1; r += 2) {
            for (int c = 2; c < cols - 1; c += 2) {
                map[r][c] = '#';
            }
        }
    }

    public void addPowerUp(PowerUp powerUp) {
        synchronized (activePowerUps) {
            activePowerUps.add(powerUp);
        }
    }

    // *** เมธอดใหม่สำหรับเคลียร์พื้นที่ผู้เล่น ***
    private void clearPlayerSpawnArea(int playerStartRow, int playerStartCol, int clearRadius) {
        for (int r = playerStartRow - clearRadius; r <= playerStartRow + clearRadius; r++) {
            for (int c = playerStartCol - clearRadius; c <= playerStartCol + clearRadius; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols && map[r][c] != '#') { // ไม่ทับกำแพงหลัก
                    map[r][c] = ' ';
                }
            }
        }
    }

    // *** เมธอดสำหรับเพิ่มคะแนน ***
    public void addScore(int points) {
        this.score += points;
    }

    // *** เมธอดสำหรับดึงคะแนน ***
    public int getScore() {
        return score;
    }

    public Player getPlayer() {
        return player;
    }

    public char[][] getMap() {
        return map;
    }

    public void refresh() {
        repaint();
    }

    // *** เมธอดสำหรับวาดหน้าจอ Menu ***
    private void drawMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200)); // พื้นหลังทึบ
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 60));
        String title = "BOMBERMAN";
        FontMetrics fmTitle = g.getFontMetrics();
        int xTitle = (getWidth() - fmTitle.stringWidth(title)) / 2;
        int yTitle = getHeight() / 2 - 80;
        g.drawString(title, xTitle, yTitle);

        // วาดปุ่ม Start
        drawButton(g, "START", (getWidth() - 150) / 2, (getHeight() - 50) / 2, 150, 50);
    }

    // *** เมธอดสำหรับวาดหน้าจอ Game Over ***
    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200)); // พื้นหลังทึบ
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        String gameOverText = "GAME OVER!";
        FontMetrics fmGameOver = g.getFontMetrics();
        int xGameOver = (getWidth() - fmGameOver.stringWidth(gameOverText)) / 2;
        int yGameOver = (getHeight() - fmGameOver.getHeight()) / 2 - 40;
        g.drawString(gameOverText, xGameOver, yGameOver);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        String finalScoreText = "Final Score: " + score;
        FontMetrics fmScore = g.getFontMetrics();
        int xFinalScore = (getWidth() - fmScore.stringWidth(finalScoreText)) / 2;
        g.drawString(finalScoreText, xFinalScore, yGameOver + 40);

        // ตำแหน่งและขนาดของปุ่ม Retry
        int retryButtonWidth = 150;
        int retryButtonHeight = 50;
        int retryX = (getWidth() - retryButtonWidth) / 2;
        int retryY = yGameOver + 40 + fmScore.getHeight() + 20; // วางปุ่มใต้ข้อความคะแนนfinalscoreText และเว้นระยะห่าง
                                                                // 20

        // วาดปุ่ม Retry
        drawButton(g, "RETRY", retryX, retryY, retryButtonWidth, retryButtonHeight);

        // คำนวณตำแหน่งเริ่มต้น Y สำหรับ Leaderboard
        // ให้เริ่มจากด้านล่างของปุ่ม Retry และเว้นระยะห่างที่เหมาะสม
        int leaderboardStartY = retryY + retryButtonHeight + 30; // 30 คือระยะห่างจากปุ่ม

        // *** เพิ่มการแสดง Leaderboard ***
        drawLeaderboard(g, leaderboardStartY);
    }

    // *** เมธอดช่วยวาดปุ่ม ***
    private void drawButton(Graphics g, String text, int x, int y, int width, int height) {
        g.setColor(Color.BLUE); // สีปุ่ม
        g.fillRect(x, y, width, height);

        g.setColor(Color.WHITE); // สีข้อความบนปุ่ม
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, textX, textY);

        g.setColor(Color.BLACK); // ขอบปุ่ม
        g.drawRect(x, y, width, height);
    }

    private void sendScoreToLeaderboard(String name, int score) {
        new Thread(() -> { // รันใน Thread แยกต่างหาก เพื่อไม่ให้บล็อก UI
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // ส่งคำสั่ง ADD_SCORE
                writer.println("ADD_SCORE " + name + " " + score);
                System.out.println("Sent score to server: " + name + " " + score);

                // Server จะส่ง "OK" กลับมาเมื่อบันทึกสำเร็จ (ถ้ามี) หรือข้อความยืนยัน
                String response = reader.readLine();
                System.out.println("Server response (ADD_SCORE): " + response);

            } catch (IOException e) {
                System.err.println("Error connecting to leaderboard server (ADD_SCORE): " + e.getMessage());
            }
        }).start();
    }

    private void requestLeaderboard() {
        new Thread(() -> { // รันใน Thread แยกต่างหาก
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // ส่งคำสั่ง GET_LEADERBOARD
                writer.println("GET_LEADERBOARD");
                System.out.println("Requested leaderboard from server.");

                // รับข้อมูล Leaderboard ทีละบรรทัดจนกว่าจะเจอ "END_LEADERBOARD"
                List<String> receivedLeaderboard = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null && !line.equals("END_LEADERBOARD")) {
                    receivedLeaderboard.add(line);
                }

                // อัปเดตและแสดงผล Leaderboard บน UI (ต้องรันบน EDT)
                SwingUtilities.invokeLater(() -> {
                    updateDisplayedLeaderboard(receivedLeaderboard);
                });

            } catch (IOException e) {
                System.err.println("Error connecting to leaderboard server (GET_LEADERBOARD): " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    // หากเกิดข้อผิดพลาดในการเชื่อมต่อ อาจแสดงข้อความบน UI
                    updateDisplayedLeaderboard(
                            Collections.singletonList("Failed to load leaderboard. Server offline?"));
                });
            }
        }).start();
    }

    private List<String> displayedLeaderboard = new ArrayList<>(); // เก็บ Leaderboard ที่จะแสดงผล

    private void updateDisplayedLeaderboard(List<String> leaderboardData) {
        this.displayedLeaderboard = leaderboardData;
        repaint(); // สั่งให้ JPanel วาดใหม่เพื่อแสดง Leaderboard ที่อัปเดต
    }

    private void drawLeaderboard(Graphics g, int startY) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String leaderboardTitle = "--- TOP SCORES ---";
        FontMetrics fm = g.getFontMetrics();
        int xTitle = (getWidth() - fm.stringWidth(leaderboardTitle)) / 2;
        g.drawString(leaderboardTitle, xTitle, startY);

        int currentY = startY + fm.getHeight() + 10; // เว้นบรรทัด

        if (displayedLeaderboard.isEmpty()) {
            String noScores = "No scores yet or loading...";
            int xNoScores = (getWidth() - fm.stringWidth(noScores)) / 2;
            g.drawString(noScores, xNoScores, currentY);
        } else {
            for (int i = 0; i < displayedLeaderboard.size() && i < 10; i++) { // แสดงสูงสุด 10 อันดับ
                String entry = (i + 1) + ". " + displayedLeaderboard.get(i);
                int xEntry = (getWidth() - fm.stringWidth(entry)) / 2;
                g.drawString(entry, xEntry, currentY + (i * fm.getHeight()));
            }
        }
    }

}