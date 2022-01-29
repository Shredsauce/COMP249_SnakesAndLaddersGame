// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.Hashtable;

/** Settings that the created board should use. These include the randomization parameters. */
public class BoardSettings {
    public Int2 boardSize = new Int2(10, 10);
    public boolean useDefault = true;
    public float horizontalChance = 1f;
    public float forwardChance = 1f;
    public double chanceOfHavingMoveTo = 0.4;

    // Ladder visual settings
    public float ladderThickness = 3f;
    public int ladderWidth = 20;
    public int rungSpacing = 8;

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

    public Hashtable<Integer, Integer> getDefaultMoveToConfig() {
        return defaultMoveToConfig;
    }

    public double getChanceOfHavingMoveTo() {
        return chanceOfHavingMoveTo;
    }
}
