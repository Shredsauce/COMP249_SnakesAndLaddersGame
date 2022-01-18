// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

public class LadderAndSnake {
    private int numPlayers;
    Random random = new Random();
    private Player[] players;
    // TODO: Let players choose their jeton
    private char[] jetonOptions = {'♟', '⛄', '☠', '☕'};

    public LadderAndSnake(int numPlayers) {
        this.numPlayers = numPlayers;

        System.out.println(numPlayers);
        players = new Player[numPlayers];

        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(i, jetonOptions[i]);
        }

//        determinePlayerOrder();
    }

    public Player[] getPlayers() {
        return players;
    }

    private void determinePlayerOrder() {
		// TODO: This doesn't work properly at all

        System.out.println(numPlayers + " players playing. Woohoo!");
        System.out.println("Let's roll the die to see who starts.");

        Scanner scanner = new Scanner(System.in);

        Hashtable<Player, Integer> playerRolls = new Hashtable<Player, Integer>();
        for (Player player : players) {
            playerRolls.put(player, 0);
        }

        boolean orderDetermined = false;

        while (!orderDetermined) {
            Hashtable<Player, Integer> playerRollsThisRound = new Hashtable<Player, Integer>(playerRolls);

            for (int i = 0; i < players.length; i++) {
                Player player = players[i];

                if (player.hasUniqueRoll(playerRolls)) {
                    continue;
                }

                int playerNumber = player.displayedPlayerNumber();

                System.out.println(String.format("Player %s, press Enter to roll the die.", playerNumber));
                scanner.nextLine();

                int roll = flipDice();
                playerRollsThisRound.put(player, roll);

                System.out.println(String.format("Player %s's roll is %d", playerNumber, roll));

            }

            playerRolls = playerRollsThisRound;

            for(Player player : players) {
                if (!player.hasUniqueRoll(playerRolls)) {
                    continue;
                }
            }

            orderDetermined = true;
        }

        System.out.println("Order has been determined");
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
}