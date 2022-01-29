// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GUIManager extends JComponent {
    public static GUIManager instance;
    public static int WIDTH = 600;
    public static int HEIGHT = 600;

    private LadderAndSnake game;
    private int numPlayersChosen;
    private boolean hasMainMenuBeenInit;

    private JFrame frame;
    private Container mainContainer;
    private JPanel choosePlayersPanel;
    private JPanel footerPanel;
    private JPanel textDisplayPanel;
    private Container startBtnContainer;
    private Board board;
    public JButton rollDieBtn;
    private String textToDisplay = "";
    private boolean isAnimating;

    public static GUIManager getInstance() {
        return instance;
    }

    public GUIManager(LadderAndSnake game) {
        instance = this;
        this.game = game;

        frame = new JFrame("Snakes and Ladders game");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        updateDisplay();
    }

    public JFrame getFrame() {
        return frame;
    }

//    private void closeWindow() {
//        if (frame != null) {
//            frame.setVisible(false);
//            frame.dispose();
//        }
//        hasMainMenuBeenInit = false;
//    }

    public void updateDisplay() {
        tryInitMainMenu();
        clearPanels();

        mainContainer.add(choosePlayersPanel, BorderLayout.NORTH);
        mainContainer.add(footerPanel, BorderLayout.WEST);
        mainContainer.add(textDisplayPanel, BorderLayout.SOUTH);

        switch(game.getGameState()) {
            case NONE:
                break;
            case CHOOSE_PLAYERS:
                displayPossiblePlayerButtons(game);
                tryDisplayStartButton();
                break;
            case CHOOSE_PLAYER_ORDER:
                board = createBoard();
                mainContainer.add(board, BorderLayout.CENTER);
                tryDisplayCancelButton();
                break;
            case BOARD_CREATION_ANIMATION:
                break;
            case PLAY:
                board = createBoard();
                board.setPlayers(game.getPlayers());
                animateShowBoard();
                mainContainer.add(board, BorderLayout.CENTER);
                tryDisplayCancelButton();
                break;
            case MAIN_MENU:

                tryDisplayStartButton();
                break;
        }

        displayTextArea();

        frame.setVisible(true);
        frame.repaint();
    }

    private void tryInitMainMenu() {
        if (!hasMainMenuBeenInit) {
            choosePlayersPanel = new JPanel();
            footerPanel = new JPanel();
            textDisplayPanel = new JPanel();

            mainContainer = frame.getContentPane();
            mainContainer.setLayout(new BorderLayout());

            hasMainMenuBeenInit = true;
        }
    }

    private void clearPanels() {
        // Remove existing buttons from the panels.
        Component[] components = mainContainer.getComponents();

        for (Component component : components) {
            if (component instanceof JPanel panel) {

                Component[] subComponents = panel.getComponents();
                for (Component subComponent : subComponents) {
                    panel.remove(subComponent);
                }

                mainContainer.remove(component);
            }
        }
    }

    private void displayPossiblePlayerButtons(LadderAndSnake game) {
        for (char jetonOption : game.getJetonOptions()) {
            if (isJetonChosenByPlayer(jetonOption)) {
                continue;
            }
 
            JButton choosePlayerBtn = new JButton(""+jetonOption);
            choosePlayerBtn.addActionListener(event -> onPlayerSelected(jetonOption));
            choosePlayersPanel.add(choosePlayerBtn);
        }
    }

    private boolean isJetonChosenByPlayer(char jeton) {
        for (char jetonOption : game.getJetonOptions()) {
            for (Player player : game.getPlayers()) {
                if (player.getIcon() == jeton) {
                    return true;
                }
            }
        }

        return false;
    }

    private void onPlayerSelected(char jetonOption) {
        System.out.println("Display button selected. Number of players so far: " + game.getPlayers().length);

        Player newPlayer = new Player(numPlayersChosen++, jetonOption);
        game.addPlayer(newPlayer);
        setDisplayText(newPlayer.getIcon() + " wants to play!");

        if (game.getPlayers().length == game.getJetonOptions().length) {
            tryStartGame();
        } else {
            updateDisplay();
        }
    }

    private void tryDisplayStartButton() {
        if (game.getPlayers().length < game.getMinPlayerCount()) return;
        if (footerPanel.getComponents().length > 0) return;

        JButton startGameBtn = new JButton("Start game");
        startGameBtn.addActionListener(event -> tryStartGame());
        footerPanel.add(startGameBtn);
    }

    private void displayTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.append(textToDisplay);

        removePreviousTextDisplays();

        textDisplayPanel.add(textArea);
    }

    private void removePreviousTextDisplays() {
        Component[] textAreaComponents = textDisplayPanel.getComponents();
        for (Component textAreaComponent : textAreaComponents) {
            textDisplayPanel.remove(textAreaComponent);
        }
    }

    public void setDisplayText(String text) {
        System.out.println(text);
        textToDisplay = text;
        displayTextArea();
        frame.setVisible(true);
        frame.repaint();
    }

    private void tryStartGame() {
        if (game.hasDeterminedPlayerOrder()) {

//            ThreadManager.getInstance().threadSleep(3000);

            game.setGameState(GameState.PLAY);
        } else {
            game.getCurrentPlayer();
            String text = "Player order must be decided based on the highest roll. " + game.getCurrentPlayer().toString() + ", click anywhere to roll the die.";
            GUIManager.getInstance().setDisplayText(text);

            game.setGameState(GameState.CHOOSE_PLAYER_ORDER);
        }

        updateDisplay();
    }

    private void tryDisplayCancelButton() {
        JButton cancelGameBtn = new JButton("Cancel game");
        footerPanel.add(cancelGameBtn);

        cancelGameBtn.addActionListener(event -> onCancelGame());
        footerPanel.add(cancelGameBtn);
    }

    private void onCancelGame() {
        game.setGameState(GameState.MAIN_MENU);
        updateDisplay();
    }

    private void onWin(Player player) {
        setDisplayText(player.toString() + " wins!");

        Thread thread = new Thread(() -> {
            board.setWinState(true);
            // TODO: Put as variable somewhere
            int numWinningDice = 400;
            for (int i = 0; i < numWinningDice; i++) {
                Random random = new Random();
                Int2 pos = new Int2(random.nextInt(0, WIDTH), random.nextInt(0, HEIGHT));
                board.setDieRollPos(pos);

                rollDie(DiceRollMode.WIN_STATE);

                // TODO: Put as variable somewhere
                ThreadManager.getInstance().threadSleep(5);
            }

            board.setDieRollPos(Board.OFFSCREEN_DIE_POS);
            board.setWinState(false);
        });
        thread.start();
    }

    public void movePlayer(Player player, int dieValue) {
        int newTileId = player.getCurrentTile().getTileId() + dieValue;

        String text = player.toString() + " rolled a " + dieValue;

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

                    currentTile = board.getTile(nextTileId);
                    player.setCurrentTile(currentTile);

                    ThreadManager.getInstance().threadSleep(300);
                }

                // If tile has a move-to (snake or ladder), move the player to move-to's tile
                if (player.getCurrentTile().hasMoveTo()) {
                    int moveToTileId = player.getCurrentTile().getMoveToTileId();

                    Tile moveToTile = board.getTile(moveToTileId);

                    player.setCurrentTile(moveToTile);
                }
                setDisplayText(text);

                isPlayingMoveAnim = false;

                if (player.getCurrentTile().getTileId() == lastTileId) {
                    onWin(player);
                }

            }
        });
        thread.start();
    }

    // TODO: Make sure the die cannot be rolled while it's being animated
    public void rollDie(DiceRollMode diceRollAction) {
        if (isAnimating) return;
        
        Thread thread = new Thread(() -> {
            setIsAnimating(true, "Start roll die");
            int dieValue = animateDie();
            ThreadManager.getInstance().threadSleep(500);
            setIsAnimating(false, "End roll die");
            onRollDieAnimComplete(dieValue, diceRollAction);
        });
        thread.start();
    }

    private void onRollDieAnimComplete(int dieValue, DiceRollMode diceRollAction) {
        switch(diceRollAction) {
            case NONE:

                break;
            case DETERMINE_ORDER:
                game.onRollToDeterminePlayerOrder(dieValue);
                break;
            case MOVE:
                Player currentPlayer = game.getCurrentPlayer();
                movePlayer(currentPlayer, dieValue);
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

            ThreadManager.getInstance().threadSleep(100);

            currentFakeRollIndex++;
        }

        board.setDieValue(actualRoll);
        return actualRoll;
    }

    private Thread animateShowBoardThread;

    private void animateShowBoard() {
        if (animateShowBoardThread != null) {
            // Using Thread.stop even though it is deprecated because it's the easiest way to stop the currently running thread
            animateShowBoardThread.stop();
            animateShowBoardThread = null;
            setIsAnimating(false, "Force stop show board anim");
        }

        animateShowBoardThread = new Thread(() -> {
            setIsAnimating(true, "Start show board anim");

            int currentTile = 1;
            while(currentTile < board.getLastTile().getTileId()) {
                ThreadManager.getInstance().threadSleep(40);
                currentTile += 1;
                board.setEndTileIdForAnim(currentTile);
            }

            board.setBoardAnimComplete();
            setIsAnimating(false, "End show board anim");

        });
        animateShowBoardThread.start();
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

    private void setIsAnimating(boolean isAnimating) {
        setIsAnimating(isAnimating, "");
    }

    private void setIsAnimating(boolean isAnimating, String context) {
        System.out.println("Set is animating: " + isAnimating + " -- context: " + context);
        this.isAnimating = isAnimating;
    }

    public boolean isInAnimation() {
        return isAnimating;
    }
}
