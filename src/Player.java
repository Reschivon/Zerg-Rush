import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Player {
    int health = 200;
    double x = 4, y = 5;

    double xVel = 0, yVel = 0;

    public static final double PLAYER_WIDTH = 1.1;

    // For pathfinding convenience, distance map surrounding player
    static int distanceMapRange = 2 * 50 + 1;
    public Integer[][] playerDistanceMap = new Integer[distanceMapRange][distanceMapRange];
    public int distanceMap2WorldOffsetX;
    public int distanceMap2WorldOffsetY;

    private void distanceMapGen(Map map) {
        // clear the map
        for (var row : playerDistanceMap)
            Arrays.fill(row, null);

        int midIndex = playerDistanceMap.length/2;
        playerDistanceMap[midIndex][midIndex] = 1;
        distanceMap2WorldOffsetX = (int)(x - midIndex - 1);
        distanceMap2WorldOffsetY = (int)(y - midIndex - 1);

        Queue<Point> bfs = new LinkedList<>();
        bfs.add(new Point(midIndex, midIndex));

        BiConsumer<Integer, Point> exploreInPos = (dist, pos) -> {
            if (pos.x >= 0 && pos.y >= 0 && pos.x < playerDistanceMap[0].length && pos.y < playerDistanceMap.length && // in bounds of map
                playerDistanceMap[pos.y][pos.x] == null && // not seen by bfs yet
                    map.collidableTileAt(pos.x + distanceMap2WorldOffsetX, // and free space
                                       pos.y + distanceMap2WorldOffsetY) == null) {
                playerDistanceMap[pos.y][pos.x] = dist + 1;
                bfs.add(pos);
            }
        };

        while (!bfs.isEmpty()) {
            Point curr = bfs.poll();
            exploreInPos.accept(playerDistanceMap[curr.y][curr.x], new Point(curr.x, curr.y + 1));
            exploreInPos.accept(playerDistanceMap[curr.y][curr.x], new Point(curr.x + 1, curr.y));
            exploreInPos.accept(playerDistanceMap[curr.y][curr.x], new Point(curr.x, curr.y - 1));
            exploreInPos.accept(playerDistanceMap[curr.y][curr.x], new Point(curr.x - 1, curr.y));
        }
    }

    public Point locationOnScreen(Render render) {
        // TODO whyyy
        int height = render.getScreenHeightTile(),
            width = render.getScreenWidthTiles();
        int ppt = render.getPixelsPerTile();
        return new Point((int)((width/2.0 - PLAYER_WIDTH/2.0) * ppt), (int)((height/2.0 - PLAYER_WIDTH/2.0) * ppt));
    }
    public void draw(Graphics g, Render render) {
        Graphics2D g2d = ((Graphics2D)g);
        g2d.setColor(new Color(217, 176, 0));
        double size = render.getPixelsPerTile() * PLAYER_WIDTH;
        var midPoint = locationOnScreen(render);
        g2d.fillRoundRect(midPoint.x, midPoint.y, (int) size, (int) size,
                                        (int)(size * 0.6), (int)(size * 0.6));
        g2d.setColor(Color.black.brighter());
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(midPoint.x, midPoint.y, (int) size, (int) size,
                (int)(size * 0.6), (int)(size * 0.6));
    }

    public void shoot(WorldState state, Render render) {
        var mousePos = render.getMousePosition();
        var diffPose = locationOnScreen(render);
        double theta = Math.atan2(mousePos.getY() - diffPose.getY(), mousePos.getX() - diffPose.getX());
        theta += Math.random() * 0.2 - 0.1;
        state.bullets.add(new Bullet(x, y, theta - 0.1));
        state.bullets.add(new Bullet(x, y, theta));
        state.bullets.add(new Bullet(x, y, theta + 0.1));

    }

    public void update(WorldState state) {
        distanceMapGen(state.map);

        // Set distance to tile property
        for(int h = 0; h < playerDistanceMap.length; h++) {
            for(int w = 0; w < playerDistanceMap[0].length; w++) {
                Tile tile = Util.atIndexOr(state.map.background, w + distanceMap2WorldOffsetX, h + distanceMap2WorldOffsetY, null);
                if (tile != null && playerDistanceMap[h][w] != null)
                    tile.distanceFromPlayer = playerDistanceMap[h][w];
            }
        }
    }

    /**
     * @param tickSize
     * @param diffX 1, 0, -1
     * @param diffY 1, 0, -1
     */
    public void move(WorldState state, double tickSize, int diffX, int diffY) {
        double xVelDiff = tickSize * Math.signum(diffX) * 0.3;
        double yVelDiff = tickSize * Math.signum(diffY) * 0.3;

        double xDiff = tickSize * (xVel + xVelDiff);
        double yDiff = tickSize * (yVel + yVelDiff);

        // kinetic friction
        xVel *= (1 - tickSize * 0.2);
        yVel *= (1 - tickSize * 0.2);

        // static friction
        if (diffX == 0 && diffY == 0) {
            // only if not key pressed
            if (Math.abs(xVel) < tickSize * 0.3) xVel *= (1 - tickSize * 3);
            if (Math.abs(yVel) < tickSize * 0.3) yVel *= (1 - tickSize * 3);
        }

        if (diffX != 0 || diffY != 0) {
            // Blood smears
            var treading = state.map.backgroundTileAt(x, y);
            treading.color = new Color(Math.max(20, treading.color.getRed() - 5),
                    Math.max(0, treading.color.getGreen() - 5),
                    Math.max(0, treading.color.getBlue() - 5));
        }

        if (!hasCollision(state, xDiff, 0)) {
            xVel += xVelDiff;
            x += xDiff;
        } else {
            xVel = -0.3 * xVel;
        }
        if (!hasCollision(state, 0, yDiff)) {
            yVel += yVelDiff;
            y += yDiff;
        } else {
            yVel = -0.3 * yVel;
        }
    }
    private boolean hasCollision(WorldState state, double deltaX, double deltaY) {
        // Check collision
        if (state.map.collidableTileAt(x + deltaX - PLAYER_WIDTH /2, y + deltaY - PLAYER_WIDTH /2) != null ||
            state.map.collidableTileAt(x + deltaX - PLAYER_WIDTH /2, y + deltaY + PLAYER_WIDTH /2) != null ||
            state.map.collidableTileAt(x + deltaX + PLAYER_WIDTH /2, y + deltaY - PLAYER_WIDTH /2) != null ||
            state.map.collidableTileAt(x + deltaX + PLAYER_WIDTH /2, y + deltaY + PLAYER_WIDTH /2) != null ||
            state.map.backgroundTileAt(x + deltaX, y + deltaY) == null)
        {
            return true;
        } else {
            return false;
        }
    }
}
