import java.awt.*;

public class Map {

    private static final int STRIDE_WIDTH = (int) Math.pow(2, 3);

    private static final int VISIBLE_RANGE = 30;
    public Tile[][] background;// TODo private
    private Tile[][] collidable;



    public Map(int width, int height) {
        // Round to nearest multiple of STRIDE_WIDTH for ease of perlin
        height = (int) (STRIDE_WIDTH * Math.round(height * 1.0 / STRIDE_WIDTH)) + 1;
        width = (int) (STRIDE_WIDTH * Math.round(width * 1.0 / STRIDE_WIDTH)) + 1;

        background = new Tile[height][width];
        collidable = new Tile[height][width];

        // Generate height map
        var heightMap = new Double[height][width];
        for(int h = 0; h < heightMap.length; h++)
            for(int w = 0; w < heightMap[0].length; w++)
                heightMap[h][w] = 0.0;

        // for every 2 power of STRIDE_WIDTH
        for (int stride = STRIDE_WIDTH; stride > 2; stride /= 2) {
            //Generate control points at each width
            for(int h = 0; h < heightMap.length; h += stride) {
                for(int w = 0; w < heightMap[0].length; w += stride) {
                    heightMap[h][w] += Math.random() * stride; // TODO height parameter
                }
            }

            // perform linear interpolation
            // Do not do every index, save the last control points
            for(int h = 0; h < heightMap.length - 1; h++) {
                for(int w = 0; w < heightMap[0].length - 1; w++) {
                    // do not interpolate if is control point
                    if (h % stride == 0 && w % stride == 0)
                        continue;

                    // normalized x and y for local plane
                    double normW = (w % stride) * 1.0 / stride;
                    double normH = (h % stride) * 1.0 / stride;

                    // Points
                    double w0h0 = heightMap[w - w % stride][h - h % stride];
                    double w0h1 = heightMap[w - w % stride][h - h % stride + stride];
                    double w1h0 = heightMap[w - w % stride + stride][h - h % stride];
                    double w1h1 = heightMap[w - w % stride + stride][h - h % stride + stride];

                    double val =
                            (1 - normW) * (1 - normH) * w0h0 +
                            (1 - normW) * (normH) * w0h1 +
                            (normW) * (1 - normH) * w1h0 +
                            (normW) * (normH) * w1h1;

                    heightMap[h][w] = val;
                }
            }
        }

        // Generate tiles
        for(int h = 0; h < heightMap.length; h++) {
            for(int w = 0; w < heightMap[0].length; w++) {
                if (heightMap[h][w] > STRIDE_WIDTH * 0.9)
                    collidable[h][w] = new Tile(new Color(60, (int)(Math.random() * 30) + 70, 80));

                background[h][w] = new Tile(new Color(200, (int) (heightMap[h][w] / (STRIDE_WIDTH * 2) * 255),
                                            (int) (heightMap[h][w]  / (STRIDE_WIDTH * 2) * 255)));
            }
        }
    }

    // Get rectangle section defined by parameters
    private Tile[][] getSlice(Tile[][] global, int x, int y, int width, int height) {
        var ret = new Tile[height][width];

        int rowIter = y < 0 ? -y : 0;

        for(; rowIter < height && (y + rowIter < global.length); rowIter++) {
            int colIter = x < 0 ? -x : 0;

            for(; colIter < width && (x + colIter < global[0].length); colIter++) {
                ret[rowIter][colIter] = global[y + rowIter][x + colIter];
            }
        }

        return ret;
    }

    public Tile[][] getBackgroundSlice(int x, int y, int width, int height) {
        return getSlice(background, x, y, width, height);
    }
    public Tile[][] getCollidableSlice(int x, int y, int width, int height) {
        return getSlice(collidable, x, y, width, height);
    }

    public Tile collidableTileAt(double x, double y) {
        int roundedX = (int) x;
        int roundedY = (int) y;

        if (roundedX < 0 || roundedY < 0 || roundedX >= collidable[0].length || roundedY >= collidable.length)
            return null;

        else return collidable[roundedY][roundedX];
    }

    public Tile backgroundTileAt(double x, double y) {
        int roundedX = (int) x;
        int roundedY = (int) y;

        if (roundedX < 0 || roundedY < 0 || roundedX >= background[0].length || roundedY >= background.length)
            return null;

        else return background[roundedY][roundedX];
    }

    public void draw(Graphics g, Render render, Player player) {
        int width = render.getScreenWidthTiles();
        int height = render.getScreenHeightTile();

        var backgroundVisible = getBackgroundSlice((int) (player.x - width/2.0),
                                      (int) (player.y - height/2.0),
                                        width, height);
        var collidableVisible = getCollidableSlice((int) (player.x - width/2.0),
                (int) (player.y - height/2.0),
                width, height);

        // slicing rounds to nearest, figure out sub-tile error
        double xError = - (player.x - width/2.0) % 1,
               yError = - (player.y - height/2.0) % 1;

        int ppt = render.getPixelsPerTile();

        // Draw background
        for(int h = 0; h < backgroundVisible.length; h++) {
            for(int w = 0; w < backgroundVisible[0].length; w++) {
                if (backgroundVisible[h][w] == null)
                    g.setColor(new Color(50, 50, 70));
                else {
                    var baseColor = backgroundVisible[h][w].color;
                    final int FULLBRIGHT_RANGE = 10;
                    double dimming = Math.max(0, (VISIBLE_RANGE - backgroundVisible[h][w].distanceFromPlayer + FULLBRIGHT_RANGE) / (1.0 * VISIBLE_RANGE));
                    if (dimming > 1 - FULLBRIGHT_RANGE * 1.0 / VISIBLE_RANGE)
                        dimming = 1;
                    g.setColor(new Color((int)(baseColor.getRed() * dimming),
                                              (int)(baseColor.getGreen() * dimming),
                                              (int)(baseColor.getBlue() * dimming)));
                }

                g.fillRect((int) ((w + xError) * ppt), (int) ((h + yError) * ppt), ppt, ppt);

//                if(backgroundVisible[h][w] != null) {
//                    g.setColor(Color.black);
//                    g.drawString(backgroundVisible[h][w].text, (int)((w + xError) * ppt), (int) ((h + yError + 1) * ppt));
//                }
            }
        }

        // Draw collidable
        for(int h = 0; h < collidableVisible.length; h++) {
            for(int w = 0; w < collidableVisible[0].length; w++) {
                if (collidableVisible[h][w] == null)
                    continue;

                g.setColor(collidableVisible[h][w].color);
                g.fillRect((int)((w + xError) * ppt), (int) ((h + yError) * ppt), ppt, ppt);

            }
        }
    }

    public Point size() {
        return new Point(collidable[0].length, collidable.length);
    }

}
