import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;


public class Driver implements KeyListener {
    public static boolean isDebug = true;

    public void keyTyped(KeyEvent e) {
        // Invoked when a key has been typed.
    }

    public void keyPressed(KeyEvent e) {
        // Invoked when a key has been pressed.
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            System.out.println("test");
        }
    }

    public void keyReleased(KeyEvent e) {
        // Invoked when a key has been released.
    }

    public static void main(String[] args) throws InterruptedException {
        // Hardcode number of players for testing
        int numPlayers = 2;

        if (!isDebug) {
            Scanner scan = new Scanner(System.in);
            System.out.print("How many players are playing?: ");

            // TODO: Validate integer value and make sure number of players is between minPlayers-maxPlayers (2-4)
            numPlayers = scan.nextInt();

    		 //scan.close();
        }

        GUIManager guiManager = new GUIManager();
        LadderAndSnake game = new LadderAndSnake(numPlayers);
        game.setVisible(true);

        game.play();
    }
}
