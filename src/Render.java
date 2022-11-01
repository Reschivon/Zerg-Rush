import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Render {
    private JFrame frame;
    private JPanel drawablePanel;
    private int pixelsPerTile = 20;

    boolean[] pressed = new boolean[0xFFFF];
    boolean mousePressed = false;
    public Render(WorldState state) {
        frame = new JFrame();
        frame.setSize(1000, 600);

         drawablePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponents(g);
                drawAll(g, state);

                repaint();
            }
        };

        frame.add(drawablePanel);
        frame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                pressed[e.getKeyCode()] = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressed[e.getKeyCode()] = false;
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed = false;
            }
        });

        frame.setVisible(true);
    }

    public void draw() {
        drawablePanel.repaint();
    }

    public boolean[] getPressed() {
        return pressed;
    }

    private void drawAll(Graphics g, WorldState state) {
        state.map.draw(g, this, state.player);
        state.player.draw(g, this);

        // Concurrent modifications require indexed loop
        for(int i = 0; i < state.bullets.size(); i++) {
            var bullet = state.bullets.get(i);
            bullet.draw(g, this, state.player);
        }

        // Concurrent modifications require indexed loop
        for(int i = 0; i < state.entities.size(); i++) {
            var entity = state.entities.get(i);
            entity.draw(g, this, state);
        }

        // drawQuad(g, state.tree, 4);

        // Player health
        g.setFont(new Font("Monospace", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("HEALTH: " + String.valueOf(state.player.health), 20, 40);
    }

    public static void drawQuad(Graphics g, QuadTree tree, double scale) {
        if (tree != null && tree.root != null) {
            drawQuadR(g, tree.root, scale);
        }
    }

    static void drawQuadR(Graphics g, Node tree, double scale) {
        g.setColor(Color.black);
        g.drawRect((int)(scale * tree.left), (int)(scale * tree.down),
                    (int)(scale * (tree.up - tree.down)), (int)(scale * (tree.right - tree.left)));

        if (!tree.isLeaf()) {
            drawQuadR(g, tree.tl, scale);
            drawQuadR(g, tree.tr, scale);
            drawQuadR(g, tree.bl, scale);
            drawQuadR(g, tree.br, scale);

            if (tree.things.size() != 0)
                System.out.println("ERROROROROR not empty");
        } else {
            for (int i = 0; i < tree.things.size(); i++) {
                var thing = tree.things.get(i);
                g.fillOval((int) (thing.x() * scale), (int) (thing.y() * scale), 4, 4);
            }

        }
    }

    public int getScreenWidthTiles() {
        return (int) (frame.getWidth() / pixelsPerTile);
    }
    public int getScreenHeightTile() {
        return (int) (frame.getHeight() / pixelsPerTile);
    }

    public Point screenPixelsSize() {
        return new Point(frame.getWidth(), frame.getHeight());
    }

    public int getPixelsPerTile() {
        return pixelsPerTile;
    }

    public Point getMousePosition() {
        Point point = new Point(MouseInfo.getPointerInfo().getLocation());
        SwingUtilities.convertPointFromScreen(point, frame);
        return new Point(point.x, point.y);
    }
}
