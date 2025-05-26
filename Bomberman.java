package Bomberman;
import javax.swing.*;

public class Bomberman {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Bomberman");
        GameGrid grid = new GameGrid(10, 10, 40);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(grid);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
