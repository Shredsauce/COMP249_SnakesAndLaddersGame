// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Random;

/** Handles the buttons and panels. */
public class GUIManager extends JComponent {
    public static GUIManager instance;
    public static int WIDTH = 600;
    public static int HEIGHT = 600;
    public static Int2 OFFSCREEN_POSITION = new Int2(-100, -100);

    private LadderAndSnake game;
    private boolean hasMainMenuBeenInit;

    private JFrame frame;
    private Container mainContainer;
    private JPanel headerPanel;
    private JPanel textDisplayPanel;
    private Board board;
    private String textToDisplay = "";
    private boolean isAnimating;
    private Thread animateShowBoardThread;

    private Int2 nextDieMouseRollPos = OFFSCREEN_POSITION;
    private Int2 previousMousePos = new Int2(0, 0);

    public static GUIManager getInstance() {
        return instance;
    }

    /** Constructor
     * @param game The game that will be played.
     * */
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

    /** Close the game and display a goodbye messge. */
    private void closeWindow() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }

        System.out.println("Thanks for playing, goodbye");
    }

    /** Called after GUI changes are made. */
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
                displayPossiblePlayerButtons(game.getJetonOptions());

                if (game.getPlayers().length >= game.getMinPlayerCount()) {
                    displayToggleGameTypeButton();
                    displayStartButton();
                }

                break;
            case MAIN_MENU:
                displayStartButton();
                displayToggleGameTypeButton();
                displayExitButton();
                break;
            case CHOOSE_PLAYER_ORDER:
                board = new Board(game);
                mainContainer.add(DrawingManager.getInstance(), BorderLayout.CENTER);
                displayCancelButton();
                break;
            case PLAY:
                board = new Board(game);
                board.initPlayers(game.getPlayers());
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

    /** Creates that main menu items only if they have not yet been created. */
    private void tryInitMainMenu() {
        if (!hasMainMenuBeenInit) {
            headerPanel = new JPanel();
            textDisplayPanel = new JPanel();

            mainContainer = frame.getContentPane();
            mainContainer.setLayout(new BorderLayout());

            hasMainMenuBeenInit = true;
        }
    }

    /** Clears any existing panels so that they can be recreated. */
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

    /** Displays the remaining player buttons that have not yet been chosen.
     * @param jetonOptions All possible icon options.
     * */
    private void displayPossiblePlayerButtons(String[] jetonOptions) {
        for (String jetonOption : jetonOptions) {
            if (isJetonChosenByPlayer(jetonOption)) {
                continue;
            }
 
            JButton choosePlayerBtn = new JButton(""+jetonOption);
            choosePlayerBtn.addActionListener(event -> onPlayerOptionSelected(jetonOption));
            headerPanel.add(choosePlayerBtn);
        }
    }

    /** @return True if an icon option has already been chosen by another player.
     * @param jeton The icon option to check.
     * */
    private boolean isJetonChosenByPlayer(String jeton) {
        for (Player player : game.getPlayers()) {
            if (player.getIcon() == jeton) {
                return true;
            }
        }

        return false;
    }

    /** Called after a player option has been selected.
     * @param jetonOption The icon option that was selected.
     * */
    private void onPlayerOptionSelected(String jetonOption) {
        System.out.println("Display button selected. Number of players so far: " + game.getPlayers().length);

        Player newPlayer = new Player(jetonOption);
        game.addPlayer(newPlayer);
        setDisplayText(newPlayer.getIcon() + " wants to play!");

        if (game.getPlayers().length == game.getJetonOptions().length) {
            game.play();
        } else {
            updateDisplay();
        }
    }

    /** Displays the start button*/
    private void displayStartButton() {
        JButton startGameBtn = new JButton("Start game");
        startGameBtn.addActionListener(event -> game.play());
        headerPanel.add(startGameBtn);
    }

    /** Displays the toggle game type button. This is used to toggle between default and random game mode. */
    private void displayToggleGameTypeButton() {
        String gameTypeText = game.getIsDefaultGameType() ? "Default game" : "Random game";
        JButton toggleGameTypeBtn = new JButton(gameTypeText);

        toggleGameTypeBtn.addActionListener(event -> toggleGameType());
        headerPanel.add(toggleGameTypeBtn);
    }

    /** Displays the exist game button. */
    private void displayExitButton() {
        JButton exitGameBtn = new JButton("Exit game");

        exitGameBtn.addActionListener(event -> closeWindow());
        headerPanel.add(exitGameBtn);
    }

    /** Toggle the game type between default and random mode. */
    private void toggleGameType() {
        game.toggleGameType();
        updateDisplay();
    }

    /** Display the label at the top of the game. */
    private void displayTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.append(textToDisplay);

        removePreviousTextDisplays();

        textDisplayPanel.add(textArea);
    }

    /** Remove the previous text displays so that the new one may be displayed. */
    private void removePreviousTextDisplays() {
        Component[] textAreaComponents = textDisplayPanel.getComponents();
        for (Component textAreaComponent : textAreaComponents) {
            textDisplayPanel.remove(textAreaComponent);
        }
    }

    /** @param text The text that will be displayed at the top of the game. */
    public void setDisplayText(String text) {
        System.out.println(text);
        textToDisplay = text;
        displayTextArea();
        frame.setVisible(true);
        frame.repaint();
    }

    /** Display the cancel button. */
    private void displayCancelButton() {
        JButton cancelGameBtn = new JButton("Cancel game");

        cancelGameBtn.addActionListener(event -> onQuitToMainMenu());
        headerPanel.add(cancelGameBtn);
    }

    /** Display the button the simulate the current player winning button. I have a really cool animation that happens when a player wins but the default snakes and ladders board takes forever for a winner to be chosen. */
    private void displaySimulateCurrentPlayerWinButton() {
        JButton simulateWinBtn = new JButton("Simulate current player win");

        simulateWinBtn.addActionListener(event -> onSimulateWin());
        headerPanel.add(simulateWinBtn);
    }

    /** Simulate the current player winning. */
    private void onSimulateWin() {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.setCurrentTile(board.getLastTile());
        validateWin(currentPlayer);
    }

    /** Quit to the main menu and reset the text display. */
    private void onQuitToMainMenu() {
        game.setGameState(GameState.MAIN_MENU);
        setDisplayText("");
        updateDisplay();
        resetPlayerPositions();
    }

    private void resetPlayerPositions() {
        for(Player player : game.getPlayers()) {
            player.setCurrentPosition(OFFSCREEN_POSITION);
        }
    }

    /** Called after a player has won.
     * @param player The player who won.
     * */
    private void onWin(Player player) {
        setDisplayText(player.toString() + " wins!");
        setIsAnimating(true, "Start win animation");

        int numWinningDice = 400;

        Thread thread = new Thread(() -> diceMania(numWinningDice));
        thread.start();

    }

    /**
     * Stop refreshing the background and change the die's position so that it looks like it's multiplying. Similar to the mountains of cards that rain down in Solitaire.
     * @param numFallingDice The number of dice that should appear. This is really the number of times the die changes position.
     */
    private void diceMania(int numFallingDice) {
        DrawingManager.getInstance().setShouldRefreshBackground(false);
        for (int i = 0; i < numFallingDice; i++) {
            Random random = new Random();
            Int2 pos = new Int2(random.nextInt(0, WIDTH), random.nextInt(0, HEIGHT));
            GUIManager.getInstance().setDieRollPos(pos);

            rollDie(DiceRollMode.WIN_STATE);

            ThreadManager.getInstance().threadSleep(5);
        }

        GUIManager.getInstance().setDieRollPos(GUIManager.OFFSCREEN_POSITION);
        DrawingManager.getInstance().setShouldRefreshBackground(true);
        setIsAnimating(false, "Start win animation");
        onQuitToMainMenu();
    }

    /** Move a player on the board.
     * @param player The player to move.
     * @param dieValue The dice value that the player rolled.
     * */
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

    /** Check if a player has won and call the win function if that is the case.
     * @param player The player whose win state should be validated.
     * */
    private void validateWin(Player player) {
        if (player.getCurrentTile().getTileId() == board.getLastTile().getTileId()) {
            onWin(player);
        }
    }

    /** Moves a player to a specified tile.
     * @param player The player to move.
     * @param newTileId The id of the new tile to move to.
     * */
    private void movePlayerToTile(Player player, int newTileId) {
        Tile currentTile = player.getCurrentTile();

        int lastTileId = board.getLastTile().getTileId();
        boolean moveForwards = true;

        int numMovesToMake = newTileId - currentTile.getTileId();
        int numMovesMade = 0;

        setIsAnimating(true, "Start player move anim");
        while (numMovesMade < numMovesToMake) {
            int currentTileId = currentTile.getTileId();

            if (currentTileId == lastTileId && newTileId != lastTileId) {
                moveForwards = false;
            }

            int nextTileId = currentTileId + (moveForwards ? 1 : -1);

            numMovesMade++;

            currentTile = board.getTile(nextTileId);
            player.setCurrentTile(currentTile);
            Int2 goalPos = currentTile.getBoardPosition();
            player.setCurrentPosition(goalPos);

            ThreadManager.getInstance().threadSleep(300);
        }

        System.out.println("Has move to: " + player.getCurrentTile().hasMoveTo());
        // If tile has a move-to (snake or ladder), move the player to move-to's tile
        if (currentTile.hasMoveTo()) {
            int moveToTileId = currentTile.getMoveToTileId();

            System.out.println("Move to tile id: " + moveToTileId);
            Tile moveToTile = board.getTile(moveToTileId);

            player.setCurrentTile(moveToTile);
            Int2 goalPos = moveToTile.getBoardPosition();
            player.setCurrentPosition(goalPos);
        }

        setIsAnimating(false, "End player move anim");
    }

    /** Roll dice. Calls onRollDieAnimComplete after the animation has completed.
     * @param diceRollAction What should happen after the dice has completed rolling.
     * */
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

    /** Called after the dice animation has completed.
     * @param dieValue The value of the rolled dice.
     * @param diceRollAction What to do after the dice has completed rolling.
     * */
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

    /** Sets the parameters needed for the dice rolling animation. */
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

    /** Sets the parameters for the snake animation that plays when the board is first shown. */
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

    /** Set the animating state of the GUI. This is a private setter used for debugging purposes.
     * @param isAnimating Is the GUI currently animating.
     * @param context The context used for debugging.
     * */
    private void setIsAnimating(boolean isAnimating, String context) {
        // Uncommented to debug
//        System.out.println("Set is animating: " + isAnimating + " -- context: " + context);
        this.isAnimating = isAnimating;
    }

    /** @return True if the GUI is currently animating. */
    public boolean isInAnimation() {
        return isAnimating;
    }

    /** Set the position of the dice.
     * @param pos The position of the dice. */
    public void setDieRollPos(Int2 pos) {
        this.nextDieMouseRollPos = pos;
    }

    /** The next dice position based on the mouse position. */
    public Int2 getNextDieMouseRollPos() {
        return nextDieMouseRollPos;
    }

    /** @return The main JFrame. */
    public JFrame getFrame() {
        return frame;
    }

    /** Called after the mouse is released.
     * @param mouseEvent The Mouse event. */
    public void onMouseReleased(MouseEvent mouseEvent) {
        if (!isInAnimation() && board != null) {
            nextDieMouseRollPos = new Int2(mouseEvent.getX(), mouseEvent.getY());
            GUIManager.getInstance().rollDie(game.getDiceRollMode());
        }
    }

    /** Called while the mouse is being dragged.
     * @param mouseEvent The Mouse event.*/
    public void onMouseDragged(MouseEvent mouseEvent) {
        if (!GUIManager.getInstance().isInAnimation()) {
            nextDieMouseRollPos = new Int2(mouseEvent.getX(), mouseEvent.getY());

            double x = previousMousePos.x - nextDieMouseRollPos.x;
            double y = previousMousePos.y - nextDieMouseRollPos.y;

            previousMousePos = nextDieMouseRollPos;
        }
    }
}
