package Bomberman;

import java.util.Random;

public class Bomb extends Thread {
    private final char[][] map;
    private final int row;
    private final int col;
    private final GameGrid grid;
    private final int explosionRange; // เพิ่มตัวแปรสำหรับรัศมีการระเบิดของผู้เล่น

    // ✅ Constructor that matches what you call in GameGrid
    public Bomb(char[][] map, int row, int col, GameGrid grid, int explosionRange) {
        this.map = map;
        this.row = row;
        this.col = col;
        this.grid = grid;
        this.explosionRange = explosionRange; // กำหนดรัศมีระเบิด
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); // Wait before explosion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ก่อนระเบิด ให้เปลี่ยนสถานะ Bomb จาก 'B' เป็น ' '
        // เพื่อให้สามารถวางระเบิดใหม่ได้
        // การทำแบบนี้จะป้องกันการนับ Bomb ที่กำลังจะระเบิดเป็นระเบิดที่ยังอยู่บนแผนที่
        // แต่ต้องระวัง race condition หากมี player พยายามวางระเบิดซ้ำที่ตำแหน่งเดิม
        // ซึ่งในโค้ดปัจจุบัน player วางได้แค่บน 'P' หรือ ' '
        synchronized (grid.map) { // ใช้ synchronized เพื่อป้องกันการเข้าถึง map พร้อมกัน
            if (map[row][col] == 'B') {
                map[row][col] = ' '; // เคลียร์ระเบิดออกจากแผนที่ก่อนระเบิด
            }
        }

        // Explode Center (สำคัญมาก ต้องระเบิดตรงกลางก่อน)
        // map[row][col] = '*'; // ไม่ต้องทำตรงนี้แล้ว เพราะจะทำในลูป

        // ใช้ synchronized รอบการอัปเดตแผนที่ทั้งหมด
        // เพื่อป้องกัน race conditions หากมีหลายระเบิดระเบิดพร้อมกัน
        synchronized (grid.map) { // ล็อก map object
            // ระเบิดตรงกลาง
            affectTile(row, col);

            // Explode UP
            for (int i = 1; i <= explosionRange; i++) { // ใช้ explosionRange
                int r = row - i;
                if (!affectTile(r, col))
                    break; // ถ้าเจอการหยุด หรือทำลายกล่อง ให้หยุด
            }

            // Explode DOWN
            for (int i = 1; i <= explosionRange; i++) { // ใช้ explosionRange
                int r = row + i;
                if (!affectTile(r, col))
                    break;
            }

            // Explode LEFT
            for (int i = 1; i <= explosionRange; i++) { // ใช้ explosionRange
                int c = col - i;
                if (!affectTile(row, c))
                    break;
            }

            // Explode RIGHT
            for (int i = 1; i <= explosionRange; i++) { // ใช้ explosionRange
                int c = col + i;
                if (!affectTile(row, c))
                    break;
            }
        } // ปิด synchronized block

        grid.repaint(); // เรียก repaint ครั้งเดียวหลังการระเบิดทั้งหมด

        try {
            Thread.sleep(1000); // Show explosion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear explosions (ใช้ synchronized เหมือนเดิม)
        synchronized (grid.map) { // ล็อก map object อีกครั้ง
            clearExplosion(row, col); // Clear center

            // Up
            for (int i = 1; i <= explosionRange; i++) {
                clearExplosion(row - i, col);
            }
            // Down
            for (int i = 1; i <= explosionRange; i++) {
                clearExplosion(row + i, col);
            }
            // Left
            for (int i = 1; i <= explosionRange; i++) {
                clearExplosion(row, col - i);
            }
            // Right
            for (int i = 1; i <= explosionRange; i++) {
                clearExplosion(row, col + i);
            }
        } // ปิด synchronized block

        grid.repaint();
    }

    // เมธอดช่วยในการจัดการผลกระทบของระเบิด
    private boolean affectTile(int r, int c) {
        // ตรวจสอบขอบเขต
        if (r < 0 || r >= map.length || c < 0 || c >= map[0].length) {
            return false; // ออกนอกแผนที่
        }

        char tile = map[r][c];
        if (tile == '#') { // กำแพงแข็ง หยุดไฟ
            return false;
        }
        if (tile == 'B') { // ถ้าเจอระเบิดอื่น ให้ระเบิดต่อ
            // ตัวเลือก: อาจจะ trigger ระเบิดลูกนั้นให้ระเบิดทันที หรือแค่ปล่อยให้ไฟผ่านไป
            // ใน Bomberman ส่วนใหญ่จะระเบิดต่อ
            // map[r][c] = '*'; // เปลี่ยนเป็นไฟ
            return true; // ไม่หยุดไฟ, ไฟจะทะลุระเบิดลูกอื่นได้
        }
        if (tile == 'X') { // ถ้าเจอ กล่อง
            map[r][c] = '*'; // ทำลายกล่อง
            spawnPowerUp(r, c); // สุ่ม Power-up
            return false; // หยุดไฟที่กล่อง
        }
        // ถ้าเป็น ' ' (ช่องว่าง) หรือ '*' (ไฟเดิม)
        map[r][c] = '*'; // เปลี่ยนเป็นไฟ
        return true; // ไปต่อ
    }

    // เมธอดช่วยในการล้างไฟระเบิด
    private void clearExplosion(int r, int c) {
        if (r >= 0 && r < map.length && c >= 0 && c < map[0].length) {
            if (map[r][c] == '*') {
                map[r][c] = ' ';
            }
            // ตรวจสอบว่ามีผู้เล่นหรือศัตรูอยู่ในตำแหน่งนี้หรือไม่
            // หากมีและตายแล้ว ไม่ต้องทำอะไร หรือทำ Animation ตาย
            // ในที่นี้คือแค่ล้างไฟ
        }
    }

    // เมธอดสำหรับสุ่มสร้าง Power-up
    private void spawnPowerUp(int r, int c) {
        Random rand = new Random();
        // โอกาสที่จะสร้าง Power-up (เช่น 40%)
        if (rand.nextDouble() < 0.4) {
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[rand.nextInt(types.length)];
            // เพิ่ม PowerUp เข้าไปใน List ของ GameGrid
            // ต้องใช้ synchronized เพื่อป้องกัน race condition
            // หากมีหลายระเบิดระเบิดพร้อมกัน
            synchronized (grid.activePowerUps) { // ล็อก activePowerUps object
                grid.activePowerUps.add(new PowerUp(r, c, randomType));
            }
            System.out.println("Spawned Power-up: " + randomType + " at (" + r + "," + c + ")");
        }
    }
}
