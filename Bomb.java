package Bomberman;

public class Bomb extends Thread {
    private final char[][] map;
    private final int row;
    private final int col;
    private final GameGrid grid;

    // ✅ Constructor that matches what you call in GameGrid
    public Bomb(char[][] map, int row, int col, GameGrid grid) {
        this.map = map;
        this.row = row;
        this.col = col;
        this.grid = grid;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); // Wait before explosion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        
        // Explode UP
        for (int i = 1; i <= 2; i++) {
            int r = row - i;
            if (r < 0 || map[r][col] == '#') break;
            if (r < 0 || map[r][col] == 'B') break;
            map[r][col] = '*';
        }

        // Explode DOWN
        for (int i = 1; i <= 2; i++) {
            int r = row + i;
            if (r < 0 || map[r][col] == '#') break;
            if (r < 0 || map[r][col] == 'B') break;
            map[r][col] = '*';
        }

        // Explode LEFT
        for (int i = 1; i <= 2; i++) {
            int c = col - i;
            if (c < 0 || map[row][c] == '#') break;
            if (c < 0 || map[row][c] == 'B') break;
            map[row][c] = '*';
        }

        // Explode RIGHT
        for (int i = 1; i <= 2; i++) {
            int c = col + i;
            if (c >= map[0].length || map[row][c] == '#') break;
            if (c >= map[0].length || map[row][c] == 'B') break;
            map[row][c] = '*';
        }

        // Always explode center
        map[row][col] = '*';



        grid.repaint();

        try {
            Thread.sleep(1000); // Show explosion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         // Clear center
        if (map[row][col] == '*') map[row][col] = ' ';
        // Up
        if (row - 1 >= 0 && map[row - 1][col] == '*') map[row - 1][col] = ' ';
        if (row - 2 >= 0 && map[row - 2][col] == '*') map[row - 2][col] = ' ';
        // Down
        if (row + 1 < map.length && map[row + 1][col] == '*') map[row + 1][col] = ' ';
        if (row + 2 < map.length && map[row + 2][col] == '*') map[row + 2][col] = ' ';
        // Left
        if (col - 1 >= 0 && map[row][col - 1] == '*') map[row][col - 1] = ' ';
        if (col - 2 >= 0 && map[row][col - 2] == '*') map[row][col - 2] = ' ';
        // Right
        if (col + 1 < map[0].length && map[row][col + 1] == '*') map[row][col + 1] = ' ';
        if (col + 2 < map[0].length && map[row][col + 2] == '*') map[row][col + 2] = ' ';

        grid.repaint();

    }
}
