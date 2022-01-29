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
    Random random = new Random();
    private Player[] players = new Player[0];

    /** Jeton options */
    private String[] jetonOptions = {"♟", "⛄", "☠", "☕"};
    private int minPlayerCount = 2;

    Hashtable<Player, Integer> playerOrderRolls = new Hashtable<Player, Integer>();

    private DiceRollMode diceRollMode = DiceRollMode.DETERMINE_ORDER;
    private Player currentPlayer;
    private GameState gameState;
    private boolean hasDeterminedPlayerOrder;

    private BoardSettings boardSettings = new BoardSettings();

    public LadderAndSnake() {
        this.gameState = GameState.CHOOSE_PLAYERS;
    }

    public void onRollToDeterminePlayerOrder(int dieValue) {
        playerOrderRolls.put(getCurrentPlayer(), dieValue);

        String text = getCurrentPlayer().toString() + " rolled " + dieValue + ".";

        if (playerOrderRolls.size() != players.length) {
            Player nextPlayerForDieRoll = getNextPlayerForRollDetermine();
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

    public boolean hasDeterminedPlayerOrder() {
        return hasDeterminedPlayerOrder;
    }

    private Player getNextPlayerForRollDetermine() {
        for (Player player : players) {
            if (!playerOrderRolls.containsKey(player)) {
                return player;
            }
        }

        return null;
    }

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

    public Player getNextPlayerForMove() {
        int currentPlayerIndex = getPlayerIndex(getCurrentPlayer());

        return players[(currentPlayerIndex + 1) % players.length];
    }

    public int getPlayerIndex(Player player) {
        int playerIndex = -1;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }

        System.out.println("Error: Could not find player index for player " + player.toString());
        return playerIndex;
    }

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

    private void removePlayersFromOrderRollList(Player[] playersToRemove) {
        for (Player player : playersToRemove) {
            playerOrderRolls.remove(player);
        }
    }

    public int flipDice() {
        int dieSides = 6;
        return random.nextInt(1, dieSides + 1);
    }

    public int getUniqueFlipDice(int excludeValue) {
        int result = flipDice();

        if (excludeValue == result) {
            getUniqueFlipDice(excludeValue);
        }

        return result;
    }

    public DiceRollMode getDiceRollMode() {
        return diceRollMode;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        Player[] incrementedPlayerList = new Player[players.length + 1];

        for (int i = 0; i < players.length; i++) {
            incrementedPlayerList[i] = players[i];
            System.out.println("i: " + i);
        }
        incrementedPlayerList[players.length] = player;

        players = incrementedPlayerList;
    }

    public Player getCurrentPlayer() {
        if (currentPlayer == null && players.length > 0) {
            currentPlayer = players[0];
        }

        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    public String[] getJetonOptions() {
        return jetonOptions;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getMinPlayerCount() {
        return minPlayerCount;
    }

    public int getMaxPlayerCount() {
        return jetonOptions.length;
    }

    public BoardSettings getBoardSettings() {
        return boardSettings;
    }

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

    public boolean getIsDefaultGameType() {
        return isDefaultGameType;
    }
}
