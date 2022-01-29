// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class LadderAndSnake {
    private boolean isDefaultGameType = true;
    private Random random = new Random();
    private Player[] players = new Player[0];
    private String[] jetonOptions = {"♟", "⛄", "☠", "☕"};
    private int minPlayerCount = 2;
    private Hashtable<Player, Integer> playerOrderRolls = new Hashtable<Player, Integer>();
    private DiceRollMode diceRollMode = DiceRollMode.DETERMINE_ORDER;
    private Player currentPlayer;
    private GameState gameState;
    private boolean hasDeterminedPlayerOrder;

    private BoardSettings boardSettings = new BoardSettings();

    /** The game's entry point. */
    public static void main(String[] args) {
        LadderAndSnake game = new LadderAndSnake();
        GUIManager guiManager = new GUIManager(game);
        DrawingManager drawingManager = new DrawingManager(guiManager.getFrame());
    }

    /** Constructor that sets the initial game state to CHOOSE_PLAYERS. */
    public LadderAndSnake() {
        this.gameState = GameState.CHOOSE_PLAYERS;
    }

    /**
     * Called during the order determine part of the game after the dice roll animation has completed for the player.
     * @param dieValue The value of the die
     * */
    public void onRollToDeterminePlayerOrder(int dieValue) {
        playerOrderRolls.put(getCurrentPlayer(), dieValue);

        String text = getCurrentPlayer().toString() + " rolled " + dieValue + ".";

        if (playerOrderRolls.size() != players.length) {
            Player nextPlayerForDieRoll = getNextPlayerForOrderDetermine();
            text += " " + nextPlayerForDieRoll.toString() + ", roll the die";
            setCurrentPlayer(nextPlayerForDieRoll);
        } else if (orderDetermineHasTies()) {
            Player[] tiedPlayers = getTiedPlayers();
            text += Player.getPlayerListAsText(tiedPlayers) + " are tied.";

            removePlayersFromOrderRollList(tiedPlayers);
        } else {
            diceRollMode = DiceRollMode.MOVE;
            players = sortPlayersByRoll(players);
            hasDeterminedPlayerOrder = true;

            text += " Player order determined. It goes " + Player.getPlayerOrderAsText(players) + ". Let's play!";
            GUIManager.getInstance().setDisplayText(text);

            gameState = GameState.PLAY;
            GUIManager.getInstance().updateDisplay();
        }

        GUIManager.getInstance().setDisplayText(text);
    }

    /** @return true if the player order has been determined. */
    public boolean hasDeterminedPlayerOrder() {
        return hasDeterminedPlayerOrder;
    }

    /** @return The next player that should be rolling to determine the player order. */
    private Player getNextPlayerForOrderDetermine() {
        for (Player player : players) {
            if (!playerOrderRolls.containsKey(player)) {
                return player;
            }
        }

        return null;
    }

    /** @return true if this round for order determination has any ties. */
    private boolean orderDetermineHasTies() {
        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (otherPlayer == player) continue;

                if (playerOrderRolls.get(player) == playerOrderRolls.get(otherPlayer)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Sort the players in ascending order by roll value.
     * @param players The array of players that will be sorted.
     * @return The sorted player array. */
    private Player[] sortPlayersByRoll(Player[] players) {
        if (orderDetermineHasTies()) {
            System.out.println("Error: There are not supposed to be any ties at this point.");
            return players;
        }

        int currentIndex = 0;
        while (currentIndex < players.length - 1) {
            int highestIndex = -1;
            int highest = -1000;

            for (int i = currentIndex; i < players.length; i++) {
                int playerRoll = playerOrderRolls.get(players[i]);

                if (playerRoll > highest) {
                    highest = playerRoll;
                    highestIndex = i;
                }
            }

            if (highestIndex > 0) {
                // Swap highest rolling player with first
                Player cachedFirstPlayer = players[currentIndex];
                players[currentIndex] = players[highestIndex];
                players[highestIndex] = cachedFirstPlayer;
            }

            currentIndex++;
        }

        return players;
    }

    /** @return The next player for moving. Wraps back around to the first player if we're at the end of the list. */
    public Player getNextPlayerForMove() {
        int currentPlayerIndex = getPlayerArrayIndex(getCurrentPlayer());

        return players[(currentPlayerIndex + 1) % players.length];
    }

    /** @return The index of the player in the players array. Null if the player is not in the array.
     * @param player The player whose index to get from the array. */
    public int getPlayerArrayIndex(Player player) {
        int playerIndex = -1;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }

        System.out.println("Error: Could not find player index for player " + player.toString());
        return playerIndex;
    }

    /** @return A list of all the players that have roll ties in the player order determine round. */
    private Player[] getTiedPlayers() {
        ArrayList<Player> playersToRemove = new ArrayList<Player>();

        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (otherPlayer == player) continue;

                if (playerOrderRolls.get(player) == playerOrderRolls.get(otherPlayer)) {
                    playersToRemove.add(player);
                }
            }
        }

        // Transform ArrayList<Player> to Player[]
        Player[] playersAsArray = new Player[playersToRemove.size()];
        for (int i = 0; i < playersAsArray.length; i++) {
            playersAsArray[i] = playersToRemove.get(i);
        }

        return playersAsArray;
    }

    /** Remove players from the roll list. This is used to exclude players who have tied as they will re-roll after the current roll round has completed.
     * @param playersToExclude players to exclude from the order roll list.
     * */
    private void removePlayersFromOrderRollList(Player[] playersToExclude) {
        for (Player player : playersToExclude) {
            playerOrderRolls.remove(player);
        }
    }

    /** @return A die value between 1 (inclusive) and 6 (inclusive) */
    public int flipDice() {
        int dieSides = 6;
        return random.nextInt(1, dieSides + 1);
    }

    /** This should only be used for the dice roll animation.
     * @param excludeValue The dice value to be excluded.
     * @return A different dice value from the one that has just been displayed.*/
    public int getUniqueFlipDice(int excludeValue) {
        int result = flipDice();

        if (excludeValue == result) {
            getUniqueFlipDice(excludeValue);
        }

        return result;
    }

    /** This is used to determine what to do after the dice has been rolled.
     * @return The dice roll mode. */
    public DiceRollMode getDiceRollMode() {
        return diceRollMode;
    }

    /** Get the list of players. */
    public Player[] getPlayers() {
        return players;
    }

    /** Add a player to the game.
     * @param player The player to add to the game. */
    public void addPlayer(Player player) {
        Player[] incrementedPlayerList = new Player[players.length + 1];

        for (int i = 0; i < players.length; i++) {
            incrementedPlayerList[i] = players[i];
            System.out.println("i: " + i);
        }
        incrementedPlayerList[players.length] = player;

        players = incrementedPlayerList;
    }

    /** @return The player whose turn it is. If the current player is null, attempts to set it using the first player. */
    public Player getCurrentPlayer() {
        if (currentPlayer == null && players.length > 0) {
            currentPlayer = players[0];
        }

        return currentPlayer;
    }

    /** Set the player those turn it is. */
    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    /** @return Player icon options. (This was supposed to be a char array but JavaDoc didn't consider them as characters.) */
    public String[] getJetonOptions() {
        return jetonOptions;
    }

    /** @return The game state for use with the menu options. */
    public GameState getGameState() {
        return gameState;
    }

    /** Set the game state which will be used to display the menu options. */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /** @return The minimum number of players needed to play the game. */
    public int getMinPlayerCount() {
        return minPlayerCount;
    }

    /** Get the maximum number of players allowed to play the game. This is determined by the number of icon options. */
    public int getMaxPlayerCount() {
        return jetonOptions.length;
    }

    /** Get the board settings that the new board should use upon creation. */
    public BoardSettings getBoardSettings() {
        return boardSettings;
    }

    /** Toggle Between the default and randomized versions of the game. */
    public void toggleGameType() {
        isDefaultGameType = !isDefaultGameType;

        if (isDefaultGameType) {
            boardSettings = new BoardSettings();
        } else {
            boardSettings.useDefault = false;
            boardSettings.horizontalChance = 0.5f;
            boardSettings.forwardChance = 0.5f;
        }
    }

    /** @return true if the game type is default, and false if the game type is randomized. */
    public boolean getIsDefaultGameType() {
        return isDefaultGameType;
    }
}
