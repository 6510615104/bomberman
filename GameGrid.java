package Bomberman;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GameGrid extends JPanel {
    private int rows;
    private int cols;
    private int cellSize;
    private Color[][] cellColors;
    private int playerRow = 1;
    private int playerCol = 1;
    private Image playerImage;
    private Image bombImage;

    public char[][] map = {
        {'#','#','#','#','#','#','#','#','#','#','#','#','#'},
        {'#','P',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#'},
        {'#',' ','#',' ','#',' ','#',' ','#',' ','#',' ','#'},
        {'#',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#'},
        {'#',' ','#',' ','#',' ','#',' ','#',' ','#',' ','#'},
        {'#',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#'},
        {'#',' ','#',' ','#',' ','#',' ','#',' ','#',' ','#'},
        {'#',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#'},
        {'#',' ','#',' ','#',' ','#',' ','#',' ','#',' ','#'},
        {'#',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#'},
        {'#','#','#','#','#','#','#','#','#','#','#','#','#'}
    };

    public GameGrid(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.cellColors = new Color[rows][cols];
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        playerImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/player.png").getImage();
        bombImage = new ImageIcon("C:\\Users\\lolma\\Desktop\\work\\project\\Bomberman\\static\\bomb.gif").getImage();


    }

    public void setCellColor(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            cellColors[row][col] = color;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            char tile = map[row][col];
            switch (tile) {
                case '#' -> g.setColor(Color.DARK_GRAY);   // Wall
                case 'B' -> {
                    g.drawImage(bombImage, col * cellSize, row * cellSize, cellSize, cellSize, null);
                    continue;}// Bomb
                case '*' -> g.setColor(Color.RED);         // Explosion
                default  -> g.setColor(Color.LIGHT_GRAY);  // Empty
            }

            g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            g.setColor(Color.BLACK);
            g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
        }
}
g.drawImage(playerImage, playerCol * cellSize, playerRow * cellSize, cellSize, cellSize, null); // Draw player image
g.setColor(Color.BLACK);
g.drawRect(playerCol * cellSize, playerRow * cellSize, cellSize, cellSize);
    }

    public void placeBomb() {
    if (map[playerRow][playerCol] == 'P' || map[playerRow][playerCol] == ' ') {
        map[playerRow][playerCol] = 'B';
        repaint();

        new Bomb(map, playerRow, playerCol, this).start();
    }
    }


    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();

                switch (key) {
                    case KeyEvent.VK_UP    -> movePlayer(-1, 0);
                    case KeyEvent.VK_DOWN  -> movePlayer(1, 0);
                    case KeyEvent.VK_LEFT  -> movePlayer(0, -1);
                    case KeyEvent.VK_RIGHT -> movePlayer(0, 1);
                    case KeyEvent.VK_SPACE -> placeBomb();
                }
            }
        });
    }

    private void movePlayer(int dRow, int dCol) {
    int newRow = playerRow + dRow;
    int newCol = playerCol + dCol;

    if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) return;
    if (map[newRow][newCol] == ' ') {
        playerRow = newRow;
        playerCol = newCol;
        repaint();
        }
    }


}
