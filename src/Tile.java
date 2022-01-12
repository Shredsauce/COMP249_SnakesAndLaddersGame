public class Tile {
    private int tileId;

    private Int2 coordinates;

    private int moveTo;

    // TODO: Create Int2 class

    public Tile (int tileId, Int2 coordinates) {
        this.tileId = tileId;
        this.coordinates = coordinates;
        // Set moveTo to itself just to be safe TODO: Clean this a little. Maybe put it as part of the constructor?
        this.moveTo = tileId;
    }

    // TODO: Validate this is a good tile (can't find any 0 or past the max tile amount)
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
        return new Int2(coordinates.x * Board.TILE_SIZE, coordinates.y * Board.TILE_SIZE);
    }
}
