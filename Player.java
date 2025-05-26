package Bomberman;

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

    public void move(int dRow, int dCol, char[][] map) {
        int newRow = row + dRow;
        int newCol = col + dCol;

        if (newRow >= 0 && newRow < map.length &&
            newCol >= 0 && newCol < map[0].length &&
            map[newRow][newCol] == ' ') {
            row = newRow;
            col = newCol;
        }
    }
}
