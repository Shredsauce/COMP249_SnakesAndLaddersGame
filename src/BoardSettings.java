// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;

/** Settings that the created board should use. These include the randomization parameters. */
public class BoardSettings {
    /** How many tiles the board should have */
    public Int2 boardSize = new Int2(10, 10);
    /** Whether the board should use the default configuration. */
    public boolean useDefault = true;
    /** The chance that the next tile being horizontal. (0 to 1 with 0 being lowest and 1 being highest chance of being horizontal). This is what causes the snake to twist to create the default board. */
    public float horizontalChance = 1f;
    /** The chance that the next has a greater x or y coordinate. (0 to 1 with 0 being lowest chance and 1 being highest chance of being forward tile)*/
    public float forwardChance = 1f;
    /** The chance of having a move-to (snake or ladder) element on the tile. (0 to 1 with 0 being lowest chance of having a move-to element and 1 being the highest chance.)*/
    public float chanceOfHavingMoveTo = 0.4f;

    // Ladder visual settings
    /** The thickness of the drawn ladder. */
    public float ladderThickness = 3f;
    /** The width of the drawn ladder. */
    public int ladderWidth = 20;
    /** The spawning between the ladder rungs. */
    public int rungSpacing = 8;

    /** The default config of the move-to (snake and ladder) elements. */
    private Hashtable<Integer, Integer> defaultMoveToConfig = new Hashtable<Integer, Integer>() {
        {put(1, 38);}
        {put(4, 14);}
        {put(9, 31);}
        {put(16, 6);}
        {put(21, 42);}
        {put(28, 84);}
        {put(36, 44);}
        {put(48, 30);}
        {put(51, 67);}
        {put(62, 19);}
        {put(64, 60);}
        {put(64, 60);}
        {put(71, 91);}
        {put(80, 100);}
        {put(93, 68);}
        {put(95, 24);}
        {put(97, 76);}
        {put(98, 78);}
    };

    /** @return the default move-to (snakes and ladders) elements. */
    public Hashtable<Integer, Integer> getDefaultMoveToConfig() {
        return defaultMoveToConfig;
    }

    /** @return the chance of having a move-to element. */
    public double getChanceOfHavingMoveTo() {
        return chanceOfHavingMoveTo;
    }

    /** Make the board use randomized settings. */
    public void SetAsRandom() {
        useDefault = false;
        horizontalChance = 0.3f;
        forwardChance = 0.8f;
    }
}
