import java.awt.*;

public class Bullet extends DeferredDeletable {
    double x, y, xVel, yVel;
    int bounces = 0;
    final static int MAX_BOUNCES = 5;

    public Bullet(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.xVel = Math.cos(theta);
        this.yVel = Math.sin(theta);
    }

    public void move(WorldState state, double tickSize) {


        // Collision
        if (state.map.collidableTileAt(x + 4 * tickSize * xVel, y) != null) {
            xVel *= -1;
            bounces++;
        } else {
            x += 4 * tickSize * xVel;
        }

        if (state.map.collidableTileAt(x, y + 4 * tickSize * yVel) != null) {
            yVel *= -1;
            bounces++;
        } else {
            y += 4 * tickSize * yVel;
        }

        if (bounces > MAX_BOUNCES)
            markDeletable();

        // out of bounds
        if (x < 0 || x >= state.map.size().x || y < 0 || y >= state.map.size().y)
            markDeletable();
    }

    public void draw(Graphics g, Render render, Player player) {
        g.setColor(Color.black);
        int height = render.getScreenHeightTile(),
            width = render.getScreenWidthTiles();
        int ppt = render.getPixelsPerTile();
        g.fillOval((int)((x + width/2.0 - player.x) * ppt), (int)((y + height/2.0 - player.y) * ppt), (int)(ppt * 0.5), (int)(ppt * 0.5));
    }
}
