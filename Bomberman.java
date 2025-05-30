package Bomberman;

import javax.swing.*;

public class Bomberman {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Bomberman");
        GameGrid grid = new GameGrid(11, 13, 70); // แผนที่ 11x13 ช่อง, แต่ละช่อง 40x40 พิกเซล
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(grid);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
