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

        String welcomeText = "Welcome to snakes and ladders.";
        welcomeText += " This is a " + game.getMinPlayerCount() + "-" + game.getMaxPlayerCount() + " player game.";
        GUIManager.getInstance().setDisplayText(welcomeText);

//        DrawingManager drawingManager = new DrawingManager(guiManager.getFrame());

//        Container mainContainer = frame.getContentPane();
//        mainContainer.setLayout(new BorderLayout());
//        mainContainer.add(DrawingManager.getInstance());
//        DrawingManager.getInstance().displayText("This is a test");
//        frame.setVisible(true);
    }
}

