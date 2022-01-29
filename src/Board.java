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

/*** * Representation of the snakes and ladders board */
public class Board extends JPanel {
    public static Int2 OFFSCREEN_DIE_POS = new Int2(-100, -100);
    public static int TILE_SIZE = 30;
    public static int TILE_HALF_SIZE = TILE_SIZE/2;
    public static int TILE_SPACING = 5;
    public static int NUM_TAIL_TILES = 1;
    public static Int2 OFFSET = new Int2(100, 100);
    private BoardSettings boardSettings;

    private LadderAndSnake game;
    private Player[] players;
    private boolean shouldRefreshBackground = true;

    // Board
    private Int2 boardSize = new Int2(10, 10);
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
    private Int2 diePositionOffset = new Int2(0, 0);
    private Int2 previousMousePos = new Int2(0, 0);
    private int endTileIdForAnim;
    private Int2 nextDieMouseRollPos = OFFSCREEN_DIE_POS;

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
        this.boardSettings = boardSettings;

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

        // Mouse related stuff inspired by this: http://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (!GUIManager.getInstance().isInAnimation()) {
                    nextDieMouseRollPos = new Int2(e.getX(), e.getY());
                    GUIManager.getInstance().rollDie(game.getDiceRollMode());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!GUIManager.getInstance().isInAnimation()) {
                    nextDieMouseRollPos = new Int2(e.getX(), e.getY());

                    double x = previousMousePos.x - nextDieMouseRollPos.x;
                    double y = previousMousePos.y - nextDieMouseRollPos.y;

                    previousMousePos = nextDieMouseRollPos;
                }
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

        if (shouldRefreshBackground){
            drawBoard(g2d, endTileIdForAnim);
        }

        if (boardShowAnimComplete && shouldRefreshBackground){
            drawMoveToElements(g2d);
        }

        if (players != null && players.length > 0 && shouldRefreshBackground) {
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
            g2d.drawString(""+player.getIcon(), playerTile.getPosition().x+shadowOffset,  playerTile.getPosition().y+shadowOffset+TILE_SIZE);

            g2d.setColor(Color.white);
            g2d.drawString(""+player.getIcon(), playerTile.getPosition().x,  playerTile.getPosition().y+TILE_SIZE);
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
            drawBoardTail(g2d, tile, nextTile);
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

    private void drawBoardTail(Graphics2D g2d, Tile tile, Tile nextTile) {
        Int2 pos = tile.getPosition();
        Int2 cellCenter = new Int2(pos.x +  TILE_HALF_SIZE, pos.y + TILE_HALF_SIZE);

        double tailRotationAngle = getSnakeTailAngle(tile.getCoordinates(), nextTile.getCoordinates());
        Polygon poly = new Polygon();
        g2d.rotate(tailRotationAngle, cellCenter.x, cellCenter.y);
        poly.addPoint(pos.x, pos.y+TILE_HALF_SIZE);
        poly.addPoint(pos.x+TILE_SIZE, pos.y+TILE_SPACING);
        poly.addPoint(pos.x+TILE_SIZE, pos.y+TILE_SIZE-TILE_SPACING);
        g2d.fillPolygon(poly);
        g2d.rotate(-tailRotationAngle, cellCenter.x, cellCenter.y);
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

    private double getSnakeTailAngle(Int2 tailTileCoord, Int2 nextTileCoord) {
        if (nextTileCoord.x > tailTileCoord.x) return 0;
        if (nextTileCoord.x < tailTileCoord.x) return Math.PI;
        if (nextTileCoord.y > tailTileCoord.y)  return Math.PI/2;
        if (nextTileCoord.y < tailTileCoord.y) return (3*Math.PI)/2;

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
        float ladderThickness = boardSettings.ladderThickness;
        int ladderWidth = boardSettings.ladderWidth;
        int rungSpacing = boardSettings.rungSpacing;

        int ladderPosYOffset = TILE_SIZE/3;

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
        
        Tile startTile = getTile(startTileId);
        Tile endTile = getTile(endTileId);

        Int2 tileStartPos = startTile.getPosition();
        Int2 tileEndPos = endTile.getPosition();

        int startPosX = tileStartPos.x + TILE_HALF_SIZE;
        int startPosY = tileStartPos.y + TILE_HALF_SIZE;

        int endPosX = tileEndPos.x + TILE_HALF_SIZE;
        int endPosY = tileEndPos.y + TILE_HALF_SIZE;

        double x = startPosX - endPosX;
        double y = startPosY - endPosY;

        double distance = (int)Math.sqrt(x*x + y*y);
        double angle = Math.atan2(y, x) + Math.PI;

        g2d.setColor(Color.red);
        g2d.rotate(angle, startPosX, startPosY);

        double time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int partLength = 10;
        double waveAmplitude = 2;
        int numParts = (int)(distance / partLength);
        double partLengthX = distance / numParts;
        int headSize = 6;

        for (int i = 0; i < numParts-1; i++) {
            double partStartX = startPosX + (i * partLengthX);
            double partStartY = startPosY + waveAmplitude*Math.sin(partStartX + time);
            double partEndX = startPosX + ((i+1) * partLengthX);
            double partEndY = startPosY + waveAmplitude*Math.sin(partEndX + time);

            g2d.setStroke(new BasicStroke(snakeThickness));
            g2d.drawLine((int)partStartX, (int)partStartY, (int)partEndX, (int)partEndY);

            // Draw the head
            if (i == 0) {
                drawCircle(g2d, new Int2((int)partStartX, (int)partStartY), headSize);
            }
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

    public void setShouldRefreshBackground(boolean shouldRefreshBackground) {
        this.shouldRefreshBackground = shouldRefreshBackground;
    }

    public void setDieRollPos(Int2 pos) {
        this.nextDieMouseRollPos = pos;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (shouldRefreshBackground) {
            super.paintComponent(g);
        }

        graphicsLoop(g);
    }
}
