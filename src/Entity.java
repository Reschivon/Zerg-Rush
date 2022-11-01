import java.awt.*;
import java.net.Inet4Address;
import java.util.*;
import java.util.List;

public class Entity extends DeferredDeletable{
    double x = 0, y = 0;
    int health = 10;

    public static final double ENTITY_WIDTH = 0.8;

    Color color = new Color(60, 10, 120);

    public Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void hit(int damage) {
        health -= damage;
    }
    public void hit() {
        health--;
    }
    public void draw(Graphics g, Render render, WorldState state) {
        g.setColor(color);
        double size = render.getPixelsPerTile() * ENTITY_WIDTH;

        int height = render.getScreenHeightTile();
        int width = render.getScreenWidthTiles();
        int ppt = render.getPixelsPerTile();

        g.fillRoundRect(
                (int)((x + width/2.0 - state.player.x) * ppt),
                (int)((y + height/2.0 - state.player.y) * ppt),
                (int)size, (int)size,
                (int)(size * 0.6), (int)(size * 0.6));
    }

    public void move(WorldState state, double tickSize) {
        var distMap = state.player.playerDistanceMap;
        var map2worldX = state.player.distanceMap2WorldOffsetX;
        var map2worldY = state.player.distanceMap2WorldOffsetY;

        // check in distance map range
        if (Util.inRange(distMap, (int)(x - map2worldX), (int)(y - map2worldY))) {
            // check lowest adjacent
            int distMapX = (int) x - map2worldX;
            int distMapY = (int) y - map2worldY;

            var directions = new ArrayList<Util.Pair<Integer, Point>>(4);
            directions.add(new Util.Pair<>(
                    Util.atIndexOr(distMap, distMapX + 1, distMapY, Integer.MAX_VALUE), new Point(distMapX + 1, distMapY)));
            directions.add(new Util.Pair<>(
                    Util.atIndexOr(distMap, distMapX, distMapY + 1, Integer.MAX_VALUE), new Point(distMapX, distMapY + 1)));
            directions.add(new Util.Pair<>(
                    Util.atIndexOr(distMap, distMapX - 1, distMapY, Integer.MAX_VALUE), new Point(distMapX - 1, distMapY)));
            directions.add(new Util.Pair<>(
                    Util.atIndexOr(distMap, distMapX, distMapY - 1, Integer.MAX_VALUE), new Point(distMapX, distMapY - 1)));

            Util.Pair<Integer, Point> min;
            try {
                // Sometimes there is no valid position to move to
                Collections.shuffle(directions); // bit of randomness in direction
                min = Collections.min(
                        directions.stream().filter(a -> a.a() != null).toList(),
                        Comparator.comparingInt(Util.Pair::a));
            } catch (NoSuchElementException e) {return;}

            // int currVal = Util.atIndexOr(distMap, distMapX, distMapY, Integer.MAX_VALUE);

            int targetX = min.b().x + map2worldX;
            int targetY = min.b().y + map2worldY;

            x += (targetX - x + 0.5) * tickSize * 0.2; //TODO WHY DOES THIS WORK
            y += (targetY - y + 0.5) * tickSize * 0.2;
        }
    }


}
