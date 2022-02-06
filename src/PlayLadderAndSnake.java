// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Scanner;

public class PlayLadderAndSnake {
    /** The game's entry point. */
    public static void main(String[] args) {
        LadderAndSnake game = new LadderAndSnake();
        GUIManager guiManager = new GUIManager(game);
        DrawingManager drawingManager = new DrawingManager(guiManager.getFrame());

        /*
        deprecatedPlayGame();
        */
    }

    /** This method is only to show that the functionality of Part II works. I am instead using 4 GUI buttons. When 2 buttons are pressed, a play game button appears. This forces a minimum of 2 players to join, and a maximum of 4 since there are only 4 buttons. */
    private static void deprecatedPlayGame() {
        System.out.println("Enter the # of players for your game - Number must be between 2 and 4 inclusively: ");

        Scanner scanner = new Scanner(System.in);

        String attemptMessage = "";
        boolean hasSucceeded = false;

        int numAttempts = 0;
        int maxNumAttempts = 4;

        while(!hasSucceeded) {
            numAttempts++;

            if (numAttempts > maxNumAttempts) {
                System.out.println("Bad attempt " + numAttempts + ". You have exhausted all your chances. Program will terminate!");
                System.exit(0);
            }

            String next = scanner.next();

            try {
                int numberOfPlayers = Integer.parseInt(next);
                if (numberOfPlayers >=2 && numberOfPlayers <= 4) {
                    hasSucceeded = true;
                    attemptMessage = "Good attempt " + numAttempts + ". " + numberOfPlayers + " players want to play!";
                    System.out.println();
                    // Would call the start playing function over here if I weren't using buttons.
                } else {
                    attemptMessage = "Bad attempt " + numAttempts + ". Please make sure your number is between 2 and 4 inclusively.";
                }
            } catch (NumberFormatException e) {
                attemptMessage = "Bad attempt " + numAttempts + ". " + next + " is not a number.";
            }

            System.out.println(attemptMessage);
        }
    }
}
