import com.sun.source.doctree.SeeTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

record QuadTreeThing (double x, double y, Object me) {}
class Node {
    double left, right, up, down;

    int level;
    List<QuadTreeThing> things = new ArrayList<>();

    Node tl, tr, bl, br;

    int capacity;

    public Node(double left, double right, double up, double down, int capacity, int level) {
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        this.capacity = capacity;
        this.level = level;
    }

    public boolean contains(QuadTreeThing thing) {
        return left <= thing.x() && thing.x() < right &&
               down <= thing.y() && thing.y() < up;
    }

    public boolean isTouching(double left, double right, double up, double down) {
        return this.left <= right && this.right > left && this.up > down && this.down <= up;
    }

    public void insert(QuadTreeThing thing) {
        if (isLeaf()) {
            things.add(thing);
            if (things.size() > capacity && level < 9) {
                // split
                double midX = (left + right) / 2.0;
                double midY = (down + up) / 2.0;
                tl = new Node(left, midX, up, midY, capacity, level + 1);
                tr = new Node(midX, right, up, midY, capacity, level + 1);
                bl =  new Node(left, midX, midY, down, capacity, level + 1);
                br =  new Node(midX, right, midY, down, capacity, level + 1);

                // push things down to leaves
                for (var t : things)
                    dumbInsert(t);

                things.clear();
            }
        } else {
            dumbInsert(thing);
        }
    }

    private void dumbInsert(QuadTreeThing thing) {
        if (tl.contains(thing)) {
            tl.insert(thing);
            // System.out.println("tl " + thing.x() + " " + thing.y());
        }
        if (tr.contains(thing)) {
            tr.insert(thing);
            // System.out.println("tr " + thing.x() + " " + thing.y());
        }
        if (bl.contains(thing)) {
            bl.insert(thing);
            // System.out.println("bl " + thing.x() + " " + thing.y());
        }
        if (br.contains(thing)) {
            br.insert(thing);
            // System.out.println("br " + thing.x() + " " + thing.y());
        }
    }

    public List<QuadTreeThing> allNodesTouching(double left, double right, double up, double down) {
        if (!isTouching(left, right, up, down))
            return new ArrayList<>();

        if (isLeaf()) {
            return things;
        } else {
            List<QuadTreeThing> ret = new ArrayList<>();
            ret.addAll(tl.allNodesTouching(left, right, up, down));
            ret.addAll(tr.allNodesTouching(left, right, up, down));
            ret.addAll(bl.allNodesTouching(left, right, up, down));
            ret.addAll(br.allNodesTouching(left, right, up, down));

            return ret;
        }
    }

    public boolean isLeaf() {
        return tl == null;
    }
}

public class QuadTree {
    Node root;

    public QuadTree(int width, int height, int capacity) {
        root = new Node(0, width, height, 0, capacity, 0);
    }

    public void insert(double x, double y, Object thing) {
        root.insert(new QuadTreeThing(x, y, thing));
    }

    public List<QuadTreeThing> allNodesTouching(double left, double right, double up, double down) {
        return root.allNodesTouching(left, right, up, down);
    }
}
