package Bomberman;

public class PowerUp {
    private int row;
    private int col;
    private PowerUpType type;
    // อาจมี Image สำหรับ Power-up แต่ละประเภท

    public PowerUp(int row, int col, PowerUpType type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public PowerUpType getType() {
        return type;
    }

    // อาจมีเมธอดสำหรับวาดตัวเอง (render) ถ้าไม่วาดใน GameGrid โดยตรง
}