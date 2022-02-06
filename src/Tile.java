// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** Represents a tile on the snakes and ladders board. */
public class Tile {
    /** The id of the tile. */
    private int tileId;
    /** The board coordinates of this tile. */
    private Int2 coordinates;
    /** The move-to (snakes and ladder) element of this tile (its own id if it does not have any) */
    private int moveToTileId;

    /** Constructor
     * @param tileId The id of the tile.
     * @param coordinates The coordinates of the tile.
     * */
    public Tile (int tileId, Int2 coordinates) {
        this.tileId = tileId;
        this.coordinates = coordinates;
        this.moveToTileId = tileId;
    }

    /** @return The tile id. */
    public int getTileId() {
        return this.tileId;
    }

    /** @return Whether the tile has a move-to element (To be used with the snake/ladder functionality). */
    public boolean hasMoveTo() {
        return moveToTileId != tileId;
    }

    /** Set the tile's move-to tile id.
     * @param moveToTileId The id of the tile to move to. */
    public void setMoveToTileId(int moveToTileId) {
        this.moveToTileId = moveToTileId;
    }

    /** @return The tile's move-to tile id. */
    public int getMoveToTileId() {
        return moveToTileId;
    }

    /** @return The tile's coordinates. */
    public Int2 getCoordinates() {
        return this.coordinates;
    }

    /** @return the tile's board position. */
    public Int2 getBoardPosition() {
        return new Int2(coordinates.x * DrawingManager.TILE_SIZE + DrawingManager.OFFSET.x, coordinates.y * DrawingManager.TILE_SIZE + DrawingManager.OFFSET.y);
    }

    /** Display some useful information about the tile. */
    @Override
    public String toString() {
        return "Tile " + tileId + " " + getBoardPosition().toString();
    }
}
