// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

public class Driver {

    public static void main(String[] args) {
        // TODO: Hardcode number of players for testing
        int numPlayers = 3;

        LadderAndSnake game = new LadderAndSnake(numPlayers);
        GUIManager guiManager = new GUIManager(game);
    }
}

