
import java.awt.*;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Board extends JPanel {
    public static int TILE_SIZE = 30;
    private Int2 size = new Int2(10, 10);

    private Tile[][] tiles;
    private Player[] players;

    private Color oddTileColor = new Color(50, 90, 200);
    private Color evenTileColor = new Color(200, 200, 50);

    public Tile getTile(int tileId) {
        // TODO: Use foreach maybe?
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                Tile tile = tiles[x][y];

                if (tile.getTileId() == tileId) {
                    return tile;
                }
            }
        }

        System.out.println("Error: Could not find tile at id " + tileId);
        return null;
    }

    public Int2 getBoardSize() {
        return size;
    }

    private int getTotalTiles() {
        return size.x * size.y;
    }

    public Board(Int2 size, Hashtable<Integer, Integer> moveToConfig) {
        this.size = size;

        // TODO: Move to drawBoard function
        this.tiles = new Tile[size.x][size.y];
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                int tileIndex = (y * size.x);
                // Account for odd rows going right to left
                if (y % 2 == 1) {
                    tileIndex += size.x - 1 - x;
                } else {
                    tileIndex += x;
                }

                int tileId = getTotalTiles() - tileIndex;

                Tile tile = new Tile(tileId, new Int2(x, y));
                tiles[x][y] = tile;

                if (moveToConfig.containsKey(tileId)) {
                    tile.setMoveTo(moveToConfig.get(tileId));
                }
            }
        }
    }

    public void setPlayers(Player[] players) {
        this.players = players;

        // Players should start off of the board and not on the first tile
        for (int i = 0; i < players.length; i++) {
            players[i].setCurrentTile(this.getTile(1));
        }
    }

    // TODO: Change name from doDrawing to something else. Also change the g2d variable name
    private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawString("Snakes and ladders", 20, 20);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, size.x*TILE_SIZE, size.y*TILE_SIZE);

        drawBoard(g2d);

        drawMoveToElements(g2d);

        if (players != null && players.length > 0) {
            for (int i = 0; i < players.length; i++) {
                Player player = players[i];

                setFontSize(g2d, 30f);
                Tile playerTile = player.getCurrentTile();

                Int2 playerCoordinates = playerTile.getCoordinates();

                g2d.setColor(Color.white);
                g2d.drawString(player.getIcon(), playerCoordinates.x * TILE_SIZE, playerCoordinates.y * TILE_SIZE + TILE_SIZE);
            }
        }

        repaint();
    }

    private void drawBoard(Graphics2D g2d) {
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                Tile tile = tiles[x][y];

                int xPos = TILE_SIZE * x;
                int yPos = TILE_SIZE * y;

                setFontSize(g2d, 10f);

                // TODO: draw rounded rectangles where the board turns
                Color tileColor = tile.getTileId() % 2 == 1 ? oddTileColor : evenTileColor;

                g2d.setColor(tileColor);

                // TODO: Try to refactor the turning tiles a little
                // Draw turning tiles
                boolean isEndPoint = tile.getTileId() == 1 || tile.getTileId() == getTotalTiles();
                if (!isEndPoint && (x % size.x == 0 || x % size.x == size.x - 1)) {
                    g2d.fillRoundRect(xPos, yPos, TILE_SIZE, TILE_SIZE, 20, 20);

                    if (x % size.x == 0) {
                        if (tile.getTileId() % 2 == 0) {
                            // Left side - Even tile
                            g2d.fillRect(xPos, yPos, TILE_SIZE, TILE_SIZE/2);
                            g2d.fillRect(xPos + TILE_SIZE/2, yPos, TILE_SIZE/2, TILE_SIZE);
                        } else {
                            // Left side - Odd tile
                            g2d.fillRect(xPos, yPos + TILE_SIZE/2, TILE_SIZE, TILE_SIZE/2);
                            g2d.fillRect(xPos + TILE_SIZE/2, yPos, TILE_SIZE/2, TILE_SIZE);
                        }
                    } else if (x % size.x == size.x - 1) {
                        if (tile.getTileId() % 2 == 0) {
                            // Right side - Even tile
                            g2d.fillRect(xPos, yPos, TILE_SIZE, TILE_SIZE/2);
                            g2d.fillRect(xPos, yPos, TILE_SIZE/2, TILE_SIZE);
                        } else {
                            // Right side - Odd tile
                            g2d.fillRect(xPos, yPos, TILE_SIZE/2, TILE_SIZE);
                            g2d.fillRect(xPos, yPos + TILE_SIZE/2, TILE_SIZE, TILE_SIZE/2);
                        }
                    }
                } else {
                    g2d.fillRect(xPos, yPos, TILE_SIZE, TILE_SIZE);
                }

                // TODO: Try to figure this out programmatically
                int boardNumberYOffset = 20;
                g2d.setColor(Color.white);
                g2d.drawString(""+tile.getTileId(), xPos, yPos + TILE_SIZE - boardNumberYOffset);
            }
        }
    }

    private void drawMoveToElements(Graphics2D g2d) {
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                Tile tile = tiles[x][y];

                if (tile.hasMoveTo()) {
                    if (tile.getMoveToTileId() > tile.getTileId()) {
                        drawLadder(g2d, tile.getTileId(), tile.getMoveToTileId());
                    } else {
                        drawSnake(g2d, tile.getTileId(), tile.getMoveToTileId());
                    }
                }
            }
        }
    }

    private void drawLadder(Graphics2D g2d, int startTileId, int endTileId) {
        // TODO: Put these vars somewhere better. Also make ends stick out a bit (maybe use two lines instead of a rectangle). Also give the ladders some weight and color
        float ladderThickness = 3f;
        int ladderWidth = 20;
        int ladderPosYOffset = TILE_SIZE/3;
        int rungSpacing = 8;

        Tile startTile = getTile(startTileId);
        Tile endTile = getTile(endTileId);

        Int2 startPos = startTile.getPosition();
        Int2 endPos = endTile.getPosition();

        int startPosX = startPos.x + TILE_SIZE/2;
        int startPosY = startPos.y + ladderPosYOffset;

        int endPosX = endPos.x + TILE_SIZE/2;
        int endPosY = endPos.y + TILE_SIZE - 2*ladderPosYOffset;

        double x = startPosX - endPosX;
        double y = startPos.y - endPosY;

        int distance = (int)Math.sqrt(x*x + y*y);
        double angle = Math.atan2(y, x) + Math.PI;

        // Ladder is drawn on its side facing the right
        // Ladder is then rotated towards the end tile. Rotation is then reset as not to affect anything else

        g2d.setStroke(new BasicStroke(ladderThickness));
        g2d.setColor(Color.green);

        g2d.rotate(angle, startPosX, startPosY);
        g2d.drawLine(startPosX, startPosY + ladderWidth/2, startPosX+distance, startPosY + ladderWidth/2);
        g2d.drawLine(startPosX, startPosY - ladderWidth/2, startPosX+distance, startPosY - ladderWidth/2);

        // Draw ladder rungs
        int rungPosX = startPosX + rungSpacing/2;
        int rungPosY = startPosY - ladderWidth/2;
        while (rungPosX < startPosX + distance - rungSpacing/2) {
            g2d.drawLine(rungPosX, rungPosY, rungPosX, rungPosY+ladderWidth);
            rungPosX += rungSpacing;
        }
        g2d.rotate(-angle, startPosX, startPosY);
    }

    private void drawSnake(Graphics2D g2d, int startTileId, int endTileId) {
        float snakeThickness = 5f;

        int snakePosYOffset = 2*TILE_SIZE/3;

        Tile startTile = getTile(startTileId);
        Tile endTile = getTile(endTileId);

        Int2 startPos = startTile.getPosition();
        Int2 endPos = endTile.getPosition();

        int startPosX = startPos.x + TILE_SIZE/2;
        int startPosY = startPos.y + snakePosYOffset;

        int endPosX = endPos.x + TILE_SIZE/2;
        int endPosY = endPos.y + TILE_SIZE - 2*snakePosYOffset;

        double x = startPosX - endPosX;
        double y = startPos.y - endPosY;

        double distance = (int)Math.sqrt(x*x + y*y);
        double angle = Math.atan2(y, x) + Math.PI;

        g2d.setColor(Color.red);
        g2d.rotate(angle, startPosX, startPosY);

        double time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int parts = 10;
        double waveAmplitude = 4;
        double partLengthX = distance / parts;
        for (int i = 0; i < parts-1; i++) {
            double partStartX = startPosX + (i * partLengthX);
            double partStartY = startPosY + waveAmplitude*Math.sin(partStartX + time);
            double partEndX = startPosX + ((i+1) * partLengthX);
            double partEndY = startPosY + waveAmplitude*Math.sin(partEndX + time);

            // TODO: The tail of the snake doesn't point exactly at the target square (95 should go to 24 but looks like it leads to the above square, 37)

            g2d.setStroke(new BasicStroke(snakeThickness));
            g2d.drawLine((int)partStartX, (int)partStartY, (int)partEndX, (int)partEndY);
        }
        g2d.rotate(-angle, startPosX, startPosY);


    }

    private void setFontSize(Graphics2D g2d, float fontSize) {
        Font currentFont = g2d.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getStyle(), fontSize);
        g2d.setFont(newFont);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
}
