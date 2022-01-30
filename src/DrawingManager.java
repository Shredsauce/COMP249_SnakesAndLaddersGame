// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Handles the drawing for the board and the die. */
public class DrawingManager extends JPanel {
    public static int TILE_SIZE = 30;
    public static int TILE_HALF_SIZE = TILE_SIZE/2;
    public static int TILE_SPACING = 5;
    public static int NUM_TAIL_TILES = 1;
    public static Int2 OFFSET = new Int2(100, 100);

    private Board board;

    private boolean shouldRefreshBackground = true;
    private double dieAngle;
    private int maxDiePositionOffset = 8;
    private Int2 diePositionOffset = new Int2(0, 0);
    private int previousDieValue;
    private Color oddTileColor = new Color(50, 90, 200);
    private Color evenTileColor = new Color(200, 200, 50);
    private Color boardColor = new Color(145, 92, 48);

    // Singleton
    private static DrawingManager instance;
    public static DrawingManager getInstance() {
        return instance;
    }

    public DrawingManager (JFrame frame) {
        instance = this;
        frame.getContentPane().add(this, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (shouldRefreshBackground) {
            super.paintComponent(g);
        }

        graphicsLoop(g);
    }

    private void graphicsLoop(Graphics g) {
        if (board == null) return;

        Graphics2D g2d = (Graphics2D) g;

        if (shouldRefreshBackground){
            drawBoard(g2d, board.getEndTileIdForAnim());
        }

        if (board.isBoardShowAnimComplete() && shouldRefreshBackground){
            drawMoveToElements(g2d);
        }

        if (board.getPlayers() != null && board.getPlayers().length > 0 && shouldRefreshBackground) {
            drawPlayers(g2d);
        }

        drawDie(g2d);
        repaint();
    }

    private void drawPlayers(Graphics2D g2d) {
        for (int i = 0; i < board.getPlayers().length; i++) {
            Player player = board.getPlayers()[i];

            int shadowOffset = 1;

            setFontSize(g2d, 30f);
            Int2 playerPosition = player.getCurrentPosition();

            g2d.setColor(Color.black);
            g2d.drawString(""+player.getIcon(), playerPosition.x+shadowOffset,  playerPosition.y+shadowOffset+TILE_SIZE);

            g2d.setColor(Color.white);
            g2d.drawString(""+player.getIcon(), playerPosition.x,  playerPosition.y+TILE_SIZE);
        }
    }

    private void drawDie(Graphics2D g2d) {
        int size = 50;
        int shadowOffset = 3;
        int edgeOffset = 10;
        int arcValue = 7;

        Int2 pos = new Int2(GUIManager.getInstance().getNextDieMouseRollPos().x - size/2, GUIManager.getInstance().getNextDieMouseRollPos().y - size/2);

        int dotRadius = 6;

        if (previousDieValue != board.getCurrentDieValue()) {
            Random random = new Random();

            dieAngle = random.nextDouble(0, 2*Math.PI);
            diePositionOffset = new Int2(random.nextInt(-maxDiePositionOffset, maxDiePositionOffset), random.nextInt(-maxDiePositionOffset, maxDiePositionOffset));

            previousDieValue = board.getCurrentDieValue();
        }

        g2d.rotate(dieAngle, pos.x + size / 2, pos.y + size / 2);
        g2d.translate(diePositionOffset.x, diePositionOffset.y);

        g2d.setColor(Color.black);
        g2d.fillRoundRect(pos.x, pos.y, size+shadowOffset, size+shadowOffset, arcValue, arcValue);
        g2d.setColor(Color.white);
        g2d.fillRoundRect(pos.x, pos.y, size, size, arcValue, arcValue);

        int die = board.getCurrentDieValue();
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

    private void drawTileNumber(Graphics2D g2d, int tileId, int xPos, int yPos) {
        int boardNumberYOffset = 20;
        g2d.setColor(Color.white);
        g2d.drawString(""+tileId, xPos, yPos + TILE_SIZE - boardNumberYOffset);
    }

    private void drawTile(Graphics2D g2d, Tile tile) {
        int endTileAnimSwitch = board.getEndTileIdForAnim() % 1;
        Color tileColor = (tile.getTileId() % 2) + endTileAnimSwitch == 1 ? oddTileColor : evenTileColor;
        g2d.setColor(tileColor);

        Int2 pos = tile.getPosition();
        int tileId = tile.getTileId();

        Tile previousTile = board.getTile(tileId - 1);
        Tile nextTile = board.getTile(tileId + 1);

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

    private void drawCircle (Graphics2D g2d, Int2 center, int radius) {
        g2d.fillOval(center.x-radius, center.y-radius, 2*radius, 2*radius);
    }

    private void drawBoard(Graphics2D g2d, int tileEndId) {
        drawBoardBackground(g2d);

        for (int y = 0; y < board.getBoardSize().y; y++) {
            for (int x = 0; x < board.getBoardSize().x; x++) {
                Tile tile = board.getTiles()[x][y];

                if (tile == null) continue;
                if (tile.getTileId() > tileEndId) continue;

                drawTile(g2d, tile);
                setFontSize(g2d, 10f);

                int xPos = TILE_SIZE * x + OFFSET.x;
                int yPos = TILE_SIZE * y + OFFSET.y;

                if (board.isBoardShowAnimComplete()) {
                    drawTileNumber(g2d, tile.getTileId(), xPos, yPos);
                }
            }
        }
    }

    private void drawMoveToElements(Graphics2D g2d) {
        for (int y = 0; y < board.getBoardSize().y; y++) {
            for (int x = 0; x < board.getBoardSize().x; x++) {
                Tile tile = board.getTiles()[x][y];

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

    private void drawBoardBackground(Graphics2D g2d) {
        g2d.setColor(boardColor);
        g2d.fill3DRect(OFFSET.x, OFFSET.y, TILE_SIZE*board.getBoardSize().x, TILE_SIZE*board.getBoardSize().y, true);
    }

    private boolean shouldDrawSnakeHead(int tileId, Tile nextTile) {
        return nextTile == null || tileId == board.getEndTileIdForAnim();
    }

    public void setShouldRefreshBackground(boolean shouldRefreshBackground) {
        this.shouldRefreshBackground = shouldRefreshBackground;
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


    private void drawTurningTile(Graphics2D g2d, Int2 pos, double angle) {
        Int2 cellCenter = new Int2(pos.x +  TILE_HALF_SIZE, pos.y + TILE_HALF_SIZE);

        g2d.rotate(angle, cellCenter.x, cellCenter.y);
        g2d.fillRoundRect(pos.x + TILE_SPACING, pos.y + TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, TILE_SIZE - 2*TILE_SPACING, 20, 20);
        g2d.fillRect(pos.x, pos.y + TILE_SPACING, TILE_HALF_SIZE, TILE_SIZE - 2*TILE_SPACING);
        g2d.fillRect(pos.x + TILE_SPACING, pos.y, TILE_SIZE - 2*TILE_SPACING, TILE_HALF_SIZE);
        g2d.rotate(-angle, cellCenter.x, cellCenter.y);
    }

    private void drawLadder(Graphics2D g2d, int startTileId, int endTileId) {
        float ladderThickness = board.getBoardSettings().ladderThickness;
        int ladderWidth = board.getBoardSettings().ladderWidth;
        int rungSpacing = board.getBoardSettings().rungSpacing;

        int ladderPosYOffset = TILE_SIZE/3;

        Tile startTile = board.getTile(startTileId);
        Tile endTile = board.getTile(endTileId);

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

        Tile startTile = board.getTile(startTileId);
        Tile endTile = board.getTile(endTileId);

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
        if (nextTileCoord.y > tailTileCoord.y) return Math.PI/2;
        if (nextTileCoord.y < tailTileCoord.y) return (3*Math.PI)/2;

        return 0;
    }

    private void setFontSize(Graphics2D g2d, float fontSize) {
        Font currentFont = g2d.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getStyle(), fontSize);
        g2d.setFont(newFont);
    }

    public void setBoard(Board board) {
        this.board =  board;
        previousDieValue = board.getCurrentDieValue();
    }
}
