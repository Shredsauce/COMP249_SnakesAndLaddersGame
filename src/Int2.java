// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** Simple container for a point at (x, y) */
public class Int2 {
    /** The x value. */
    public int x;
    /** The y value. */
    public int y;

    /** Constructor that takes an x and a y integer parameters.
     * @param x The x value.
     * @param y The y value. */
    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Constructor that takes no parameters. x and y use their default initialization of 0. */
    public Int2() {
        // x and y default to 0
    }

    /** Overridden toString function that prints the point in a (x, y) format. */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
