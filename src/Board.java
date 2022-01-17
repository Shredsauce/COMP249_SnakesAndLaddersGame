
import java.awt.*;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Board extends JPanel {
    public static int TILE_SIZE = 30;
    public static int TILE_SPACING = 2;
    public static Int2 OFFSET = new Int2(100, 100);
    public static Int2 BOARD_SIZE = new Int2(10, 10);

    private Player[] players;

    // Tiles
    private Tile[][] tiles;
    private Color oddTileColor = new Color(50, 90, 200);
    private Color evenTileColor = new Color(200, 200, 50);

    // Dice
    private int currentDieValue = 6;
    private int previousDieValue = currentDieValue;
    private int maxDiePositionOffset = 8;
    private double dieAngle;
    private Int2 diePositionOffset = new Int2();


    public Tile getTile(int tileId) {
        // TODO: Use foreach maybe?
        for (int y = 0; y < BOARD_SIZE.y; y++) {
            for (int x = 0; x < BOARD_SIZE.x; x++) {
                Tile tile = tiles[x][y];

                if (tile.getTileId() == tileId) {
                    return tile;
                }
            }
        }

        return null;
    }

    private int getTotalTiles() {
        return BOARD_SIZE.x * BOARD_SIZE.y;
    }

    public Board(Int2 size, Hashtable<Integer, Integer> moveToConfig) {
        this.BOARD_SIZE = size;

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

        drawBoard(g2d);

        drawMoveToElements(g2d);

        if (players != null && players.length > 0) {
            for (int i = 0; i < players.length; i++) {
                Player player = players[i];

                int shadowOffset = 1;

                setFontSize(g2d, 30f);
                Tile playerTile = player.getCurrentTile();
                g2d.setColor(Color.black);
                g2d.drawString(player.getIcon(), playerTile.getPosition().x+shadowOffset,  playerTile.getPosition().y+shadowOffset+TILE_SIZE);

                g2d.setColor(Color.white);
                g2d.drawString(player.getIcon(), playerTile.getPosition().x,  playerTile.getPosition().y+TILE_SIZE);
            }
        }

        drawDie(g2d);
        repaint();
    }

    private void drawDie(Graphics2D g2d) {
        Int2 pos = new Int2(400, 20);
        int size = 50;
        int shadowOffset = 3;
        int edgeOffset = 10;
        int arcValue = 7;

        int dotRadius = 6;

        if (previousDieValue != currentDieValue) {
            Random random = new Random();
            dieAngle = random.nextDouble(0, 2*Math.PI);
            diePositionOffset = new Int2(random.nextInt(-maxDiePositionOffset, maxDiePositionOffset), random.nextInt(-maxDiePositionOffset, maxDiePositionOffset));

            previousDieValue = currentDieValue;
        }
        g2d.rotate(dieAngle, pos.x + size / 2, pos.y + size / 2);
        g2d.translate(diePositionOffset.x, diePositionOffset.y);

        g2d.setColor(Color.black);
        g2d.fillRoundRect(pos.x, pos.y, size+shadowOffset, size+shadowOffset, arcValue, arcValue);
        g2d.setColor(Color.white);
        g2d.fillRoundRect(pos.x, pos.y, size, size, arcValue, arcValue);

        int die = currentDieValue;
        g2d.setColor(Color.black);

        if (die == 1 || die == 3 || die == 5) {
            drawCircle(g2d, new Int2(pos.x + size/2, pos.y + size/2), dotRadius); // Middle
        }

        if (die != 1){
            drawCircle(g2d, new Int2(pos.x + edgeOffset, pos.y + edgeOffset), dotRadius); // Top left
            drawCircle(g2d, new Int2(size + pos.x - edgeOffset, size + pos.y - edgeOffset), dotRadius); // Bottom right
        }

        if (die == 4 || die == 5 || die == 6) {
            drawCircle(g2d, new Int2(pos.x + edgeOffset, size + pos.y - edgeOffset), dotRadius); // Bottom left
            drawCircle(g2d, new Int2(size + pos.x - edgeOffset, pos.y + edgeOffset), dotRadius); // Top right
        }

        if (die == 6) {
            drawCircle(g2d, new Int2(pos.x + edgeOffset, pos.y + size/2), dotRadius); // Middle left
            drawCircle(g2d, new Int2(size + pos.x - edgeOffset, pos.y + size/2), dotRadius); // Middle right
        }

        g2d.rotate(-dieAngle, pos.x + size / 2, pos.y + size / 2);
        g2d.translate(-diePositionOffset.x, -diePositionOffset.y);
    }

    private void drawCircle (Graphics2D g2d, Int2 center, int radius) {
        g2d.fillOval(center.x-radius, center.y-radius, 2*radius, 2*radius);
    }

    private void drawBoard(Graphics2D g2d) {
        for (int y = 0; y < BOARD_SIZE.y; y++) {
            for (int x = 0; x < BOARD_SIZE.x; x++) {
                Tile tile = tiles[x][y];

                drawTile(g2d, tile);
                setFontSize(g2d, 10f);

                int xPos = TILE_SIZE * x + OFFSET.x;
                int yPos = TILE_SIZE * y + OFFSET.y;

                // TODO: Try to figure this out programmatically
                int boardNumberYOffset = 20;
                g2d.setColor(Color.white);
                g2d.drawString(""+tile.getTileId(), xPos, yPos + TILE_SIZE - boardNumberYOffset);

                // Only for testing
//                g2d.drawString(tile.getCoordinates().toString(), xPos, yPos + TILE_SIZE - boardNumberYOffset);
            }
        }
    }

    private void drawTile(Graphics2D g2d, Tile tile) {
        Color tileColor = tile.getTileId() % 2 == 1 ? oddTileColor : evenTileColor;
        g2d.setColor(tileColor);

        Int2 pos = tile.getPosition();
        int tileId = tile.getTileId();

        Tile previousTile = getTile(tileId - 1);
        Tile nextTile = getTile(tileId + 1);

        if (previousTile != null && nextTile != null) {
            Int2 tileCoord = tile.getCoordinates();
            Int2 prevCoord = previousTile.getCoordinates();
            Int2 nextCoord = nextTile.getCoordinates();

            Int2 prevDir = new Int2(prevCoord.x - tileCoord.x, prevCoord.y - tileCoord.y);
            Int2 nextDir = new Int2(nextCoord.x - tileCoord.x, nextCoord.y - tileCoord.y);

            if (prevCoord.y == tileCoord.y && nextCoord.y == tileCoord.y) {
                drawHorizontalTile(g2d, pos);
            } else if (prevCoord.x == tileCoord.x && nextCoord.x == tileCoord.x) {
                drawVerticalTile(g2d, pos);
            } else {
                double angle = getTurningTileAngle(prevDir, nextDir);
                drawTurningTile(g2d, pos, angle);
            }
        }
    }

    private void drawHorizontalTile(Graphics2D g2d, Int2 pos) {
        drawStraightTile(g2d, pos, false);
    }

    private void drawVerticalTile(Graphics2D g2d, Int2 pos) {
        drawStraightTile(g2d, pos, true);
    }

    private void drawStraightTile(Graphics2D g2d, Int2 pos, boolean isVertical) {
        Int2 cellCenter = new Int2(pos.x + TILE_SIZE/2, pos.y + TILE_SIZE/2);
        double cellRotation = isVertical ? Math.PI/2 : 0;

        g2d.rotate(cellRotation, cellCenter.x, cellCenter.y);

        g2d.rotate(cellRotation, cellCenter.x, cellCenter.y);
        g2d.fillRect(pos.x, pos.y + TILE_SPACING, TILE_SIZE, TILE_SIZE - 2*TILE_SPACING);

        g2d.rotate(-cellRotation, cellCenter.x, cellCenter.y);
    }

    private double getTurningTileAngle(Int2 prevDir, Int2 nextDir) {
        if ((prevDir.x < 0 && nextDir.y < 0) || (nextDir.x < 0 && prevDir.y < 0)) return 0;
        if ((prevDir.x < 0 && nextDir.y > 0) || (nextDir.x < 0 && prevDir.y > 0)) return (3*Math.PI)/2;
        if ((prevDir.x > 0 && nextDir.y < 0) || (nextDir.x > 0 && prevDir.y < 0)) return Math.PI/2;
        if ((prevDir.x > 0 && nextDir.y > 0) || (nextDir.x > 0 && prevDir.y > 0)) return Math.PI;

        return 0;
    }

    private void drawTurningTile(Graphics2D g2d, Int2 pos, double angle) {
        Int2 cellCenter = new Int2(pos.x + TILE_SIZE/2, pos.y + TILE_SIZE/2);

        g2d.rotate(angle, cellCenter.x, cellCenter.y);
        g2d.fillRoundRect(pos.x + TILE_SPACING, pos.y + TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, 20, 20);
        g2d.fillRect(pos.x, pos.y + TILE_SPACING, TILE_SIZE/2, TILE_SIZE - 2*TILE_SPACING);
        g2d.fillRect(pos.x + TILE_SPACING, pos.y, TILE_SIZE - 2*TILE_SPACING, TILE_SIZE/2);
        g2d.rotate(-angle, cellCenter.x, cellCenter.y);
    }

    private void drawMoveToElements(Graphics2D g2d) {
        for (int y = 0; y < BOARD_SIZE.y; y++) {
            for (int x = 0; x < BOARD_SIZE.x; x++) {
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
        g2d.setColor(Color.gray);

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

    public void setDieValue(int dieValue) {
        currentDieValue = dieValue;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
}
