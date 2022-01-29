// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;

public class Player {

    private Tile currentTile;
    private String icon;
    private int playerIndex;
    private boolean orderRollComplete;

    public Player(int playerIndex, String icon) {
        this.icon = icon;
        this.playerIndex = playerIndex;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    public String getIcon() {
        return icon;
    }

    public int displayedPlayerNumber() {
        return playerIndex + 1;
    }

    public boolean hasUniqueRoll(Hashtable<Player, Integer> playerRolls) {
        int thisPlayerRoll = playerRolls.get(this);

        for(Player player : playerRolls.keySet()) {
            if (player != this && playerRolls.get(this) == thisPlayerRoll) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "Player "+icon;
    }

    public boolean hasCompletedOrderRoll() {
        return orderRollComplete;
    }

    public void setOrderRollComplete(boolean complete) {
        orderRollComplete = complete;
    }

    public static String getPlayerOrderAsText(Player[] players) {
        return getPlayersAsText(players, "then");
    }

    public static String getPlayerListAsText(Player[] players) {
        return getPlayersAsText(players, "and");
    }

    private static String getPlayersAsText(Player[] players, String lastJoiningWord) {
        String text = "";
        for (int i = 0; i < players.length; i++) {
            if (i == players.length - 1) {
                text += " " + lastJoiningWord + " ";
            } else if (i > 0) {
                text += ", ";
            }

            text += players[i].getIcon();
        }
        return text;
    }
}
