package Bomberman;

import javax.swing.*;
import java.awt.*;

public class GameGrid extends JPanel {
    private int rows;
    private int cols;
    private int cellSize;
    private Color[][] cellColors;
    private Player player;
    private Image playerImage;
    private Image bombImage;
    private Image enemyImage;
    private Enemy enemy;

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
        bombImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/bomb.gif").getImage();
        enemyImage = new ImageIcon("C:/Users/lolma/Desktop/work/project/Bomberman/static/Pontan.png").getImage();

        if (enemyImage == null) {
            System.out.println("❌ Enemy image failed to load.");
        } else {
            System.out.println("✅ Enemy image loaded successfully.");
        }

        player = new Player(1, 1);
        player.setupKeyControls(this, this); // only works if GameGrid extends JPanel ✅
        enemy = new Enemy(map, 1, 3, this); // guaranteed empty tile
        enemy.start();
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
                    case '#' -> g.setColor(Color.DARK_GRAY); // Wall
                    case 'B' -> {
                        g.drawImage(bombImage, col * cellSize, row * cellSize, cellSize, cellSize, null);
                        continue;
                    }
                    case '*' -> g.setColor(Color.RED); // Explosion
                    default -> g.setColor(Color.LIGHT_GRAY); // Empty
                }
                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }

        g.drawImage(playerImage, player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize, null);
        g.setColor(Color.BLACK);
        g.drawRect(player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize);

        // Draw enemy image last
        g.drawImage(enemyImage, enemy.getCol() * cellSize, enemy.getRow() * cellSize, cellSize, cellSize, null);
    }

    public void placeBomb() {
        if (map[player.getRow()][player.getCol()] == 'P' || map[player.getRow()][player.getCol()] == ' ') {
            map[player.getRow()][player.getCol()] = 'B';
            repaint();
            new Bomb(map, player.getRow(), player.getCol(), this).start();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public char[][] getMap() {
        return map;
    }

    public void refresh() {
        repaint();
    }
}
