import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;


public class Driver implements KeyListener {

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



        Scanner scan = new Scanner(System.in);
        System.out.print("How many players are playing?: ");

        // TODO: Validate integer value and make sure number of players is between minPlayers-maxPlayers (2-4)
        int numPlayers = scan.nextInt();

//        scan.close();
//        // TODO: Temporarily hardcode numPlayers to 2
//        int numPlayers = 2;

        LadderAndSnake game = new LadderAndSnake(numPlayers);
        game.setVisible(true);

        game.play();
    }
}
