package Bomberman;

import java.util.Random;

public class Bomb extends Thread {
    private final char[][] map;
    private final int row;
    private final int col;
    private final GameGrid grid;
    private final int explosionRange; // The range of explosion

    public Bomb(char[][] map, int row, int col, GameGrid grid, int explosionRange) {
        this.map = map;
        this.row = row;
        this.col = col;
        this.grid = grid;
        this.explosionRange = explosionRange; // Set the explosion range
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); // Wait for the bomb to explode
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear the bomb from the map before the explosion
        synchronized (grid.map) {
            if (map[row][col] == 'B') {
                map[row][col] = ' '; // Clear the bomb from the map
            }
        }

        // Start the explosion process
        synchronized (grid.map) {
            // Explode in all directions
            affectTile(row, col); // Explode the center

            // Explode UP
            for (int i = 1; i <= explosionRange; i++) {
                int r = row - i;
                if (!affectTile(r, col)) break; // Stop if wall or limit reached
            }

            // Explode DOWN
            for (int i = 1; i <= explosionRange; i++) {
                int r = row + i;
                if (!affectTile(r, col)) break;
            }

            // Explode LEFT
            for (int i = 1; i <= explosionRange; i++) {
                int c = col - i;
                if (!affectTile(row, c)) break;
            }

            // Explode RIGHT
            for (int i = 1; i <= explosionRange; i++) {
                int c = col + i;
                if (!affectTile(row, c)) break;
            }
        }

        grid.repaint(); // Repaint after the explosion

        try {
            Thread.sleep(1000); // Show explosion for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear the explosion marks after displaying
        synchronized (grid.map) {
            clearExplosion(row, col);

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
        }

        grid.repaint();
    }

    // This method affects a tile with explosion
    private boolean affectTile(int r, int c) {
        if (r < 0 || r >= map.length || c < 0 || c >= map[0].length) {
            return false; // Out of bounds
        }

        char tile = map[r][c];
        if (tile == '#') { // Wall stops the explosion
            return false;
        }
        if (tile == 'B') { // If there's another bomb, continue the explosion
            return true;
        }
        if (tile == 'X') { // If there's a box, destroy it and possibly spawn a power-up
            map[r][c] = '*'; // Destroy the box
            spawnPowerUp(r, c); // Optionally spawn a power-up
            return false; // Stop explosion at the box
        }

        // If the tile is empty (' ') or already exploded ('*'), continue explosion
        map[r][c] = '*'; // Mark the explosion
        checkAndDestroyEnemy(r, c); // Check for enemy at this position
        return true; // Continue explosion
    }

    // Check and destroy enemy if it is in the explosion area
    private void checkAndDestroyEnemy(int r, int c) {
        for (Enemy enemy : grid.getEnemies()) {
            if (enemy.getRow() == r && enemy.getCol() == c) {
                enemy.stopEnemy(); // Stop enemy thread
                grid.getEnemies().remove(enemy); // Remove enemy from the grid
                System.out.println("💥 Enemy destroyed at (" + r + "," + c + ")");
                break; // Stop checking further after destroying the first enemy
            }
        }
    }

    // This method clears the explosion marks after a while
    private void clearExplosion(int r, int c) {
        if (r >= 0 && r < map.length && c >= 0 && c < map[0].length) {
            if (map[r][c] == '*') {
                map[r][c] = ' ';
            }
        }
    }

    // Spawn power-ups after destroying a box
    private void spawnPowerUp(int r, int c) {
        Random rand = new Random();
        if (rand.nextDouble() < 0.4) { // 40% chance to spawn a power-up
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[rand.nextInt(types.length)];
            synchronized (grid.activePowerUps) {
                grid.activePowerUps.add(new PowerUp(r, c, randomType));
            }
            System.out.println("Power-up spawned at (" + r + "," + c + "): " + randomType);
        }
    }
}
