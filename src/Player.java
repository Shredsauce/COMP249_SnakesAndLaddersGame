// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;

public class Player {

    private Tile currentTile;
    private char icon;
    private int playerIndex;

    public Player(int playerIndex, char icon) {
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
        return icon+"";
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
}
