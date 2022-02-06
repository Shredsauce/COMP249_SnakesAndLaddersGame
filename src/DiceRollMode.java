// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** Used to determine what to do after the die has completed rolling. I would have liked to use callbacks for this but Java doesn't make it esay to use those. Maybe on the next project. */
public enum DiceRollMode {
    /** Don't do anything after rolling the die. */
    NONE,
    /** Die was rolled to determine the player order. */
    DETERMINE_ORDER,
    /** Die was rolled to move the current player. */
    MOVE,
    /** This is used for the falling dice animation when a player wins. */
    FALLING_ANIMATION
}
