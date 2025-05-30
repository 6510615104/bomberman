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
        this.explosionRange = explosionRange; // Set the explosion range from player's power-up
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); // Wait for the bomb to explode (2 seconds)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear the bomb from the map before the explosion animation starts
        synchronized (grid.map) {
            if (map[row][col] == 'B') {
                map[row][col] = ' '; // Change 'B' (bomb) to ' ' (empty)
            }
        }

        // List to store all affected tiles that should show explosion ('*')
        // We will collect these first, then apply them
        java.util.List<int[]> affectedTiles = new java.util.ArrayList<>();

        // Add center tile to affected list
        affectedTiles.add(new int[] { row, col }); // Center of explosion

        // Determine all tiles that *would* be affected by the explosion
        // This logic is crucial to ensure player damage and visual match

        // Explode UP
        for (int i = 1; i <= explosionRange; i++) {
            int r = row - i;
            if (r < 0)
                break; // Out of bounds
            char tile = map[r][col];
            affectedTiles.add(new int[] { r, col }); // Add to list for visual and damage check
            if (tile == '#' || tile == 'X')
                break; // Stop if hits solid wall or destructible box
        }

        // Explode DOWN
        for (int i = 1; i <= explosionRange; i++) {
            int r = row + i;
            if (r >= map.length)
                break; // Out of bounds
            char tile = map[r][col];
            affectedTiles.add(new int[] { r, col });
            if (tile == '#' || tile == 'X')
                break; // Stop if hits solid wall or destructible box
        }

        // Explode LEFT
        for (int i = 1; i <= explosionRange; i++) {
            int c = col - i;
            if (c < 0)
                break; // Out of bounds
            char tile = map[row][c];
            affectedTiles.add(new int[] { row, c });
            if (tile == '#' || tile == 'X')
                break; // Stop if hits solid wall or destructible box
        }

        // Explode RIGHT
        for (int i = 1; i <= explosionRange; i++) {
            int c = col + i;
            if (c >= map[0].length)
                break; // Out of bounds
            char tile = map[row][c];
            affectedTiles.add(new int[] { row, c });
            if (tile == '#' || tile == 'X')
                break; // Stop if hits solid wall or destructible box
        }

        // --- Apply explosion effects based on affectedTiles list ---
        synchronized (grid.map) {
            for (int[] tilePos : affectedTiles) {
                int r = tilePos[0];
                int c = tilePos[1];

                // Ensure bounds check for safety
                if (r >= 0 && r < map.length && c >= 0 && c < map[0].length) {
                    char currentTile = map[r][c];
                    if (currentTile == 'X') { // If it's a destructible box
                        map[r][c] = '*'; // Change to explosion mark
                        spawnPowerUp(r, c); // Try to spawn power-up
                    } else if (currentTile == ' ' || currentTile == 'B') { // If empty or another bomb
                        map[r][c] = '*'; // Change to explosion mark
                    }
                    // '#' (solid walls) are unaffected and stop the visual spread, so no change
                    // here
                }
            }
        }

        // Now that the map state reflects the explosion, check for player and enemy
        // hits
        checkPlayerHitByExplosion(affectedTiles); // Pass the list of tiles that actually exploded
        checkAndDestroyEnemy(affectedTiles); // Updated to use the list of affected tiles

        grid.repaint(); // Update the visual grid to show explosions

        try {
            Thread.sleep(1000); // Show explosion for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear explosion marks
        synchronized (grid.map) {
            for (int[] tilePos : affectedTiles) {
                int r = tilePos[0];
                int c = tilePos[1];
                if (r >= 0 && r < map.length && c >= 0 && c < map[0].length && map[r][c] == '*') {
                    map[r][c] = ' '; // Change '*' (explosion) back to ' ' (empty)
                }
            }
        }
        grid.repaint(); // Update the visual grid to clear explosions
    }

    // This method is no longer needed as explosion logic is now within run() and
    // checkPlayerHitByExplosion()
    // private boolean affectTile(int r, int c) { ... }

    // *** แก้ไข: ปรับ logic ใน checkPlayerHitByExplosion ให้ใช้ affectedTiles ***
    private void checkPlayerHitByExplosion(java.util.List<int[]> explodedTiles) {
        Player player = grid.getPlayer();
        if (player.isInvulnerable()) {
            return; // Player is invulnerable, no damage taken
        }

        for (int[] tilePos : explodedTiles) {
            int r = tilePos[0];
            int c = tilePos[1];

            // Check if player is on any of the exploded tiles
            if (player.getRow() == r && player.getCol() == c) {
                player.takeDamage();
                return; // Player took damage, no need to check further
            }
        }
    }

    // *** แก้ไข: ปรับ logic ใน checkAndDestroyEnemy ให้ใช้ affectedTiles ***
    private void checkAndDestroyEnemy(java.util.List<int[]> explodedTiles) {
        synchronized (grid.getEnemies()) {
            java.util.Iterator<Enemy> iterator = grid.getEnemies().iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                for (int[] tilePos : explodedTiles) {
                    int r = tilePos[0];
                    int c = tilePos[1];
                    if (enemy.getRow() == r && enemy.getCol() == c) {
                        enemy.stopEnemy(); // Stop enemy thread
                        iterator.remove(); // Remove enemy from the grid list safely
                        grid.addScore(100); // Add score for destroying enemy
                        System.out.println("💥 Enemy destroyed at (" + r + "," + c + ") Score: " + grid.getScore());
                        // Important: After an enemy is destroyed, we can break from this inner loop
                        // and continue checking the next enemy.
                        break;
                    }
                }
            }
        }
    }

    private void spawnPowerUp(int r, int c) {
        Random rand = new Random();
        if (rand.nextDouble() < 0.4) { // 40% chance to spawn a power-up
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[rand.nextInt(types.length)];
            synchronized (grid.activePowerUps) {
                grid.addPowerUp(new PowerUp(r, c, randomType));
                System.out.println("Generated Power-up: " + randomType + " at (" + r + "," + c + ")");
            }
        }
    }
}