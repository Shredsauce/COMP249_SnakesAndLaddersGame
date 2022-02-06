// -----------------------------------------------------
// Assignment 1 due February 7
// Inspiration for using Graphics2D comes from https://zetcode.com/gfx/java2d/introduction/
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.*;

/** Representation of the snakes and ladders board */
public class Board {
    /** Board settings. */
    private BoardSettings boardSettings;

    /** Reference to the snakes and ladders game. */
    private LadderAndSnake game;
    /** Array of players. */
    private Player[] players;

    /** 2D array of tiles. */
    private Tile[][] tiles;
    /** A reference to the start tile. */
    private Tile startTile;
    /** A reference to last tile. */
    private Tile lastTile;
    /** The id of the end tile used for the board showing animation. */
    private int endTileIdForAnim;

    /** Whether the board showing animation has completed. */
    private boolean boardShowAnimComplete;

    /** The number of tiles that make up the width and height of the board. Set to 10x10 by default. */
    private Int2 boardSize = new Int2(10, 10);
    /** The current value of the die.*/
    private int currentDieValue = 6;

    /** @return The tile with tile id tileId.
     * @param tileId The id related to the tile being searched for. */
    public Tile getTile(int tileId) {
        // The zeroth start tile is not part of the 2D array of tiles so it is handled this way
        if (tileId == 0) {
            return startTile;
        }

        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Tile tile = tiles[x][y];

                if (tile != null && tile.getTileId() == tileId) {
                    return tile;
                }
            }
        }

        return null;
    }

    /** @return The tile at the specified coordinates. Returns null if the tile at the specified coordinates is non-existant. Clamps the coordinates to the board size.
     * @param coord The coordinates to get the tile at.
     * */
    public Tile getTileAtCoordinates(Int2 coord) {
        int clampedX = coord.x;
        if (clampedX < 0) {
            clampedX = 0;
        } else if (clampedX >= boardSize.x) {
            clampedX = boardSize.x - 1;
        }

        int clampedY = coord.y;
        if (clampedY < 0) {
            clampedY = 0;
        } else if (clampedY >= boardSize.y) {
            clampedY = boardSize.y - 1;
        }

        Tile tile = tiles[clampedX][clampedY];

        return tile;
    }

    /** Constructor that takes a LadderAndSnake game as a parameter.
     * @param game The snakes and ladders game.
     * */
    public Board(LadderAndSnake game) {
        this.boardSettings = game.getBoardSettings();
        this.boardSize = boardSettings.boardSize;
        this.game = game;

        DrawingManager.getInstance().setBoard(this);

        if (boardSettings.useDefault) {
            boardSettings.horizontalChance = 1f;
            boardSettings.forwardChance = 1f;
        }

        Int2 coord = new Int2(0, boardSize.y - 1);

        this.tiles = new Tile[boardSize.x][boardSize.y];

        int currentTileId = 1;
        boolean allTilesFound = false;
        while (!allTilesFound) {
            Tile tile = new Tile(currentTileId, coord);
            tiles[coord.x][coord.y] = tile;

            Random random = new Random();
            float horizontalChanceResult = random.nextFloat(0f, 1f);
            float forwardChanceResult = random.nextFloat(0f, 1f);
            boolean preferHorizontal = horizontalChanceResult < boardSettings.horizontalChance;
            boolean preferForward = forwardChanceResult < boardSettings.forwardChance;

            ArrayList<Int2> validNeighborCoords = getNeighboringTileCoords(tile);
            validNeighborCoords = sortValidCoordsByPreference(tile, validNeighborCoords, preferHorizontal, preferForward);

            if (validNeighborCoords.size() == 0) {
                allTilesFound = true;
                break;
            }

            coord = validNeighborCoords.get(0);

            currentTileId++;
        }

        startTile = createStartTile();
        lastTile = getTile(currentTileId);

        Hashtable<Integer, Integer> moveToConfig = generateMoveToConfig(boardSettings, lastTile);
        applyMoveToConfig(moveToConfig);
    }

    /** This creates the start tile at with id zero in the offscreen position.
     * @return The start tile that was created. */
    private Tile createStartTile() {
        if (startTile != null) {
            System.out.println("Error: Start tile already exists");
            return startTile;
        }

        Int2 startTilePos = GUIManager.OFFSCREEN_POSITION;
        return new Tile(0, startTilePos);
    }

    /** Generates the config needed for the 'move to' (snake and ladder) elements on the board. This is only needed for the random game mode. The default board uses a hardcoded config.
     * @return The hashtable key is the id of the start tile. The value is the id of the end tile.
     * @param boardSettings The  board settings for use with the randomization.
     * @param lastTile The tile on the board. */
    private Hashtable<Integer, Integer> generateMoveToConfig(BoardSettings boardSettings, Tile lastTile) {
        if (boardSettings.useDefault) {
            return boardSettings.getDefaultMoveToConfig();
        }

        Hashtable<Integer, Integer> generatedMoveToConfig = new Hashtable<Integer, Integer>();

        for (int i = 1; i < lastTile.getTileId() - 1; i++) {
            Random random = new Random();
            double moveToRandomResult = random.nextDouble(0.0, 1.0);

            if (moveToRandomResult > boardSettings.getChanceOfHavingMoveTo()) continue;

            int moveToId = random.nextInt(1, lastTile.getTileId());
            boolean moveToAlreadySet = false;
            for (int j = 0; j < generatedMoveToConfig.size(); j++) {
                if (generatedMoveToConfig.containsValue(moveToId)) {
                    moveToAlreadySet = true;
                }
            }

            if (!moveToAlreadySet) {
                generatedMoveToConfig.put(i, moveToId);
            }
        }

        return generatedMoveToConfig;
    }

    /** Set the move-to (snake and ladder element functionality) to the board. */
    private void applyMoveToConfig(Hashtable<Integer, Integer> moveToConfig) {
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Tile tile = tiles[x][y];

                if (tile != null && moveToConfig.containsKey(tile.getTileId())) {
                    tile.setMoveToTileId(moveToConfig.get(tile.getTileId()));
                }
            }
        }
    }

    /** Returns a list of neighboring tile coordinates.
     * @param tile The tile's whose neighbors will be checked
     * */
    private ArrayList<Int2> getNeighboringTileCoords(Tile tile) {
        Int2 coord = tile.getCoordinates();
        Int2 rightNeighborCoord = new Int2(coord.x + 1, coord.y);
        Int2 leftNeighborCoord = new Int2(coord.x - 1, coord.y);
        Int2 upNeighborCoord = new Int2(coord.x, coord.y - 1);
        Int2 downNeighborCoord = new Int2(coord.x, coord.y + 1);

        Tile rightNeighbor = getTileAtCoordinates(rightNeighborCoord);
        Tile leftNeighbor = getTileAtCoordinates(leftNeighborCoord);
        Tile upNeighbor = getTileAtCoordinates(upNeighborCoord);
        Tile downNeighbor = getTileAtCoordinates(downNeighborCoord);

        ArrayList<Int2> validCoords = new ArrayList<Int2>();

        if (rightNeighbor == null) { validCoords.add(rightNeighborCoord); }
        if (leftNeighbor == null) { validCoords.add(leftNeighborCoord); }

        if (upNeighbor == null) {
            validCoords.add(upNeighborCoord);
        }

        if (downNeighbor == null) {
            validCoords.add(downNeighborCoord);
        }

        return validCoords;
    }

    /** Sort the valid neighboring coordinates by their horizontal and forward preference. */
    private ArrayList<Int2> sortValidCoordsByPreference(Tile tile, ArrayList<Int2> validCoords, boolean preferHorizontal, boolean preferForward) {
        ArrayList<Int2> sortedCoords = new ArrayList<Int2>();
        ArrayList<Int2> rejectedCoords = new ArrayList<Int2>();

        for(Int2 validCoord : validCoords) {
            Int2 directionFromTile = new Int2(validCoord.x - tile.getCoordinates().x, validCoord.y - tile.getCoordinates().y);

            if (preferHorizontal && directionFromTile.y == 0) {
                sortedCoords.add(validCoord);
            } else if (!preferHorizontal && directionFromTile.x == 0) {
                sortedCoords.add(validCoord);
            }
        }

        for(Int2 validCoord : validCoords) {
            Int2 directionFromTile = new Int2(validCoord.x - tile.getCoordinates().x, validCoord.y - tile.getCoordinates().y);

            if (preferForward && directionFromTile.x > 0 || directionFromTile.y > 0) {
                sortedCoords.add(validCoord);
            } else if (!preferForward && directionFromTile.x < 0 || directionFromTile.y < 0) {
                sortedCoords.add(validCoord);
            } else {
                rejectedCoords.add(validCoord);
            }
        }

        sortedCoords.addAll(rejectedCoords);

        if (!boardSettings.useDefault) {
            sortedCoords = sortCoordinatesWonkily(sortedCoords);
        }

        return sortedCoords;
    }

    /** This is an attempt at randomizing the coordinates if the default board settings aren't used. */
    private ArrayList<Int2> sortCoordinatesWonkily(ArrayList<Int2> sortedCoords) {
        ArrayList<Int2> boundaryCoords = new ArrayList<>();
        for(Int2 sortedCoord : sortedCoords) {
            if (sortedCoord.x < 3 || sortedCoord.x >= boardSize.x - 3 ||
                    sortedCoord.y < 3 || sortedCoord.y >= boardSize.y - 3) {

                boundaryCoords.add(sortedCoord);
            }
        }

        ArrayList<Int2> preferNoBoundaryCoords = new ArrayList<>();

        for (Int2 sortedCoord : sortedCoords) {
            if (boundaryCoords.contains(sortedCoord)) continue;

            preferNoBoundaryCoords.add(sortedCoord);
        }

        preferNoBoundaryCoords.addAll(boundaryCoords);
        sortedCoords = preferNoBoundaryCoords;
        return sortedCoords;
    }

    /** Set the players to the board and place them on the starting tile.
     * @param players the player to initialize.
     * */
    public void initPlayers(Player[] players) {
        this.players = players;

        for (int i = 0; i < players.length; i++) {
            players[i].setCurrentTile(this.getTile(0));
        }
    }

    /** @return The last tile on the board. */
    public Tile getLastTile() {
        return lastTile;
    }

    /** @return The players on the board. */
    public Player[] getPlayers() {
        return players;
    }

    /** Set the board's die value.
     * @param dieValue The value of the die.
     * */
    public void setDieValue(int dieValue) {
        currentDieValue = dieValue;
    }

    /** @return The board's current die value. */
    public int getCurrentDieValue() {
        return currentDieValue;
    }

    /** @return the size of the board. */
    public Int2 getBoardSize() {
        return boardSize;
    }

    /** @return The 2D array of tiles. */
    public Tile[][] getTiles() {
        return tiles;
    }

    /** @return The settings for the board. */
    public BoardSettings getBoardSettings() {
        return boardSettings;
    }

    /** Set the end tile's id that will be used for the board showing animation.
     * @param endTileIdForAnim The id of the end tile that will be used for the board showing animation.
     * */
    public void setEndTileIdForAnim(int endTileIdForAnim) {
        this.endTileIdForAnim = endTileIdForAnim;
    }

    /** Get the id of the end tile that will be used for the board showing animation.
     * @return The id of the end tile used for the show board animation. */
    public int getEndTileIdForAnim() {
        return endTileIdForAnim;
    }

    /** Notify that board animation has completed. */
    public void setBoardAnimComplete() {
        boardShowAnimComplete = true;
    }

    /** @return Whether the board intro animation is showing. */
    public boolean isBoardShowAnimComplete() {
        return boardShowAnimComplete;
    }
}
