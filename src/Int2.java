// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

public class Int2 {
    public int x;
    public int y;

    /** Constructor that takes an x and a y integer parameters. */
    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Constructor that takes no parameters. x and y use their default initialization of 0. */
    public Int2() {
        // x and y default to 0
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
