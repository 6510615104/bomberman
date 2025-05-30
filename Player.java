package Bomberman;

public class Player {
    private int row;
    private int col;

    // สำหรับการเคลื่อนที่แบบคูลดาวน์
    private int baseMoveDelay = 250; // มิลลิวินาที (ค่าเริ่มต้น เช่น 150ms ต่อ 1 ช่อง)
    private int currentMoveDelay; // ค่าดีเลย์ปัจจุบัน (ลดลงเมื่อเก็บ Power-up)
    private long lastMoveTime; // เวลาที่เคลื่อนที่ล่าสุด (System.currentTimeMillis())

    // สถานะการกดปุ่ม (ใช้สำหรับ Game Loop)
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // เพิ่มตัวแปรสำหรับ Power-up ที่ Player มี (เพื่อให้ GameGrid
    // ดึงไปแสดงผลได้ง่าย)
    private int bombCount = 1;
    private int explosionRange = 2;
    private int speedLevel = 1; // ระดับความเร็ว (ไม่ใช่ delay โดยตรง)

    // *** เพิ่มตัวแปรสำหรับพลังชีวิตและสถานะอมตะ ***
    private int lives; // พลังชีวิต
    private boolean invulnerable; // สถานะอมตะ
    private long invulnerableStartTime; // เวลาที่เริ่มอมตะ
    private final long INVULNERABILITY_DURATION = 2000; // 2 วินาที (2000 มิลลิวินาที)

    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.currentMoveDelay = baseMoveDelay;
        this.lastMoveTime = System.currentTimeMillis();

        // *** กำหนดค่าเริ่มต้นพลังชีวิต ***
        this.lives = 2; // เริ่มต้น 2 ชีวิต
        this.invulnerable = false; // ยังไม่อมตะ
        this.invulnerableStartTime = 0; // ยังไม่มีเวลาเริ่มอมตะ
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCurrentMoveDelay() {
        return currentMoveDelay;
    }

    public void decreaseMoveDelay(int amount) {
        this.currentMoveDelay = Math.max(50, this.currentMoveDelay - amount);
        System.out.println("Player speed increased! New move delay: " + this.currentMoveDelay + "ms");
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
    }

    public void setMovingDown(boolean movingDown) {
        this.movingDown = movingDown;
    }

    public void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    public boolean isMovingUp() {
        return movingUp;
    }

    public boolean isMovingDown() {
        return movingDown;
    }

    public boolean isMovingLeft() {
        return movingLeft;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    // *** เมธอดสำหรับจัดการพลังชีวิตและอมตะ ***
    public int getLives() {
        return lives;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public long getInvulnerableStartTime() {
        return invulnerableStartTime;
    }

    public void setInvulnerableStartTime(long invulnerableStartTime) {
        this.invulnerableStartTime = invulnerableStartTime;
    }

    public long getInvulnerabilityDuration() {
        return INVULNERABILITY_DURATION;
    }

    public void takeDamage() {
        if (!invulnerable) { // เสียชีวิตได้ก็ต่อเมื่อไม่อยู่ในสถานะอมตะ
            lives--;
            System.out.println("Player took damage! Lives left: " + lives);
            if (lives > 0) {
                // เข้าสู่สถานะอมตะชั่วคราว
                invulnerable = true;
                invulnerableStartTime = System.currentTimeMillis();
            } else {
                // ผู้เล่นตาย (อาจจะเรียกเมธอด gameOver() ของ GameGrid)
                System.out.println("Game Over!");
            }
        }
    }

    // *** Getter/Setter สำหรับ Power-up ที่ผู้เล่นมี ***
    public int getBombCount() {
        return bombCount;
    }

    public void increaseBombCount() {
        this.bombCount++;
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public void increaseExplosionRange() {
        this.explosionRange++;
    }

    public int getSpeedLevel() {
        return speedLevel;
    } // ใช้สำหรับแสดงผล

    // เพิ่มเมธอดสำหรับเมื่อผู้เล่นตาย (ถ้าพลังชีวิตเป็น 0)
    public boolean isAlive() {
        return lives > 0;
    }

}