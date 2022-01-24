// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class LadderAndSnake {
    private int numPlayers;
    Random random = new Random();
    private Player[] players = new Player[0];
    // TODO: Let players choose their jeton
    private char[] jetonOptions = {'♟', '⛄', '☠', '☕'};

    Hashtable<Player, Integer> playerOrderRolls = new Hashtable<Player, Integer>();

    private DiceRollAction diceRollMode = DiceRollAction.DETERMINE_ORDER;
    private Player currentPlayer;
    private GameState gameState;
    private boolean hasDeterminedPlayerOrder;

    public LadderAndSnake(int numPlayers) {
        this.gameState = GameState.CHOOSE_PLAYERS;
    }


    public void start() {
        currentPlayer = players[0];
    }

    public void onRollToDeterminePlayerOrder(int dieValue) {
        playerOrderRolls.put(currentPlayer, dieValue);

        debugDisplayPlayerOrderDetermineRolls();

        if (playerOrderRolls.size() != players.length) {
            currentPlayer = getNextPlayerForRollDetermine();
        } else if (orderDetermineHasTies()) {
            removeTiedPlayersFromList();
        } else {
            System.out.println("Yay, we may begin!");

            gameState = GameState.PLAY;
            GUIManager.getInstance().redraw();

            diceRollMode = DiceRollAction.MOVE;
            sortPlayersByRoll();
            hasDeterminedPlayerOrder = true;
            debugDisplayPlayerOrderDetermineRolls();
        }
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

    private void lockPlayerOrderDetermineRolls() {
        for (Player player : players) {
            boolean isTied = false;

            for (Player otherPlayer : players) {
                if (otherPlayer == player) continue;

                if (playerOrderRolls.get(player) == playerOrderRolls.get(otherPlayer)) {
                    isTied = true;
                }
            }

            player.setOrderRollComplete(!isTied);
        }
    }

    private void sortPlayersByRoll() {
        if (orderDetermineHasTies()) {
            System.out.println("Error: There are not supposed to be any ties at this point.");
            return;
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
    }

    private void debugDisplayPlayerOrderDetermineRolls() {
        System.out.println("-------------------------------------------------------");
        for (Player player : players) {
            if (currentPlayer == player){
                System.out.print("*");
            }
            System.out.println("Player "+player.getIcon() + " rolled " + playerOrderRolls.get(player));
        }
    }

    public Player getNextPlayerForMove() {
        int currentPlayerIndex = getPlayerIndex(currentPlayer);

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

    private void removeTiedPlayersFromList() {
        ArrayList<Player> playersToRemove = new ArrayList<Player>();

        for (Player player : players) {
            boolean isTied = false;

            for (Player otherPlayer : players) {
                if (otherPlayer == player) continue;

                if (playerOrderRolls.get(player) == playerOrderRolls.get(otherPlayer)) {
                    playersToRemove.add(player);
                }
            }
        }

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

    public DiceRollAction getDiceRollMode() {
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
        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    public char[] getJetonOptions() {
        return jetonOptions;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
