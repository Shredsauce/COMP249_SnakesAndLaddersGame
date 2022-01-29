// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

public class Driver {

    public static void main(String[] args) {
        LadderAndSnake game = new LadderAndSnake();
        GUIManager guiManager = new GUIManager(game);
        DrawingManager drawingManager = new DrawingManager(guiManager.getFrame());
    }
}

