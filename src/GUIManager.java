// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.security.auth.callback.Callback;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GUIManager extends JComponent {
    public static GUIManager instance;
    public static int WIDTH = 600;
    public static int HEIGHT = 600;

    private LadderAndSnake game;

    private Board board;
    public JButton rollDieBtn;

    public static GUIManager getInstance() {
        return instance;
    }

    public GUIManager(LadderAndSnake game) {
        instance = this;
        this.game = game;

        // TODO: Create button to regenerate board
        Board board = createBoard();

        animateShowBoard();

        JFrame frame = new JFrame("");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(board, BorderLayout.CENTER);

        JPanel controls = new JPanel();

        // TODO: Remove this
        rollDieBtn = new JButton("Simulate win");
        rollDieBtn.addActionListener(event -> onWin(game.getPlayers()[0]));

        controls.add(rollDieBtn);

        content.add(controls, BorderLayout.NORTH);

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        board.setPlayers(game.getPlayers());
    }

    private void onWin(Player player) {
        System.out.println(player.toString() + " wins!");

        Thread thread = new Thread(() -> {
            board.setWinState(true);
            // TODO: Put as variable somewhere
            int numWinningDice = 400;
            for (int i = 0; i < numWinningDice; i++) {
                Random random = new Random();
                Int2 pos = new Int2(random.nextInt(0, WIDTH), random.nextInt(0, HEIGHT));
                board.setDieRollPos(pos);

                rollDie(DiceRollAction.WIN_STATE);

                // TODO: Put as variable somewhere
                threadSleep(5);
            }

            board.setDieRollPos(Board.OFFSCREEN_DIE_POS);
            board.setWinState(false);
        });
        thread.start();
    }

    public void movePlayerToTile(Player player, int newTileId) {
        Thread thread = new Thread(() -> {
            boolean isPlayingMoveAnim = true;
            while (isPlayingMoveAnim) {
                if (game.getPlayers() == null || game.getPlayers().length == 0) continue;

                // TODO: Put this stuff in a function
                Tile currentTile = player.getCurrentTile();

                if (currentTile == null) continue;

                int lastTileId = board.getLastTile().getTileId();
                boolean moveForwards = true;

                int numMovesToMake = newTileId - currentTile.getTileId();
                int numMovesMade = 0;

                while (numMovesMade < numMovesToMake) {
                    int currentTileId = currentTile.getTileId();

                    if (currentTileId == lastTileId && newTileId != lastTileId) {
                        moveForwards = false;
                    }

                    int nextTileId = currentTileId + (moveForwards ? 1 : -1);

                    numMovesMade++;

                    System.out.println("Set player to tile " + nextTileId);

                    currentTile = board.getTile(nextTileId);
                    player.setCurrentTile(currentTile);

                    threadSleep(300);
                }

                // If tile has a move-to (snake or ladder), move the player to move-to's tile
                if (player.getCurrentTile().hasMoveTo()) {
                    int moveToTileId = player.getCurrentTile().getMoveToTileId();

                    Tile moveToTile = board.getTile(moveToTileId);

                    player.setCurrentTile(moveToTile);
                }

                isPlayingMoveAnim = false;

                if (player.getCurrentTile().getTileId() == lastTileId) {
                    onWin(player);
                }

            }
        });
        thread.start();
    }

    // TODO: Make sure the die cannot be rolled while it's being animated
    public void rollDie(DiceRollAction diceRollAction) {
        Thread thread = new Thread(() -> {
            int dieValue = animateDie();
            threadSleep(500);
            onRollDieAnimComplete(dieValue, diceRollAction);
        });
        thread.start();
    }

    private void onRollDieAnimComplete(int dieValue, DiceRollAction diceRollAction) {
        switch(diceRollAction) {
            case NONE:

                break;
            case DETERMINE_ORDER:
                game.onRollToDeterminePlayerOrder(dieValue);
                break;
            case MOVE:
                Player currentPlayer = game.getCurrentPlayer();
                int newTileId = currentPlayer.getCurrentTile().getTileId() + dieValue;
                movePlayerToTile(currentPlayer, newTileId);
                game.setCurrentPlayer(game.getNextPlayerForMove());
                break;
            case WIN_STATE:

                break;
        }
    }

    private int animateDie() {
        int actualRoll = game.flipDice();

        // Roll animation
        int lastRoll = 0;
        int numFakeRolls = 6;
        int currentFakeRollIndex = 0;

        // Make sure the fake die animation rolling doesn't get repeats
        // Roll until the number of fake rolls has exhausted, but also make sure we don't end on the same roll as the actual rolls
        // Otherwise the die appears to stall at the end of its roll
        while (currentFakeRollIndex < numFakeRolls || lastRoll == actualRoll){
            int uniqueRoll = game.getUniqueFlipDice(lastRoll);
            lastRoll = uniqueRoll;

            board.setDieValue(uniqueRoll);

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
                threadSleep(50);
                currentTile += 1;
                board.setEndTileIdForAnim(currentTile);
            }

            board.setBoardAnimComplete();
        });
        thread.start();
    }

    // TODO: This should be in the Board class
    public Board createBoard() {
        BoardSettings boardSettings = new BoardSettings();

        // TODO: Uncomment for randomly generated board
        /*
        boardSettings.useDefault = false;
        boardSettings.horizontalChance = 0.5f;
        boardSettings.forwardChance = 0.5f;
        */

        board = new Board(game, boardSettings);

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
