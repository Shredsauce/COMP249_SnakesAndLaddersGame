// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** The state of the game. */
public enum GameState {
    /** Doesn't do anything. */
    NONE,
    /** When the players are being chosen. */
    CHOOSE_PLAYERS,
    /** When the order of the players is being determined. */
    CHOOSE_PLAYER_ORDER,
    /** During an actual game. */
    PLAY,
    /** While we are in the main menu and not choosing players or determining player order. */
    MAIN_MENU,
}
