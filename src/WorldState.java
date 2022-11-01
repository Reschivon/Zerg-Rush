import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldState {
    public Player player = new Player();
    public List<Bullet> bullets = new ArrayList<>();
    public List<Entity> entities = new ArrayList<>();
    Map map = new Map(500, 500);
    QuadTree tree;
    long lastTick = -1;

    private long nextShotTime = System.currentTimeMillis();

    public WorldState() {
        // Valid position for player
        while (true) {
            int posX = (int) (Math.random() * map.size().x);
            int posY = (int) (Math.random() * map.size().y);
            if (map.collidableTileAt(posX, posY) == null &&
                    map.collidableTileAt(posX + 1, posY) == null &&
                    map.collidableTileAt(posX, posY + 1) == null &&
                    map.collidableTileAt(posX + 1, posY + 1) == null) {
                player.x = posX;
                player.y = posY;
                return;
            }
        }

    }
    public void gameTick(Render render) {
        // Compute tickSize
        double tickSize;
        if (lastTick == -1) {
            lastTick = System.currentTimeMillis();
            tickSize = 1.0;
        } else {
            long currentTime = System.currentTimeMillis();
            tickSize = (currentTime - lastTick) / 60.0;
            lastTick = currentTime;
        }

        // System.out.println("FPS: " + (60.0 / tickSize));

        var pressed = render.getPressed();
        int diffX = 0, diffY = 0;
        if (pressed[KeyEvent.VK_D])
            diffX += 1;
        if (pressed[KeyEvent.VK_A])
            diffX -= 1;
        if (pressed[KeyEvent.VK_W])
            diffY -= 1;
        if (pressed[KeyEvent.VK_S])
            diffY += 1;
        if (pressed[KeyEvent.VK_E] && System.currentTimeMillis() - nextShotTime > 0) {
            for(double i = 0; i < 2 * Math.PI; i += 0.15)
                bullets.add(new Bullet(player.x, player.y, i));
            nextShotTime = System.currentTimeMillis() + 1000;
        }
        if (pressed[KeyEvent.VK_Q] && System.currentTimeMillis() - nextShotTime > 0) {
            for(double i = 0; i < 30; i++)
                player.shoot(this, render);
            nextShotTime = System.currentTimeMillis() + 1400;
        }

        player.move(this, tickSize, diffX, diffY);

        if (render.mousePressed && System.currentTimeMillis() - nextShotTime > 0) {
            player.shoot(this, render);
            nextShotTime = System.currentTimeMillis() + 100;
        }

        player.update(this);

        // bullets
        for (var bullet : bullets) {
            bullet.move(this, tickSize);
        }

        // delete bullets
        bullets = bullets.stream().filter(bullet -> !bullet.deleteRequested()).collect(Collectors.toList());

        // spawn entities
        if (Math.random() < tickSize * 1) {
            for(int i = 0; i < Math.random() * 16; i++) {
                double x = player.x + Math.random() * 90 - 45;
                double y = player.y + Math.random() * 90 - 45;

                if (map.collidableTileAt(x, y) == null && Math.hypot(y - player.y, x - player.x) > 30) {
                    entities.add(new Entity(x, y));
                }
            }
        }

        // Move entities
        for (var entity : entities) {
            entity.move(this, tickSize);
            if (entity.health <= 0)
                entity.markDeletable();
        }

        // delete entities
        entities = entities.stream().filter(entity -> !entity.deleteRequested()).collect(Collectors.toList());

        tree = new QuadTree(map.size().x, map.size().y, 30);
        for (var entity : entities) {
            tree.insert(entity.x, entity.y, entity);
        }


        for (var bullet : bullets) {
            var touching = tree.allNodesTouching(bullet.x - 1, bullet.x + 1, bullet.y + 1, bullet.y - 1);
            var collided = touching.stream().filter(entity -> Math.hypot(entity.y() - bullet.y, entity.x() - bullet.x) < 1).findFirst();
            collided.ifPresent(thing -> ((Entity) thing.me()).hit());
                    // .forEach(thing -> ((Entity)thing.me()).color = Color.orange);
        }

        // Player damage
        var touching = tree.allNodesTouching(player.x - 1, player.x + 1, player.y + 1, player.y - 1);
        touching.stream().filter(entity -> Math.hypot(entity.y() - player.y, entity.x() - player.x) < 1)
                .forEach(thing -> {((Entity) thing.me()).hit();
                                    player.health--;});
    }
}
