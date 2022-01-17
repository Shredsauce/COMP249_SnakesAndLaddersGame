import javax.swing.*;
import java.awt.*;

public class GUIManager {
    private LadderAndSnake game;

    private Board board;
    public JButton rollDieBtn;

    public GUIManager(LadderAndSnake game) {
        this.game = game;

        Board board = createBoard();

        animateShowBoard();

        JFrame frame = new JFrame("");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(board, BorderLayout.CENTER);

        JPanel controls = new JPanel();

        rollDieBtn = new JButton("Roll die");
        // TODO: Make onRollDie a little more generic so it can be used to determine player order. Also make sure the die cannot be rolled while it's being animated (see if button can be hidden while it's rolling)
        rollDieBtn.addActionListener(event -> onRollDie());

        controls.add(rollDieBtn);

        content.add(controls, BorderLayout.NORTH);

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        board.setPlayers(game.getPlayers());
    }

    public void movePlayerToTile(Player player, int newTileId) {
        Thread thread = new Thread(() -> {
            boolean isPlayingMoveAnim = true;
            while (isPlayingMoveAnim) {
                if (game.getPlayers() == null || game.getPlayers().length == 0) continue;

                // TODO: Put this stuff in a function
                Tile goalTile = board.getTile(newTileId);
                Tile currentTile = player.getCurrentTile();

                if (currentTile == null) continue;

                while (currentTile.getTileId() < goalTile.getTileId()) {
                    currentTile = board.getTile(currentTile.getTileId() + 1);

                    player.setCurrentTile(currentTile);
//                    System.out.println("Setting player to " + currentTile.getTileId());

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (player.getCurrentTile().hasMoveTo()) {
                    int moveToTileId = player.getCurrentTile().getMoveToTileId();
                    Tile moveToTile = board.getTile(moveToTileId);

                    player.setCurrentTile(moveToTile);
                }
                isPlayingMoveAnim = false;
            }
        });
        thread.start();
    }

    private void onRollDie() {
        Thread thread = new Thread(() -> {

            int dieValue = animateDie();

            // Wait a bit before actually moving the player
            threadSleep(500);

            // TODO: Alternate players. Using first player for testing
            Player currentPlayer = game.getPlayers()[0];

            int newTileId = currentPlayer.getCurrentTile().getTileId() + dieValue;

            movePlayerToTile(currentPlayer, newTileId);
        });
        thread.start();
    }

    private int animateDie() {
        int actualRoll = game.flipDice();
//        System.out.println(String.format("Player's roll is " + actualRoll));

        // Roll animation
        int lastRoll = 0;
        int numFakeRolls = 6;
        int currentFakeRollIndex = 0;

        while (currentFakeRollIndex < numFakeRolls){
            int uniqueRoll = game.getUniqueFlipDice(lastRoll);
            lastRoll = uniqueRoll;

            board.setDieValue(uniqueRoll);

            // Pause for a bit dice change
            threadSleep(100);

            currentFakeRollIndex++;
        }

        board.setDieValue(actualRoll);
        return actualRoll;
    }

    private void animateShowBoard() {
        Thread thread = new Thread(() -> {
            int currentTile = 1;
            while(currentTile < board.getLastTile().getTileId()) {
                threadSleep(5);
                currentTile += 1;
                board.setEndTileIdForAnim(currentTile);
            }

            board.setBoardAnimComplete();
        });
        thread.start();


    }

    public Board createBoard() {
        // TODO: Remove this. I forget what it's referring to
        // Using https://zetcode.com/gfx/java2d/introduction/ as boilerplate code to get a graphics window open

        // TODO: width and height should be their own variables probably

        BoardSettings boardSettings = new BoardSettings();
        board = new Board(boardSettings);

        return board;
    }

    private void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
