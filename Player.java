package Bomberman;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

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

    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.currentMoveDelay = baseMoveDelay; // กำหนดค่าเริ่มต้น
        this.lastMoveTime = System.currentTimeMillis(); // ตั้งค่าเริ่มต้น
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) { // เพิ่ม setter สำหรับ update ตำแหน่งโดยตรง
        this.row = row;
    }

    public void setCol(int col) { // เพิ่ม setter สำหรับ update ตำแหน่งโดยตรง
        this.col = col;
    }

    public int getCurrentMoveDelay() {
        return currentMoveDelay;
    }

    public void decreaseMoveDelay(int amount) {
        // ลด moveDelay แต่ไม่ให้ต่ำกว่าค่า min (เช่น 50ms)
        this.currentMoveDelay = Math.max(50, this.currentMoveDelay - amount);
        System.out.println("Player speed increased! New move delay: " + this.currentMoveDelay + "ms");
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    // Setter สำหรับสถานะการกดปุ่ม
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

    // Getter สำหรับสถานะการกดปุ่ม
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

    // ไม่ต้องมี move(int newRow, int newCol) แบบเดิมแล้ว
    // การเคลื่อนที่จะถูกจัดการใน GameGrid update loop
}
