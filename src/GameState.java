// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** The state of the game. NONE doesn't do anything, CHOOSE_PLAYERS is when the players are being chosen. CHOOSE_PLAYER_ORDER is when the order of the players is being determined. PLAY is during an actual game. MAIN_MENU is while we are in the main menu and not choosing players or determining player order. */
public enum GameState {
    NONE,
    CHOOSE_PLAYERS,
    CHOOSE_PLAYER_ORDER,
    PLAY,
    MAIN_MENU,
}
