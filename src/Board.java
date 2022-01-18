// -----------------------------------------------------
// Assignment 1
// Inspiration for using Graphics2D comes from https://zetcode.com/gfx/java2d/introduction/
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Board extends JPanel {
    public static Int2 OFFSCREEN_DIE_POS = new Int2(-100, -100);
    public static int TILE_SIZE = 30;
    public static int TILE_HALF_SIZE = TILE_SIZE/2;
    public static int TILE_SPACING = 5;
    public static int NUM_TAIL_TILES = 1;
    public static Int2 OFFSET = new Int2(100, 100);

    // TODO: Make sure the value of an entry is not the key of another entry
    // TODO: Option to randomly generate this. make sure elements are never on the same row
    private Hashtable<Integer, Integer> defaultMoveToConfig = new Hashtable<Integer, Integer>() {
        {put(1, 38);}
        {put(4, 14);}
        {put(9, 31);}
        {put(16, 6);}
        {put(21, 42);}
        {put(28, 84);}
        {put(36, 44);}
        {put(48, 30);}
        {put(51, 67);}
        {put(62, 19);} // Most of the snake's head is on the 62
        {put(64, 60);}
        {put(64, 60);}
        {put(71, 91);}
        {put(80, 100);}
        {put(93, 68);}
        {put(95, 24);}
        {put(97, 76);}
        {put(98, 78);}
    };

    private LadderAndSnake game;
    private Player[] players;
    private boolean isWinState;

    // Board
    public Int2 boardSize = new Int2(10, 10);
    private boolean boardShowAnimComplete;
    private Color boardColor = new Color(145, 92, 48);

    // Tiles
    private Tile[][] tiles;
    private Color oddTileColor = new Color(50, 90, 200);
    private Color evenTileColor = new Color(200, 200, 50);
    private Tile startTile;
    private Tile lastTile;

    // Dice
    private int currentDieValue = 6;
    private int previousDieValue = currentDieValue;
    private int maxDiePositionOffset = 8;
    private double dieAngle;
    private Int2 diePositionOffset = new Int2();
    private Int2 previousMousePos = new Int2();
    private int endTileIdForAnim;
    private Int2 nextDieMouseRollPos = OFFSCREEN_DIE_POS;
    private double dieRollMagnitude;
    private double dieRollAngle;

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

    public Board(LadderAndSnake game, BoardSettings boardSettings) {
        this.boardSize = boardSettings.boardSize;
        this.game = game;

        Hashtable<Integer, Integer> moveToConfig = new Hashtable<Integer, Integer>();

        if (boardSettings.useDefault) {
            moveToConfig = defaultMoveToConfig;
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

        moveToConfig = generateMoveToConfig(boardSettings);
        applyMoveToConfig(moveToConfig);

        // Mouse related stuff inspired by this: http://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
        // TODO: De-spaghettify this a bit.
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                nextDieMouseRollPos = new Int2(e.getX(), e.getY());
                GUIManager.getInstance().rollDie(game.getDiceRollMode());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                nextDieMouseRollPos = new Int2(e.getX(), e.getY());

                double x = previousMousePos.x - nextDieMouseRollPos.x;
                double y = previousMousePos.y - nextDieMouseRollPos.y;

                // TODO: Do something with this or remove it
                dieRollMagnitude = Math.sqrt(x*x + y*y);
                dieRollAngle = Math.atan2(y, x);

                previousMousePos = nextDieMouseRollPos;
            }
        });
    }

    private Tile createStartTile() {
        if (startTile != null) {
            System.out.println("Error: Start tile already exists");
            return startTile;
        }
        Tile firstTile = getTile(1);
        Int2 startTilePos = new Int2(firstTile.getPosition().x - TILE_SIZE, firstTile.getPosition().y);
        return new Tile(0, startTilePos);
    }

    private Hashtable<Integer, Integer> generateMoveToConfig(BoardSettings boardSettings) {
        if (boardSettings.useDefault) {
            return defaultMoveToConfig;
        }

        // TODO: Randomly generate move to config
        return new Hashtable<Integer, Integer>();
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

    // TODO: No longer used
    public void loadDefaultBoard(Int2 boardSize) {
        Hashtable<Integer, Integer> moveToConfig = defaultMoveToConfig;

        this.tiles = new Tile[boardSize.x][boardSize.y];
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {

                int tileIndex = (y * boardSize.x);
                // Account for odd rows going right to left
                if (y % 2 == 1) {
                    tileIndex += boardSize.x - 1 - x;
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

        for (int i = 0; i < players.length; i++) {
            players[i].setCurrentTile(this.getTile(0));
        }
    }

    public void setBoardAnimComplete() {
        boardShowAnimComplete = true;
    }

    public void setEndTileIdForAnim(int endTileIdForAnim) {
        this.endTileIdForAnim = endTileIdForAnim;
    }

    public Tile getLastTile() {
        return lastTile;
    }

    private void graphicsLoop(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawString("Snakes and ladders", 20, 20);

        if (!isWinState){
            drawBoard(g2d, endTileIdForAnim);
        }

        if (boardShowAnimComplete && !isWinState){
            drawMoveToElements(g2d);
        }

        if (players != null && players.length > 0 && !isWinState) {
            drawPlayers(g2d);
        }

        drawDie(g2d);
        repaint();
    }

    private void drawPlayers(Graphics2D g2d) {
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

    private void drawDie(Graphics2D g2d) {
        int size = 50;
        int shadowOffset = 3;
        int edgeOffset = 10;
        int arcValue = 7;

        Int2 pos = new Int2(nextDieMouseRollPos.x - size/2, nextDieMouseRollPos.y - size/2);

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

    private void drawBoard(Graphics2D g2d, int tileEndId) {
        drawBoardBackground(g2d);

        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Tile tile = tiles[x][y];

                if (tile == null) continue;
                if (tile.getTileId() > tileEndId) continue;

                drawTile(g2d, tile);
                setFontSize(g2d, 10f);

                int xPos = TILE_SIZE * x + OFFSET.x;
                int yPos = TILE_SIZE * y + OFFSET.y;

                if (boardShowAnimComplete) {
                    drawTileNumber(g2d, tile.getTileId(), xPos, yPos);
                }
            }
        }
    }

    private void drawBoardBackground(Graphics2D g2d) {
        g2d.setColor(boardColor);
        g2d.fill3DRect(OFFSET.x, OFFSET.y, TILE_SIZE*boardSize.x, TILE_SIZE*boardSize.y, true);
    }

    private void drawTileNumber(Graphics2D g2d, int tileId, int xPos, int yPos) {
        // TODO: Try to figure this out programmatically
        int boardNumberYOffset = 20;
        g2d.setColor(Color.white);
        g2d.drawString(""+tileId, xPos, yPos + TILE_SIZE - boardNumberYOffset);
    }

    private void drawTile(Graphics2D g2d, Tile tile) {
        int endTileAnimSwitch = endTileIdForAnim % 2;
        Color tileColor = (tile.getTileId() % 2) + endTileAnimSwitch == 1 ? oddTileColor : evenTileColor;
        g2d.setColor(tileColor);

        Int2 pos = tile.getPosition();
        int tileId = tile.getTileId();

        Tile previousTile = getTile(tileId - 1);
        Tile nextTile = getTile(tileId + 1);

        if (shouldDrawSnakeHead(tileId, nextTile)) {
            drawBoardHead(g2d, tile, previousTile);
        } else if (shouldDrawSnakeTail(tileId)) {
            drawBoardTail(g2d, tile, previousTile);
        } else {
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

    private boolean shouldDrawSnakeHead(int tileId, Tile nextTile) {
        return nextTile == null || tileId == endTileIdForAnim;
    }

    private boolean shouldDrawSnakeTail(int tileId) {
        return tileId <= NUM_TAIL_TILES;
    }

    private void drawHorizontalTile(Graphics2D g2d, Int2 pos) {
        drawStraightTile(g2d, pos, false);
    }

    private void drawVerticalTile(Graphics2D g2d, Int2 pos) {
        drawStraightTile(g2d, pos, true);
    }

    private void drawStraightTile(Graphics2D g2d, Int2 pos, boolean isVertical) {
        Int2 cellCenter = new Int2(pos.x + TILE_HALF_SIZE, pos.y + TILE_HALF_SIZE);
        double cellRotation = isVertical ? Math.PI/2 : 0;

        g2d.rotate(cellRotation, cellCenter.x, cellCenter.y);
        g2d.fillRect(pos.x, pos.y + TILE_SPACING, TILE_SIZE, TILE_SIZE - 2*TILE_SPACING);

        g2d.rotate(-cellRotation, cellCenter.x, cellCenter.y);
    }

    private void drawBoardHead(Graphics2D g2d, Tile tile, Tile previousTile) {
        Int2 pos = tile.getPosition();
        Int2 previousPos = previousTile != null ? previousTile.getPosition() : pos;

        double headAngle = getSnakeHeadAngle(pos, previousPos);

        Int2 cellCenter = new Int2(pos.x + TILE_HALF_SIZE, pos.y + TILE_HALF_SIZE);

        int headLength = (int)(1.5*TILE_HALF_SIZE);
        int headWidth = TILE_HALF_SIZE;

        // TODO: Put this as a global variable somewhere
        double time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        Color headColor = g2d.getColor();
        g2d.setColor(Color.red);

        int tongueOutPosX = 10;
        int tongueInPosX = -5;

        g2d.rotate(headAngle, cellCenter.x, cellCenter.y);

        // Tongue animation
        boolean showTongue = time % 3 < 0.05;
        int tonguePosX = showTongue ? tongueOutPosX : tongueInPosX;
        double tongueSplitAngle = 0.3;

        // Tongue
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(pos.x - tonguePosX, pos.y + TILE_HALF_SIZE, pos.x, pos.y + TILE_HALF_SIZE);
        g2d.setStroke(new BasicStroke(3));

        g2d.rotate(tongueSplitAngle, pos.x - tonguePosX, pos.y + TILE_HALF_SIZE);
        g2d.drawLine(pos.x - tonguePosX - 7, pos.y + TILE_HALF_SIZE, pos.x - tonguePosX, pos.y + TILE_HALF_SIZE);
        g2d.rotate(-2*tongueSplitAngle, pos.x - tonguePosX, pos.y + TILE_HALF_SIZE);
        g2d.drawLine(pos.x - tonguePosX - 7, pos.y + TILE_HALF_SIZE, pos.x - tonguePosX, pos.y + TILE_HALF_SIZE);
        g2d.rotate(tongueSplitAngle, pos.x - tonguePosX, pos.y + TILE_HALF_SIZE);

        // Eyes
        g2d.setColor(headColor);
        g2d.fillOval(cellCenter.x-headLength, cellCenter.y-headWidth, 2*headLength, 2*headWidth);
        g2d.setColor(Color.red);
        drawCircle(g2d, new Int2(cellCenter.x+3, cellCenter.y-6), 3);
        drawCircle(g2d, new Int2(cellCenter.x+3, cellCenter.y+6), 3);

        g2d.rotate(-headAngle, cellCenter.x, cellCenter.y);
    }

    private void drawBoardTail(Graphics2D g2d, Tile tile, Tile previousTile) {
        Int2 pos = tile.getPosition();
//
//        int tileId = tile.getTileId();
//
//        int offset = NUM_TAIL_TILES - tileId;
//
        // TODO: Make tail longer (more than 1 cell)
        // TODO: Rotate tail correctly
        Polygon poly = new Polygon();
        poly.addPoint(pos.x, pos.y+TILE_HALF_SIZE);
        poly.addPoint(pos.x+TILE_SIZE, pos.y+TILE_SPACING);
        poly.addPoint(pos.x+TILE_SIZE, pos.y+TILE_SIZE-TILE_SPACING);
        g2d.fillPolygon(poly);
    }

    private double getSnakeHeadAngle(Int2 pos, Int2 previousPos) {
        Int2 previousTileDir = new Int2(previousPos.x - pos.x, previousPos.y - pos.y);
        if (previousTileDir.x > 0) return 0;
        if (previousTileDir.x < 0) return Math.PI;
        if (previousTileDir.y > 0) return Math.PI/2;
        if (previousTileDir.y < 0) return (3*Math.PI)/2;

        return 0;
    }

    private double getTurningTileAngle(Int2 prevDir, Int2 nextDir) {
        if ((prevDir.x < 0 && nextDir.y < 0) || (nextDir.x < 0 && prevDir.y < 0)) return 0;
        if ((prevDir.x < 0 && nextDir.y > 0) || (nextDir.x < 0 && prevDir.y > 0)) return (3*Math.PI)/2;
        if ((prevDir.x > 0 && nextDir.y < 0) || (nextDir.x > 0 && prevDir.y < 0)) return Math.PI/2;
        if ((prevDir.x > 0 && nextDir.y > 0) || (nextDir.x > 0 && prevDir.y > 0)) return Math.PI;

        return 0;
    }

    private void drawTurningTile(Graphics2D g2d, Int2 pos, double angle) {
        Int2 cellCenter = new Int2(pos.x +  TILE_HALF_SIZE, pos.y + TILE_HALF_SIZE);

        g2d.rotate(angle, cellCenter.x, cellCenter.y);
        g2d.fillRoundRect(pos.x + TILE_SPACING, pos.y + TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, 20, 20);
        g2d.fillRect(pos.x, pos.y + TILE_SPACING, TILE_HALF_SIZE, TILE_SIZE - 2*TILE_SPACING);
        g2d.fillRect(pos.x + TILE_SPACING, pos.y, TILE_SIZE - 2*TILE_SPACING, TILE_HALF_SIZE);
        g2d.rotate(-angle, cellCenter.x, cellCenter.y);
    }

    private void drawMoveToElements(Graphics2D g2d) {
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Tile tile = tiles[x][y];

                if (tile == null) continue;

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

        int startPosX = startPos.x + TILE_HALF_SIZE;
        int startPosY = startPos.y + ladderPosYOffset;

        int endPosX = endPos.x + TILE_HALF_SIZE;
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

        Int2 tileStartPos = startTile.getPosition();
        Int2 tileEndPos = endTile.getPosition();

        int startPosX = tileStartPos.x + TILE_HALF_SIZE;
        int startPosY = tileStartPos.y + snakePosYOffset;

        int endPosX = tileEndPos.x + TILE_HALF_SIZE;
        int endPosY = tileEndPos.y + TILE_SIZE - 2*snakePosYOffset;

        double x = startPosX - endPosX;
        double y = tileStartPos.y - endPosY;

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

    public void setWinState(boolean isWinState) {
        this.isWinState = isWinState;
    }

    public void setDieRollPos(Int2 pos) {
        this.nextDieMouseRollPos = pos;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!isWinState) {
            super.paintComponent(g);
        }
        graphicsLoop(g);
    }
}
