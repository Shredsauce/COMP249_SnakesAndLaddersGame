import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GUIManager {
    private LadderAndSnake game;

    private Board board;

    // Only for testing.
    private JLabel label;
    private JFrame frame;
    private JPanel panel;

    public JButton rollDieBtn;

    public GUIManager(LadderAndSnake game) {
        this.game = game;

        Board board = createBoard();

        JFrame frame = new JFrame("");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(board, BorderLayout.CENTER);

        JPanel controls = new JPanel();

        rollDieBtn = new JButton("Roll die");
        rollDieBtn.addActionListener(event -> onRollDie());

        controls.add(rollDieBtn);

        content.add(controls, BorderLayout.NORTH);

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        board.setPlayers(game.getPlayers());
    }

    public void play() throws InterruptedException {
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        while (true) {
            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            long time = timeSeconds - startTime;

            Scanner scan = new Scanner(System.in);
            int newTileId = scan.nextInt();

            if (game.getPlayers() == null || game.getPlayers().length == 0) continue;

            // TODO: Alternate players. Using first player for testing
            Player currentPlayer = game.getPlayers()[0];

            // TODO: Put this stuff in a function
            Tile goalTile = getBoard().getTile(newTileId);
            Tile currentTile = currentPlayer.getCurrentTile();

            if (currentTile == null) continue;

            while (currentTile.getTileId() < goalTile.getTileId()) {
                currentTile = getBoard().getTile(currentTile.getTileId() + 1);

                currentPlayer.setCurrentTile(currentTile);

                Thread.sleep(10);
            }

            if (currentPlayer.getCurrentTile().hasMoveTo()) {
                int moveToTileId = currentPlayer.getCurrentTile().getMoveToTileId();
                Tile moveToTile = getBoard().getTile(moveToTileId);

                currentPlayer.setCurrentTile(moveToTile);
            }

            Thread.sleep(10);
        }
    }

    private void onRollDie() {
        int roll = game.flipDice();
        System.out.println(String.format("Player's roll is " + roll));
    }

    public Board createBoard() {
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
        board = new Board(new Int2(10, 10), moveToConfig);

        return board;
    }

    public Board getBoard() {
        return board;
    }
}
