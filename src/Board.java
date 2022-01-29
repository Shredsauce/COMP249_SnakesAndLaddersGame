// -----------------------------------------------------
// Assignment 1
// Inspiration for using Graphics2D comes from https://zetcode.com/gfx/java2d/introduction/
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.util.*;

/** Representation of the snakes and ladders board */
public class Board {
    private BoardSettings boardSettings;

    private LadderAndSnake game;
    private Player[] players;

    // Tiles
    private Tile[][] tiles;
    private Tile startTile;
    private Tile lastTile;
    private int endTileIdForAnim;

    private boolean boardShowAnimComplete;

    // Board
    private Int2 boardSize = new Int2(10, 10);


    // Dice
    private int currentDieValue = 6;

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

    public int getTotalTiles() {
        return boardSize.x * boardSize.y;
    }

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

            ArrayList<Int2> validNeighborCoords = getValidNeighborCoords(tile);
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

    private Tile createStartTile() {
        if (startTile != null) {
            System.out.println("Error: Start tile already exists");
            return startTile;
        }
        Tile firstTile = getTile(1);
        Int2 startTilePos = new Int2(firstTile.getPosition().x - DrawingManager.TILE_SIZE, firstTile.getPosition().y);
        return new Tile(0, startTilePos);
    }

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

    private void applyMoveToConfig(Hashtable<Integer, Integer> moveToConfig) {
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Tile tile = tiles[x][y];

                if (tile != null && moveToConfig.containsKey(tile.getTileId())) {
                    tile.setMoveTo(moveToConfig.get(tile.getTileId()));
                }
            }
        }
    }

    private ArrayList<Int2> getValidNeighborCoords(Tile tile) {
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

        if (rightNeighbor == null) {
            validCoords.add(rightNeighborCoord);
        }

        if (leftNeighbor == null) {
            validCoords.add(leftNeighborCoord);
        }

        if (upNeighbor == null) {
            validCoords.add(upNeighborCoord);
        }

        if (downNeighbor == null) {
            validCoords.add(downNeighborCoord);
        }

        return validCoords;
    }

    private ArrayList<Int2> sortValidCoordsByPreference(Tile tile, ArrayList<Int2> validCoords, boolean preferHorizontal, boolean preferForward) {
        ArrayList<Int2> sortedCoords = new ArrayList<Int2>();

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
            }
        }

        return sortedCoords;
    }

    public void setPlayers(Player[] players) {
        this.players = players;

        for (int i = 0; i < players.length; i++) {
            players[i].setCurrentTile(this.getTile(0));
        }
    }


    public Tile getLastTile() {
        return lastTile;
    }

    public Player[] getPlayers() {
        return players;
    }


    public void setDieValue(int dieValue) {
        currentDieValue = dieValue;
    }


    public int getCurrentDieValue() {
        return currentDieValue;
    }

    public Int2 getBoardSize() {
        return boardSize;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public BoardSettings getBoardSettings() {
        return boardSettings;
    }

    public void setEndTileIdForAnim(int endTileIdForAnim) {
        this.endTileIdForAnim = endTileIdForAnim;
    }

    public int getEndTileIdForAnim() {
        return endTileIdForAnim;
    }


    public void setBoardAnimComplete() {
        boardShowAnimComplete = true;
    }

    public boolean isBoardShowAnimComplete() {
        return boardShowAnimComplete;
    }
}
