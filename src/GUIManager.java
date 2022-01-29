// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Random;

public class GUIManager extends JComponent {
    public static GUIManager instance;
    public static int WIDTH = 600;
    public static int HEIGHT = 600;
    public static Int2 OFFSCREEN_DIE_POS = new Int2(-100, -100);

    private LadderAndSnake game;
    private int numPlayersChosen;
    private boolean hasMainMenuBeenInit;

    private JFrame frame;
    private Container mainContainer;
    private JPanel headerPanel;
    private JPanel textDisplayPanel;
    private Board board;
    private String textToDisplay = "";
    private boolean isAnimating;

    private Int2 nextDieMouseRollPos = OFFSCREEN_DIE_POS;
    private Int2 previousMousePos = new Int2(0, 0);

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

        String welcomeText = "Welcome to snakes and ladders.";
        welcomeText += " This is a " + game.getMinPlayerCount() + "-" + game.getMaxPlayerCount() + " player game. Please select the players from the options below.";
        setDisplayText(welcomeText);
    }

    public JFrame getFrame() {
        return frame;
    }

    private void closeWindow() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }

        System.out.println("Thanks for playing, goodbye");
    }

    public void updateDisplay() {
        tryInitMainMenu();
        clearPanels();

        mainContainer.add(headerPanel, BorderLayout.SOUTH);
        mainContainer.add(textDisplayPanel, BorderLayout.NORTH);
        mainContainer.add(new MouseHandler());

        switch(game.getGameState()) {
            case NONE:
                break;
            case CHOOSE_PLAYERS:
                displayPossiblePlayerButtons(game);
                tryDisplayStartButton();
                break;
            case MAIN_MENU:
                tryDisplayStartButton();
                displayToggleGameTypeButton();
                displayExitButton();
                break;
            case CHOOSE_PLAYER_ORDER:
                board = createBoard(game.getBoardSettings());
                mainContainer.add(DrawingManager.getInstance(), BorderLayout.CENTER);
                displayCancelButton();
                break;
            case PLAY:
                board = createBoard(game.getBoardSettings());
                board.setPlayers(game.getPlayers());
                animateShowBoard();
                mainContainer.add(DrawingManager.getInstance(), BorderLayout.CENTER);
                displayCancelButton();
                displaySimulateCurrentPlayerWinButton();
                break;
        }

        displayTextArea();

        frame.setVisible(true);
        frame.repaint();
    }

    private void tryInitMainMenu() {
        if (!hasMainMenuBeenInit) {
            headerPanel = new JPanel();
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
        for (String jetonOption : game.getJetonOptions()) {
            if (isJetonChosenByPlayer(jetonOption)) {
                continue;
            }
 
            JButton choosePlayerBtn = new JButton(""+jetonOption);
            choosePlayerBtn.addActionListener(event -> onPlayerSelected(jetonOption));
            headerPanel.add(choosePlayerBtn);
        }
    }

    private boolean isJetonChosenByPlayer(String jeton) {
        for (String jetonOption : game.getJetonOptions()) {
            for (Player player : game.getPlayers()) {
                if (player.getIcon() == jeton) {
                    return true;
                }
            }
        }

        return false;
    }

    private void onPlayerSelected(String jetonOption) {
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

        JButton startGameBtn = new JButton("Start game");
        startGameBtn.addActionListener(event -> tryStartGame());
        headerPanel.add(startGameBtn);
    }

    private void displayToggleGameTypeButton() {
        String gameTypeText = game.getIsDefaultGameType() ? "Default game" : "Random game";
        JButton toggleGameTypeBtn = new JButton(gameTypeText);

        toggleGameTypeBtn.addActionListener(event -> toggleGameType());
        headerPanel.add(toggleGameTypeBtn);
    }

    private void displayExitButton() {
        JButton exitGameBtn = new JButton("Exit game");

        exitGameBtn.addActionListener(event -> closeWindow());
        headerPanel.add(exitGameBtn);
    }

    private void toggleGameType() {
        game.toggleGameType();
        updateDisplay();
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

            game.setGameState(GameState.PLAY);
        } else {
            game.getCurrentPlayer();
            String text = "Player order must be decided based on the highest roll. " + game.getCurrentPlayer().toString() + ", click anywhere to roll the die.";
            GUIManager.getInstance().setDisplayText(text);

            game.setGameState(GameState.CHOOSE_PLAYER_ORDER);
        }

        updateDisplay();
    }

    private void displayCancelButton() {
        JButton cancelGameBtn = new JButton("Cancel game");

        cancelGameBtn.addActionListener(event -> onQuitToMainMenu());
        headerPanel.add(cancelGameBtn);
    }

    private void displaySimulateCurrentPlayerWinButton() {
        JButton simulateWinBtn = new JButton("Simulate current player win");

        simulateWinBtn.addActionListener(event -> onSimulateWin());
        headerPanel.add(simulateWinBtn);
    }

    private void onSimulateWin() {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.setCurrentTile(board.getLastTile());
        validateWin(currentPlayer);
    }

    private void onQuitToMainMenu() {
        game.setGameState(GameState.MAIN_MENU);
        setDisplayText("");
        updateDisplay();
    }

    private void onWin(Player player) {
        setDisplayText(player.toString() + " wins!");
        setIsAnimating(true, "Start win animation");

        int numWinningDice = 400;

        Thread thread = new Thread(() -> {
            DrawingManager.getInstance().setShouldRefreshBackground(false);
            for (int i = 0; i < numWinningDice; i++) {
                Random random = new Random();
                Int2 pos = new Int2(random.nextInt(0, WIDTH), random.nextInt(0, HEIGHT));
                GUIManager.getInstance().setDieRollPos(pos);

                rollDie(DiceRollMode.WIN_STATE);

                ThreadManager.getInstance().threadSleep(5);
            }

            GUIManager.getInstance().setDieRollPos(GUIManager.OFFSCREEN_DIE_POS);
            DrawingManager.getInstance().setShouldRefreshBackground(true);
            setIsAnimating(false, "Start win animation");
            onQuitToMainMenu();
        });
        thread.start();
    }

    public void movePlayer(Player player, int dieValue) {
        int newTileId = player.getCurrentTile().getTileId() + dieValue;

        Thread thread = new Thread(() -> {
            boolean isPlayingMoveAnim = true;
            while (isPlayingMoveAnim) {
                if (game.getPlayers() == null || game.getPlayers().length == 0) continue;
                if (player.getCurrentTile() == null) continue;

                movePlayerToTile(player, newTileId);
                isPlayingMoveAnim = false;
                validateWin(player);
            }
        });
        thread.start();
    }

    private void validateWin(Player player) {
        if (player.getCurrentTile().getTileId() == board.getLastTile().getTileId()) {
            onWin(player);
        }
    }

    private void movePlayerToTile(Player player, int newTileId) {
        Tile currentTile = player.getCurrentTile();

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
            movePlayerToTile(player, moveToTile.getTileId());
        }
    }

    // TODO: Make sure the die cannot be rolled while it's being animated
    public void rollDie(DiceRollMode diceRollAction) {
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
        if (game.getGameState() == GameState.MAIN_MENU) return;

        switch(diceRollAction) {
            case NONE:
                break;
            case DETERMINE_ORDER:
                game.onRollToDeterminePlayerOrder(dieValue);
                break;
            case MOVE:
                Player currentPlayer = game.getCurrentPlayer();
                String text = currentPlayer.toString() + " rolled a " + dieValue + ".";

                movePlayer(currentPlayer, dieValue);
                game.setCurrentPlayer(game.getNextPlayerForMove());

                text += " Your turn to roll the die " + game.getCurrentPlayer().toString();
                setDisplayText(text);

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

            String text = game.getCurrentPlayer().toString() + " roll the die to start";
            setDisplayText(text);

            board.setBoardAnimComplete();
            setIsAnimating(false, "End show board anim");
        });
        animateShowBoardThread.start();
    }

    public Board createBoard(BoardSettings boardSettings) {
        board = new Board(game, boardSettings);
        return board;
    }

    private void setIsAnimating(boolean isAnimating) {
        setIsAnimating(isAnimating, "");
    }

    private void setIsAnimating(boolean isAnimating, String context) {
//        System.out.println("Set is animating: " + isAnimating + " -- context: " + context);
        this.isAnimating = isAnimating;
    }

    public boolean isInAnimation() {
        return isAnimating;
    }

    public void setDieRollPos(Int2 pos) {
        this.nextDieMouseRollPos = pos;
    }


    public Int2 getNextDieMouseRollPos() {
        return nextDieMouseRollPos;
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        if (!isInAnimation() && board != null) {
            nextDieMouseRollPos = new Int2(mouseEvent.getX(), mouseEvent.getY());
            GUIManager.getInstance().rollDie(game.getDiceRollMode());
        }
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        if (!GUIManager.getInstance().isInAnimation()) {
            nextDieMouseRollPos = new Int2(mouseEvent.getX(), mouseEvent.getY());

            double x = previousMousePos.x - nextDieMouseRollPos.x;
            double y = previousMousePos.y - nextDieMouseRollPos.y;

            previousMousePos = nextDieMouseRollPos;
        }
    }
}
