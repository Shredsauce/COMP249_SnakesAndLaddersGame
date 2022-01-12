public class Player {

    private Tile currentTile;
    private char icon;

    public Player(char icon) {
        this.icon = icon;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    public String getIcon() {
        return icon+"";
    }
}
