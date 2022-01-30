// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;

/** Represents a player that is playing the snakes and ladders game. */
public class Player {
    private Tile currentTile;
    private String icon;
    private Int2 position = new Int2();

    /** Player constructor
     * @param icon The icon that the player will use.
     * */
    public Player(String icon) {
        this.icon = icon;
    }

    /** Get the current tile that the player is on. */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /** Set the player to the tile.
     * @param tile Tile that the player should be set to. */
    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    /** @return The icon uses by the player. */
    public String getIcon() {
        return icon;
    }

    /** @return The name of the player which includes their icon. */
    public String toString() {
        return "Player "+icon;
    }

    /** @return A formatted list of players used for the order determination message.
     * @param players The players that will be put into a formatted list.
     * */
    public static String getPlayerOrderAsText(Player[] players) {
        return getPlayersAsText(players, "then");
    }

    /** @return A formatted list of players
     * @param players The players that will be put into a formatted list.
     * */
    public static String getPlayerListAsText(Player[] players) {
        return getPlayersAsText(players, "and");
    }

    /** @return A formatted list of players.
     * @param players The players that will be put into a formatted list.
     * @param lastJoiningWord The last word that will be used to join the player list. (Example: If the last joining word is "and", the player list will go "player1, player2 and player3")
     * */
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

    public Int2 getCurrentPosition() {
        return position;
    }

    public void setCurrentPosition(Int2 position) {
        this.position = position;
    }
}
