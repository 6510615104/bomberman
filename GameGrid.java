package Bomberman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameGrid extends JPanel {
    private int rows;
    private int cols;
    private int cellSize;
    private Color[][] cellColors;
    private Player player; // สร้าง instance ของ Player แทน
    private int playerBombCount = 1; // จำนวนระเบิดที่ผู้เล่นวางได้พร้อมกัน
    private int playerExplosionRange = 2; // รัศมีการระเบิดของผู้เล่น (ปัจจุบันคุณตั้งไว้ที่ 2 ใน Bomb.java)
    public List<PowerUp> activePowerUps; // รายการ Power-up ที่อยู่บนแผนที่

    // เพิ่ม Timer สำหรับ Game Loop
    private Timer gameTimer;
    private final int FRAME_RATE = 60; // 60 เฟรมต่อวินาที

    private Image playerImage;
    private Image bombImage;
    private Image enemyImage;
    private List<Enemy> enemies = new ArrayList<>();
    private Enemy enemy;

    public char[][] map;

    public GameGrid(int rows, int cols, int cellSize) {
    this.rows = rows;
    this.cols = cols;
    this.cellSize = cellSize;
    this.cellColors = new Color[rows][cols];

    // Initialize the map before creating the enemy
    initializeMap();

    // Create the player and enemy after the map is initialized
    player = new Player(1, 1); // Player's initial position
    enemy = new Enemy(map, 7, 3, this); // Create the enemy at (7, 3) with the initialized map
    enemy.start(); // Start enemy movement
    setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));

        playerImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/player.png").getImage();
        bombImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/bomb.gif").getImage();
        enemyImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/pontan.png").getImage();

        activePowerUps = new ArrayList<>(); // เริ่มต้น List
        initializeMap();
        // เคลียร์พื้นที่รอบผู้เล่นหลังจากสร้างแผนที่
        clearPlayerSpawnArea(player.getRow(), player.getCol(), 1); // เคลียร์ 3x3 รอบผู้เล่น

        generateRandomBoxes(30);
        Enemy initialEnemy = new Enemy(map, 7, 3, this);
        initialEnemy.start();
        enemies.add(initialEnemy);
        startEnemySpawner();

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

        // *** สร้างและเริ่ม Game Timer ***
        gameTimer = new Timer(1000 / FRAME_RATE, e -> updateGame()); // Timer จะเรียก updateGame() ทุกๆ 1/FRAME_RATE //
                                                                     // วินาที
        gameTimer.start();
    }

    private void startEnemySpawner() {
    new Thread(() -> {
        Random rand = new Random();
        while (true) {
            try {
                Thread.sleep(10000); // Spawn every 10 seconds

                // Check if there are already 4 enemies on the map
                if (enemies.size() >= 4) {
                    System.out.println("❌ Cannot spawn more enemies. Maximum limit reached.");
                    continue; // Skip spawning if the limit is reached
                }

                for (int i = 0; i < 4; i++) { // up to 20 attempts
                    int r = rand.nextInt(map.length);
                    int c = rand.nextInt(map[0].length);
                    if (map[r][c] == ' ') {
                        Enemy e = new Enemy(map, r, c, this);
                        e.start();
                        enemies.add(e); // Add the new enemy to the list
                        System.out.println("👾 Spawned enemy at (" + r + "," + c + ")");
                        break; // Exit the loop once an enemy is spawned
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }).start();
    }   


    // เมธอดหลักในการอัปเดตสถานะเกม
    private void updateGame() {
        // อัปเดตการเคลื่อนที่ของผู้เล่น (ตามคูลดาวน์)
        updatePlayerMovement();

        // สามารถเพิ่ม logic อื่นๆ ที่ต้องอัปเดตในแต่ละเฟรมที่นี่
        // เช่น อัปเดต AI ของศัตรู, อัปเดต animation, ฯลฯ

        repaint(); // เรียก repaint เพื่อวาดภาพใหม่ทุกเฟรม
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                char tile = map[row][col];
                switch (tile) {
                    case '#' -> g.setColor(Color.DARK_GRAY); // Wall
                    case 'X' -> g.setColor(new Color(160, 82, 45)); // กล่อง (สีไม้)
                    case 'B' -> {
                        g.drawImage(bombImage, col * cellSize, row * cellSize, cellSize, cellSize, null);
                        continue;
                    } // Bomb
                    case '*' -> g.setColor(Color.RED); // Explosion
                    default -> g.setColor(Color.LIGHT_GRAY); // Empty
                }

                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
        // วาด Power-ups
        for (PowerUp pu : activePowerUps) {
            // คุณสามารถใช้รูปภาพสำหรับ Power-up แต่ละประเภทได้
            // ตอนนี้ใช้สีเป็นตัวอย่าง
            switch (pu.getType()) {
                case BOMB_COUNT -> g.setColor(Color.BLUE);
                case EXPLOSION_RANGE -> g.setColor(Color.ORANGE);
                case SPEED -> g.setColor(Color.GREEN);
            }
            g.fillOval(pu.getCol() * cellSize + cellSize / 4, pu.getRow() * cellSize + cellSize / 4, cellSize / 2,
                    cellSize / 2);
        }
        g.drawImage(playerImage, player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize, null);
        g.setColor(Color.BLACK);
        g.drawRect(player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize);
        for (Enemy e : enemies) {
            g.drawImage(enemyImage, e.getCol() * cellSize, e.getRow() * cellSize, cellSize, cellSize, null);
        }
    }

    // อัปเดต placeBomb ให้เรียกใช้ Player object
    // ปรับปรุง placeBomb
    public void placeBomb() {
        int currentBombsOnMap = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] == 'B') {
                    currentBombsOnMap++;
                }
            }
        }

        // ผู้เล่นสามารถวางระเบิดได้ถ้าช่องที่ยืนอยู่เป็นช่องว่าง
        if (map[player.getRow()][player.getCol()] == ' ' && currentBombsOnMap < playerBombCount) {
            map[player.getRow()][player.getCol()] = 'B';
            // ไม่ต้อง repaint ตรงนี้ เพราะ Game Loop จะเรียก repaint เองทุกเฟรม
            new Bomb(map, player.getRow(), player.getCol(), this, playerExplosionRange).start();
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

    // ปรับปรุง checkAndCollectPowerUp
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
                    case BOMB_COUNT -> playerBombCount++;
                    case EXPLOSION_RANGE -> playerExplosionRange++;
                    case SPEED -> player.decreaseMoveDelay(20); // ลดคูลดาวน์ลง 20ms
                }
                System.out.println("Collected Power-up: " + collectedPowerUp.getType() +
                        ", Bomb Count: " + playerBombCount +
                        ", Explosion Range: " + playerExplosionRange +
                        ", Current Move Delay: " + player.getCurrentMoveDelay() + "ms");
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

    public Player getPlayer() {
        return player;
    }

    public char[][] getMap() {
        return map;
    }

    public void refresh() {
        repaint();
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

}