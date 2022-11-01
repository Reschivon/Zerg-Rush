public class Game {
    public static void main(String[] args) {
        WorldState state = new WorldState();
        Render render = new Render(state);

        render.draw(); // TODO do this elegantly

        while (true) {
            state.gameTick(render);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}