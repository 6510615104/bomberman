package Bomberman;

import java.util.Random;

public class Enemy extends Thread {
    private char[][] map;
    private int row, col;
    private GameGrid grid;
    private boolean running = true;
    private final Random random = new Random();

    public Enemy(char[][] map, int startRow, int startCol, GameGrid grid) {
        this.map = map;
        this.row = startRow;
        this.col = startCol;
        this.grid = grid;
    }

    @Override
    public void run() {
        while (running) {
            int[] dRow = {-1, 1, 0, 0}; // up, down, left, right
            int[] dCol = {0, 0, -1, 1};

            int dir = random.nextInt(4);
            int newRow = row + dRow[dir];
            int newCol = col + dCol[dir];

            // Move if the next tile is walkable
            if (map[newRow][newCol] == ' ') {

                row = newRow;
                col = newCol;
                grid.repaint();
            }

            try {
                Thread.sleep(500); // move every 0.5 seconds
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void stopEnemy() {
        running = false;
        interrupt();
    }
}
