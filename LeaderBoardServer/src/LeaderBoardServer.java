
// LeaderBoardServer.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // ใช้สำหรับ thread-safe list

public class LeaderBoardServer {
    private int port;
    private ServerSocket serverSocket;
    // ใช้ CopyOnWriteArrayList สำหรับ thread-safe list ในกรณีที่มีหลาย Client
    // เข้าถึงพร้อมกัน
    private List<ScoreEntry> leaderboard = new CopyOnWriteArrayList<>();

    public LeaderBoardServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Leaderboard Server started on port " + port + ". Waiting for clients...");

        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    System.out.println("Server socket closed.");
                } else {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Leaderboard Server stopped.");
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    // เมธอดสำหรับเพิ่มคะแนน
    public void addScore(String playerName, int score) {
        // อัปเดตคะแนนของผู้เล่นที่มีอยู่ หรือเพิ่มเป็นรายการใหม่
        boolean found = false;
        for (ScoreEntry entry : leaderboard) {
            if (entry.getPlayerName().equalsIgnoreCase(playerName)) {
                if (score > entry.getScore()) { // อัปเดตถ้าคะแนนใหม่สูงกว่า
                    entry.setScore(score);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            leaderboard.add(new ScoreEntry(playerName, score));
        }
        // จัดเรียง Leaderboard ใหม่จากคะแนนสูงสุดไปต่ำสุด
        Collections.sort(leaderboard, Comparator.comparingInt(ScoreEntry::getScore).reversed());
        // จำกัดจำนวนรายการใน Leaderboard (เช่น 10 อันดับแรก)
        if (leaderboard.size() > 10) {
            leaderboard = new CopyOnWriteArrayList<>(leaderboard.subList(0, 10));
        }
        System.out.println("Score added/updated: " + playerName + " " + score);
        System.out.println("Current Leaderboard: " + leaderboard); // แสดงใน Server console
    }

    // เมธอดสำหรับดึง Leaderboard
    public List<ScoreEntry> getLeaderboard() {
        return new ArrayList<>(leaderboard); // ส่ง copy ไปให้เพื่อป้องกันการแก้ไขโดยตรง
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from client " + clientSocket.getInetAddress() + ": " + inputLine);
                    // ตรวจสอบคำสั่ง
                    if (inputLine.startsWith("ADD_SCORE")) {
                        String[] parts = inputLine.split(" ");
                        if (parts.length == 3) {
                            String name = parts[1];
                            int score = Integer.parseInt(parts[2]);
                            addScore(name, score); // เพิ่มคะแนน
                            out.println("OK"); // ส่งการยืนยันกลับไป
                        } else {
                            out.println("ERROR: Invalid ADD_SCORE format. Use ADD_SCORE PlayerName Score");
                        }
                    } else if (inputLine.equals("GET_LEADERBOARD")) {
                        List<ScoreEntry> currentLeaderboard = getLeaderboard();
                        if (currentLeaderboard.isEmpty()) {
                            out.println("No scores yet."); // ถ้ายังไม่มีคะแนน
                        } else {
                            for (ScoreEntry entry : currentLeaderboard) {
                                out.println(entry.getPlayerName() + ":" + entry.getScore());
                            }
                        }
                        out.println("END_LEADERBOARD"); // สำคัญมาก: ส่งสัญญาณจบ
                    } else {
                        out.println("ERROR: Unknown command.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler error for " + clientSocket.getInetAddress() + ": " + e.getMessage());
            } finally {
                try {
                    if (out != null)
                        out.close();
                    if (in != null)
                        in.close();
                    if (clientSocket != null)
                        clientSocket.close();
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }

    // คลาสสำหรับเก็บข้อมูลคะแนน
    private static class ScoreEntry {
        private String playerName;
        private int score;

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        @Override
        public String toString() {
            return playerName + ":" + score;
        }
    }

    public static void main(String[] args) {
        // ใช้ port เดียวกับที่ client (GameGrid) กำหนดไว้
        LeaderBoardServer server = new LeaderBoardServer(5000); // SERVER_PORT = 5000 ใน GameGrid.java
        try {
            server.startServer();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}