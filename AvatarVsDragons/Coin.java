public class Coin extends GameObject{
    private boolean collected;
    public Coin(Location location) {
        super(location, "coin_nobg.png",35,35);
        collected=false;
    }
    public boolean isCollected() {
        return collected;
    }
    public void collect() {
        collected= true;
    }
    public void draw(SimpleCanvas canvas) {
        if (!collected) {
            super.draw(canvas);
        }
    }
}