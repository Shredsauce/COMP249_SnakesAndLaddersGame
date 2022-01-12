import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class LadderAndSnake extends JFrame {
    private int numPlayers;
    Random random = new Random();
    private Board board;
    private Player[] players;
    // TODO: Let players choose their jeton
    private char[] jetonOptions = {'♟', '⛄', '☠', '☕'};

    public LadderAndSnake(int numPlayers) {
        this.numPlayers = numPlayers;

        players = new Player[numPlayers];

        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(jetonOptions[i]);
        }

        determinePlayerOrder();
    }

    private void determinePlayerOrder() {
        System.out.println(numPlayers + " players playing. Woohoo!");
        System.out.println("Let's roll the die to see who starts.");





//        while (!orderDetermined) {
//            int playerNumber = 1; // TODO: Get the actual player number
//            String next = scanner.nex
//            System.out.println("Player " + playerNumber + ", press R to roll the die.");
//
//            if (next == "r") {
//                int dieValue = flipDice();
//                System.out.println(next + " "+dieValue);
//            }
//        }
    }

    public int flipDice() {
        int dieSides = 6;
        return random.nextInt(1, dieSides + 1);
    }

    public void play() throws InterruptedException {
        initBoard();

        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        // Players should start off of the board and not on the first tile
        for (int i = 0; i < players.length; i++) {
            players[i].setCurrentTile(board.getTile(1));
        }


        while (true) {
            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            long time = timeSeconds - startTime;

            Scanner scan = new Scanner(System.in);
            int newTileId = scan.nextInt();

            // TODO: Alternate players. Using first player for testing
            Player currentPlayer = players[0];

            // TODO: Put this stuff in a function
            Tile goalTile = board.getTile(newTileId);
            Tile currentTile = currentPlayer.getCurrentTile();

            while (currentTile.getTileId() < goalTile.getTileId()) {
                currentTile = board.getTile(currentTile.getTileId() + 1);

                currentPlayer.setCurrentTile(currentTile);

                Thread.sleep(10);
            }

            if (currentPlayer.getCurrentTile().hasMoveTo()) {
                int moveToTileId = currentPlayer.getCurrentTile().getMoveToTileId();
                Tile moveToTile = board.getTile(moveToTileId);

                currentPlayer.setCurrentTile(moveToTile);
            }


            Thread.sleep(10);
        }


    }

    private void initBoard() {
        // Using https://zetcode.com/gfx/java2d/introduction/ as boilerplate code to get a graphics window open

        // TODO: Make sure the value of an entry is not the key of another entry
        // TODO: Option to randomly generate this. make sure elements are never on the same row
        Hashtable<Integer, Integer> moveToConfig = new Hashtable<Integer, Integer>() {
            {put(1, 38);}
            {put(4, 14);}
            {put(9, 31);}
            {put(16, 6);}
            {put(21, 42);}
            {put(28, 84);}
            {put(36, 44);}
            {put(48, 30);}
            {put(51, 67);}
            {put(62, 19);} // Most of the snake's head is on the 62
            {put(64, 60);}
            {put(64, 60);}
            {put(71, 91);}
            {put(80, 100);}
            {put(93, 68);}
            {put(95, 24);}
            {put(97, 76);}
            {put(98, 78);}
        };

        // TODO: width and height should be their own variables probably
        board = new Board(players, new Int2(10, 10), moveToConfig);

        add(board);

        setTitle("Snakes and Ladders");
        // TODO: Better way of setting width and height (use tile size)
        setSize(340, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }



}