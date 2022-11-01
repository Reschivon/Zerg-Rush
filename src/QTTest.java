import javax.swing.*;
import java.awt.*;

public class QTTest {
    public static void main(String[] args) {
        QuadTree q = new QuadTree(256, 256, 3);

        JFrame frame = new JFrame();
        frame.setSize(256 * 4, 256 * 4);
        frame.setVisible(true);
        JPanel pane = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                Render.drawQuad(g, q, 4);
            }
        };
        frame.add(pane);

        for(int i = 0; i < 10; i++) {
            q.insert(Math.random() * 256, Math.random() * 256, new Object());

            pane.repaint();


            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
