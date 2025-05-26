package Bomberman;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

public class Player {
    private int row;
    private int col;

    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


    public void setupKeyControls(JPanel panel, GameGrid grid) {
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_UP -> move(-1, 0, grid);
                    case KeyEvent.VK_DOWN -> move(1, 0, grid);
                    case KeyEvent.VK_LEFT -> move(0, -1, grid);
                    case KeyEvent.VK_RIGHT -> move(0, 1, grid);
                    case KeyEvent.VK_SPACE -> grid.placeBomb();
                }
            }
        });
    }

    private void move(int dRow, int dCol, GameGrid grid) {
        int newRow = row + dRow;
        int newCol = col + dCol;
        char[][] map = grid.getMap();

        if (newRow < 0 || newRow >= map.length || newCol < 0 || newCol >= map[0].length) return;
        if (map[newRow][newCol] == ' ') {
            row = newRow;
            col = newCol;
            grid.refresh();
        }
    }
}
