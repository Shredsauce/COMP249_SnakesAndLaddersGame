// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** Used to determine what to do after the die has completed rolling. I would have liked to use callbacks for this but Java doesn't make it esay to use those. Maybe on the next project. */
public enum DiceRollMode {
    NONE,
    DETERMINE_ORDER,
    MOVE,
    WIN_STATE
}
