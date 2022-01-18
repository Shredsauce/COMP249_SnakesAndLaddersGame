// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

public class Tile {
    private int tileId;
    private Int2 coordinates;
    private int moveTo;

    public Tile (int tileId, Int2 coordinates) {
        this.tileId = tileId;
        this.coordinates = coordinates;
        this.moveTo = tileId;
    }

    public int getTileId() {
        return this.tileId;
    }

    public boolean hasMoveTo() {
        return moveTo != tileId;
    }

    public void setMoveTo(int moveTo) {
        this.moveTo = moveTo;
    }

    public int getMoveToTileId() {
        return moveTo;
    }

    public Int2 getCoordinates() {
        return this.coordinates;
    }

    public Int2 getPosition() {
        // TODO: Better way to do this without accessing TILE_SIZE this way?
        return new Int2(coordinates.x * Board.TILE_SIZE + Board.OFFSET.x, coordinates.y * Board.TILE_SIZE + Board.OFFSET.y);
    }
}
