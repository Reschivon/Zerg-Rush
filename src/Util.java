public class Util {
    public static boolean inRange(Object[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map[0].length && y < map.length;
    }

    public static <T> T atIndexOr(T[][] map, int x, int y, T alternative) {
        if (inRange(map, x, y))
            return map[y][x];
        return alternative;
    }

    record  Pair<T, E> (T a, E b){}

    static class ArrayWrapper<T>  {
        Object[] array;
        public ArrayWrapper(int size) {
            this.array = new Object[size];
        }

        public T get(int i) {
            return (T) array[i];
        }
        public void set(int i, T thing) {
            array[i] = thing;
        }
    }
}
